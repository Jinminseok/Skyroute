package kr.spring.staff.basedata.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import kr.spring.staff.basedata.service.StaffAirportService;
import kr.spring.staff.basedata.vo.AirportVO;

import kr.spring.admin.vo.AirCraftVO;
import kr.spring.staff.basedata.service.StaffSeatService;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.DataIntegrityViolationException;

@Controller
@RequestMapping("/staff/basedata")
public class StaffAirportController {

    @Autowired
    private StaffAirportService airportService;

    // 좌석 관련 데이터를
    @Autowired
    private StaffSeatService staffSeatService;

    // 1. 공항 마스터 관리 페이지 조회 (목록 렌더링)
    @GetMapping("/airport/list")
    public String getAirportPage(Model model) {
        
        // 탭 메뉴 활성화를 위해 추가
        model.addAttribute("activeMenu", "base");

        // 공항 목록 담기
        List<AirportVO> airportList = airportService.getAirportList();
        model.addAttribute("airportList", airportList);
        
        // 항공기 목록 담기
        List<AirCraftVO> aircraftList = staffSeatService.getAircraftList();
        model.addAttribute("aircraftList", aircraftList);
        
        return "thviews/staff_main/basedata/base_list"; 
    }

    // 2. 공항 등록 (AJAX)
    @PostMapping("/airport/insert")
    @ResponseBody
    public String insertAirport(@RequestBody AirportVO airportVO) {
        try {
            airportService.registerAirport(airportVO);
            return "success";
        } catch (DuplicateKeyException e) {
            return "duplicate"; 
        } catch (DataIntegrityViolationException e) {
            return "constraint"; 
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }

    // 3. 공항 수정 (AJAX)
    @PostMapping("/airport/update")
    @ResponseBody
    public String updateAirport(@RequestBody AirportVO airportVO) {
        try {
            airportService.modifyAirport(airportVO);
            return "success";
        } catch (DuplicateKeyException e) {
            return "duplicate"; 
        } catch (DataIntegrityViolationException e) {
            return "constraint"; 
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }

    // 4. 공항 삭제 (AJAX)
    @PostMapping("/airport/delete")
    @ResponseBody
    public String deleteAirport(@RequestParam("airportId") int airportId) {
        try {
            airportService.removeAirport(airportId);
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }
    
    // 5. 공항 사용 여부 토글 (AJAX)
    @PostMapping("/airport/toggle")
    @ResponseBody
    public String toggleAirport(@RequestBody AirportVO airportVO) {
        try {
            airportService.toggleAirportActive(airportVO);
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }
}