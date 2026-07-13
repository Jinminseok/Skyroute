package kr.spring.member.booking.vo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

	private String departureIataCode;

	private String arrivalIataCode;

	private LocalDateTime departureTime;

	private LocalDateTime arrivalTime;

	private Long seatClassId;

	private String seatClassName;

	/* FLIGHT_FARE 스냅샷 가격 (1인 1구간) */
	private Long price;

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
}