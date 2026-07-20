package kr.spring.staff.operation.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;
import kr.spring.staff.operation.service.StaffCheckService;
import kr.spring.staff.operation.vo.CheckVO;

@Controller
@RequestMapping("/staff/operation") 
public class StaffCheckController {

    @Autowired
    private StaffCheckService staffCheckService;

    // ▼ 이 부분의 리턴 값을 이미지에 있는 실제 경로로 완벽하게 맞춰줍니다!
    @GetMapping("/passenger/list")
    public String boardingPage() {
        // templates 폴더 하위의 경로를 확장자(.html) 없이 적어줍니다.
        return "thviews/staff_main/operation/passenger/passenger_list"; 
    }

 // 1. 셀렉트 박스에 넣을 항공편 목록 조회 (추가됨)
    @ResponseBody
    @GetMapping("/api/flights")
    public Map<String, Object> getFlightList() {
        Map<String, Object> map = new HashMap<>();
        try {
            List<Map<String, Object>> list = staffCheckService.getFlightList();
            map.put("result", "success");
            map.put("data", list);
        } catch (Exception e) {
            map.put("result", "error");
        }
        return map;
    }

    // 2. 항공편별 승객 리스트 조회 (flightCode -> flightId 로 변경)
    @ResponseBody
    @GetMapping("/api/passengers")
    public Map<String, Object> getPassengerList(@RequestParam Long flightId) {
        Map<String, Object> map = new HashMap<>();
        try {
            List<CheckVO> list = staffCheckService.getPassengerList(flightId);
            map.put("result", "success");
            map.put("data", list);
        } catch (Exception e) {
            map.put("result", "error");
            map.put("message", e.getMessage());
        }
        return map;
    }

    @ResponseBody
    @PostMapping("/api/updateStatus")
    public Map<String, Object> updateStatus(@RequestBody CheckVO checkVO, HttpSession session) {
    	System.out.println("컨트롤러에서 받은 값: " + checkVO);
        Map<String, Object> map = new HashMap<>();
        try {
            checkVO.setCheckedInBy(3L); // DB MEMBER 테이블에 1002번이 반드시 실존해야 함
            staffCheckService.modifyTicketStatus(checkVO);
            map.put("result", "success");
        } catch (Exception e) {
            e.printStackTrace(); // 필수 추가: 쿼리가 실패한 진짜 원인을 스프링 콘솔에 빨간 글로 출력
            map.put("result", "error");
            map.put("message", e.getMessage()); // 프론트 화면 알림창에 실제 DB 에러 내용 노출
        }
        return map;
    }
}