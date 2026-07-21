package kr.spring.member.booking.payment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;

import kr.spring.member.booking.exception.SHRefundReconciliationException;
import kr.spring.member.booking.service.SHBookingService;
import kr.spring.member.booking.vo.SHBookingVO;
import kr.spring.member.booking.vo.SHPaymentVO;
import kr.spring.member.booking.vo.SHTicketVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SHBookingCancelFacade {

	private final SHBookingService shBookingService;
	private final SHIamportClient iamportClient;
	private final TossPaymentsClient tossPaymentsClient;

	public SHBookingCancelResult cancelFullBooking(Long bookingId, Long memberId, String reason) {

		SHBookingVO booking = shBookingService.getBookingDetail(bookingId, memberId);

		if (booking == null) {
			throw new IllegalStateException("예약 정보를 찾을 수 없습니다.");
		}

		SHPaymentVO payment = booking.getPayment();

		if (isAlreadyCompleted(booking, payment)) {

			Long refundAmount = payment.getRefundAmount() == null ? payment.getAmount() : payment.getRefundAmount();

			return new SHBookingCancelResult(bookingId, refundAmount, null, true);
		}

		validateCancelable(booking, payment);

		String normalizedReason = normalizeReason(reason);

		BookingCancelQuote quote = shBookingService.calculateCancellationQuote(bookingId, memberId);

		ExternalCancellationResult externalResult = cancelExternalPayment(payment, quote, normalizedReason);

		Long refundAmount = applyDatabaseCancellation(bookingId, memberId, normalizedReason, quote,
				externalResult.paymentProvider(), externalResult.externalPaymentId(), externalResult.cancellationId());

		return new SHBookingCancelResult(bookingId, refundAmount, externalResult.cancellationId(), false);
	}

	private ExternalCancellationResult cancelExternalPayment(SHPaymentVO payment, BookingCancelQuote quote,
			String reason) {

		String provider = payment.getPaymentProvider();

		if ("PORTONE".equals(provider)) {

			return cancelPortOnePayment(payment, quote, reason);
		}

		if ("TOSS_PAYMENTS".equals(provider)) {

			return cancelTossPayment(payment, quote, reason);
		}

		throw new IllegalStateException("지원하지 않는 결제 제공자입니다.");
	}

	private ExternalCancellationResult cancelPortOnePayment(SHPaymentVO payment, BookingCancelQuote quote,
			String reason) {

		String externalPaymentId = payment.getMerchantUid();

		SHIamportPayment remotePayment = iamportClient.getPayment(externalPaymentId);

		if (!Objects.equals(remotePayment.getAmount(), payment.getAmount())) {

			throw new IllegalStateException("PortOne 결제 금액이 일치하지 않습니다.");
		}

		String remoteStatus = remotePayment.getStatus();

		if ("CANCELLED".equals(remoteStatus)) {

			if (!isFullRefund(payment, quote)) {

				throw new SHRefundReconciliationException("PortOne은 전액 취소됐지만 " + "내부 계산은 부분환불입니다. " + "관리자 확인이 필요합니다.",
						new IllegalStateException("PortOne 취소 상태와 " + "환불 계산 결과 불일치"));
			}

			return new ExternalCancellationResult("PORTONE", externalPaymentId, null);
		}

		if ("PARTIAL_CANCELLED".equals(remoteStatus)) {

			throw new SHRefundReconciliationException("PortOne에 이미 부분취소 내역이 있습니다. " + "중복환불 방지를 위해 관리자 확인이 필요합니다.",
					new IllegalStateException("PortOne 상태=" + remoteStatus));
		}

		if (!remotePayment.isPaid()) {

			throw new IllegalStateException("PortOne 결제 상태가 " + "결제 완료가 아닙니다.");
		}

		SHIamportCancellation cancellation;

		if (isFullRefund(payment, quote)) {

			cancellation = iamportClient.cancelPayment(externalPaymentId, reason);

		} else {

			cancellation = iamportClient.cancelPayment(externalPaymentId, reason, quote.totalRefundAmount(),
					payment.getAmount());
		}

		if (cancellation.isRequested()) {

			throw new IllegalStateException("환불 요청이 접수되었습니다. " + "최종 완료 후 다시 확인해 주세요.");
		}

		if (!cancellation.isSucceeded()) {

			throw new IllegalStateException("PortOne 환불이 완료되지 않았습니다.");
		}

		return new ExternalCancellationResult("PORTONE", externalPaymentId, cancellation.getCancellationId());
	}

	private ExternalCancellationResult cancelTossPayment(SHPaymentVO payment, BookingCancelQuote quote, String reason) {

		String paymentKey = payment.getProviderPaymentKey();

		JsonNode remotePayment = tossPaymentsClient.getPayment(paymentKey);

		long remoteAmount = remotePayment.path("totalAmount").asLong();

		if (!Objects.equals(remoteAmount, payment.getAmount())) {

			throw new IllegalStateException("토스 결제 금액이 일치하지 않습니다.");
		}

		String remoteOrderId = remotePayment.path("orderId").asText();

		if (!Objects.equals(remoteOrderId, payment.getMerchantUid())) {

			throw new IllegalStateException("토스 주문번호가 일치하지 않습니다.");
		}

		String remoteStatus = remotePayment.path("status").asText();

		if ("CANCELED".equals(remoteStatus)) {

			if (!isFullRefund(payment, quote)) {

				throw new SHRefundReconciliationException("토스 결제는 전액 취소됐지만 " + "내부 계산은 부분환불입니다. " + "관리자 확인이 필요합니다.",
						new IllegalStateException("토스 취소 상태와 " + "환불 계산 결과 불일치"));
			}

			return new ExternalCancellationResult("TOSS_PAYMENTS", paymentKey, null);
		}

		if ("PARTIAL_CANCELED".equals(remoteStatus)) {

			throw new SHRefundReconciliationException("토스에 이미 부분취소 내역이 있습니다. " + "중복환불 방지를 위해 관리자 확인이 필요합니다.",
					new IllegalStateException("토스 상태=" + remoteStatus));
		}

		if (!"DONE".equals(remoteStatus)) {

			throw new IllegalStateException("토스 결제 상태가 결제 완료가 아닙니다.");
		}

		boolean fullRefund = isFullRefund(payment, quote);

		if (!fullRefund && !remotePayment.path("isPartialCancelable").asBoolean(false)) {

			throw new IllegalStateException("해당 토스 결제는 부분환불이 불가능합니다.");
		}

		JsonNode cancellationResult;

		if (fullRefund) {

			cancellationResult = tossPaymentsClient.cancelPayment(paymentKey, reason);

		} else {

			cancellationResult = tossPaymentsClient.cancelPayment(paymentKey, reason, quote.totalRefundAmount());
		}

		String resultStatus = cancellationResult.path("status").asText();

		String expectedStatus = fullRefund ? "CANCELED" : "PARTIAL_CANCELED";

		if (!expectedStatus.equals(resultStatus)) {

			throw new IllegalStateException("토스 환불이 완료되지 않았습니다. " + "결제 상태=" + resultStatus);
		}

		String cancellationId = extractTossCancellationId(cancellationResult);

		return new ExternalCancellationResult("TOSS_PAYMENTS", paymentKey, cancellationId);
	}

	private String extractTossCancellationId(JsonNode paymentResult) {

		JsonNode cancellations = paymentResult.path("cancels");

		if (!cancellations.isArray() || cancellations.isEmpty()) {

			throw new IllegalStateException("토스 취소 응답에 취소 내역이 없습니다.");
		}

		JsonNode lastCancellation = cancellations.get(cancellations.size() - 1);

		String transactionKey = lastCancellation.path("transactionKey").asText();

		if (transactionKey == null || transactionKey.isBlank()) {

			throw new IllegalStateException("토스 취소 거래 식별값이 없습니다.");
		}

		return transactionKey;
	}

	private Long applyDatabaseCancellation(Long bookingId, Long memberId, String reason, BookingCancelQuote quote,
			String paymentProvider, String externalPaymentId, String cancellationId) {

		try {

			return shBookingService.applyCancellation(bookingId, memberId, reason, quote);

		} catch (RuntimeException e) {

			log.error(
					"<<환불 정합성 오류>> " + "외부 취소 성공 후 DB 반영 실패 " + "bookingId={}, " + "provider={}, " + "paymentId={}, "
							+ "cancellationId={}, " + "refundAmount={}",
					bookingId, paymentProvider, externalPaymentId, cancellationId, quote.totalRefundAmount(), e);

			throw new SHRefundReconciliationException("외부 환불은 완료됐으나 " + "내부 예약 상태 반영에 실패했습니다. " + "관리자 확인이 필요합니다.", e);
		}
	}

	private void validateCancelable(SHBookingVO booking, SHPaymentVO payment) {

		if (!"CONFIRMED".equals(booking.getStatus())) {

			throw new IllegalStateException("취소할 수 없는 예약 상태입니다.");
		}

		if (payment == null || !"PAID".equals(payment.getStatus())) {

			throw new IllegalStateException("환불 가능한 결제 정보를 찾을 수 없습니다.");
		}

		if ("VBANK".equals(payment.getMethod())) {

			throw new IllegalStateException("가상계좌 환불은 지원하지 않습니다.");
		}

		String provider = payment.getPaymentProvider();

		if ("PORTONE".equals(provider)) {

			if (payment.getMerchantUid() == null || payment.getMerchantUid().isBlank()) {

				throw new IllegalStateException("PortOne 결제 식별값이 없습니다.");
			}

		} else if ("TOSS_PAYMENTS".equals(provider)) {

			if (payment.getProviderPaymentKey() == null || payment.getProviderPaymentKey().isBlank()) {

				throw new IllegalStateException("토스 결제 식별값이 없습니다.");
			}

			if (payment.getMerchantUid() == null || payment.getMerchantUid().isBlank()) {

				throw new IllegalStateException("토스 주문번호가 없습니다.");
			}

		} else {

			throw new IllegalStateException("결제 제공자 정보가 올바르지 않습니다.");
		}

		if (booking.getOutboundDepartureTime() == null
				|| !booking.getOutboundDepartureTime().isAfter(LocalDateTime.now())) {

			throw new IllegalStateException("이미 출발했거나 출발 시각이 지난 " + "예약은 취소할 수 없습니다.");
		}

		List<SHTicketVO> tickets = booking.getTicketList();

		if (tickets == null || tickets.isEmpty()) {

			throw new IllegalStateException("취소할 티켓이 없습니다.");
		}

		for (SHTicketVO ticket : tickets) {

			if (!"CONFIRMED".equals(ticket.getHoldStatus())) {

				throw new IllegalStateException("이미 취소됐거나 유효하지 않은 " + "티켓이 포함되어 있습니다.");
			}

			if (!"NOT_CHECKED_IN".equals(ticket.getCheckinStatus())) {

				throw new IllegalStateException("체크인 또는 탑승 처리된 " + "항공권은 취소할 수 없습니다.");
			}
		}

		long ticketAmount = tickets.stream().map(SHTicketVO::getFareAmount).filter(Objects::nonNull)
				.mapToLong(Long::longValue).sum();

		if (!Objects.equals(booking.getTotalAmount(), ticketAmount)
				|| !Objects.equals(payment.getAmount(), ticketAmount)) {

			throw new IllegalStateException("예약 원장 금액과 환불 대상 금액이 " + "일치하지 않습니다.");
		}
	}

	private boolean isFullRefund(SHPaymentVO payment, BookingCancelQuote quote) {

		return Objects.equals(quote.totalRefundAmount(), payment.getAmount());
	}

	private boolean isAlreadyCompleted(SHBookingVO booking, SHPaymentVO payment) {

		return "CANCELLED".equals(booking.getStatus()) && payment != null
				&& ("REFUNDED".equals(payment.getStatus()) || "PARTIAL_REFUNDED".equals(payment.getStatus()));
	}

	private String normalizeReason(String reason) {

		String value = reason == null || reason.isBlank() ? "고객 요청 - 예약 전체 취소" : reason.trim();

		return value.length() <= 200 ? value : value.substring(0, 200);
	}

	private record ExternalCancellationResult(String paymentProvider, String externalPaymentId, String cancellationId) {
	}
}