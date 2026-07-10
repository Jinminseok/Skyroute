package kr.spring.member.booking.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.spring.member.booking.dao.FlightSearchMapper;
import kr.spring.member.booking.vo.AirportOptionVO;
import kr.spring.member.booking.vo.FlightSearchForm;
import kr.spring.member.booking.vo.FlightSearchQueryVO;
import kr.spring.member.booking.vo.FlightSearchResultVO;
import kr.spring.member.booking.vo.SeatClassOptionVO;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FlightSearchServiceImpl implements FlightSearchService {

	private final FlightSearchMapper flightSearchMapper;


	@Override
	public List<AirportOptionVO>selectActiveAirportList() {

		return flightSearchMapper .selectActiveAirportList();
	}


	@Override
	public List<SeatClassOptionVO>selectSeatClassList() {

		return flightSearchMapper.selectSeatClassList();
	}


	@Override
	public void validateReferenceData(FlightSearchForm form) {

		/*
		 * 클라이언트가 임의의 공항 PK를 보낼 수 있으므로
		 * 서버에서 실제 활성 공항인지 다시 검사한다.
		 */
		int airportCount = flightSearchMapper.countActiveAirportPair(form.getDepartureAirportId(),form.getArrivalAirportId());

		if (airportCount != 2) {
			throw new IllegalArgumentException(
				"현재 사용할 수 없는 공항이 포함되어 있습니다. 다시 선택해 주세요.");
		}


		/*
		 * 좌석 등급 PK도 서버에서 다시 검사한다.
		 */
		int seatClassCount = flightSearchMapper.countSeatClass(form.getSeatClassId());

		if (seatClassCount != 1) {
			throw new IllegalArgumentException("현재 사용할 수 없는 좌석 등급입니다. 다시 선택해 주세요.");
		}
	}


	@Override
	public List<FlightSearchResultVO>searchOutboundFlightList(FlightSearchForm form) {

		FlightSearchQueryVO query = createQuery(form.getDepartureAirportId(), form.getArrivalAirportId(), form.getDepartureDate(), form);

		return flightSearchMapper.selectFlightList(query);
	}


	@Override
	public List<FlightSearchResultVO>searchInboundFlightList(FlightSearchForm form) {

		if (!form.isRoundTrip()) {
			return List.of();
		}

		/*
		 * 오는 편은 출발지와 도착지를 반대로 조회한다.
		 */
		FlightSearchQueryVO query = createQuery(form.getArrivalAirportId(), form.getDepartureAirportId(), form.getReturnDate(), form);

		return flightSearchMapper.selectFlightList(query);
	}


	private FlightSearchQueryVO createQuery(Long departureAirportId, Long arrivalAirportId, LocalDate departureDate, FlightSearchForm form) {

		return FlightSearchQueryVO.builder().departureAirportId(departureAirportId)
											.arrivalAirportId(arrivalAirportId)
											.seatClassId(form.getSeatClassId())
											.passengerCount(form.getPassengerCount())
											.departureStart(departureDate.atStartOfDay())
											.departureEnd(departureDate.plusDays(1).atStartOfDay())
											.build();
	}
}