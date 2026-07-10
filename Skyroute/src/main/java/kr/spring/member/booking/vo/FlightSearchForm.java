package kr.spring.member.booking.vo;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class FlightSearchForm {

	/*
	 * ONEWAY    : 편도
	 * ROUNDTRIP : 왕복
	 */
	@NotBlank(message = "여정 유형을 선택해 주세요.")
	@Pattern(
		regexp = "ONEWAY|ROUNDTRIP",
		message = "올바른 여정 유형을 선택해 주세요."
	)
	private String tripType = "ROUNDTRIP";


	@NotNull(message = "출발지를 선택해 주세요.")
	private Long departureAirportId;


	@NotNull(message = "도착지를 선택해 주세요.")
	private Long arrivalAirportId;


	@NotNull(message = "출발일을 선택해 주세요.")
	@FutureOrPresent(
		message = "출발일은 오늘 이후 날짜만 선택할 수 있습니다."
	)
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate departureDate;


	/*
	 * 편도이면 null 허용
	 */
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate returnDate;


	@Min(
		value = 1,
		message = "성인은 최소 1명이어야 합니다."
	)
	@Max(
		value = 9,
		message = "성인은 최대 9명까지 선택할 수 있습니다."
	)
	private int adultCount = 1;


	@Min(
		value = 0,
		message = "소아 인원은 0명 이상이어야 합니다."
	)
	@Max(
		value = 8,
		message = "소아는 최대 8명까지 선택할 수 있습니다."
	)
	private int childCount;


	@Min(
		value = 0,
		message = "유아 인원은 0명 이상이어야 합니다."
	)
	@Max(
		value = 8,
		message = "유아는 최대 8명까지 선택할 수 있습니다."
	)
	private int infantCount;


	@NotNull(message = "좌석 등급을 선택해 주세요.")
	private Long seatClassId;


	public int getPassengerCount() {
		return adultCount
				+ childCount
				+ infantCount;
	}


	public boolean isRoundTrip() {
		return "ROUNDTRIP".equals(tripType);
	}


	@AssertTrue(
		message = "출발지와 도착지는 서로 달라야 합니다."
	)
	public boolean isAirportPairValid() {

		if (departureAirportId == null
				|| arrivalAirportId == null) {

			return true;
		}

		return !departureAirportId.equals(
			arrivalAirportId
		);
	}


	@AssertTrue(
		message = "왕복 여정은 귀국일을 선택하고 출발일 이후로 지정해 주세요."
	)
	public boolean isDateRangeValid() {

		if (!isRoundTrip()) {
			return true;
		}

		if (departureDate == null) {
			return true;
		}

		return returnDate != null
				&& !returnDate.isBefore(
					departureDate
				);
	}


	@AssertTrue(
		message = "전체 승객은 1명 이상 9명 이하로 선택해 주세요."
	)
	public boolean isPassengerCountValid() {

		int passengerCount =
				getPassengerCount();

		return passengerCount >= 1
				&& passengerCount <= 9;
	}


	@AssertTrue(
		message = "유아 수는 성인 수를 초과할 수 없습니다."
	)
	public boolean isInfantCountValid() {
		return infantCount <= adultCount;
	}
}