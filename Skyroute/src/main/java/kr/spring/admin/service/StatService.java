package kr.spring.admin.service;

import java.util.Map;

import kr.spring.admin.vo.StatVO;

public interface StatService {
	//모든 통계 데이터 불러오기
	Map<String, Object> getDashboardStatistics(StatVO statVO);
}
