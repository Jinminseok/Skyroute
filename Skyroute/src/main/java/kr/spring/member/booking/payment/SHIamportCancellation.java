package kr.spring.member.booking.payment;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/*
 * PortOne V2 결제 취소 결과
 * 
 * status:
 * - SUCCEEDED : 취소 완료
 * - REQUESTED : 취소 요청 접수, 비동기 처리 중
 * - FAILED : 취소 실패
 */

@Getter
@Setter
@ToString
public class SHIamportCancellation {
	
	private String cancellationId;
	private String pgCancellationId;
	private String status;
	private String reason;
	
	public boolean isSucceeded() {
		return "SUCCEEDED".equals(status);
	}
	
	public boolean isRequested() {
		return "REQUESTED".equals(status);
	}
	
	public boolean isFailed() {
		return "FAILED".equals(status);
	}
}
