package kr.spring.staff.basedata.vo;

import lombok.Data;

@Data
public class GateVO {
	private Long gateId;          // 게이트ID
    private Long airportId;       // 공항ID
    private String airportName;   // 공항명 (JOIN)
    private String iataCode;      // IATA 코드 (JOIN)
    private String gateCode;      // 게이트코드
    private Long gateAreaId;      // 게이트구역ID (1: A구역, 2: B구역, 3: C구역, 4: D구역)
    private String areaName;      // 구역명 (JOIN)
    private String flightType;    // 국내/국제 (DOM/INT)
    private String isActive;      // 사용여부 (Y/N)
}	
