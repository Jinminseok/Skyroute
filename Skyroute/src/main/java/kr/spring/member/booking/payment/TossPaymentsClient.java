package kr.spring.member.booking.payment;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TossPaymentsClient {

	private final ObjectMapper objectMapper;
	private final HttpClient httpClient = HttpClient.newHttpClient();

	@Value("${toss.secret-key}")
	private String secretKey;

	public JsonNode confirmPayment(String paymentKey, String orderId, Long amount) {
		return post(
				"https://api.tosspayments.com/v1/payments/confirm",
				Map.of(
						"paymentKey", paymentKey,
						"orderId", orderId,
						"amount", amount
				)
		);
	}

	public JsonNode cancelPayment(String paymentKey, String reason) {
		String encodedPaymentKey = UriUtils.encodePathSegment(paymentKey, StandardCharsets.UTF_8);

		return post(
				"https://api.tosspayments.com/v1/payments/" + encodedPaymentKey + "/cancel",
				Map.of("cancelReason", reason)
		);
	}

	private JsonNode post(String url, Map<String, Object> body) {
		try {
			String authorization = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(url))
					.timeout(Duration.ofSeconds(30))
					.header("Authorization", "Basic " + authorization)
					.header("Content-Type", "application/json")
					.header("Accept", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
					.build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			JsonNode responseBody = objectMapper.readTree(response.body());

			if (response.statusCode() < 200 || response.statusCode() >= 300) {
				String code = responseBody.path("code").asText("TOSS_API_ERROR");
				String message = responseBody.path("message").asText("토스페이먼츠 요청에 실패했습니다.");

				throw new IllegalStateException(code + ": " + message);
			}

			return responseBody;

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("토스페이먼츠 요청이 중단되었습니다.", e);
		} catch (IllegalStateException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("토스페이먼츠 통신 중 오류가 발생했습니다.", e);
		}
	}
}