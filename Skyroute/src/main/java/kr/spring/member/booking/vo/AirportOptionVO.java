package kr.spring.member.booking.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AirportOptionVO {

	private Long airportId;

	private String iataCode;

	private String airportName;

	private String country;

	private String flightType;


	public String getDisplayName() {
		return iataCode
				+ " "
				+ airportName;
	}
}