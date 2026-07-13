package kr.spring.admin.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AirCraftSeatClassVO {
	private int seat_class_id;
	private String class_name;
	private int seat_count;
}