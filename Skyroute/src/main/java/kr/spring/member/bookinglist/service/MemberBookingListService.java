package kr.spring.member.bookinglist.service;

import java.util.List;
import java.util.Map;

public interface MemberBookingListService {
    List<Map<String, Object>> selectMyBookingList(Map<String, Object> paramMap);
    void cancelMyBooking(Long bookingId);
}