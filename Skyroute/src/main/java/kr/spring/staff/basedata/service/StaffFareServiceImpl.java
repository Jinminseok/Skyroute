package kr.spring.staff.basedata.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.spring.admin.vo.SeasonVO;
import kr.spring.staff.basedata.dao.StaffFareMapper;
import kr.spring.staff.basedata.vo.FareVO;

@Service
@Transactional
public class StaffFareServiceImpl implements StaffFareService{


	@Autowired
	private StaffFareMapper fareMapper;	

	@Override
	public List<FareVO> selectFareList(Map<String, Object> map) {

		return fareMapper.selectFareList(map);
	}

	@Override
	public int selectFareCount(Map<String, Object> map) {
		return fareMapper.selectFareCount(map);
	}

	@Override
	public FareVO selectFare(Long fare_id) {
		return fareMapper.selectFare(fare_id);
	}

	@Override
	public void insertFare(FareVO fareVO) {
		fareMapper.insertFare(fareVO);

	}
	@Override
	public void updateFare(FareVO fareVO) {
		fareMapper.updateFare(fareVO);

	}

	@Override
	public String updateFareActive(Map<String, Object> payload) {
		Long fare_id = Long.valueOf(payload.get("fare_id").toString());
		String is_active = (String) payload.get("is_active");

		if ("N".equals(is_active)) {
			int useCount = fareMapper.checkFareInUse(fare_id);
			if (useCount > 0) {
				return "in_use"; // 스케줄에 사용 중이면 차단
			}
		}

		fareMapper.updateFareActive(payload);
		return "success";
	}

	@Override
	public boolean disableFare(Long fare_id) {
		int useCount = fareMapper.checkFareInUse(fare_id);
		if (useCount > 0) {
			return false;
		}
		fareMapper.updateFareActive(Map.of("fare_id", fare_id, "is_active", "N"));
		return true;
	}

	@Override
	public List<SeasonVO> getSeasonList() {
		return fareMapper.selectSeasonList();
	}

}
