package kr.spring.admin.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import kr.spring.admin.vo.StatVO;

@Mapper
public interface StatMapper {
	// 기간별 누적 매출액
    long selectTotalRevenue(StatVO statVO);
	
    // 기간별 예약 건수
    long selectTotalBooking(StatVO statVO);
	
    // 노선별 매출
    List<StatVO> selectRevenueByRoute(StatVO statVO);
	
}
