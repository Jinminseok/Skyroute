package kr.spring.admin.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.spring.admin.dao.RegionMapper;
import kr.spring.admin.vo.RegionVO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class AdminController {

	@Autowired
	private RegionMapper regionMapper;

	@GetMapping("/admin/base1")
	public String adminMain(Model model) {

		List<RegionVO> regionList = regionMapper.selectRegionList();

		model.addAttribute("regionList", regionList);

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


}