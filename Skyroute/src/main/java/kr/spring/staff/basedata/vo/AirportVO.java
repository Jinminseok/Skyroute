package kr.spring.staff.basedata.vo;

import lombok.Data;

@Data
public class AirportVO {
	private int airportId;  // 공항 ID
	private String iataCode;  // IATA 코드
	private String airportName;  // 공항명
	private String country;  // 국가
	private String timezone;  // 타임존
	private int regionId;  // 권역 ID
	private String flightType;  // 국내/국제선 구분 (DOM/INT)
	private String isActive;  // 사용 여부 (Y/N)
	
	private String regionName; // 출력용 권역명
}
