package kr.spring.member.booking.payment;

/**
 * 결제 관련 AJAX 요청 바디(JSON) 매핑용 record 모음.
 * Java 17 record 라 접근자는 {@code bookingId()} 처럼 필드명 그대로 호출한다.
 */
public class SHPayDto {

	/** 결제 준비: 결제창 열기 직전 */
	public record Prepare(Long bookingId, String method) {}

	/** 결제 완료: IMP 콜백 성공 후 서버 검증 요청 */
	public record Complete(Long bookingId, String impUid, String merchantUid, String method) {}

	/** 결제 취소/이탈: 결제창을 닫거나 실패했을 때 좌석 반납 */
	public record Cancel(Long bookingId) {}

	private SHPayDto() { }
}