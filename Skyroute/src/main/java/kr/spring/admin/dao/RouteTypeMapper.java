package kr.spring.admin.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.spring.admin.vo.RouteTypeVO;

@Mapper
public interface RouteTypeMapper {
	//노선 전체 조회
	List<RouteTypeVO> selectRouteTypeList();
	
	//노선 유형 단일 조회
	RouteTypeVO selectRouteType(int routeTypeId);
	
	//노선 유형 등록
	int insertRouteType(RouteTypeVO routeType);
	
	//노선 유형 수정
	int updateRouteType(RouteTypeVO routeType);
	
	//노선 유형 삭제
	int deleteRouteType(int routeTypeId);
	
	// 해당 유형을 사용하는 노선 수
	int countRoutesByRouteTypeId(int routeTypeId);

	// 노선 유형 상태 변경
	int updateRouteTypeStatus(
		@Param("routeTypeId") int routeTypeId,
		@Param("isActive") String isActive
	);
}
