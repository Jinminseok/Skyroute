package kr.spring.staff.basedata.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import kr.spring.staff.basedata.service.StaffFareService;
import kr.spring.staff.basedata.vo.FareVO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/staff/basedata/fare")
public class StaffFareController {

    @Autowired
    private StaffFareService fareService;

    // 1. 등록 (AJAX)
    @PostMapping("/insert")
    @ResponseBody
    public String insertFare(@RequestBody FareVO fareVO) {
        try {
        	log.info(">>>> 운임 등록 데이터 (가격 자동계산됨): {}", fareVO);
        	if (fareService.checkDuplicateFare(fareVO) > 0) {
                return "duplicate"; 
            }
            fareService.insertFare(fareVO);
            return "success";
        }catch(org.springframework.dao.DuplicateKeyException e) {
        	log.error("DB 중복키 에러 상세 원인 (FARE_ID인지 확인!): ", e);
        	return "duplicate";
        }catch (Exception e) {
            log.error("운임 등록 오류", e);
            return "fail";
        }
    }

    // 2. 수정 (AJAX)
    @PostMapping("/update")
    @ResponseBody
    public String updateFare(@RequestBody FareVO fareVO) {
        try {
            log.info(">>>> 운임 수정 데이터: {}", fareVO);
            fareService.updateFare(fareVO);
            return "success";
        } catch (Exception e) {
            log.error("운임 수정 오류", e);
            return "fail";
        }
    }

    // 3. 삭제 (AJAX)
    @PostMapping("/delete")
    @ResponseBody
    public String deleteFare(@RequestParam("fare_id") Long fare_id) {
        try {
            // 삭제 전 사용 여부 체크 로직 포함 (service에 구현된 메서드 활용)
            boolean result = fareService.disableFare(fare_id);
            return result ? "success" : "in_use";
        } catch (Exception e) {
            log.error("운임 삭제 오류", e);
            return "fail";
        }
    }

    // 4. 상태 토글 (AJAX)
    @PostMapping("/updateFareActive")
    @ResponseBody
    public Map<String, Object> updateFareActive(@RequestBody Map<String, Object> payload) {
        Map<String, Object> result = new HashMap<>();
        try {
            String status = fareService.updateFareActive(payload);
            if ("in_use".equals(status)) {
                result.put("result", "in_use");
                result.put("message", "사용 중인 운임이라 변경할 수 없습니다.");
            } else {
                result.put("result", "success");
            }
        } catch (Exception e) {
            log.error("상태 변경 오류", e);
            result.put("result", "error");
        }
        return result;
    }
 // 5. 프론트엔드 실시간 가격 계산용 API (AJAX)
    @GetMapping("/calculatePrice")
    @ResponseBody
    public String calculatePrice(FareVO fareVO) {
        try {
            int expectedPrice = fareService.calculateExpectedPrice(fareVO);
            return String.valueOf(expectedPrice);
        } catch (Exception e) {
            log.error("가격 실시간 계산 오류", e);
            return "0";
        }
    }
}