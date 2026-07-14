package kr.spring.member.bookinglist.service; 

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service; 

import kr.spring.member.bookinglist.dao.MemberBookingListMapper;

@Service 
public class MemberBookingListServiceImpl implements MemberBookingListService{

	@Autowired
    private MemberBookingListMapper bookingListMapper;

    @Override
    public List<Map<String, Object>> selectMyBookingList(Map<String, Object> paramMap) {
        return bookingListMapper.selectMyBookingList(paramMap);
    }

    //취소처리
    @Override
    public void cancelMyBooking(Long bookingId) {
        // A. 티켓 테이블의 선점 상태를 'CANCELLED'로 일괄 변경
        bookingListMapper.updateTicketStatusToCancel(bookingId);
        
        // B. 예약 마스터 테이블의 상태를 'CANCELLED'로 변경 및 취소일시 주입
        bookingListMapper.updateBookingStatusToCancel(bookingId);
    }

}
