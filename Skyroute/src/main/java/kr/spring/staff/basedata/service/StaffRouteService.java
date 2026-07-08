package kr.spring.staff.basedata.service;
import java.util.List;
import java.util.Map;
import kr.spring.staff.basedata.vo.RouteVO;

public interface StaffRouteService {
    public List<RouteVO> getRouteList();
    public String insertRoute(RouteVO routeVO); // 중복 체크 로직을 위해 String 반환
    public String updateRoute(RouteVO routeVO);
    public void deleteRoute(int route_id);
    public void updateRouteActive(Map<String, Object> payload);
}