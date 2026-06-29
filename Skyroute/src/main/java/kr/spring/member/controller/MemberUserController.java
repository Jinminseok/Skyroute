package kr.spring.member.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import kr.spring.member.service.MemberService;
import kr.spring.member.vo.MemberVO;
import kr.spring.member.vo.PrincipalDetails;
import kr.spring.util.FileUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/member")
public class MemberUserController {
	@Autowired
	private MemberService memberService;

	//자바빈(VO) 초기화
	@ModelAttribute
	public MemberVO initCommand() {
		return new MemberVO();
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
	//프로필 사진 출력(로그인 전용)
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/photoView")
	public String getProfile(@AuthenticationPrincipal PrincipalDetails principal,HttpServletRequest request,Model model) {
		try {
			MemberVO user = principal.getMemberVO();
			log.debug("<<photoView>> : {}", user);
			MemberVO memberVO = memberService.selectMember(user.getMem_num());
			viewProfile(memberVO,request,model);
		}catch(Exception e) {
			getBasicProfileImage(request,model);
		}
		return "imageView";
	}

	//프로필 사진 출력(회원번호 지정)
	@GetMapping("/viewProfile")
	public String getProfileByMem_num(long mem_num,
			HttpServletRequest request,
			Model model) {
		MemberVO memberVO = memberService.selectMember(mem_num);

		viewProfile(memberVO,request,model);

		return "imageView";
	}

	//프로필 사진 처리를 위한 공통 코드
	public void viewProfile(MemberVO memberVO,HttpServletRequest request, Model model) {
		if(memberVO==null || memberVO.getPhoto_name()==null) {
			//DB에 저장된 프로필 이미지가 없기 때문에 기본 이미지 호출
			getBasicProfileImage(request,model);
		}else {//업로드한 프로필 이미지 읽기
			//속성명       속성값(byte[]의 데이터)
			model.addAttribute("imageFile", memberVO.getPhoto());
			model.addAttribute("filename", memberVO.getPhoto_name());
		}
	}
	//기본 이미지 읽기
	public void getBasicProfileImage(HttpServletRequest request,Model model) {
		byte[] readbyte = FileUtil.getBytes(request.getServletContext().getRealPath("/assets/image_bundle/face.png"));
		//속성명       속성값(byte[]의 데이터)
		model.addAttribute("imageFile", readbyte);
		model.addAttribute("filename", "face.png");
	}
}
