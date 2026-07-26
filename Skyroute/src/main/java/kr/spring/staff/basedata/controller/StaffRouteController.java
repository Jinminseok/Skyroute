package kr.spring.staff.basedata.controller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.spring.staff.basedata.service.StaffRouteService;
import kr.spring.staff.basedata.vo.RouteVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/staff/basedata/route")
public class StaffRouteController {

    private final StaffRouteService staffRouteService;

    // 노선 등록
    @PostMapping("/insert")
    @ResponseBody
    public String insertRoute(@RequestBody RouteVO routeVO) {
        try {
            if(routeVO.getDeparture_airport_id() == routeVO.getArrival_airport_id()) {
                return "same_airport"; // 출발지와 도착지가 같으면 튕겨냄
            }
            return staffRouteService.insertRoute(routeVO);
        } catch (Exception e) {
            log.error("노선 등록 중 오류 발생", e);
            return "error";
        }
    }

    // 노선 수정
    @PostMapping("/update")
    @ResponseBody
    public String updateRoute(@RequestBody RouteVO routeVO) {
        try {
            if (routeVO.getDeparture_airport_id() == routeVO.getArrival_airport_id()) {
                return "same_airport";
            }
            return staffRouteService.updateRoute(routeVO);
        } catch (IllegalStateException e) {
            return "in_use";
        } catch (Exception e) {
            log.error("노선 수정 중 오류 발생", e);
            return "error";
        }
    }

    // 노선 삭제
    @PostMapping("/delete")
    @ResponseBody
    public String deleteRoute(@RequestParam("routeId") int routeId) {
        try {
            staffRouteService.deleteRoute(routeId);
            return "success";
        } catch (IllegalStateException e) {
            return "in_use";
        } catch (Exception e) {
            log.error("노선 삭제 중 오류 발생", e);
            return "error";
        }
    }

    // 노선 사용 여부 토글
    @PostMapping("/toggle")
    @ResponseBody
    public String toggleRouteActive(@RequestBody Map<String, Object> payload) {
        try {
            staffRouteService.updateRouteActive(payload);
            return "success";
        } catch (IllegalStateException e) {
            return "in_use";
        } catch (Exception e) {
            log.error("노선 토글 중 오류 발생", e);
            return "error";
        }
    }
}