package kr.spring.member.vo;

import java.io.IOException;
import java.sql.Date;
import java.util.Arrays;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
/*
정규표현식

한글 한 글자 이상 가능
/^[가-힣]+$/

한글, 띄어쓰기 한 글자 이상 가능
/^[가-힣\s]+$/

한글, 영문 한 글자 이상 가능
/^[가-힣a-zA-Z]+$

숫자 한 글자 이상 가능
/^[0-9]+$

문자, 숫자만 허용 최소6자 최대 12자
^[A-Za-z\d]{6,12}$
^[A-Za-z0-9]{6,12}$

문자, 숫자, 특수 문자 모두 무조건 1개 이상, 최소 6자 최대 12자
^(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#$%^&])[A-Za-z\d!@#$%^&]{6,12}$
 */
@Getter
@Setter
@ToString(exclude = {"photo"})
public class MemberVO{

	public MemberVO() {}
	private long member_id;
	@Pattern(regexp="^[A-Za-z0-9]{4,14}$")
	private String id;
	@Pattern(regexp="^[A-Za-z0-9]{4,12}$")
	private String password;
	@NotBlank
	private String name;
	@Email
	@NotBlank
	private String email;
	@NotBlank
	private String phone;
	private String role;
	@Size(min=5,max=5)
	private String zipcode;
	@NotBlank
	private String address1;
	@NotBlank
	private String address2;
	private String status;
	private String suspend_reason;
	private Date created_at;
	private Date update_at;

	//비밀번호 변경시에만 조건체크
	@Pattern(regexp="^[A-Za-z0-9]+$")
	private String captcha_chars;

	//비밀번호 변경시 현재 비밀번호를 저장하는 용도로 사용
	//회원 가입, 회원 정보 수정 폼에서 데이터 전송시 now_passwd를 표시하지 않기 때문에 name이 전송되지 않아 에러가 발생하지 않음
	//@Pattern(regexp="^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&])[A-Za-z\\d!@#$%^&]{4,10}$")
	@Pattern(regexp="^[A-Za-z0-9]{4,12}$")
	private String now_passwd;
	
	//마이페이지 여권 관리
	private String passport_no;
	private Date passport_expiry;
	
	public int getRoleOrdinal() {
		if(role == null) return -1;
		
		if(role.equals(UserRole.ACTIVE.getValue())) {
			return UserRole.ACTIVE.ordinal();//0
		}else if(role.equals(UserRole.SUSPENDED.getValue())) {
			return UserRole.SUSPENDED.ordinal();//1
		}else if(role.equals(UserRole.DELETED.getValue())) {
			return UserRole.DELETED.ordinal();//2
		}else if(role.equals(UserRole.INACTIVE.getValue())) {
			return UserRole.INACTIVE.ordinal();//3
		}else if(role.equals(UserRole.USER.getValue())) {
			return UserRole.USER.ordinal();//4
		}else if(role.equals(UserRole.ADMIN.getValue())) {
			return UserRole.ADMIN.ordinal();//5
		}else if(role.equals(UserRole.STAFF.getValue())) {
			return UserRole.STAFF.ordinal();//6
		}else {
			return -1;
		}
	}
	
	//===========비밀번호 일치 여부 체크====================//

	


}



