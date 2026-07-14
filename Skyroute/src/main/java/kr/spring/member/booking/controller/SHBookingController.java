package kr.spring.member.booking.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import kr.spring.member.booking.exception.SHSeatTakenException;
import kr.spring.member.booking.service.SHBookingService;
import kr.spring.member.booking.vo.SHBookingVO;
import kr.spring.member.booking.vo.SHPassengerForm;
import kr.spring.member.booking.vo.SHReserveForm;
import kr.spring.member.booking.vo.SHSeatMapVO;
import kr.spring.member.booking.vo.SHSeatVO;
import kr.spring.member.vo.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/*
 * 예약 진행 컨트롤러 (회원 전용)
 *
 * ─────────────────────────────────────────────────────────────
 * 흐름
 *
 *   검색 결과
 *     │  POST /booking/reserve/start   (항공편 + 좌석등급 + 인원)
 *     ▼
 *   [1] 탑승객 정보 입력          GET/POST /booking/reserve/passenger
 *     │                            → 세션(SHReserveForm)에만 저장. DB 기록 없음.
 *     ▼
 *   [2] 좌석 선택                 GET/POST /booking/reserve/seat
 *     │                            → 세션에만 저장. DB 기록 없음.
 *     ▼
 *   [3] 결제                      GET /booking/reserve/payment
 *     │                            → 이 시점에 BOOKING(PENDING) + TICKET(HOLDING) 생성
 *     │                              좌석 10분 선점 시작
 *     ▼
 *   [4] 완료                      POST /booking/reserve/complete (PortOne 검증 후)
 *                                  → TICKET CONFIRMED + BOOKING CONFIRMED
 *
 * BOOKING 을 [3]에서야 만드는 이유
 *   - TICKET.booking_passenger_id 가 NOT NULL 이라 승객 없이 티켓을 못 만든다.
 *   - 미리 만들면 결제 없이 이탈한 PENDING 쓰레기가 계속 쌓인다.
 * ─────────────────────────────────────────────────────────────
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/booking/reserve")
@SessionAttributes("shReserveForm")
public class SHBookingController {

	private final SHBookingService shBookingService;


	/* ===================================================================
	   [0] 검색 결과 → 예약 시작
	   =================================================================== */

	@PostMapping("/start")
	public String start(@RequestParam(name = "tripType") String tripType,
						@RequestParam(name = "outboundFlightId") Long outboundFlightId,
						@RequestParam(name = "inboundFlightId", required = false) Long inboundFlightId,
						@RequestParam(name = "seatClassId") Long seatClassId,
						@RequestParam(name = "adultCount", defaultValue = "1") int adultCount,
						@RequestParam(name = "childCount", defaultValue = "0") int childCount,
						@RequestParam(name = "infantCount", defaultValue = "0") int infantCount,
						Model model) {

		SHReserveForm reserveForm = new SHReserveForm();

		reserveForm.setTripType(tripType);
		reserveForm.setOutboundFlightId(outboundFlightId);
		reserveForm.setInboundFlightId("ROUNDTRIP".equals(tripType) ? inboundFlightId : null);
		reserveForm.setSeatClassId(seatClassId);
		reserveForm.setAdultCount(adultCount);
		reserveForm.setChildCount(childCount);
		reserveForm.setInfantCount(infantCount);

		/*
		 * 승객 유형은 사용자가 고르는 값이 아니다.
		 * 검색 단계의 인원 구성대로 폼을 미리 만들어 둔다.
		 */
		reserveForm.setPassengers(createPassengerForms(adultCount, childCount, infantCount));

		model.addAttribute("shReserveForm", reserveForm);

		return "redirect:/booking/reserve/passenger";
	}


	private List<SHPassengerForm> createPassengerForms(int adult, int child, int infant) {

		List<SHPassengerForm> passengers = new ArrayList<>();

		for (int i = 0; i < adult; i++) {
			passengers.add(newPassenger("ADULT"));
		}

		for (int i = 0; i < child; i++) {
			passengers.add(newPassenger("CHILD"));
		}

		for (int i = 0; i < infant; i++) {
			passengers.add(newPassenger("INFANT"));
		}

		return passengers;
	}


	private SHPassengerForm newPassenger(String type) {

		SHPassengerForm passenger = new SHPassengerForm();

		passenger.setPassengerType(type);

		return passenger;
	}


	/* ===================================================================
	   [1] 탑승객 정보
	   =================================================================== */

	@GetMapping("/passenger")
	public String passengerForm(@ModelAttribute("shReserveForm") SHReserveForm reserveForm,
								@AuthenticationPrincipal PrincipalDetails principal,
								Model model) {

		if (reserveForm.getOutboundFlightId() == null) {
			return "redirect:/main/home";
		}

		model.addAttribute("savedPassengerList",
				shBookingService.getSavedPassengerList(getMemberId(principal)));

		model.addAttribute("today", LocalDate.now());
		model.addAttribute("activeMenu", "book");

		return "thviews/member/member_booking_passenger";
	}


	@PostMapping("/passenger")
	public String submitPassenger(@Valid @ModelAttribute("shReserveForm") SHReserveForm reserveForm,
								  BindingResult bindingResult,
								  @AuthenticationPrincipal PrincipalDetails principal,
								  Model model) {

		if (bindingResult.hasErrors()) {

			log.debug("<<탑승객 정보 오류>> : {}", bindingResult.getAllErrors());

			return passengerForm(reserveForm, principal, model);
		}

		return "redirect:/booking/reserve/seat";
	}


	/* ===================================================================
	   [2] 좌석 선택
	   =================================================================== */

	@GetMapping("/seat")
	public String seatMap(@ModelAttribute("shReserveForm") SHReserveForm reserveForm,
						  Model model) {

		if (!reserveForm.isPassengerReady()) {
			return "redirect:/booking/reserve/passenger";
		}

		SHSeatMapVO outboundSeatMap = shBookingService.getSeatMap(
				reserveForm.getOutboundFlightId(),
				reserveForm.getSeatClassId(),
				"OUTBOUND");

		model.addAttribute("outboundSeatMap", outboundSeatMap);

		if (reserveForm.isRoundTrip()) {

			model.addAttribute("inboundSeatMap", shBookingService.getSeatMap(
					reserveForm.getInboundFlightId(),
					reserveForm.getSeatClassId(),
					"INBOUND"));
		}

		/* 좌석이 필요한 승객만 (유아 제외) */
		model.addAttribute("seatPassengers", reserveForm.getSeatPassengers());

		model.addAttribute("activeMenu", "book");

		return "thviews/member/member_select_seat";
	}


	@PostMapping("/seat")
	public String submitSeat(
			@ModelAttribute("shReserveForm")
			SHReserveForm reserveForm,
			@RequestParam(name = "outboundSeatIds")
			List<Long> outboundSeatIds,
			@RequestParam(
				name = "inboundSeatIds",
				required = false
			)
			List<Long> inboundSeatIds,
			RedirectAttributes redirectAttributes) {

		reserveForm.setOutboundSeatIds(
				outboundSeatIds
		);

		reserveForm.setInboundSeatIds(
				inboundSeatIds != null
					? inboundSeatIds
					: new ArrayList<>()
		);

		if (!reserveForm.isSeatReady()) {

			redirectAttributes.addFlashAttribute(
					"error",
					"탑승객 수만큼 모든 구간의 좌석을 선택해 주세요."
			);

			return "redirect:/booking/reserve/seat";
		}

		return "redirect:/booking/reserve/confirm";
	}
	
	//예약 확인
	@GetMapping("/confirm")
	public String confirm(
			@ModelAttribute("shReserveForm")
			SHReserveForm reserveForm,
			Model model) {

		if (!reserveForm.isSeatReady()) {
			return "redirect:/booking/reserve/seat";
		}

		SHSeatMapVO outboundSeatMap =
				shBookingService.getSeatMap(
						reserveForm.getOutboundFlightId(),
						reserveForm.getSeatClassId(),
						"OUTBOUND"
				);

		List<SHSeatVO> outboundSelectedSeats =
				findSelectedSeats(
						outboundSeatMap,
						reserveForm.getOutboundSeatIds()
				);

		SHSeatMapVO inboundSeatMap = null;

		List<SHSeatVO> inboundSelectedSeats =
				new ArrayList<>();

		if (reserveForm.isRoundTrip()) {

			inboundSeatMap =
					shBookingService.getSeatMap(
							reserveForm.getInboundFlightId(),
							reserveForm.getSeatClassId(),
							"INBOUND"
					);

			inboundSelectedSeats =
					findSelectedSeats(
							inboundSeatMap,
							reserveForm.getInboundSeatIds()
					);
		}

		long totalAmount =
				outboundSeatMap.getPrice()
				* reserveForm.getSeatPassengerCount();

		if (reserveForm.isRoundTrip()) {

			totalAmount +=
					inboundSeatMap.getPrice()
					* reserveForm.getSeatPassengerCount();
		}

		boolean requiresGuardianConsent =
				requiresGuardianConsent(
						reserveForm,
						outboundSeatMap
				);

		model.addAttribute(
				"outboundSeatMap",
				outboundSeatMap
		);

		model.addAttribute(
				"inboundSeatMap",
				inboundSeatMap
		);

		model.addAttribute(
				"outboundSelectedSeats",
				outboundSelectedSeats
		);

		model.addAttribute(
				"inboundSelectedSeats",
				inboundSelectedSeats
		);

		model.addAttribute(
				"seatPassengers",
				reserveForm.getSeatPassengers()
		);

		model.addAttribute(
				"totalAmount",
				totalAmount
		);

		model.addAttribute(
				"requiresGuardianConsent",
				requiresGuardianConsent
		);

		model.addAttribute(
				"activeMenu",
				"book"
		);

		return "thviews/member/member_booking_confirm";
	}


	/* ===================================================================
	   [3] 결제 화면 — 여기서 좌석을 실제로 선점한다
	   =================================================================== */

	@PostMapping("/hold")
	public String hold(
			@ModelAttribute("shReserveForm")
			SHReserveForm reserveForm,
			@RequestParam(
				name = "agreeTerms",
				defaultValue = "false"
			)
			boolean agreeTerms,
			@RequestParam(
				name = "agreeRefund",
				defaultValue = "false"
			)
			boolean agreeRefund,
			@RequestParam(
				name = "guardianConsent",
				defaultValue = "false"
			)
			boolean guardianConsent,
			@AuthenticationPrincipal
			PrincipalDetails principal,
			RedirectAttributes redirectAttributes) {

		if (!reserveForm.isSeatReady()) {
			return "redirect:/booking/reserve/seat";
		}

		SHSeatMapVO outboundSeatMap =
				shBookingService.getSeatMap(
						reserveForm.getOutboundFlightId(),
						reserveForm.getSeatClassId(),
						"OUTBOUND"
				);

		boolean guardianRequired =
				requiresGuardianConsent(
						reserveForm,
						outboundSeatMap
				);

		if (!agreeTerms || !agreeRefund) {

			redirectAttributes.addFlashAttribute(
					"error",
					"필수 약관과 취소·환불 규정에 동의해 주세요."
			);

			return "redirect:/booking/reserve/confirm";
		}

		if (guardianRequired && !guardianConsent) {

			redirectAttributes.addFlashAttribute(
					"error",
					"만 14세 미만 승객의 법정대리인 동의가 필요합니다."
			);

			return "redirect:/booking/reserve/confirm";
		}

		Long memberId = getMemberId(principal);

		try {

			Long bookingId =
					shBookingService.holdSeats(
							reserveForm,
							memberId
					);

			return "redirect:/booking/reserve/payment?bookingId="
					+ bookingId;

		} catch (SHSeatTakenException e) {

			reserveForm.setOutboundSeatIds(
					new ArrayList<>()
			);

			reserveForm.setInboundSeatIds(
					new ArrayList<>()
			);

			redirectAttributes.addFlashAttribute(
					"error",
					e.getMessage()
			);

			return "redirect:/booking/reserve/seat";

		} catch (IllegalStateException e) {

			redirectAttributes.addFlashAttribute(
					"error",
					e.getMessage()
			);

			return "redirect:/booking/reserve/confirm";
		}
	}
	
	@GetMapping("/payment")
	public String payment(
			@RequestParam(name = "bookingId")
			Long bookingId,
			@AuthenticationPrincipal
			PrincipalDetails principal,
			Model model) {

		Long memberId = getMemberId(principal);

		SHBookingVO booking =
				shBookingService.getBookingDetail(
						bookingId,
						memberId
				);

		if (
			booking == null ||
			!"PENDING".equals(booking.getStatus())
		) {
			return "redirect:/main/home";
		}

		model.addAttribute(
				"booking",
				booking
		);

		model.addAttribute(
				"holdMinutes",
				10
		);

		model.addAttribute(
				"activeMenu",
				"book"
		);

		return "thviews/member/sh_booking_payment";
	}


	/* ===================================================================
	   [4] 예약 완료 (결제 검증 후 진입)
	   =================================================================== */

	@GetMapping("/complete")
	public String complete(@RequestParam(name = "bookingId") Long bookingId,
						   @AuthenticationPrincipal PrincipalDetails principal,
						   SessionStatus sessionStatus,
						   Model model) {

		SHBookingVO booking =
				shBookingService.getBookingDetail(bookingId, getMemberId(principal));

		if (booking == null) {
			return "redirect:/main/home";
		}

		/* 예약이 끝났으므로 세션의 진행 상태를 비운다 */
		sessionStatus.setComplete();

		model.addAttribute("booking", booking);
		model.addAttribute("activeMenu", "book");

		return "thviews/member/sh_booking_complete";
	}


	/* ===================================================================
	   중도 이탈 : 선점한 좌석 반납
	   =================================================================== */

	@PostMapping("/cancel-hold")
	public String cancelHold(@RequestParam(name = "bookingId") Long bookingId,
							 @AuthenticationPrincipal PrincipalDetails principal,
							 SessionStatus sessionStatus) {

		shBookingService.failPayment(bookingId, getMemberId(principal));

		sessionStatus.setComplete();

		return "redirect:/main/home";
	}


	private Long getMemberId(PrincipalDetails principal) {

		if (principal == null || principal.getMemberVO() == null) {
			throw new IllegalStateException("로그인이 필요합니다.");
		}

		return principal.getMemberVO().getMember_id();
	}
	
	private List<SHSeatVO> findSelectedSeats(
			SHSeatMapVO seatMap,
			List<Long> seatIds) {

		List<SHSeatVO> selectedSeats =
				new ArrayList<>();

		for (Long seatId : seatIds) {

			SHSeatVO selectedSeat =
					seatMap.getSeatList()
						.stream()
						.filter(seat ->
							Objects.equals(
									seat.getSeatId(),
									seatId
							)
						)
						.findFirst()
						.orElseThrow(() ->
							new IllegalStateException(
									"선택한 좌석 정보를 찾을 수 없습니다."
							)
						);

			selectedSeats.add(
					selectedSeat
			);
		}

		return selectedSeats;
	}


	private boolean requiresGuardianConsent(
			SHReserveForm reserveForm,
			SHSeatMapVO outboundSeatMap) {

		LocalDate departureDate =
				outboundSeatMap
					.getDepartureTime()
					.toLocalDate();

		LocalDate ageCutoff =
				departureDate.minusYears(14);

		return reserveForm.getPassengers()
				.stream()
				.anyMatch(passenger ->
					passenger.getBirthDate() != null
					&& passenger
						.getBirthDate()
						.isAfter(ageCutoff)
				);
	}
}