package kr.spring.member.bookinglist.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberBookingListMapper {
	// 1. 나의 항공권 예약 내역 동적 조건 조회 (XML 매핑)
    List<Map<String, Object>> selectMyBookingList(Map<String, Object> paramMap);

    // 2. 예약 취소 시 티켓 선점 상태 일괄 변경 (XML 매핑)
    int updateTicketStatusToCancel(Long bookingId);

    // 3. 예약 취소 시 예약 마스터 상태 및 취소 일자 반영 (XML 매핑)
    int updateBookingStatusToCancel(Long bookingId);
}
