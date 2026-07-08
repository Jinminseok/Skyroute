package kr.spring.staff.basedata.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import kr.spring.staff.basedata.vo.RouteVO;

@Mapper
public interface StaffRouteMapper {
    // 노선 목록 조회
    public List<RouteVO> selectRouteList();
    
    // 중복 노선 체크 (출발-도착 공항이 동일한 노선이 이미 있는지)
    public int checkDuplicateRoute(RouteVO routeVO);
    
    // 노선 등록
    public void insertRoute(RouteVO routeVO);
    
    // 노선 수정
    public void updateRoute(RouteVO routeVO);
    
    // 노선 삭제
    public void deleteRoute(int route_id);
    
    // 노선 사용 여부 (토글)
    public void updateRouteActive(Map<String, Object> payload);
}