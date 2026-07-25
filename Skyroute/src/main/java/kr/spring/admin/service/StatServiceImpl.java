package kr.spring.admin.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// StatMapper import 추가
import kr.spring.admin.dao.StatMapper;
import kr.spring.admin.vo.StatVO;

@Service
public class StatServiceImpl implements StatService {
	
    @Autowired
    private StatMapper statMapper; // 변수명을 소문자로 변경 (statMapper)
	
    @Override
    public Map<String, Object> getDashboardStatistics(StatVO statVO) {
		
        Map<String, Object> resultMap = new HashMap<>();
        
        long totalRevenue = statMapper.selectTotalRevenue(statVO);
        // Mapper의 메서드명과 정확히 일치하도록 수정 (selectTotalBooking)
        long totalBookings = statMapper.selectTotalBooking(statVO);
        List<StatVO> routeRevenues = statMapper.selectRevenueByRoute(statVO);
        List<StatVO> routeBookings = statMapper.selectBookingByRoute(statVO);
        
        resultMap.put("totalRevenue", totalRevenue);
        resultMap.put("totalBookings", totalBookings);
        resultMap.put("routeRevenues", routeRevenues);
        resultMap.put("routeBookings", routeBookings);
        
        return resultMap;
    }
}