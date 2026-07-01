package kr.spring.member.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.spring.member.service.MemberService;
import kr.spring.member.vo.MemberVO;
import kr.spring.member.vo.PrincipalDetails;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/member")
public class MemberRestController {
	@Autowired
	private MemberService memberService;
	
	//아이디 중복 체크
	@GetMapping("/confirmId/{id}")
	public ResponseEntity<Map<String,String>> checkId(@PathVariable("id") String id){
		log.debug("<<아이디 중복 체크>> : " + id);
		
		Map<String,String> mapAjax = new HashMap<String,String>();
		Map<String,String> map = new HashMap<String,String>();
		map.put("id", id);
		
		MemberVO member = memberService.selectCheckMember(id);
		if(id!=null) {//아이디 체크
			if(member!=null) {
				//아이디 중복
				mapAjax.put("result", "idDuplicated");
			}else {
				if(!Pattern.matches("^[A-Za-z0-9]{4,14}$", id)) {
					//패턴 불일치
					mapAjax.put("result", "notMatchPattern");
				}else {
					//패턴 일치하면서 아이디 미중복
					mapAjax.put("result", "idNotFound");
				}
			}
		}else {
			mapAjax.put("result", "error");
		}		
		return new ResponseEntity<Map<String,String>>(mapAjax,HttpStatus.OK);
	}
	


	
}













