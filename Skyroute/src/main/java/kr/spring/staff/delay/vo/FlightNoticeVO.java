package kr.spring.staff.delay.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class FlightNoticeVO {
	private int flight_notice_id;	// 안내 ID
	private int flight_id;			// 항공편 ID
	private String notice_type;		// 안내 유형
	private String reason;			// 사유
	private int delay_minutes;		// 예상 지연 시간
	private int created_by;			// 등록자
	private String created_at;		// 등록 일시
}