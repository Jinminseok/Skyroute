package kr.spring.member.booking.vo;

import java.time.LocalDate;
import java.util.Locale;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SHPassengerForm {

	private String passengerType;

	@NotBlank(message = "영문 성을 입력해 주세요.")
	@Pattern(
		regexp = "^[A-Za-z][A-Za-z\\s'-]*$",
		message = "영문 성은 영문자, 공백, 하이픈, 작은따옴표만 입력할 수 있습니다."
	)
	private String lastName;

	@NotBlank(message = "영문 이름을 입력해 주세요.")
	@Pattern(
		regexp = "^[A-Za-z][A-Za-z\\s'-]*$",
		message = "영문 이름은 영문자, 공백, 하이픈, 작은따옴표만 입력할 수 있습니다."
	)
	private String firstName;

	@NotNull(message = "생년월일을 입력해 주세요.")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate birthDate;

	@Pattern(regexp = "^[MF]$", message = "성별을 선택해 주세요.")
	private String gender;

	@NotBlank(message = "휴대폰 번호를 입력해 주세요.")
	private String phone;

	@NotBlank(message = "여권번호를 입력해 주세요.")
	private String passportNo;

	@NotNull(message = "여권 만료일을 입력해 주세요.")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate passportExpiry;

	private Long savedPassengerId;

	public String getName() {

		String normalizedLastName = normalizeName(lastName);
		String normalizedFirstName = normalizeName(firstName);

		return (normalizedLastName + " " + normalizedFirstName).trim();
	}

	private String normalizeName(String value) {

		if (value == null) {
			return "";
		}

		return value.trim()
				.replaceAll("\\s+", " ")
				.toUpperCase(Locale.ROOT);
	}

	public boolean isInfant() {
		return "INFANT".equals(passengerType);
	}

	public boolean needsSeat() {
		return !isInfant();
	}
}