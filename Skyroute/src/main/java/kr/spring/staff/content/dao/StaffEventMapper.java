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
	public Integer selectEventRowCount(Map<String, Object> map);
	public List<EventVO> selectEventSearchList(Map<String, Object> map);
	public Map<String, Object> selectEventStats();
	public EventVO selectEvent(@Param("event_id") long event_id);
	public EventVO selectActiveEvent(@Param("event_id") long event_id);
	public EventVO selectEventForDraw(@Param("event_id") long event_id);

	public void insertEvent(EventVO event);
	public void updateEvent(EventVO event);
	public void hideEvent(@Param("event_id") long event_id);
	public void endEvent(@Param("event_id") long event_id);
	public void showEvent(@Param("event_id") long event_id);
	public void announceEventResult(@Param("event_id") long event_id);

	public List<EventVO> selectActiveEventList();

	public List<EventParticipationVO> selectParticipationList(@Param("event_id") long event_id);
	public List<EventParticipationVO> selectRandomWinnerList(Map<String, Long> map);

	public Integer selectParticipationCount(Map<String, Long> map);

	public void insertParticipation(EventParticipationVO participation);
	public void updateNotSelected(@Param("event_id") long event_id);
	public void updateWinnerResult(@Param("participationIds") List<Long> participationIds);

	public List<EventParticipationVO> selectMyParticipationList(@Param("member_id") long member_id);
	public EventParticipationVO selectMyEventParticipation(
			@Param("event_id") long event_id,
			@Param("member_id") long member_id);
	
}