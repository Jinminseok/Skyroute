package kr.spring.member.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import kr.spring.member.dao.MemberMapper;

@Service
public class MemberMailServiceImpl implements MemberMailService {

    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private MemberMapper memberMapper;

    @Override
    public void sendMail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("yoonho7198@naver.com"); // 보내는 사람 네이버 계정
        message.setTo(toEmail);                       // 받는 사람 이메일
        message.setSubject(subject);                   // 메일 제목
        message.setText(body);                         // 메일 내용
        
        mailSender.send(message); // 메일 발송 실행
    }

	@Override
	public String findIdByNameAndEmail(String name, String email) {		
		return memberMapper.findIdByNameAndEmail(name, email);
	}

	@Override
	public boolean checkMemberForResetPw(String id, String name, String email) {
		return memberMapper.checkMemberForResetPw(id, name, email);
	}

	@Override
	public void updatePassword(String id, String encodedPw) {
		memberMapper.updatePassword(id, encodedPw);
	}
}
