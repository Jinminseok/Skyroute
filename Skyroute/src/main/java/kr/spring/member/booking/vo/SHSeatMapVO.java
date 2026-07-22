package kr.spring.member.booking.vo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/*
 * 한 항공편(가는 편 또는 오는 편)의 좌석맵
 */
@Getter
@Setter
@ToString
public class SHSeatMapVO {

	private Long flightId;

	private String legType;              // OUTBOUND / INBOUND

	private String flightNo;

	private String aircraftModelName;

	private String departureIataCode;

	private String arrivalIataCode;
	
	private String departureTimezone;

	private String arrivalTimezone;

	private LocalDateTime departureTime;

	private LocalDateTime arrivalTime;

	private Long seatClassId;

	private String seatClassName;

	/* FLIGHT_FARE 스냅샷 가격 (1인 1구간) */
	private Long price;
	
	private Integer maxSeatColumns;
	
	public boolean isWideBody() {
	    return maxSeatColumns != null
	            && maxSeatColumns >= 7;
	}

	public boolean isAisleStart(int columnCount, int seatIndex) {

	    if (seatIndex <= 0) {
	        return false;
	    }

	    if (isWideBody()) {

	        switch (columnCount) {
	            case 4:
	                return seatIndex == 1
	                        || seatIndex == 3;

	            case 6:
	                return seatIndex == 2
	                        || seatIndex == 4;

	            case 7:
	                return seatIndex == 2
	                        || seatIndex == 5;

	            case 8:
	                return seatIndex == 2
	                        || seatIndex == 6;

	            case 9:
	                return seatIndex == 3
	                        || seatIndex == 6;

	            case 10:
	                return seatIndex == 3
	                        || seatIndex == 7;

	            default:
	                int leftCount =
	                        columnCount / 3;

	                int rightCount =
	                        columnCount / 3;

	                int centerCount =
	                        columnCount
	                        - leftCount
	                        - rightCount;

	                return seatIndex == leftCount
	                        || seatIndex
	                        == leftCount + centerCount;
	        }
	    }

	    int leftCount;

	    if (columnCount == 4) {
	        leftCount = 2;
	    } else if (columnCount == 6) {
	        leftCount = 3;
	    } else {
	        leftCount = columnCount / 2;
	    }

	    return seatIndex == leftCount;
	}
	
	private List<SHSeatVO> seatList = new ArrayList<>();


	/* 열 번호 → 그 열의 좌석들 (화면 렌더링용) */
	public Map<Integer, List<SHSeatVO>> getSeatRowMap() {

	    Map<Integer, List<SHSeatVO>> rowMap = new TreeMap<>();

	    for (SHSeatVO seat : seatList) {

	        rowMap.computeIfAbsent(
	                seat.getRowNo(),
	                key -> new ArrayList<>()
	        ).add(seat);
	    }

	    return rowMap;
	}


	/* 열 코드 목록 (A, B, C, ...) */
	public List<String> getColCodeList() {

		Map<String, Boolean> unique = new LinkedHashMap<>();

		for (SHSeatVO seat : seatList) {
			unique.put(seat.getColCode(), Boolean.TRUE);
		}

		return new ArrayList<>(unique.keySet());
	}


	public long getRemainingSeats() {

		return seatList.stream()
				.filter(seat -> !seat.isOccupied())
				.count();
	}
	
	
	
	// 총 여정시간 분 단위로 계산
	public long getJourneyDurationMinutes() {

	    if (departureTime == null
	            || arrivalTime == null) {

	        return 0L;
	    }

	    try {

	        if (departureTimezone != null
	                && !departureTimezone.isBlank()
	                && arrivalTimezone != null
	                && !arrivalTimezone.isBlank()) {

	            ZonedDateTime departure =
	                    departureTime.atZone(
	                            ZoneId.of(
	                                    departureTimezone
	                            )
	                    );

	            ZonedDateTime arrival =
	                    arrivalTime.atZone(
	                            ZoneId.of(
	                                    arrivalTimezone
	                            )
	                    );

	            return Math.max(
	                    0L,
	                    Duration.between(
	                            departure,
	                            arrival
	                    ).toMinutes()
	            );
	        }

	    } catch (Exception e) {

	        
	    }

	    return Math.max(
	            0L,
	            Duration.between(
	                    departureTime,
	                    arrivalTime
	            ).toMinutes()
	    );
	}


	// 총 여정시간
	public String getJourneyDurationText() {

	    long totalMinutes =
	            getJourneyDurationMinutes();

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
	
	
	// 오늘부터 출발일까지 남은 날짜를 계산
	public long getDaysBeforeDeparture() {

	    if (departureTime == null) {
	        return 0L;
	    }

	    return ChronoUnit.DAYS.between(
	            LocalDate.now(),
	            departureTime.toLocalDate()
	    );
	}


	/*
	 * 출발 91일 이상 전인지 확인해야 함
	 *
	 * true  : 위약금 없음 표시
	 * false : 위약금 있음 표시
	 */
	public boolean isRefundPenaltyFree() {

	    return getDaysBeforeDeparture() >= 91L;
	}
}