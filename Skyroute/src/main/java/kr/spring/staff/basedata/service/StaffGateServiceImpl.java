package kr.spring.staff.basedata.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import kr.spring.staff.basedata.dao.StaffGateMapper;
import kr.spring.staff.basedata.vo.GateVO;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StaffGateServiceImpl implements StaffGateService {

    private final StaffGateMapper staffGateMapper;

    @Override
    public List<GateVO> getGateList(GateVO searchVO) {
        return staffGateMapper.selectGateList(searchVO);
    }

    @Override
    @Transactional
    public void registerGate(GateVO gateVO) {
        int count = staffGateMapper.checkGateCodeDuplicate(gateVO.getAirportId(), gateVO.getGateCode());
        if (count > 0) {
            throw new IllegalArgumentException("해당 공항에 이미 등록된 게이트 코드입니다");
        }
        staffGateMapper.insertGate(gateVO);
    }

    @Override
    @Transactional
    public void modifyGate(GateVO gateVO) {
        GateVO existingGate = staffGateMapper.selectGateById(gateVO.getGateId());
        if (existingGate == null) {
            throw new IllegalArgumentException("존재하지 않는 게이트입니다.");
        }
        boolean codeChanged = gateVO.getGateCode() != null
                && !gateVO.getGateCode().equals(existingGate.getGateCode());
        if (codeChanged) {
            // 사용 중이면 코드 변경 차단
            if (isGateInUse(gateVO.getGateId())) {
                throw new IllegalStateException("해당 게이트를 사용하는 운항 스케줄을 먼저 정리해주세요");
            }
            // 새 코드 중복 검사 (같은 공항 내)
            if (staffGateMapper.checkGateCodeDuplicate(existingGate.getAirportId(), gateVO.getGateCode()) > 0) {
                throw new IllegalArgumentException("해당 공항에 이미 등록된 게이트 코드입니다");
            }
        }
        staffGateMapper.updateGate(gateVO);
    }

    @Override
    @Transactional
    public void removeGate(Long gateId) {
        if (isGateInUse(gateId)) {
            throw new IllegalStateException("해당 게이트를 사용하는 운항 스케줄을 먼저 정리해주세요");
        }
        staffGateMapper.deleteGate(gateId);
    }

    @Override
    @Transactional
    public void toggleGateActive(Long gateId, String isActive) {
        if ("N".equals(isActive) && isGateInUse(gateId)) {
            throw new IllegalStateException("해당 게이트를 사용하는 운항 스케줄을 먼저 정리해주세요");
        }
        staffGateMapper.updateGateStatus(gateId, isActive);
    }

    // 운항 스케줄(FLIGHT) 사용 여부. 테이블 미존재 등은 미사용으로 간주
    private boolean isGateInUse(Long gateId) {
        try {
            return staffGateMapper.checkGateUsedInSchedule(gateId) > 0;
        } catch (Exception e) {
            System.out.println("게이트 사용 검증 건너뜀: " + e.getMessage());
            return false;
        }
    }
}