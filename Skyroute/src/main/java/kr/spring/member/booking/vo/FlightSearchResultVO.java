package kr.spring.member.booking.vo;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class FlightSearchResultVO {

	private static final DateTimeFormatter
			DATE_FORMATTER =
			DateTimeFormatter.ofPattern(
				"yyyy.MM.dd"
			);

	private static final DateTimeFormatter
			TIME_FORMATTER =
			DateTimeFormatter.ofPattern(
				"HH:mm"
			);


	private Long flightId;

	private String flightNo;


	private Long departureAirportId;

	private String departureIataCode;

	private String departureAirportName;


	private Long arrivalAirportId;

	private String arrivalIataCode;

	private String arrivalAirportName;


	private LocalDateTime departureTime;

	private LocalDateTime arrivalTime;


	private String flightStatus;

	private Integer delayMinutes;

	private String aircraftModel;

	private String departureGateCode;

	private String arrivalGateCode;


	private Long seatClassId;

	private String seatClassName;

	private Long price;

	private Integer remainingSeats;


	public String getDepartureDateText() {

		if (departureTime == null) {
			return "-";
		}

		return departureTime.format(
			DATE_FORMATTER
		);
	}


	public String getDepartureTimeText() {

		if (departureTime == null) {
			return "-";
		}

		return departureTime.format(
			TIME_FORMATTER
		);
	}


	public String getArrivalDateText() {

		if (arrivalTime == null) {
			return "-";
		}

		return arrivalTime.format(
			DATE_FORMATTER
		);
	}


	public String getArrivalTimeText() {

		if (arrivalTime == null) {
			return "-";
		}

		return arrivalTime.format(
			TIME_FORMATTER
		);
	}


	public String getDurationText() {

		if (departureTime == null
				|| arrivalTime == null) {

			return "-";
		}

		long totalMinutes =
				Duration.between(
					departureTime,
					arrivalTime
				).toMinutes();

		long hours =
				totalMinutes / 60;

		long minutes =
				totalMinutes % 60;

		if (hours == 0) {
			return minutes + "분";
		}

		if (minutes == 0) {
			return hours + "시간";
		}

		return hours
				+ "시간 "
				+ minutes
				+ "분";
	}


	public String getStatusLabel() {

		if ("DELAYED".equals(
				flightStatus)) {

			if (delayMinutes != null
					&& delayMinutes > 0) {

				return "지연 "
						+ delayMinutes
						+ "분";
			}

			return "지연";
		}

		return "정상 운항";
	}
}