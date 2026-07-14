package kr.spring.staff.content.service;

import java.util.List;
import java.util.Map;

import kr.spring.staff.content.vo.EventParticipationVO;
import kr.spring.staff.content.vo.EventVO;

public interface StaffEventService {

	public List<EventVO> selectEventList();
	public EventVO selectEvent(long event_id);
	public void insertEvent(EventVO event);
	public void updateEvent(EventVO event);
	public void hideEvent(long event_id);
	public void endEvent(long event_id);
	public int showEvent(long event_id);
	public List<EventParticipationVO> selectParticipationList(long event_id);

	public List<EventVO> selectActiveEventList();
	public List<EventVO> selectUserEventList();
	public List<EventVO> selectEndedEventList();
	public int selectWinnerEventRowCount(Map<String, Object> map);

	public List<EventVO> selectWinnerEventList(Map<String, Object> map);

	public List<EventParticipationVO> selectWinnerList(long event_id);

	public void participateEvent(long event_id, long member_id);
	public EventParticipationVO selectMyEventParticipation(long event_id, long member_id);
	public List<EventParticipationVO> selectMyParticipationList(long member_id);
	public EventVO selectActiveEvent(long event_id);
	public boolean isParticipated(long event_id, long member_id);
	public int drawAndAnnounceEvent(long event_id);
	public int selectEventRowCount(Map<String, Object> map);
	public List<EventVO> selectEventSearchList(Map<String, Object> map);
	public Map<String, Object> selectEventStats();
	
	
}