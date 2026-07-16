package kr.spring.member.booking.payment;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * PortOne V2 REST 클라이언트.
 *
 * <p>결제 파트의 핵심 책임인 <b>서버 사이드 위변조 검증</b>을 담당한다.
 * 브라우저(PortOne SDK)가 넘겨준 <b>paymentId</b>(= 우리가 채번한 merchant_uid)로
 * PortOne 서버에 직접 조회하여 <b>실제 결제 금액/상태</b>를 가져온다.
 * 이 값을 원장(BOOKING.total_amount)과 대조하는 로직은
 * {@code SHBookingServiceImpl.confirmPayment} 안에 있다.
 *
 * <p>V1과 달리 <b>토큰 발급 단계가 없다.</b> V2 API Secret 을 매 요청 헤더
 * {@code Authorization: PortOne {SECRET}} 에 그대로 넣는다.
 */
@Slf4j
@Component
public class SHIamportClient {

	private static final String API_HOST = "https://api.portone.io";
	private static final MediaType JSON =
			MediaType.parse("application/json; charset=utf-8");

	private final OkHttpClient http = new OkHttpClient();
	private final ObjectMapper om = new ObjectMapper();

	/** 관리자콘솔 > 결제연동 > API Keys 에서 발급한 <b>V2 API Secret</b> */
	@Value("${imp.api-secret-v2}")
	private String apiSecret;

	/** 모든 V2 요청 공통 인증 헤더 값 */
	private String authHeader() {
		return "PortOne " + apiSecret;
	}

	/* ------------------------------------------------------------------ */
	/* 1. 결제 단건 조회 (검증용)                                           */
	/* ------------------------------------------------------------------ */

	/**
	 * paymentId 로 PortOne 서버의 실제 결제 건을 조회한다.
	 * 여기서 돌려준 {@code amount}/{@code status} 를 원장과 대조해야 한다.
	 *
	 * @param paymentId 결제창에 넘긴 주문번호(= merchant_uid)
	 */
	public SHIamportPayment getPayment(String paymentId) {

		Request req = new Request.Builder()
				.url(API_HOST + "/payments/" + paymentId)
				.addHeader("Authorization", authHeader())
				.get()
				.build();

		try (Response res = http.newCall(req).execute()) {

			JsonNode root = readJson(res);

			// V2 는 실패 시 HTTP 4xx + { type, message } 형태로 응답한다.
			if (!res.isSuccessful()) {
				String msg = root.path("message").asText();
				if (msg == null || msg.isEmpty()) {
					msg = root.path("type").asText();
				}
				throw new IllegalStateException("PortOne 결제 조회 실패: " + msg);
			}

			SHIamportPayment p = new SHIamportPayment();
			p.setPaymentId(root.path("id").asText());                       // = merchant_uid
			p.setPgTxId(root.path("pgTxId").asText());                      // PG사 거래번호(선택)
			p.setStatus(root.path("status").asText());                      // PAID/READY/CANCELLED/FAILED ...
			p.setAmount(root.path("amount").path("total").asLong());        // 실제 결제 금액
			p.setPayMethod(root.path("method").path("type").asText());
			p.setPgProvider(root.path("channel").path("pgProvider").asText());
			p.setReceiptUrl(root.path("receiptUrl").asText());

			return p;

		} catch (IOException e) {
			throw new IllegalStateException("PortOne 결제 조회 통신 오류", e);
		}
	}

	/* ------------------------------------------------------------------ */
	/* 2. 결제 취소(환불)                                                   */
	/* ------------------------------------------------------------------ */

	/**
	 * 전액 취소. "결제는 됐는데 좌석이 만료된" 경우처럼
	 * 서버 검증이 실패했을 때 자동 환불에 쓴다.
	 *
	 * @param paymentId 결제창에 넘긴 주문번호(= merchant_uid)
	 */
	public void cancelPayment(String paymentId, String reason) {

		Map<String, String> body = new LinkedHashMap<>();
		body.put("reason", reason == null ? "" : reason);

		try {
			Request req = new Request.Builder()
					.url(API_HOST + "/payments/" + paymentId + "/cancel")
					.addHeader("Authorization", authHeader())
					.post(RequestBody.create(om.writeValueAsString(body), JSON))
					.build();

			try (Response res = http.newCall(req).execute()) {

				JsonNode root = readJson(res);

				if (!res.isSuccessful()) {
					String msg = root.path("message").asText();
					log.warn("<<PortOne 취소 응답>> http={}, msg={}", res.code(), msg);
					throw new IllegalStateException("PortOne 결제 취소 실패: " + msg);
				}

				log.debug("<<PortOne 결제취소 완료>> paymentId={}", paymentId);
			}

		} catch (IOException e) {
			throw new IllegalStateException("PortOne 결제 취소 통신 오류", e);
		}
	}

	/* ------------------------------------------------------------------ */

	private JsonNode readJson(Response res) throws IOException {
		ResponseBody rb = res.body();
		String text = (rb == null) ? "" : rb.string();
		if (text.isEmpty()) {
			return om.createObjectNode();
		}
		return om.readTree(text);
	}
}