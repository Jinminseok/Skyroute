package kr.spring.admin.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.spring.admin.dao.GateAreaMapper;
import kr.spring.admin.dao.RegionMapper;
import kr.spring.admin.dao.RouteTypeMapper;
import kr.spring.admin.vo.GateAreaVO;
import kr.spring.admin.vo.RegionVO;
import kr.spring.admin.vo.RouteTypeVO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class AdminController {

	@Autowired
	private RegionMapper regionMapper; // 권역
	
	@Autowired
    private GateAreaMapper gateAreaMapper; //게이트
	
	@Autowired
	private RouteTypeMapper routeTypeMapper; // 노선

	@GetMapping("/admin/base1")
	public String adminMain(Model model) {
		// 권역
		List<RegionVO> regionList = regionMapper.selectRegionList();
		model.addAttribute("regionList", regionList);
		//게이트
		List<GateAreaVO> gateAreaList = gateAreaMapper.selectGateAreaList();
        model.addAttribute("gateAreaList", gateAreaList);
        // 노선
        List<RouteTypeVO> routeTypeList = routeTypeMapper.selectRouteTypeList();
        model.addAttribute("routeTypeList", routeTypeList);

		return "thviews/admin_main/admin_base1"; 
	}


	@GetMapping("/admin/base2")
	public String adminBase2() {

		return "thviews/admin_main/admin_base2"; 
	}


	// 3. 계정/회원 관리 페이지
	@GetMapping("/admin/accounts")
	public String accounts() {
		return "thviews/admin_main/admin_accounts";
	}

	// 4. 전사 운영 통계 페이지
	@GetMapping("/admin/statistics")
	public String statistics() {
		return "thviews/admin_main/admin_statistics";
	}
	//========================
	//권역부분
	//========================
	// 권역 등록 (모달에서 등록 버튼 클릭 시)
	@PostMapping("/admin/region/insert")
	public String insertRegion(RegionVO region) {
		regionMapper.insertRegion(region);
		// 등록 후 다시 리스트 화면(base1)으로 리다이렉트
		return "redirect:/admin/base1";
	}

	// 권역 수정 (모달에서 수정 버튼 클릭 시)
	@PostMapping("/admin/region/update")
	public String updateRegion(RegionVO region) {
		regionMapper.updateRegion(region);
		return "redirect:/admin/base1";
	}

	// 권역 삭제 (모달에서 삭제 버튼 클릭 시)
	@GetMapping("/admin/region/delete")
	public String deleteRegion(int regionId) {
		regionMapper.deleteRegion(regionId);
		return "redirect:/admin/base1";
	}
	// ========================
	// 비동기 통신: 권역 사용 여부(토글) 변경
	// ========================
	@PostMapping("/admin/region/toggleStatus")
	@ResponseBody
	public String toggleRegionStatus(int regionId, String isActive) {
		try {
			
			regionMapper.updateRegionStatus(regionId, isActive);
			return "success";
		} catch (Exception e) { 
			log.error("권역 상태 토글 변경 중 오류 발생", e);
			return "fail";
		}
	}
	
	// ====== 게이트 구역 (GATE_AREA) 처리 로직 ======
	@PostMapping("/admin/gate/insert")
	public String insertGateArea(GateAreaVO gateAreaVO) {
	    gateAreaMapper.insertGateArea(gateAreaVO);
	    return "redirect:/admin/base1"; // 본인의 메인 페이지 URL에 맞게 수정
	}

	@PostMapping("/admin/gate/update")
	public String updateGateArea(GateAreaVO gateAreaVO) {
	    gateAreaMapper.updateGateArea(gateAreaVO);
	    return "redirect:/admin/base1";
	}

	@GetMapping("/admin/gate/delete")
	public String deleteGateArea(@RequestParam int gateAreaId) {
	    gateAreaMapper.deleteGateArea(gateAreaId);
	    return "redirect:/admin/base1";
	}

	@PostMapping("/admin/gate/toggleStatus")
	@ResponseBody // AJAX 요청이므로 값만 반환
	public String toggleGateStatus(@RequestParam int gateAreaId, @RequestParam String isActive) {
	    try {
	        // 기존 데이터를 불러와서 상태만 변경 후 업데이트
	        GateAreaVO gateArea = gateAreaMapper.selectGateArea(gateAreaId);
	        if (gateArea != null) {
	            gateArea.setIsActive(isActive);
	            gateAreaMapper.updateGateArea(gateArea);
	            return "success";
	        }
	        return "fail";
	    } catch (Exception e) {
	        e.printStackTrace();
	        return "error";
	    }
	}
	// ====== 노선 처리 로직 ======
	@PostMapping("/admin/routeType/insert")
	public String insertRouteType(RouteTypeVO routeTypeVO) {
	    routeTypeMapper.insertRouteType(routeTypeVO);
	    return "redirect:/admin/base1";
	}

	@PostMapping("/admin/routeType/update")
	public String updateRouteType(RouteTypeVO routeTypeVO) {
	    routeTypeMapper.updateRouteType(routeTypeVO);
	    return "redirect:/admin/base1";
	}

	@GetMapping("/admin/routeType/delete")
	public String deleteRouteType(@RequestParam int routeTypeId) {
	    routeTypeMapper.deleteRouteType(routeTypeId);
	    return "redirect:/admin/base1";
	}

	@PostMapping("/admin/routeType/toggleStatus")
	@ResponseBody
	public String toggleRouteTypeStatus(@RequestParam int routeTypeId, @RequestParam String isActive) {
	    try {
	        RouteTypeVO routeType = routeTypeMapper.selectRouteType(routeTypeId);
	        if (routeType != null) {
	            routeType.setIsActive(isActive);
	            routeTypeMapper.updateRouteType(routeType);
	            return "success";
	        }
	        return "fail";
	    } catch (Exception e) {
	        log.error("노선 유형 상태 토글 중 오류 발생", e);
	        return "error";
	    }
	}
}