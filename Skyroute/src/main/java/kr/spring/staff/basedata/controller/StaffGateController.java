package kr.spring.staff.basedata.controller;

import kr.spring.staff.basedata.service.StaffGateService;
import kr.spring.staff.basedata.vo.GateVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/staff/basedata/gate") // JS 요청 주소와 정확히 일치시킴
@RequiredArgsConstructor
public class StaffGateController {

    private final StaffGateService staffGateService;

    // 1. 게이트 등록
    @PostMapping("/insert")
    public String insertGate(@RequestBody GateVO gateVO) {
        try {
            staffGateService.registerGate(gateVO);
            return "success";
        } catch (IllegalArgumentException e) {
            return "duplicate";
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }

    // 2. 게이트 수정
    @PostMapping("/update")
    public String updateGate(@RequestBody GateVO gateVO) {
        try {
            staffGateService.modifyGate(gateVO);
            return "success";
        } catch (IllegalStateException e) {
            return "in_use";
        } catch (IllegalArgumentException e) {
            return "duplicate";
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }

    // 3. 게이트 삭제 (신규 추가)
    @PostMapping("/delete")
    public String deleteGate(@RequestParam("gateId") Long gateId) {
        try {
            staffGateService.removeGate(gateId);
            return "success";
        } catch (IllegalStateException e) {
            return "in_use";
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }

    // 4. 게이트 사용 여부 토글 (컨트롤러는 요청만 받고 서비스로 넘깁니다)
    @PostMapping("/toggle")
    public String toggleGate(@RequestBody Map<String, Object> payload) {
        try {
            Long gateId = Long.valueOf(payload.get("gateId").toString());
            String isActive = payload.get("isActive").toString();
            
            staffGateService.toggleGateActive(gateId, isActive);
            return "success";
        } catch (IllegalStateException e) {
            // 스케줄에 사용 중일 때의 예외 처리
            return "in_use";
        } catch (Exception e) {
            e.printStackTrace(); 
            return "fail";
        }
    }
}