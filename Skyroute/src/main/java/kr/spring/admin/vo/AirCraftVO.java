package kr.spring.admin.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AirCraftVO {
	private int aircraft_id;	// 항공기 ID (PK)
	private String reg_no;		// 등록기호 (예: HL1234)
	private String model_name;	// 모델명 (예: B777)
	private int total_seats;	// 총 좌석 수
	private String status_name;	// 운영상태 (운항가능, 정비중, 퇴역)
	private String is_active;	// 사용여부 (Y/N)
}
