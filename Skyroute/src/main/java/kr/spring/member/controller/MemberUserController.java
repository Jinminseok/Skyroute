package kr.spring.member.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import kr.spring.member.service.MemberMailService;
import kr.spring.member.service.MemberService;
import kr.spring.member.vo.MemberVO;
import kr.spring.member.vo.PrincipalDetails;
import kr.spring.util.FileUtil;
import kr.spring.util.ValidationUtil;
import lombok.extern.slf4j.Slf4j;
import kr.spring.staff.content.service.StaffEventService;

@Slf4j
@Controller
@RequestMapping("/member")
public class MemberUserController {
	@Autowired
	private MemberService memberService;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private MemberMailService memberMailService;
	
	//자바빈(VO) 초기화
	@ModelAttribute
	public MemberVO initCommand() {
		return new MemberVO();
	}
	
	//회원 가입 폼 호출
	@GetMapping("/registerUser")
	public String form() {
		return "thviews/member/memberRegister";
	}
	//회원 가입 데이터 처리
		@PostMapping("/registerUser")
		@ResponseBody
		public String submit(@Valid MemberVO memberVO, BindingResult result, Model model, HttpServletRequest request) {
			log.debug("<<회원 가입>> : " + memberVO);
			
			// 유효성 체크 결과 오류가 있으면 폼 호출 (기존 로직 유지)
			if(result.hasErrors()) {
				// 유효성 체크 결과 오류 필드 출력
				ValidationUtil.printErrorFields(result);
								
				return "<script>" +
				       "alert('입력 정보를 다시 확인해주세요.');" +
				       "history.back();" +
				       "</script>";
			}
			
			// 비밀번호 암호화
			memberVO.setPassword(passwordEncoder.encode(memberVO.getPassword()));
			
			// 회원 가입
			memberService.insertMember(memberVO);
						
			String homepageUrl = request.getContextPath() + "/main/home";
			
			return "<script>" +
			       "alert('회원 가입이 완료되었습니다.');" +
			       "location.href='" + homepageUrl + "';" +
			       "</script>";
		}
		
		//회원가입 이메일 중복 체크		
		@PostMapping("/confirmEmail")
		@ResponseBody
		public Map<String, String> confirmEmail(@RequestParam("email") String email) {
			Map<String, String> mapJson = new HashMap<String, String>();
			
			MemberVO member = memberService.selectCheckEmail(email);

			if (member != null) {				
				mapJson.put("result", "emailDuplicated");
			} else {				
				mapJson.put("result", "emailNotFound");
			}

			return mapJson;
		}
	
	//아이디 찾기 처리
		@PostMapping("/findId")
		@ResponseBody
		public String findId(@RequestParam("name") String name, @RequestParam("email") String email) {
			
			// 이름과 이메일로 DB 조회
			String foundId = memberMailService.findIdByNameAndEmail(name, email);
			
			if (foundId == null) {
				return "일치하는 회원 정보가 존재하지 않습니다.";
			}
			
			// 일치하는 회원이 있을 경우 메일 발송
			String subject = "[SkyRoute] 아이디 찾기 안내";
			String body = "안녕하세요 " + name + "님,\n\n가입하신 아이디는 [" + foundId + "] 입니다.";
			
			memberMailService.sendMail(email, subject, body);
			
			return "입력하신 이메일 주소로 아이디를 발송했습니다.";
		}
	
		// 비밀번호 찾기 (임시 비밀번호 발급) 처리
		@PostMapping("/findPw")
		@ResponseBody
		public String findPw(@RequestParam("id") String id, 
							 @RequestParam("name") String name, 
							 @RequestParam("email") String email) {
			
			// 아이디, 이름, 이메일 일치 여부 확인
			boolean isExist = memberMailService.checkMemberForResetPw(id, name, email);
			
			if (!isExist) {
				return "일치하는 회원 정보가 존재하지 않습니다.";
			}
			
			// 8자리 임시 비밀번호 난수 생성
			String tempPw = java.util.UUID.randomUUID().toString().substring(0, 8);
			
			// 생성된 임시 비밀번호를 암호화하여 DB 업데이트
			String encodedPw = passwordEncoder.encode(tempPw);
			memberMailService.updatePassword(id, encodedPw);
			
			// 임시 비밀번호 메일 발송
			String subject = "[SkyRoute] 임시 비밀번호 안내";
			String body = "안녕하세요 " + name + "님,\n\n요청하신 임시 비밀번호는 [" + tempPw + "] 입니다.\n로그인 후 즉시 비밀번호를 변경해 주세요.";
			
			memberMailService.sendMail(email, subject, body);
			
			return "입력하신 이메일 주소로 임시 비밀번호를 발송했습니다.";
		}
		
		
	//예약 조회변경 폼 호출
	@GetMapping("/member_booking")
	public String bookingForm() {	    
	    return "thviews/member/member_booking"; 
	}
	
	
	
	
	//출/도착지 및 스케쥴 폼 호출
	@GetMapping("/member_schedule")
	public String scheduleForm(@RequestParam(value = "tabType", required = false, defaultValue = "STATUS") String tabType,
			Model model) {
		model.addAttribute("tabType", tabType);
		return "thviews/member/member_schedule";
	}
	
	
	
	
	//운항스케쥴 폼 조회
	@GetMapping("/member_route")
	public String routeForm(Model model) {
		
		List<Map<String, Object>> regionList = memberService.selectActiveRegionList();
		List<Map<String, Object>> routeList = memberService.selectActiveRouteList();
		
		model.addAttribute("regionList", regionList);
		model.addAttribute("routeList", routeList);
		
		return "thviews/member/member_route";
	}
	
	
	
	
	//기능 모아보기 폼 호출
	@GetMapping("/member_functions")
	public String functionsForm() {
		return "thviews/member/member_functions";
	}
	
	
	
