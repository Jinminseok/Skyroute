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
 * PortOne(구 아임포트) V1 REST 클라이언트.
 *
 * 결제 파트의 핵심 책임인 서버 사이드 위변조 검증을 담당한다.
 * 브라우저(IMP SDK)가 넘겨준 imp_uid 로 PortOne 서버에 직접 조회하여
 * 실제 결제 금액/상태를 가져온다. 이 값을 원장(BOOKING.total_amount)과
 * 대조하는 로직은 {@code SHBookingServiceImpl.confirmPayment} 안에 있다.
 *
 * <p>새 의존성 없이 이미 build.gradle 에 있는 okhttp 로 구현했다.
 */
@Slf4j
@Component
public class SHIamportClient {

	private static final String API_HOST = "https://api.iamport.kr";
	private static final MediaType JSON =
			MediaType.parse("application/json; charset=utf-8");

	private final OkHttpClient http = new OkHttpClient();
	private final ObjectMapper om = new ObjectMapper();

	/** 관리자콘솔 > 결제연동 > 식별코드·API Keys > V1 API 의 REST API Key */
	@Value("${imp.api-key}")
	private String apiKey;

	/** 위 화면의 REST API Secret */
	@Value("${imp.api-secret}")
	private String apiSecret;

	/* ------------------------------------------------------------------ */
	/* 1. 액세스 토큰 발급                                                  */
	/* ------------------------------------------------------------------ */

	private String getToken() throws IOException {

		Map<String, String> body = new LinkedHashMap<>();
		body.put("imp_key", apiKey);
		body.put("imp_secret", apiSecret);

		Request req = new Request.Builder()
				.url(API_HOST + "/users/getToken")
				.post(RequestBody.create(om.writeValueAsString(body), JSON))
				.build();

		try (Response res = http.newCall(req).execute()) {

			JsonNode root = readJson(res);

			if (root.path("code").asInt() != 0) {
				throw new IllegalStateException(
						"PortOne 토큰 발급 실패: " + root.path("message").asText());
			}

			return root.path("response").path("access_token").asText();
		}
	}

	/* ------------------------------------------------------------------ */
	/* 2. 결제 단건 조회 (검증용)                                           */
	/* ------------------------------------------------------------------ */

	/**
	 * imp_uid 로 PortOne 서버의 실제 결제 건을 조회한다.
	 * 여기서 돌려준 {@code amount}/{@code status} 를 원장과 대조해야 한다.
	 */
	public SHIamportPayment getPayment(String impUid) {

		try {
			String token = getToken();

			Request req = new Request.Builder()
					.url(API_HOST + "/payments/" + impUid)
					.addHeader("Authorization", token)
					.get()
					.build();

			try (Response res = http.newCall(req).execute()) {

				JsonNode root = readJson(res);

				if (root.path("code").asInt() != 0) {
					throw new IllegalStateException(
							"PortOne 결제 조회 실패: " + root.path("message").asText());
				}

				JsonNode r = root.path("response");

				SHIamportPayment p = new SHIamportPayment();
				p.setImpUid(r.path("imp_uid").asText());
				p.setMerchantUid(r.path("merchant_uid").asText());
				p.setStatus(r.path("status").asText());          // ready/paid/cancelled/failed
				p.setAmount(r.path("amount").asLong());
				p.setPayMethod(r.path("pay_method").asText());
				p.setPgProvider(r.path("pg_provider").asText());
				p.setReceiptUrl(r.path("receipt_url").asText());

				return p;
			}

		} catch (IOException e) {
			throw new IllegalStateException("PortOne 결제 조회 통신 오류", e);
		}
	}

	/* ------------------------------------------------------------------ */
	/* 3. 결제 취소(환불)                                                   */
	/* ------------------------------------------------------------------ */

	/**
	 * 전액 취소. "결제는 됐는데 좌석이 만료된" 경우처럼
	 * 서버 검증이 실패했을 때 자동 환불에 쓴다.
	 */
	public void cancelPayment(String impUid, String reason) {

		try {
			String token = getToken();

			Map<String, String> body = new LinkedHashMap<>();
			body.put("imp_uid", impUid);
			body.put("reason", reason == null ? "" : reason);

			Request req = new Request.Builder()
					.url(API_HOST + "/payments/cancel")
					.addHeader("Authorization", token)
					.post(RequestBody.create(om.writeValueAsString(body), JSON))
					.build();

			try (Response res = http.newCall(req).execute()) {

				JsonNode root = readJson(res);

				if (root.path("code").asInt() != 0) {
					log.warn("<<PortOne 취소 응답>> code={}, msg={}",
							root.path("code").asInt(), root.path("message").asText());
					throw new IllegalStateException(
							"PortOne 결제 취소 실패: " + root.path("message").asText());
				}

				log.debug("<<PortOne 결제취소 완료>> imp_uid={}", impUid);
			}

		} catch (IOException e) {
			throw new IllegalStateException("PortOne 결제 취소 통신 오류", e);
		}
	}

	/* ------------------------------------------------------------------ */

	private JsonNode readJson(Response res) throws IOException {
		ResponseBody rb = res.body();
		String text = (rb == null) ? "" : rb.string();
		return om.readTree(text);
	}
}