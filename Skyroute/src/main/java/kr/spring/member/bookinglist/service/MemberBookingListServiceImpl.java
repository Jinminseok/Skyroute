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

}
