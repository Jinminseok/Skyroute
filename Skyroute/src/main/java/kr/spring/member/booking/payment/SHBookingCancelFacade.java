package kr.spring.member.booking.payment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import kr.spring.member.booking.exception
        .SHRefundReconciliationException;
import kr.spring.member.booking.service.SHBookingService;
import kr.spring.member.booking.vo.SHBookingVO;
import kr.spring.member.booking.vo.SHPaymentVO;
import kr.spring.member.booking.vo.SHTicketVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 사용자 예약 전체 취소 파사드.
 *
 * PortOne 취소를 먼저 완료한 뒤
 * 내부 DB 취소 트랜잭션을 실행한다.
 *
 * 외부 API 호출을 DB 트랜잭션 안에 넣지 않는다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SHBookingCancelFacade {

    private final SHBookingService shBookingService;

    private final SHIamportClient iamportClient;


    public SHBookingCancelResult cancelFullBooking(
    		Long bookingId,
    		Long memberId,
    		String reason) {

    	SHBookingVO booking =
    			shBookingService.getBookingDetail(
    					bookingId,
    					memberId
    			);

    	if (booking == null) {
    		throw new IllegalStateException(
    				"예약 정보를 찾을 수 없습니다."
    		);
    	}

    	SHPaymentVO payment =
    			booking.getPayment();

    	if (isAlreadyCompleted(
    			booking,
    			payment)) {

    		Long refundAmount =
    				payment.getRefundAmount() == null
    						? payment.getAmount()
    						: payment.getRefundAmount();

    		return new SHBookingCancelResult(
    				bookingId,
    				refundAmount,
    				null,
    				true
    		);
    	}

    	validateCancelable(
    			booking,
    			payment
    	);

    	String normalizedReason =
    			normalizeReason(reason);

    	BookingCancelQuote quote =
    			shBookingService
    					.calculateCancellationQuote(
    							bookingId,
    							memberId
    					);

    	String externalPaymentId =
    			payment.getMerchantUid();

    	SHIamportPayment remotePayment =
    			iamportClient.getPayment(
    					externalPaymentId
    			);

    	if (!Objects.equals(
    			remotePayment.getAmount(),
    			payment.getAmount())) {

    		throw new IllegalStateException(
    				"결제 금액이 일치하지 않아 "
    				+ "예약을 취소할 수 없습니다."
    		);
    	}

    	String remoteStatus =
    			remotePayment.getStatus();

    	/*
    	 * 전액 취소 후 DB 반영만 실패한 경우.
    	 *
    	 * 계산된 환불액도 전액인 경우에만
    	 * 기존 복구 경로를 허용한다.
    	 */
    	if ("CANCELLED".equals(remoteStatus)) {

    		if (!Objects.equals(
    				quote.totalRefundAmount(),
    				payment.getAmount())) {

    			throw new SHRefundReconciliationException(
    					"PortOne은 전액 취소됐지만 "
    					+ "내부 계산은 부분 환불입니다. "
    					+ "관리자 확인이 필요합니다.",
    					new IllegalStateException(
    							"원격 결제 상태와 "
    							+ "환불 계산 결과 불일치"
    					)
    			);
    		}

    		Long refundAmount =
    				applyDatabaseCancellation(
    						bookingId,
    						memberId,
    						normalizedReason,
    						quote,
    						externalPaymentId,
    						null
    				);

    		return new SHBookingCancelResult(
    				bookingId,
    				refundAmount,
    				null,
    				false
    		);
    	}

    	/*
    	 * 이전 부분 취소가 PortOne에서 성공했지만
    	 * DB 반영이 실패했을 가능성이 있으므로,
    	 * 같은 금액을 다시 취소하지 않고 관리자 확인으로 넘긴다.
    	 */
    	if ("PARTIAL_CANCELLED".equals(
    			remoteStatus)) {

    		throw new SHRefundReconciliationException(
    				"PortOne에 이미 부분 취소 내역이 있습니다. "
    				+ "중복 환불 방지를 위해 관리자 확인이 필요합니다.",
    				new IllegalStateException(
    						"PortOne 상태="
    						+ remoteStatus
    				)
    		);
    	}

    	if (!remotePayment.isPaid()) {
    		throw new IllegalStateException(
    				"PortOne 결제 상태가 "
    				+ "결제 완료가 아닙니다."
    		);
    	}

    	SHIamportCancellation cancellation;

    	if (Objects.equals(quote.totalRefundAmount(), payment.getAmount())) {
    		cancellation = iamportClient.cancelPayment(externalPaymentId, normalizedReason);
    	} else {
    		cancellation = iamportClient.cancelPayment(externalPaymentId, normalizedReason, quote.totalRefundAmount(), payment.getAmount());
    	}

    	if (cancellation.isRequested()) {
    		throw new IllegalStateException(
    				"환불 요청이 접수되었습니다. "
    				+ "최종 완료 후 다시 확인해 주세요."
    		);
    	}

    	if (!cancellation.isSucceeded()) {
    		throw new IllegalStateException(
    				"PortOne 환불이 완료되지 않았습니다."
    		);
    	}

    	Long refundAmount =
    			applyDatabaseCancellation(
    					bookingId,
    					memberId,
    					normalizedReason,
    					quote,
    					externalPaymentId,
    					cancellation
    							.getCancellationId()
    			);

    	return new SHBookingCancelResult(
    			bookingId,
    			refundAmount,
    			cancellation.getCancellationId(),
    			false
    	);
    }


    private Long applyDatabaseCancellation(
    		Long bookingId,
    		Long memberId,
    		String reason,
    		BookingCancelQuote quote,
    		String externalPaymentId,
    		String cancellationId) {

    	try {

    		return shBookingService
    				.applyCancellation(
    						bookingId,
    						memberId,
    						reason,
    						quote
    				);

    	} catch (RuntimeException e) {

    		log.error(    
    				"<<환불 정합성 오류>> "
    				+ "PortOne 취소 성공 후 DB 반영 실패 "
    				+ "bookingId={}, "
    				+ "paymentId={}, "
    				+ "cancellationId={}, "
    				+ "refundAmount={}",
    				bookingId,
    				externalPaymentId,
    				cancellationId,
    				quote.totalRefundAmount(),
    				e
    		);

    		throw new SHRefundReconciliationException(
    				"외부 환불은 완료됐으나 "
    				+ "내부 예약 상태 반영에 실패했습니다. "
    				+ "관리자 확인이 필요합니다.",
    				e
    		);
    	}
    }


    private void validateCancelable(SHBookingVO booking, SHPaymentVO payment) {

        if (!"CONFIRMED".equals(booking.getStatus())) {

            throw new IllegalStateException(
                    "취소할 수 없는 예약 상태입니다."
            );
        }

        if (payment == null || !"PAID".equals(payment.getStatus())) {

            throw new IllegalStateException(
                    "환불 가능한 결제 정보를 "
                    + "찾을 수 없습니다."
            );
        }

        /*
         * 가상계좌는 현재 구현 범위에서 제외.
         */
        if ("VBANK".equals(payment.getMethod())) {
            throw new IllegalStateException(
                    "가상계좌 환불은 아직 "
                    + "지원하지 않습니다."
            );
        }

        if (payment.getMerchantUid() == null || payment.getMerchantUid().isBlank()) {

            throw new IllegalStateException(
                    "PortOne 결제 식별값이 없습니다."
            );
        }

        /*
         * 예약 전체 취소이므로
         * 최초 출발편이 이미 출발했다면 차단한다.
         */
        if (booking.getOutboundDepartureTime() == null || !booking.getOutboundDepartureTime().isAfter(LocalDateTime.now())) {

            throw new IllegalStateException(
                    "이미 출발했거나 출발 시각이 지난 "
                    + "예약은 취소할 수 없습니다."
            );
        }

        List<SHTicketVO> tickets = booking.getTicketList();

        if (tickets == null || tickets.isEmpty()) {
            throw new IllegalStateException(
                    "취소할 티켓이 없습니다."
            );
        }

        for (SHTicketVO ticket : tickets) {

            if (!"CONFIRMED".equals(ticket.getHoldStatus())) {

                throw new IllegalStateException(
                        "이미 취소됐거나 유효하지 않은 "
                        + "티켓이 포함되어 있습니다."
                );
            }

            if (!"NOT_CHECKED_IN".equals(ticket.getCheckinStatus())) {

                throw new IllegalStateException(
                        "체크인 또는 탑승 처리된 "
                        + "항공권은 취소할 수 없습니다."
                );
            }
        }

        long ticketAmount = tickets.stream().map(SHTicketVO::getFareAmount).filter(Objects::nonNull).mapToLong(Long::longValue).sum();

        /*
         * 1차 구현은 예약 전체 전액 환불이므로
         * 티켓 합계 = 예약 원장 = 결제금액이어야 한다.
         */
        if (!Objects.equals(booking.getTotalAmount(), ticketAmount) || !Objects.equals(payment.getAmount(), ticketAmount)) {

            throw new IllegalStateException(
                    "예약 원장 금액과 환불 대상 금액이 "
                    + "일치하지 않습니다."
            );
        }
    }


    private boolean isAlreadyCompleted(SHBookingVO booking, SHPaymentVO payment) {

    	return "CANCELLED".equals(booking.getStatus())
    			&& payment != null
    			&& ("REFUNDED".equals(payment.getStatus()) || "PARTIAL_REFUNDED".equals(payment.getStatus()));
    }


    private String normalizeReason(
            String reason) {

        String value =
                reason == null || reason.isBlank()
                        ? "고객 요청 - 예약 전체 취소"
                        : reason.trim();

        /*
         * REFUND.reason 컬럼 길이가 VARCHAR2(200)
         */
        return value.length() <= 200
                ? value
                : value.substring(0, 200);
    }
}