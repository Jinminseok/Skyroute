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
        staffRouteMapper.updateRoute(routeVO);
        return "success";
    }

    @Override
    public void deleteRoute(int route_id) {
        staffRouteMapper.deleteRoute(route_id);
    }

    @Override
    public void updateRouteActive(Map<String, Object> payload) {
        staffRouteMapper.updateRouteActive(payload);
    }
}
