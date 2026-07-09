package kr.spring.staff.basedata.vo;

import lombok.Data;

@Data
public class FareVO {
	private Long fare_id;        // 운임 ID
    private Long route_id;       // 노선 ID
    private Long seat_class_id;  // 좌석 등급 ID
    private Long season_id;      // 시즌 ID
    private Integer price;       // 가격액
    private String is_active;    // 사용 여부 (Y/N)

    private String departure_airport_code; // 출발 공항 코드 (예: ICN)
    private String arrival_airport_code;   // 도착 공항 코드 (예: NRT)
    private String class_name;             // 좌석 등급명 (예: 일반석, 비즈니스)
    private String season_name;            // 시즌명 (예: 성수기, 비성수기)
}
