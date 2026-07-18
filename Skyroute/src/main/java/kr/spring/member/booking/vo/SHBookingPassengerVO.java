package kr.spring.member.booking.vo;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/*
 * BOOKING_PASSENGER 행
 *
 * passengerType 은 예약 생성 시점에 한 번만 판정해서 저장하고,
 * 이후에는 절대 재계산하지 않는다.
 * (출발일이 바뀌면 만 12세 직전 승객의 유형이 흔들리기 때문)
 */
@Getter
@Setter
@ToString
public class SHBookingPassengerVO {

	private Long bookingPassengerId;

	private Long bookingId;

	private Long savedPassengerId;

	private Long memberId;          // SAVED_PASSENGER 조회 결과 매핑용

	private String name;

	private LocalDate birthDate;

	private String phone;

	private String gender;

	private String passportNo;

	private LocalDate passportExpiry;

	/* ADULT / CHILD / INFANT */
	private String passengerType;
}