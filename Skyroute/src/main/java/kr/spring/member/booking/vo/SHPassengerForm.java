package kr.spring.member.booking.vo;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/*
 * 탑승객 1명의 입력 정보
 *
 * passengerType 은 사용자가 고르는 값이 아니라
 * 검색 단계의 인원 구성(성인 N / 소아 N / 유아 N)에 따라
 * 서버가 폼을 만들 때 이미 정해 둔 값이다.
 */
@Getter
@Setter
@ToString
public class SHPassengerForm {

	/* ADULT / CHILD / INFANT - 화면에서는 readonly */
	private String passengerType;

	@NotBlank(message = "탑승객 이름을 입력해 주세요.")
	private String name;

	@NotNull(message = "생년월일을 입력해 주세요.")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate birthDate;

	@Pattern(regexp = "^[MF]$", message = "성별을 선택해 주세요.")
	private String gender;

	private String phone;

	private String passportNo;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate passportExpiry;

	/* 저장된 탑승객에서 불러온 경우 */
	private Long savedPassengerId;

	/* 다음에도 사용하도록 SAVED_PASSENGER 에 저장할지 */
	private boolean saveForNextTime;


	/* 유아는 좌석을 점유하지 않는다 → TICKET 을 만들지 않는다 */
	public boolean isInfant() {
		return "INFANT".equals(passengerType);
	}


	public boolean needsSeat() {
		return !isInfant();
	}
}