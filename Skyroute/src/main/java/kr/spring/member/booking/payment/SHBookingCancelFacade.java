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

        /*
         * memberId 조건으로 조회하므로
         * 다른 회원의 예약은 조회되지 않는다.
         */
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

        SHPaymentVO payment = booking.getPayment();

        /*
         * 동일 요청 재전송 방어.
         *
         * 이미 DB까지 정상 반영된 예약이라면
         * PortOne 취소 API를 다시 호출하지 않는다.
         */
        if (isAlreadyCompleted(booking, payment)) {

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

        /*
         * V2 paymentId로 사용하는 값.
         *
         * 현재 프로젝트에서는
         * merchant_uid = PortOne paymentId
         */
        String externalPaymentId =
                payment.getMerchantUid();

        /*
         * 취소 전에 PortOne 실제 결제 상태와 금액을
         * 한 번 더 확인한다.
         */
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

        /*
         * 이전 요청에서 PortOne 취소는 성공했으나
         * DB 반영만 실패한 경우의 복구 경로.
         */
        if ("CANCELLED".equals(
                remotePayment.getStatus())) {

            Long refundAmount =
                    applyDatabaseCancellation(
                            bookingId,
                            memberId,
                            normalizedReason,
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

        if (!remotePayment.isPaid()) {
            throw new IllegalStateException(
                    "PortOne 결제 상태가 "
                    + "결제 완료가 아닙니다."
            );
        }

        /*
         * PortOne 전액 환불.
         */
        SHIamportCancellation cancellation =
                iamportClient.cancelPayment(
                        externalPaymentId,
                        normalizedReason
                );

        /*
         * 일부 결제수단은 취소가 비동기로 처리될 수 있다.
         * 이 경우 아직 REFUNDED로 변경하면 안 된다.
         */
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

        /*
         * PortOne 환불 성공 이후에만
         * DB 예약·결제·티켓 상태를 변경한다.
         */
        Long refundAmount =
                applyDatabaseCancellation(
                        bookingId,
                        memberId,
                        normalizedReason,
                        externalPaymentId,
                        cancellation.getCancellationId()
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
            String externalPaymentId,
            String cancellationId) {

        try {

            return shBookingService
                    .applyFullCancellation(
                            bookingId,
                            memberId,
                            reason
                    );

        } catch (RuntimeException e) {

            /*
             * 외부 환불은 이미 완료됐으므로
             * 단순 롤백으로 해결할 수 없는 상태다.
             */
            log.error(
                    "<<환불 정합성 오류>> "
                    + "PortOne 취소 성공 후 DB 반영 실패 "
                    + "bookingId={}, "
                    + "paymentId={}, "
                    + "cancellationId={}",
                    bookingId,
                    externalPaymentId,
                    cancellationId,
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


    private void validateCancelable(
            SHBookingVO booking,
            SHPaymentVO payment) {

        if (!"CONFIRMED".equals(
                booking.getStatus())) {

            throw new IllegalStateException(
                    "취소할 수 없는 예약 상태입니다."
            );
        }

        if (payment == null
                || !"PAID".equals(
                        payment.getStatus())) {

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

        if (payment.getMerchantUid() == null
                || payment.getMerchantUid()
                          .isBlank()) {

            throw new IllegalStateException(
                    "PortOne 결제 식별값이 없습니다."
            );
        }

        /*
         * 예약 전체 취소이므로
         * 최초 출발편이 이미 출발했다면 차단한다.
         */
        if (booking.getOutboundDepartureTime() == null
                || !booking
                        .getOutboundDepartureTime()
                        .isAfter(LocalDateTime.now())) {

            throw new IllegalStateException(
                    "이미 출발했거나 출발 시각이 지난 "
                    + "예약은 취소할 수 없습니다."
            );
        }

        List<SHTicketVO> tickets =
                booking.getTicketList();

        if (tickets == null || tickets.isEmpty()) {
            throw new IllegalStateException(
                    "취소할 티켓이 없습니다."
            );
        }

        for (SHTicketVO ticket : tickets) {

            if (!"CONFIRMED".equals(
                    ticket.getHoldStatus())) {

                throw new IllegalStateException(
                        "이미 취소됐거나 유효하지 않은 "
                        + "티켓이 포함되어 있습니다."
                );
            }

            if (!"NOT_CHECKED_IN".equals(
                    ticket.getCheckinStatus())) {

                throw new IllegalStateException(
                        "체크인 또는 탑승 처리된 "
                        + "항공권은 취소할 수 없습니다."
                );
            }
        }

        long ticketAmount =
                tickets.stream()
                        .map(SHTicketVO::getFareAmount)
                        .filter(Objects::nonNull)
                        .mapToLong(Long::longValue)
                        .sum();

        /*
         * 1차 구현은 예약 전체 전액 환불이므로
         * 티켓 합계 = 예약 원장 = 결제금액이어야 한다.
         */
        if (!Objects.equals(
                    booking.getTotalAmount(),
                    ticketAmount)
                || !Objects.equals(
                    payment.getAmount(),
                    ticketAmount)) {

            throw new IllegalStateException(
                    "예약 원장 금액과 환불 대상 금액이 "
                    + "일치하지 않습니다."
            );
        }
    }


    private boolean isAlreadyCompleted(
            SHBookingVO booking,
            SHPaymentVO payment) {

        return "CANCELLED".equals(
                    booking.getStatus())
                && payment != null
                && "REFUNDED".equals(
                    payment.getStatus());
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