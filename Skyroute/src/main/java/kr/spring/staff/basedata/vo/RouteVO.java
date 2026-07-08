package kr.spring.staff.basedata.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RouteVO {
    private int route_id;               // 노선 ID (PK)
    private int departure_airport_id;   // 출발 공항 ID (FK)
    private int arrival_airport_id;     // 도착 공항 ID (FK)
    private String flight_type;         // 국내/국제선 구분 (DOM/INT)
    private int route_type_id;          // 노선 유형 ID (FK)
    private String is_active;           // 노선 사용 여부 (Y/N)
    
    private String dep_airport_name;    // 출발 공항명 (예: GMP (서울 김포공항))
    private String arr_airport_name;    // 도착 공항명 (예: CJU (제주공항))
    private String type_name;     		// 노선 유형명 (예: 주력노선, 비인기노선 등)
}