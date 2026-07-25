package kr.spring.admin.controller;

import java.util.List;
import java.util.Map; // 추가: Map import
import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.spring.admin.dao.AdminMemberMapper;
import kr.spring.admin.dao.GateAreaMapper;
import kr.spring.admin.dao.RegionMapper;
import kr.spring.admin.dao.RouteTypeMapper;
import kr.spring.admin.dao.SeasonMapper;
import kr.spring.admin.service.AirCraftService;
import kr.spring.admin.service.StatService;
import kr.spring.admin.vo.AirCraftVO;
import kr.spring.admin.vo.GateAreaVO;
import kr.spring.admin.vo.RegionVO;
import kr.spring.admin.vo.RouteTypeVO;
import kr.spring.admin.vo.SeasonVO;
import kr.spring.admin.vo.StatVO; // 추가: StatVO import
import kr.spring.member.vo.MemberVO;
import kr.spring.admin.service.RefundPolicyService;
import kr.spring.admin.vo.RefundPolicyVO;

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

	@Autowired
	private SeasonMapper seasonMapper; //시즌
	
	@Autowired
	private RefundPolicyService refundPolicyService;

	@Autowired
	private AdminMemberMapper adminMemberMapper; //회원 관리
	
	@Autowired
	private AirCraftService airCraftService;	// 항공기 관리
	
	@Autowired
	private StatService statService; // 통계 서비스

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
	public String adminBase2(Model model) {

		// 항공기 목록 조회
		List<AirCraftVO> aircraftList = airCraftService.selectListAircraft();
		model.addAttribute("aircraftList", aircraftList);

		// 시즌
		List<SeasonVO> seasonList = seasonMapper.selectSeasonList();
		model.addAttribute("seasonList", seasonList);

		// 환불
		List<RefundPolicyVO> refundPolicyList = refundPolicyService.selectRefundPolicyList();
		model.addAttribute("refundPolicyList", refundPolicyList);

		return "thviews/admin_main/admin_base2";
	}

	// 4. 전사 운영 통계 페이지 (기본 뷰 반환)
	@GetMapping("/admin/statistics")
	public String statistics() {
		return "thviews/admin_main/admin_statistics";
	}

	// ==========================================
	// 5. 통계 데이터 비동기(AJAX) 요청 처리 API (추가된 부분)
	// ==========================================
	@GetMapping("/admin/statistics/data")
	@ResponseBody
	public Map<String, Object> getStatisticsData(
			@RequestParam(required = false) String startDate, 
			@RequestParam(required = false) String endDate) {
		
		StatVO statVO = new StatVO();
		statVO.setStartDate(startDate);
		statVO.setEndDate(endDate);
		
		// 날짜에 따른 매출, 예약, 노선별 데이터를 Map으로 반환 (Spring이 자동으로 JSON 변환)
		return statService.getDashboardStatistics(statVO);
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
	public String toggleRegionStatus(
			@RequestParam("regionId") int regionId,
			@RequestParam("isActive") String isActive) {

		if (!"Y".equals(isActive)
				&& !"N".equals(isActive)) {

			return "invalid";
		}

		try {

			/*
			 * 사용 중인 권역은 미사용으로 변경할 수 없습니다.
			 * 다시 활성화하는 것은 허용합니다.
			 */
			if ("N".equals(isActive)
					&& regionMapper
						.countAirportsByRegionId(regionId) > 0) {

				return "in-use";
			}

			int updatedCount =
					regionMapper.updateRegionStatus(
							regionId,
							isActive
					);

			return updatedCount == 1
					? "success"
					: "not-found";

		} catch (Exception e) {

			log.error(
				"권역 상태 변경 중 오류 발생. regionId={}",
				regionId,
				e
			);

			return "error";
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
	@ResponseBody
	public String toggleGateStatus(
			@RequestParam("gateAreaId") int gateAreaId,
			@RequestParam("isActive") String isActive) {

		if (!"Y".equals(isActive)
				&& !"N".equals(isActive)) {

			return "invalid";
		}

		try {

			/*
			 * 사용 중인 게이트 구역은
			 * 미사용으로 변경하지 않습니다.
			 */
			if ("N".equals(isActive)
					&& gateAreaMapper
						.countGatesByGateAreaId(gateAreaId) > 0) {

				return "in-use";
			}

			int updatedCount =
					gateAreaMapper.updateGateAreaStatus(
							gateAreaId,
							isActive
					);

			return updatedCount == 1
					? "success"
					: "not-found";

		} catch (Exception e) {

			log.error(
				"게이트 구역 상태 변경 중 오류 발생. gateAreaId={}",
				gateAreaId,
				e
			);

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
	public String toggleRouteTypeStatus(
			@RequestParam("routeTypeId") int routeTypeId,
			@RequestParam("isActive") String isActive) {

		if (!"Y".equals(isActive)
				&& !"N".equals(isActive)) {

			return "invalid";
		}

		try {

			/*
			 * 사용 중인 노선 유형은
			 * 미사용으로 변경하지 않습니다.
			 */
			if ("N".equals(isActive)
					&& routeTypeMapper
						.countRoutesByRouteTypeId(routeTypeId) > 0) {

				return "in-use";
			}

			int updatedCount =
					routeTypeMapper.updateRouteTypeStatus(
							routeTypeId,
							isActive
					);

			return updatedCount == 1
					? "success"
					: "not-found";

		} catch (Exception e) {

			log.error(
				"노선 유형 상태 변경 중 오류 발생. routeTypeId={}",
				routeTypeId,
				e
			);

			return "error";
		}
	}
	
	// ==========================================
	// 항공기 처리
	// ==========================================

	@PostMapping("/admin/aircraft/insert")
	public String insertAircraft(
	        AirCraftVO airCraftVO) {

	    log.debug(
	        "항공기 등록 요청: {}",
	        airCraftVO
	    );

	    airCraftService.insertAircraft(
	        airCraftVO
	    );

	    return "redirect:/admin/base2";
	}


	/*
	 * 항공기 제원 수정
	 *
	 * AJAX 요청이므로 처리 결과 문자열만 반환합니다.
	 */
	@PostMapping("/admin/aircraft/update")
	@ResponseBody
	public String updateAircraft(
	        AirCraftVO airCraftVO) {

	    try {

	        return airCraftService
	                .updateAircraft(airCraftVO);

	    } catch (Exception e) {

	        log.error(
	            "항공기 수정 중 오류 발생. aircraftId={}",
	            airCraftVO.getAircraft_id(),
	            e
	        );

	        return "error";
	    }
	}


	/*
	 * 항공기 사용 여부 토글
	 */
	@PostMapping("/admin/aircraft/toggleStatus")
	@ResponseBody
	public String toggleAircraftStatus(
	        @RequestParam("aircraft_id")
	        int aircraftId,

	        @RequestParam("is_active")
	        String isActive) {

	    try {

	        return airCraftService
	                .updateAircraftStatus(
	                    aircraftId,
	                    isActive
	                );

	    } catch (Exception e) {

	        log.error(
	            "항공기 사용 여부 변경 중 오류 발생. aircraftId={}",
	            aircraftId,
	            e
	        );

	        return "error";
	    }
	}


	/*
	 * 항공기 삭제
	 *
	 * GET이 아니라 POST로 처리합니다.
	 */
	@PostMapping("/admin/aircraft/delete")
	@ResponseBody
	public String deleteAircraft(
	        @RequestParam("aircraft_id")
	        int aircraftId) {

	    try {

	        return airCraftService
	                .deleteAircraft(aircraftId);

	    } catch (DataIntegrityViolationException e) {

	        /*
	         * 검증 직후 다른 데이터가 등록되는 경쟁 상황 등으로
	         * FK 오류가 발생한 경우의 최종 방어입니다.
	         */
	        log.warn(
	            "참조 데이터가 있는 항공기 삭제 시도. aircraftId={}",
	            aircraftId,
	            e
	        );

	        return "in-use";

	    } catch (Exception e) {

	        log.error(
	            "항공기 삭제 중 오류 발생. aircraftId={}",
	            aircraftId,
	            e
	        );

	        return "error";
	    }
	}
	// ====== 시즌 처리 로직 ======
	@PostMapping("/admin/season/insert")
	public String insertSeason(SeasonVO seasonVO) {
		seasonMapper.insertSeason(seasonVO);
		return "redirect:/admin/base2";
	}

	@PostMapping("/admin/season/update")
	public String updateSeason(SeasonVO seasonVO) {
		seasonMapper.updateSeason(seasonVO);
		return "redirect:/admin/base2";
	}

	@GetMapping("/admin/season/delete")
	public String deleteSeason(@RequestParam int seasonId) {
		seasonMapper.deleteSeason(seasonId);
		return "redirect:/admin/base2";
	}

	@PostMapping("/admin/season/toggleStatus")
	@ResponseBody // AJAX 비동기 처리
	public String toggleSeasonStatus(@RequestParam int seasonId, @RequestParam String isActive) {
		try {
			seasonMapper.updateSeasonStatus(seasonId, isActive);
			return "success";
		} catch (Exception e) {
			log.error("운임 시즌 상태 토글 중 오류 발생", e);
			return "error";
		}
	}
	
	
	@PostMapping("/admin/refundPolicy/update")
	@ResponseBody
	public String updateRefundPolicy(
			@RequestParam long policyId,
			@RequestParam BigDecimal feeRate) {

		try {

			refundPolicyService.updateFeeRate(policyId, feeRate);

			return "success";

		} catch (IllegalArgumentException e) {

			log.warn("환불 수수료율 입력값 오류: {}", e.getMessage());

			return "invalid";

		} catch (IllegalStateException e) {

			log.warn("환불 수수료율 수정 거부: {}", e.getMessage());

			return "locked";

		} catch (Exception e) {

			log.error("환불 수수료율 수정 중 오류 발생", e);

			return "error";
		}
	}
	
	

	// 시스템 계정 및 가입 회원 관리 페이지 매핑
	@GetMapping("/admin/accounts")
	public String accountsManage(@RequestParam(required = false) String keyword,Model model) {
		// USER 권한을 가진 일반 회원 목록 조회
		List<MemberVO> userList = adminMemberMapper.selectMemberListByRoleAndKeyword("USER", keyword);
		// STAFF 권한을 가진 지상직 회원 목록 조회
		List<MemberVO> staffList = adminMemberMapper.selectMemberListByRoleAndKeyword("STAFF", keyword);

		// 모델에 데이터 담기
		model.addAttribute("userList", userList);
		model.addAttribute("staffList", staffList);
		model.addAttribute("keyword", keyword);

		return "thviews/admin_main/admin_accounts";
	}

	@GetMapping("/admin/member/detail")
	@ResponseBody
	public MemberVO getMemberDetail(@RequestParam long memberId) {
		return adminMemberMapper.selectMemberById(memberId);
	}

	// 회원 정보 수정 (상태 및 권한 변경)
	@PostMapping("/admin/member/update")
	public String updateMemberStatusAndRole(MemberVO memberVO,
	        @RequestParam(required = false, defaultValue = "user") String tab) {
	    adminMemberMapper.updateMemberStatusAndRole(memberVO);
	    return "redirect:/admin/accounts?tab=" + tab;
	}
	
	//지상직 계정 생성
	@PostMapping("/admin/staff/insert")
	public String insertStaff(MemberVO memberVO) {
	    try {
	        adminMemberMapper.insertStaff(memberVO);
	    } catch (org.springframework.dao.DuplicateKeyException e) {
	        log.error("계정 생성 실패 - 중복된 데이터(아이디/이메일/연락처) 존재");
	        return "redirect:/admin/accounts?tab=staff&error=duplicate";
	    } catch (Exception e) {
	        log.error("계정 생성 중 알 수 없는 오류 발생", e);
	        return "redirect:/admin/accounts?tab=staff&error=true";
	    }
	    return "redirect:/admin/accounts?tab=staff";
	}
}