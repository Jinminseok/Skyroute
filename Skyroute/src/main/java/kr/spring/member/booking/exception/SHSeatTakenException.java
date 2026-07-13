package kr.spring.member.booking.exception;

/*
 * 좌석 선점 실패
 *
 * - 다른 사용자가 먼저 같은 좌석을 HOLD 한 경우 (DB 유니크 제약 위반)
 * - HOLD 유지 시간(10분)이 만료된 경우
 *
 * 사용자를 좌석 선택 화면으로 되돌려야 하는 상황이다.
 */
public class SHSeatTakenException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public SHSeatTakenException(String message) {
		super(message);
	}
}