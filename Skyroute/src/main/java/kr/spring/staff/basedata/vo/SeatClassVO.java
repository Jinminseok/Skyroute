package kr.spring.staff.basedata.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SeatClassVO {
    private int seat_class_id;  // 등급 ID (PK)
    private String class_name;  // 등급명 (이코노미, 비즈니스 등)
    private double class_ratio; // 운임 배율 (예: 1.0, 1.5, 2.0)
    private int sort_order;     // 우선순위 (1:일등석, 2:비즈니스, 3:이코노미)
}