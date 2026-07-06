package kr.spring.staff.content.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.spring.staff.content.vo.EventParticipationVO;
import kr.spring.staff.content.vo.EventVO;

@Mapper
public interface StaffEventMapper {

	public List<EventVO> selectEventList();

	public EventVO selectEvent(long event_id);

	public void insertEvent(EventVO event);

	public void updateEvent(EventVO event);

	public void hideEvent(long event_id);

	public void endEvent(long event_id);

	public List<EventVO> selectActiveEventList();

	public List<EventParticipationVO> selectParticipationList(long event_id);

	public Integer selectParticipationCount(Map<String, Long> map);

	public void insertParticipation(EventParticipationVO participation);

	public List<EventParticipationVO> selectMyParticipationList(long member_id);
	
	public EventVO selectActiveEvent(@Param("event_id") long event_id);
}