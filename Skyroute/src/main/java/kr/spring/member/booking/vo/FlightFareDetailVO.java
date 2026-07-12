package kr.spring.member.booking.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class FlightFareDetailVO {

    private Long flightFareId;
    private Long seatClassId;
    private String seatClassName;
    private Integer sortOrder;

    private Long price;

    private Integer totalSeats;
    private Integer occupiedSeats;
    private Integer remainingSeats;

    public boolean isSoldOut() {
        return remainingSeats == null || remainingSeats <= 0;
    }
}