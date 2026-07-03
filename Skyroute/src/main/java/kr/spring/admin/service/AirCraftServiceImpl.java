package kr.spring.admin.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.spring.admin.dao.AirCraftMapper;
import kr.spring.admin.vo.AirCraftVO;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AirCraftServiceImpl implements AirCraftService{
	
	private final AirCraftMapper airCraftMapper;

	@Override
    public void insertAircraft(AirCraftVO aircraft) {
        airCraftMapper.insertAircraft(aircraft);
    }

    @Override
    public List<AirCraftVO> selectListAircraft() {
        return airCraftMapper.selectListAircraft();
    }

}