	//마이페이지 폼 호출
	@GetMapping("/member_mypage")
	public String mypageForm(@AuthenticationPrincipal PrincipalDetails principalDetails,
			Model model) {
		if (principalDetails == null || principalDetails.getMemberVO() == null) {
			return "redirect:/member/login";
		}

		MemberVO memberVO = principalDetails.getMemberVO();
		long memberId = memberVO.getMember_id();

		model.addAttribute("member", memberVO);

		model.addAttribute("eventParticipationList",
				staffEventService.selectMyParticipationList(memberId));

		return "thviews/member/member_mypage";
	}
	
	// 내 정보 관리 - 프로필 수정 처리
	@PostMapping("/updateProfile")
	@ResponseBody
	public String updateProfile(@AuthenticationPrincipal PrincipalDetails principalDetails,
								MemberVO memberVO) {

		if (principalDetails == null || principalDetails.getMemberVO() == null) {
			return "로그인 세션이 만료되었습니다. 다시 로그인 후 시도해 주세요.";
		}

		MemberVO loginMember = principalDetails.getMemberVO();

		if (!passwordEncoder.matches(memberVO.getNow_passwd(), loginMember.getPassword())) {
			return "현재 비밀번호가 일치하지 않습니다."; 
		}

		memberVO.setMember_id(loginMember.getMember_id());
		memberVO.setId(loginMember.getId()); 

		if (memberVO.getPassword() != null && !memberVO.getPassword().trim().isEmpty()) {
			String encodedNewPw = passwordEncoder.encode(memberVO.getPassword());
			memberVO.setPassword(encodedNewPw);
		} else {
			memberVO.setPassword(loginMember.getPassword());
		}

		memberService.updateMemberProfile(memberVO);
		principalDetails.setMemberVO(memberVO);

		return "회원 정보 수정이 완료되었습니다."; 
	}
	
	//회원 탈퇴 기능
	@PostMapping("/deleteAccount") 
	@ResponseBody
	public String deleteAccount(@RequestParam("now_passwd") String nowPasswd,
								@AuthenticationPrincipal PrincipalDetails principalDetails,
								HttpServletRequest request) {
		
		if (principalDetails == null || principalDetails.getMemberVO() == null) {
			return "로그인 세션이 만료되었습니다. 다시 로그인 후 시도해 주세요.";
		}

		MemberVO loginMember = principalDetails.getMemberVO();

		if (!passwordEncoder.matches(nowPasswd, loginMember.getPassword())) {
			return "현재 비밀번호가 일치하지 않습니다."; 
		}

		memberService.deleteAccount(loginMember.getMember_id());

		SecurityContextHolder.clearContext();
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
		
		return "회원 탈퇴 처리가 완료되었습니다."; 
	}
	
	
	
	//항공권 세부 검색 폼 호출
	@GetMapping("/member_flight_list")
	public String flightListForm() {
		return "thviews/member/member_flight_list";
	}
	
	
	
	
	//항공권 예매 폼 호출
	@GetMapping("/member_booking_passenger")
	public String bookingPassengerForm() {
		return "thviews/member/member_booking_passenger";
	}
	
	
	
	//좌석 선택 폼 호출
	@GetMapping("/member_select_seat")
	public String selectSeatForm() {
		return "thviews/member/member_select_seat";
	}
	
	
	
	//MY페이지
		/*
		 * @PreAuthorize
		 * 메서드 호출 이전에 접근 권한을 검사
		 * 메서드 실행 전에 주어진 SpEL(Spring Expression Language) 조 건을 평가하여 접근을 허용할지 결정
		 * 자주 사용하는 SpEl 표현식
		 * 표현식                       의미
		 * hasRole('ROLE_ADMIN')       'ROLE_ADMIN' 권한이 있는 사용자만 허용  
		 * hasAuthority('admin')       'admin' 권한이 있는 사용자만 허용
		 * isAuthenticated()            로그인된 사용자만 허용
		 * isAnonymous()                로그인된 되지 않은 사용자만 허용
		 * #id = authentication.principal.id   파라미터와 현재 사용자 비교
		 * @AuthenticationPrincipal 
		 * 현재 로그인한 사용자의 정보를 메서드 파라미터로 주입해주는 
		 * Spring Sercuity의 애너테이션 인증된 사용자가 없으면 null
		 * 
		 */
	/*============================================
	 * 회원로그인
	 *===========================================*/
	//로그인 폼
	@GetMapping("/login")
	public String formLogin() {
		return "thviews/member/memberLogin";
	}	
	
	/*============================================
	 * 프로필 사진 출력
	 *===========================================*/

	

	
	//기본 이미지 읽기
	public void getBasicProfileImage(HttpServletRequest request,Model model) {
		byte[] readbyte = FileUtil.getBytes(request.getServletContext().getRealPath("/assets/image_bundle/face.png"));
		//속성명       속성값(byte[]의 데이터)
		model.addAttribute("imageFile", readbyte);
		model.addAttribute("filename", "face.png");
	}
	
	@Autowired
	private StaffEventService staffEventService;
}
