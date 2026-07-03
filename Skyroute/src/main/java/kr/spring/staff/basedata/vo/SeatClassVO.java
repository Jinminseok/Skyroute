package kr.spring.staff.basedata.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SeatClassVO {
    private int seat_class_id;  // 등급 ID (PK)
    private int aircraft_id;    // 항공기 ID (어느 비행기의 도면인가?)
    private String class_name;  // 등급명 (이코노미, 비즈니스 등)
    private int seat_rows;      // 행 수 (예: 30) - 예약어 충돌 방지
    private int seat_columns;   // 열 수 (예: 10) - 예약어 충돌 방지
    private int sort_order;     // 우선순위 (1:일등석, 2:비즈니스, 3:이코노미)
}