package kr.spring.member.booking.vo;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class FlightDetailVO {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");

    private Long flightId;
    private String flightNo;
    private String flightType;

    // 출발 정보
    private String departureIataCode;
    private String departureAirportName;
    private String departureCountry;
    private LocalDateTime departureTime;
    private String departureGateCode;
    private String departureGateAreaName;

    // 도착 정보
    private String arrivalIataCode;
    private String arrivalAirportName;
    private String arrivalCountry;
    private LocalDateTime arrivalTime;
    private String arrivalGateCode;
    private String arrivalGateAreaName;

    // 운항 상태
    private String flightStatus;
    private Integer delayMinutes;

    // 항공기 정보
    private String aircraftRegNo;
    private String aircraftModel;
    private Integer totalSeats;

    // 좌석 등급별 운임
    private List<FlightFareDetailVO> fareList = new ArrayList<>();


    public String getDepartureDateText() {
        return formatDate(departureTime);
    }

    public String getDepartureTimeText() {
        return formatTime(departureTime);
    }

    public String getArrivalDateText() {
        return formatDate(arrivalTime);
    }

    public String getArrivalTimeText() {
        return formatTime(arrivalTime);
    }

    public String getDurationText() {

        if (departureTime == null || arrivalTime == null) {
            return "-";
        }

        long totalMinutes =
                Duration.between(
                        departureTime,
                        arrivalTime
                ).toMinutes();

        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;

        if (hours == 0) {
            return minutes + "분";
        }

        if (minutes == 0) {
            return hours + "시간";
        }

        return hours + "시간 " + minutes + "분";
    }

    public String getStatusLabel() {

        if (flightStatus == null) {
            return "상태 미정";
        }

        return switch (flightStatus) {

            case "SCHEDULED" -> "정상 운항";

            case "DELAYED" ->
                    delayMinutes != null && delayMinutes > 0
                            ? "지연 " + delayMinutes + "분"
                            : "지연";

            case "BOARDING" -> "탑승 중";
            case "DEPARTED" -> "출발 완료";
            case "ARRIVED" -> "도착 완료";
            case "CANCELLED" -> "결항";
            case "COMPLETED" -> "운항 종료";

            default -> flightStatus;
        };
    }

    public String getFlightTypeLabel() {

        if ("DOM".equals(flightType)) {
            return "국내선";
        }

        if ("INT".equals(flightType)) {
            return "국제선";
        }

        return "-";
    }

    public String getDepartureGateDisplay() {
        return buildGateDisplay(
                departureGateAreaName,
                departureGateCode
        );
    }

    public String getArrivalGateDisplay() {
        return buildGateDisplay(
                arrivalGateAreaName,
                arrivalGateCode
        );
    }

    private String formatDate(LocalDateTime dateTime) {

        if (dateTime == null) {
            return "-";
        }

        return dateTime.format(DATE_FORMATTER);
    }

    private String formatTime(LocalDateTime dateTime) {

        if (dateTime == null) {
            return "-";
        }

        return dateTime.format(TIME_FORMATTER);
    }

    private String buildGateDisplay(
            String gateAreaName,
            String gateCode) {

        if (gateCode == null || gateCode.isBlank()) {
            return "미정";
        }

        if (gateAreaName == null || gateAreaName.isBlank()) {
            return gateCode;
        }

        return gateAreaName + " · " + gateCode;
    }
}