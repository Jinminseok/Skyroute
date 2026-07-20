package kr.spring.member.bookinglist.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberBookingListMapper {
	// 1. 나의 항공권 예약 내역 동적 조건 조회 (XML 매핑)
    List<Map<String, Object>> selectMyBookingList(Map<String, Object> paramMap);
}
