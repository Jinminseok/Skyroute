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
        staffGateMapper.updateGate(gateVO);
    }

    @Override
    @Transactional
    public void removeGate(Long gateId) {
        staffGateMapper.deleteGate(gateId);
    }

    // 4. 게이트 사용 여부 토글 (안전장치 로직은 여기서 처리합니다)
    @Override
    @Transactional
    public void toggleGateActive(Long gateId, String isActive) {
        // 비활성화(N)로 바꿀 때만 스케줄 검사
        if ("N".equals(isActive)) {
            try {
                // 미래를 위한 안전장치: 나중에 스케줄 테이블이 생기면 이 로직이 정상 작동합니다.
                int usedCount = staffGateMapper.checkGateUsedInSchedule(gateId);
                if (usedCount > 0) {
                    throw new IllegalStateException("해당 게이트를 사용하는 운항 스케줄을 먼저 정리해주세요");
                }
            } catch (IllegalStateException e) {
                // 스케줄이 배정되어 있어서 막아야 하는 경우 (정상적인 차단)
                throw e; 
            } catch (Exception e) {
                // ORA-00942: 테이블이 아직 없어서 나는 에러라면? 
                // -> 아하, 아직 초기 세팅 단계구나! 하고 무시하고 넘어갑니다.
                System.out.println("운항 스케줄 테이블이 아직 없어 검증을 건너뜁니다.");
            }
        }
        
        // 상태 업데이트
        staffGateMapper.updateGateStatus(gateId, isActive);
    }
}