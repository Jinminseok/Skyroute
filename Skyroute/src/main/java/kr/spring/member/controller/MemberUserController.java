package kr.spring.member.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
	public String submit(@Valid MemberVO memberVO, BindingResult result, Model model, HttpServletRequest request) {
		log.debug("<<회원 가입>> : " + memberVO);
		
		//유효성 체크 결과 오류가 있으면 폼 호출
		if(result.hasErrors()) {
			//유효성 체크 결과 오류 필드 출력
			ValidationUtil.printErrorFields(result);
			return form();
		}
		
		//비밀번호 암호화
		memberVO.setPassword(passwordEncoder.encode(memberVO.getPassword()));
		
		//회원 가입
		memberService.insertMember(memberVO);
		
		//결과 메시지 처리
		model.addAttribute("accessTitle", "회원 가입");
		model.addAttribute("accessMsg", "회원 가입이 완료되었습니다.");
		model.addAttribute("accessBtn", "홈으로");
		model.addAttribute("accessUrl", request.getContextPath()+"/main/home");
		
		return "thviews/common/resultView";
	}
	
	//예약 조회변경 폼 호출
	@GetMapping("/member_booking")
	public String bookingForm() {	    
	    return "thviews/member/member_booking"; 
	}
	
	
	
	
	//체크인 폼 호출
	@GetMapping("/member_checkin")
	public String checkinForm() {
		return "thviews/member/member_checkin";
	}
	
	
	
	
	//운항스케쥴 조회
	@GetMapping("/member_schedule")
	public String scheduleForm() {
		return "thviews/member/member_schedule";
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

		long memberId = principalDetails.getMemberVO().getMember_id();

		model.addAttribute("eventParticipationList",
				staffEventService.selectMyParticipationList(memberId));

		return "thviews/member/member_mypage";
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
