package kr.spring.staff.content.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.spring.staff.content.dao.StaffEventMapper;
import kr.spring.staff.content.vo.EventParticipationVO;
import kr.spring.staff.content.vo.EventVO;

@Service
public class StaffEventServiceImpl implements StaffEventService {

	@Autowired
	private StaffEventMapper staffEventMapper;

	@Override
	public List<EventVO> selectEventList() {
		return staffEventMapper.selectEventList();
	}

	@Override
	public EventVO selectEvent(long event_id) {
		return staffEventMapper.selectEvent(event_id);
	}

	@Override
	public void insertEvent(EventVO event) {
		staffEventMapper.insertEvent(event);
	}

	@Override
	public void updateEvent(EventVO event) {
		staffEventMapper.updateEvent(event);
	}

	@Override
	public void hideEvent(long event_id) {
		staffEventMapper.hideEvent(event_id);
	}

	@Override
	public void endEvent(long event_id) {
		staffEventMapper.endEvent(event_id);
	}

	@Override
	public List<EventParticipationVO> selectParticipationList(long event_id) {
		return staffEventMapper.selectParticipationList(event_id);
	}

	@Override
	public List<EventVO> selectActiveEventList() {
		return staffEventMapper.selectActiveEventList();
	}

	@Transactional
	@Override
	public void participateEvent(long event_id, long member_id) {
		// 현재 실제로 참여 가능한 이벤트인지 확인
		EventVO event = staffEventMapper.selectActiveEvent(event_id);

		if (event == null) {
			throw new IllegalStateException("현재 참여할 수 없는 이벤트입니다.");
		}

		Map<String, Long> map = new HashMap<String, Long>();
		map.put("event_id", event_id);
		map.put("member_id", member_id);

		Integer count = staffEventMapper.selectParticipationCount(map);

		if (count != null && count > 0) {
			throw new IllegalStateException("이미 참여한 이벤트입니다.");
		}

		EventParticipationVO participation = new EventParticipationVO();
		participation.setEvent_id(event_id);
		participation.setMember_id(member_id);

		try {
			staffEventMapper.insertParticipation(participation);
		} catch (DuplicateKeyException e) {
			throw new IllegalStateException("이미 참여한 이벤트입니다.");
		}
	}

	@Override
	public List<EventParticipationVO> selectMyParticipationList(long member_id) {
		return staffEventMapper.selectMyParticipationList(member_id);
	}

	@Override
	public EventVO selectActiveEvent(long event_id) {
		return staffEventMapper.selectActiveEvent(event_id);
	}

	@Override
	public boolean isParticipated(long event_id, long member_id) {
		Map<String, Long> map = new HashMap<String, Long>();
		map.put("event_id", event_id);
		map.put("member_id", member_id);

		Integer count = staffEventMapper.selectParticipationCount(map);

		return count != null && count > 0;
	}
}