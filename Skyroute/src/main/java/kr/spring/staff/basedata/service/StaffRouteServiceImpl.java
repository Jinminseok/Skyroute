package kr.spring.staff.basedata.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.spring.staff.basedata.dao.StaffRouteMapper;
import kr.spring.staff.basedata.vo.RouteVO;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class StaffRouteServiceImpl implements StaffRouteService {

    private final StaffRouteMapper staffRouteMapper;

    @Override
    public List<RouteVO> getRouteList() {
        return staffRouteMapper.selectRouteList();
    }

    @Override
    public String insertRoute(RouteVO routeVO) {
        // 똑같은 출발-도착 공항이 있으면 거절
        if (staffRouteMapper.checkDuplicateRoute(routeVO) > 0) {
            return "duplicate";
        }
        staffRouteMapper.insertRoute(routeVO);
        return "success";
    }

    @Override
    public String updateRoute(RouteVO routeVO) {
        // 운항 스케줄에서 사용 중이면 수정 차단
        if (staffRouteMapper.checkRouteUsedInFlight(routeVO.getRoute_id()) > 0) {
            throw new IllegalStateException("해당 노선을 사용하는 운항 스케줄이 존재합니다.");
        }
        staffRouteMapper.updateRoute(routeVO);   
        return "success";                        
    }

    @Override
    public void deleteRoute(int route_id) {
        if (staffRouteMapper.checkRouteUsedInFlight(route_id) > 0) {
            throw new IllegalStateException("해당 노선을 사용하는 운항 스케줄이 존재합니다.");
        }
        staffRouteMapper.deleteRoute(route_id);
    }

    @Override
    public void updateRouteActive(Map<String, Object> payload) {
        String isActive = String.valueOf(payload.get("isActive"));
        int routeId = Integer.parseInt(String.valueOf(payload.get("routeId")));
        // 미사용(N) 처리 시, 사용 중이면 차단
        if ("N".equals(isActive) && staffRouteMapper.checkRouteUsedInFlight(routeId) > 0) {
            throw new IllegalStateException("해당 노선을 사용하는 운항 스케줄이 존재합니다.");
        }
        staffRouteMapper.updateRouteActive(payload);
    }
}
