package kr.spring.member.booking.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.validation.Valid;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/*
 * 예약 진행 상태 (세션 보관)
 *
 * [설계 의도]
 * BOOKING / TICKET 은 "승객 정보 + 좌석 선택"이 모두 끝난 뒤에야 생성된다.
 *
 *  - TICKET.booking_passenger_id 가 NOT NULL 이므로
 *    승객 행 없이는 티켓을 만들 수 없다.
 *  - 미리 BOOKING(PENDING) 을 만들어 두면 결제 없이 이탈한
 *    쓰레기 예약이 계속 쌓인다.
 *
 * 따라서 그 전 단계는 DB 가 아니라 이 객체로 세션에 들고 간다.
 */
@Getter
@Setter
@ToString
public class SHReserveForm implements Serializable {

	private static final long serialVersionUID = 1L;

	/* ===== 검색 단계에서 넘어온 값 ===== */

	private String tripType;              // ONEWAY / ROUNDTRIP

	private Long outboundFlightId;

	private Long inboundFlightId;         // 편도면 null

	private Long seatClassId;

	private int adultCount;

	private int childCount;

	private int infantCount;


	/* ===== 2단계 : 탑승객 정보 ===== */
	@Valid
	private List<SHPassengerForm> passengers = new ArrayList<>();


	/* ===== 3단계 : 좌석 선택 ===== */

	/* passengers 중 좌석이 필요한 승객(성인 + 소아) 순서와 1:1 대응 */
	private List<Long> outboundSeatIds = new ArrayList<>();

	private List<Long> inboundSeatIds = new ArrayList<>();


	public boolean isRoundTrip() {
		return "ROUNDTRIP".equals(tripType);
	}


	/* 좌석을 점유하는 승객 수 (유아 제외) */
	public int getSeatPassengerCount() {
		return adultCount + childCount;
	}


	public int getTotalPassengerCount() {
		return adultCount + childCount + infantCount;
	}


	/* 좌석이 필요한 승객만 순서대로 */
	public List<SHPassengerForm> getSeatPassengers() {

		List<SHPassengerForm> list = new ArrayList<>();

		for (SHPassengerForm passenger : passengers) {
			if (passenger.needsSeat()) {
				list.add(passenger);
			}
		}

		return list;
	}


	/* 승객 정보 입력이 끝났는가 */
	public boolean isPassengerReady() {
		return passengers.size() == getTotalPassengerCount();
	}


	/* 좌석 선택이 끝났는가 */
	public boolean isSeatReady() {

		int need = getSeatPassengerCount();

		if (outboundSeatIds.size() != need) {
			return false;
		}

		if (isRoundTrip() && inboundSeatIds.size() != need) {
			return false;
		}

		return true;
	}
}