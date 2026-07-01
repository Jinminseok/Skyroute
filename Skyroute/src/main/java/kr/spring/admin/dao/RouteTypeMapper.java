package kr.spring.admin.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

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
}
