package kr.spring.staff.basedata.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SeatVO {
    private int seat_id;        // 좌석 고유 ID (PK)
    private int aircraft_id;    // 항공기 ID (FK)
    private int seat_class_id;  // 등급 ID (FK - 가격 책정용)
    private String seat_no;     // 좌석 번호 (예: 1A, 12J)
    private String is_active;   // 좌석 고장/사용 여부 (Y/N)
    
    private String reg_no;      // AIRCRAFT 테이블의 비행기 등록번호
    private String class_name;  // SEAT_CLASS 테이블의 등급명
}