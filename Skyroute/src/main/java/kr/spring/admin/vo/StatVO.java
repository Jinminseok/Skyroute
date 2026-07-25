package kr.spring.admin.vo;

import lombok.Data;

@Data
public class StatVO {
	//검색 조건
	private String startDate;
	private String endDate;
	
	//결과 반환
	private long totalRevenue; // 누적 매출
	private long totalBookings; //전체 예약수
	
	//노선 
	private String routeName; // 노선명
	private long routeRevenue; //해당 노선 매출액
	private long routeBookings; // 해당 노선 예약 건수
}
