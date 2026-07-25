package kr.spring.staff.basedata.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import kr.spring.staff.basedata.service.StaffAirportService;
import kr.spring.staff.basedata.service.StaffFareService;
import kr.spring.staff.basedata.vo.AirportVO;

import kr.spring.admin.vo.AirCraftVO;
import kr.spring.staff.basedata.service.StaffSeatService;

// 게이트 처리를 위해 추가된 Import
import kr.spring.staff.basedata.service.StaffGateService;
import kr.spring.staff.basedata.vo.GateVO;
import kr.spring.staff.basedata.vo.SeatVO;

// 👉 [추가] 운항 노선 및 노선 유형(관리자) 처리를 위한 Import
import kr.spring.admin.dao.RouteTypeMapper;
import kr.spring.admin.vo.RouteTypeVO;
import kr.spring.admin.vo.SeasonVO;
import kr.spring.staff.basedata.service.StaffRouteService;
import kr.spring.staff.basedata.vo.RouteVO;
import kr.spring.staff.basedata.vo.SeatClassVO;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.HashMap;
import kr.spring.staff.basedata.vo.FareVO;

@Controller
@RequestMapping("/staff/basedata")
public class StaffAirportController {

    @Autowired
    private StaffAirportService airportService;

    // 좌석 관련 데이터
    @Autowired
    private StaffSeatService staffSeatService;

    // 게이트 관련 데이터
    @Autowired
    private StaffGateService staffGateService;

    // 노선 유형 데이터 - 관리자
    @Autowired
    private RouteTypeMapper routeTypeMapper;

    // 운항 노선 데이터 - 지상직
    @Autowired
    private StaffRouteService staffRouteService;
    
    //운임 서비스를 주입
    @Autowired
    private StaffFareService fareService;

    // 1. 공항 마스터 관리 페이지 조회 (목록 렌더링)
    @GetMapping("/airport/list")
    public String getAirportPage(Model model) {
        
    	// 탭 메뉴 활성화를 위해 추가
        model.addAttribute("activeMenu", "base");

        // 1. 공항 목록
        List<AirportVO> airportList = airportService.getAirportList();
        model.addAttribute("airportList", (airportList != null) ? airportList : new java.util.ArrayList<>());
        
        // 2. 항공기 목록
        List<AirCraftVO> aircraftList = staffSeatService.getAircraftList();
        model.addAttribute("aircraftList", (aircraftList != null) ? aircraftList : new java.util.ArrayList<>());

        // 3. 기재별 좌석 데이터
        List<Map<String, Object>> seatSummaryList = staffSeatService.getSeatSummaryList();
        model.addAttribute("seatSummaryList", (seatSummaryList != null) ? seatSummaryList : new java.util.ArrayList<>());
        
        // 4. 게이트 목록 
        GateVO gateSearchVO = new GateVO();
        List<GateVO> gateList = staffGateService.getGateList(gateSearchVO);
        model.addAttribute("gateList", (gateList != null) ? gateList : new java.util.ArrayList<>());
        
        // 5. 노선 유형 리스트
        List<RouteTypeVO> routeTypeList = routeTypeMapper.selectRouteTypeList();
        model.addAttribute("routeTypeList", (routeTypeList != null) ? routeTypeList : new java.util.ArrayList<>());

        // 6. 운항 노선 리스트
        List<RouteVO> routeList = staffRouteService.getRouteList();
        model.addAttribute("routeList", (routeList != null) ? routeList : new java.util.ArrayList<>());
        
        // 7. 운임 리스트
        Map<String, Object> fareMap = new HashMap<>();
        fareMap.put("start", 1);
        fareMap.put("end", 1000); 
        List<FareVO> fareList = fareService.selectFareList(fareMap);
        model.addAttribute("fareList", (fareList != null) ? fareList : new java.util.ArrayList<>());
        
        // 8. 좌석 등급 리스트 (에러 방지: null일 경우 빈 리스트 생성)
        List<SeasonVO> seasonList = fareService.getSeasonList();
        System.out.println("DEBUG: 가져온 시즌 리스트 -> " + seasonList);
        model.addAttribute("seasonList", (seasonList == null) ? new java.util.ArrayList<>() : seasonList);
        
        // 9. 시즌 리스트 (중복 선언 제거)
        List<SeatClassVO> seatClassList = staffSeatService.getSeatClassList();
        model.addAttribute("seatClassList", (seatClassList == null) ? new java.util.ArrayList<>() : seatClassList);

        //10. 권역 리스트 조회 및 모델 추가
        List<Map<String, Object>> regionList = airportService.getRegionList();
        model.addAttribute("regionList", (regionList != null) ? regionList : new java.util.ArrayList<>());
        
        return "thviews/staff_main/basedata/base_list"; 
    }

    // 2. 공항 등록 (AJAX)
    @PostMapping("/airport/insert")
    @ResponseBody
    public String insertAirport(@RequestBody AirportVO airportVO) {
        try {
        	if (airportService.checkDuplicateAirport(airportVO) > 0) {
                return "duplicate"; 
            }
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
        	if (airportService.checkDuplicateAirport(airportVO) > 0) {
                return "duplicate"; 
            }
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
            // 노선/운항 스케줄에서 사용 중이면 삭제 차단
            if (airportService.countAirportUsage(airportId) > 0) {
                return "in_use";
            }
            airportService.removeAirport(airportId);
            return "success";
        } catch (DataIntegrityViolationException e) {
            // 혹시 남은 FK 참조로 삭제 실패한 경우도 사용 중으로 처리
            return "in_use";
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
            // 미사용(N) 처리 시, 사용 중이면 차단
            if ("N".equals(airportVO.getIsActive())
                    && airportService.countAirportUsage(airportVO.getAirportId()) > 0) {
                return "in_use";
            }
            airportService.toggleAirportActive(airportVO);
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }
}