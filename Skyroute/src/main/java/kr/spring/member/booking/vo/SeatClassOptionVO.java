package kr.spring.member.booking.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SeatClassOptionVO {

	private Long seatClassId;

	private String className;

	private Integer sortOrder;
}