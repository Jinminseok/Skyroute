package kr.spring.member.booking.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import kr.spring.member.booking.exception.SHSeatTakenException;
import kr.spring.member.booking.payment.SHIamportClient;
import kr.spring.member.booking.payment.SHIamportPayment;
import kr.spring.member.booking.payment.SHPayDto;
import kr.spring.member.booking.service.SHBookingService;
import kr.spring.member.booking.vo.SHBookingPassengerVO;
import kr.spring.member.booking.vo.SHBookingVO;
import kr.spring.member.booking.vo.SHPassengerForm;
import kr.spring.member.booking.vo.SHReserveForm;
import kr.spring.member.booking.vo.SHSeatMapVO;
import kr.spring.member.booking.vo.SHSeatVO;
import kr.spring.member.vo.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.databind.JsonNode;

import kr.spring.member.booking.payment.TossPaymentsClient;
import kr.spring.member.booking.vo.SHPaymentVO;


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
	
	private final SHIamportClient iamportClient;
	
	private final TossPaymentsClient tossPaymentsClient;

	@Value("${toss.client-key}")
	private String tossClientKey;
	@Value("${imp.code}")
	private String impCode;
	@Value("${imp.channel-kakao}")
	private String channelKakao;
	@Value("${imp.channel-card}")
	private String channelCard;
	@Value("${imp.store_id}")
	private String storeId;


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

		log.warn(
			    "[예약 시작] tripType={}, outboundFlightId={}, inboundFlightId={}, seatClassId={}",
			    tripType,
			    outboundFlightId,
			    inboundFlightId,
			    seatClassId
			);
		
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

	private boolean hasText(String value) {
	    return value != null && !value.isBlank();
	}


	/* ===================================================================
	   [1] 탑승객 정보
	   =================================================================== */

	@GetMapping("/passenger")
	public String passengerForm(
	        @ModelAttribute("shReserveForm") SHReserveForm reserveForm,
	        @AuthenticationPrincipal PrincipalDetails principal,
	        Model model) {

	    if (reserveForm.getOutboundFlightId() == null) {
	        return "redirect:/main/home";
	    }

	    Long memberId = getMemberId(principal);

	    List<SHBookingPassengerVO> savedPassengerList =
	            shBookingService.getSavedPassengerList(memberId);

	    SHBookingPassengerVO savedPassenger = savedPassengerList.isEmpty() ? null : savedPassengerList.get(0);

	    model.addAttribute(
	            "savedPassengerList",
	            savedPassengerList
	    );

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
		
		log.warn(
			    "[좌석 화면] tripType={}, outboundFlightId={}, inboundFlightId={}, seatClassId={}",
			    reserveForm.getTripType(),
			    reserveForm.getOutboundFlightId(),
			    reserveForm.getInboundFlightId(),
			    reserveForm.getSeatClassId()
			);

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
	        @RequestParam(name = "outboundSeatIds", required = false)
	        List<Long> outboundSeatIds,
	        @RequestParam(name = "inboundSeatIds", required = false)
	        List<Long> inboundSeatIds,
	        @ModelAttribute("shReserveForm")
	        SHReserveForm shReserveForm,
	        RedirectAttributes redirectAttributes) {

	    int requiredSeatCount =
	            shReserveForm.getSeatPassengerCount();

	    if (outboundSeatIds == null
	            || outboundSeatIds.size() != requiredSeatCount) {

	        redirectAttributes.addFlashAttribute(
	                "error",
	                "가는 편 좌석을 모든 탑승객에게 선택해 주세요."
	        );

	        return "redirect:/booking/reserve/seat";
	    }

	    if (shReserveForm.isRoundTrip()
	            && (inboundSeatIds == null
	            || inboundSeatIds.size() != requiredSeatCount)) {

	        redirectAttributes.addFlashAttribute(
	                "error",
	                "오는 편 좌석을 모든 탑승객에게 선택해 주세요."
	        );

	        return "redirect:/booking/reserve/seat";
	    }

	    shReserveForm.setOutboundSeatIds(
	            new ArrayList<>(outboundSeatIds)
	    );

	    if (shReserveForm.isRoundTrip()) {
	        shReserveForm.setInboundSeatIds(
	                new ArrayList<>(inboundSeatIds)
	        );
	    } else {
	        shReserveForm.setInboundSeatIds(
	                new ArrayList<>()
	        );
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
			@RequestParam(name = "paymentMethod")
			String paymentMethod,
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

		
		if (!List.of(
				"KAKAOPAY",
				"CARD",
				"TOSSPAY",
				"TRANSFER"
		).contains(paymentMethod)) {

			redirectAttributes.addFlashAttribute(
					"error",
					"지원하지 않는 결제수단입니다."
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

			if ("KAKAOPAY".equals(paymentMethod)
					|| "CARD".equals(paymentMethod)) {

				return "redirect:/booking/reserve/payment?bookingId="
						+ bookingId
						+ "&method="
						+ paymentMethod;
			}

			return "redirect:/booking/reserve/toss-payment?bookingId="
					+ bookingId
					+ "&method="
					+ paymentMethod;

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
		model.addAttribute("impCode", impCode);
		
		return "thviews/member/sh_booking_payment";
	}
	
	
	@GetMapping("/toss-payment")
	public String tossPayment(
			@RequestParam(name = "bookingId")
			Long bookingId,
			@RequestParam(name = "method")
			String method,
			@AuthenticationPrincipal
			PrincipalDetails principal,
			Model model,
			RedirectAttributes redirectAttributes) {

		if (!List.of(
				"TOSSPAY",
				"TRANSFER"
		).contains(method)) {

			redirectAttributes.addFlashAttribute(
					"error",
					"지원하지 않는 토스 결제수단입니다."
			);

			return "redirect:/main/home";
		}

		Long memberId = getMemberId(principal);

		SHBookingVO booking =
				shBookingService.getBookingDetail(
						bookingId,
						memberId
				);

		if (booking == null
				|| !"PENDING".equals(booking.getStatus())) {

			redirectAttributes.addFlashAttribute(
					"error",
					"결제할 수 있는 예약 정보를 찾을 수 없습니다."
			);

			return "redirect:/main/home";
		}

		model.addAttribute(
				"booking",
				booking
		);

		model.addAttribute(
				"paymentMethod",
				method
		);

		model.addAttribute(
				"holdMinutes",
				10
		);

		model.addAttribute(
				"activeMenu",
				"book"
		);

		return "thviews/member/toss_booking_payment";
	}
	
	
	
	/* ========================= 결제 (PortOne V2) ========================= */

	/** 결제 준비: PAYMENT(READY) 생성 + 결제창에 넘길 정보 반환 */
	@PostMapping("/pay/prepare")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> payPrepare(
	        @RequestBody SHPayDto.Prepare req,
	        @AuthenticationPrincipal PrincipalDetails principal) {

	    try {

	        Long memberId = getMemberId(principal);

	        if ("CARD".equals(req.method())) {
	            validateCardCustomer(principal);
	        }

	        String merchantUid = shBookingService.preparePayment(req.bookingId(), memberId, req.method());

	        SHBookingVO booking = shBookingService.getBookingDetail(req.bookingId(), memberId);

	        Map<String, Object> res = new HashMap<>();

	        res.put("storeId", storeId);
	        res.put("channelKey", resolveChannelKey(req.method()));
	        res.put("paymentId", merchantUid);
	        res.put("orderName", buildOrderName(booking));
	        res.put("totalAmount", booking.getTotalAmount());
	        res.put("payMethod", resolvePayMethod(req.method()));
	        res.put("easyPayProvider", resolveEasyPay(req.method()));
	        res.put("buyerName", principal.getMemberVO().getName());
	        res.put("buyerEmail", principal.getMemberVO().getEmail());
	        res.put("buyerTel", principal.getMemberVO().getPhone());

	        return ResponseEntity.ok(res);

	    } catch (
	            SHSeatTakenException | IllegalStateException e
	    ) {
	    	return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("result", "FAIL","message", e.getMessage()));
	    }
	}
	
	// 토스 결제 준비
	@PostMapping("/toss/prepare")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> tossPrepare(
			@RequestBody SHPayDto.Prepare req,
			@AuthenticationPrincipal PrincipalDetails principal) {

		try {
			if (!"TOSSPAY".equals(req.method()) && !"TRANSFER".equals(req.method())) {
				throw new IllegalStateException("지원하지 않는 토스 결제수단입니다.");
			}

			Long memberId = getMemberId(principal);
			String orderId = shBookingService.preparePayment(req.bookingId(), memberId, req.method());
			SHBookingVO booking = shBookingService.getBookingDetail(req.bookingId(), memberId);

			if (booking == null) {
				throw new IllegalStateException("예약 정보를 찾을 수 없습니다.");
			}

			Map<String, Object> result = new HashMap<>();
			result.put("clientKey", tossClientKey);
			result.put("customerKey", "SKYROUTE-" + memberId);
			result.put("orderId", orderId);
			result.put("orderName", buildOrderName(booking));
			result.put("totalAmount", booking.getTotalAmount());

			return ResponseEntity.ok(result);

		} catch (SHSeatTakenException | IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(Map.of(
							"result", "FAIL",
							"message", e.getMessage()
					));
		}
	}

	/** 결제 완료: PortOne 서버 검증 → 좌석 확정 / 실패 시 환불 */
	@PostMapping("/pay/complete")
	@ResponseBody
	public Map<String, Object> payComplete(
			@RequestBody SHPayDto.Complete req,
			@AuthenticationPrincipal PrincipalDetails principal) {

		Long memberId = getMemberId(principal);
		Map<String, Object> res = new HashMap<>();

		/* 1) PortOne 서버에서 실제 결제 건 조회 (위변조 방지의 핵심) */
		//SHIamportPayment paid = iamportClient.getPayment(req.impUid());
		SHIamportPayment p = iamportClient.getPayment(req.merchantUid());
		
		/* 2) 결제 완료 상태가 아니면 실패 처리 */
		if (!p.isPaid()) {
			shBookingService.failPayment(req.bookingId(), memberId);
			res.put("result", "FAIL");
			res.put("message", "결제가 완료되지 않았습니다.");
			return res;
		}

		try {
			/* 3) 좌석 확정 + 금액 검증(서버 조회 금액 기준) */
			shBookingService.confirmPayment(req.bookingId(), memberId, req.merchantUid(), req.method(), p.getAmount(), "PORTONE", null);

			res.put("result", "PAID");
			res.put("redirectUrl", "/booking/reserve/complete?bookingId=" + req.bookingId());

		} catch (SHSeatTakenException e) {
			/* 결제는 됐는데 좌석 만료 → 자동 환불 */
			iamportClient.cancelPayment(req.merchantUid(), "좌석 선점 만료 - 자동 환불");
			shBookingService.failPayment(req.bookingId(), memberId);
			res.put("result", "SEAT_EXPIRED");
			res.put("message", e.getMessage());

		} catch (IllegalStateException e) {
			/* 금액 불일치 등 검증 실패 → 자동 환불 */
			iamportClient.cancelPayment(req.merchantUid(), "결제 검증 실패 - 자동 환불");
			shBookingService.failPayment(req.bookingId(), memberId);
			res.put("result", "FAIL");
			res.put("message", e.getMessage());
		}
		return res;
	}
	
	// 토스 결제 성공
	@GetMapping("/toss/success")
	public String tossSuccess(
			@RequestParam Long bookingId,
			@RequestParam String paymentKey,
			@RequestParam String orderId,
			@RequestParam Long amount,
			@AuthenticationPrincipal PrincipalDetails principal,
			RedirectAttributes redirectAttributes) {

		Long memberId = getMemberId(principal);
		SHBookingVO booking = shBookingService.getBookingDetail(bookingId, memberId);

		if (booking == null) {
			return "redirect:/main/home";
		}

		if ("CONFIRMED".equals(booking.getStatus())) {
			return "redirect:/booking/reserve/complete?bookingId=" + bookingId;
		}

		if (!"PENDING".equals(booking.getStatus())) {
			redirectAttributes.addFlashAttribute("paymentError", "결제할 수 있는 예약 상태가 아닙니다.");
			return "redirect:/main/home";
		}

		SHPaymentVO payment = booking.getPayment();

		if (payment == null
				|| !"READY".equals(payment.getStatus())
				|| !"TOSS_PAYMENTS".equals(payment.getPaymentProvider())) {

			redirectAttributes.addFlashAttribute("paymentError", "토스 결제 준비 정보를 찾을 수 없습니다.");
			return "redirect:/booking/reserve/payment?bookingId=" + bookingId;
		}

		if (!Objects.equals(payment.getMerchantUid(), orderId)) {
			redirectAttributes.addFlashAttribute("paymentError", "토스 주문번호가 일치하지 않습니다.");
			return "redirect:/booking/reserve/payment?bookingId=" + bookingId;
		}

		if (!Objects.equals(payment.getAmount(), amount)
				|| !Objects.equals(booking.getTotalAmount(), amount)) {

			redirectAttributes.addFlashAttribute("paymentError", "토스 결제금액이 예약 금액과 일치하지 않습니다.");
			return "redirect:/booking/reserve/payment?bookingId=" + bookingId;
		}

		JsonNode tossPayment;

		try {
			tossPayment = tossPaymentsClient.confirmPayment(paymentKey, orderId, amount);
		} catch (IllegalStateException e) {
			redirectAttributes.addFlashAttribute("paymentError", e.getMessage());
			return "redirect:/booking/reserve/payment?bookingId=" + bookingId;
		}

		if (!"DONE".equals(tossPayment.path("status").asText())) {
			redirectAttributes.addFlashAttribute("paymentError", "토스 결제가 완료되지 않았습니다.");
			return "redirect:/booking/reserve/payment?bookingId=" + bookingId;
		}

		String confirmedOrderId = tossPayment.path("orderId").asText();
		long confirmedAmount = tossPayment.path("totalAmount").asLong();
		String partialCancelableYn = tossPayment.path("isPartialCancelable").asBoolean(false) ? "Y" : "N";

		if (!Objects.equals(orderId, confirmedOrderId)) {
			try {
				tossPaymentsClient.cancelPayment(paymentKey, "주문번호 검증 실패");
			} catch (Exception cancelException) {
				log.error("토스 자동취소 실패 paymentKey={}", paymentKey, cancelException);
			}

			shBookingService.failPayment(bookingId, memberId);
			redirectAttributes.addFlashAttribute("paymentError", "토스 주문번호 검증에 실패했습니다.");
			return "redirect:/main/home";
		}

		try {
			shBookingService.confirmPayment(
					bookingId,
					memberId,
					paymentKey,
					payment.getMethod(),
					confirmedAmount,
					"TOSS_PAYMENTS",
					partialCancelableYn
			);

			return "redirect:/booking/reserve/complete?bookingId=" + bookingId;

		} catch (SHSeatTakenException | IllegalStateException e) {
			try {
				tossPaymentsClient.cancelPayment(paymentKey, "예약 확정 실패 자동 환불");
			} catch (Exception cancelException) {
				log.error("토스 자동환불 실패 paymentKey={}", paymentKey, cancelException);
			}

			shBookingService.failPayment(bookingId, memberId);
			redirectAttributes.addFlashAttribute("paymentError", e.getMessage());

			return "redirect:/main/home";
		}
	}
	

	/** 결제창 취소/이탈: 좌석 반납 */
	@PostMapping("/pay/cancel")
	@ResponseBody
	public Map<String, Object> payCancel(
			@RequestBody SHPayDto.Cancel req,
			@AuthenticationPrincipal PrincipalDetails principal) {

		shBookingService.failPayment(req.bookingId(), getMemberId(principal));
		return Map.of("result", "CANCELLED");
	}
	
	
	// 토스 결제 실패
	@GetMapping("/toss/fail")
	public String tossFail(
			@RequestParam Long bookingId,
			@RequestParam(required = false) String code,
			@RequestParam(required = false) String message,
			@AuthenticationPrincipal PrincipalDetails principal,
			RedirectAttributes redirectAttributes) {

		Long memberId = getMemberId(principal);

		shBookingService.failPayment(
				bookingId,
				memberId
		);

		String errorMessage =
				hasText(message)
				? message
				: "토스 결제가 취소되었거나 완료되지 않았습니다.";

		if (hasText(code)) {
			errorMessage += " (" + code + ")";
		}

		redirectAttributes.addFlashAttribute(
				"paymentError",
				errorMessage
		);

		return "redirect:/main/home";
	}
	

	/* ---- 결제 헬퍼 ---- */
	
	private void validateCardCustomer(PrincipalDetails principal) {
		if(principal ==null || principal.getMemberVO() == null) {
			throw new IllegalStateException("로그인이 필요합니다.");
		}
		
		var member = principal.getMemberVO();
		
		if(!hasText(member.getName())) {
			throw new IllegalStateException("카드 결제를 위해 회원 이름이 필요합니다.");
		}
		
		if(!hasText(member.getEmail())) {
			throw new IllegalStateException("카드 결제를 위해 회원 이메일이 필요합니다.");
		}
		
		if(!hasText(member.getPhone())) {
			throw new IllegalStateException("카드 결제를 위해 회원 연락처가 필요합니다.");
		}
	}

	private String resolveChannelKey(String method) {
		return switch (method) {
			case "KAKAOPAY" -> channelKakao;
			case "CARD"     -> channelCard;
			default -> throw new IllegalStateException("지원하지 않는 결제수단: " + method);
		};
	}

	private String resolvePayMethod(String method) {
		return "KAKAOPAY".equals(method) ? "EASY_PAY" : "CARD";
	}
	
	private String resolveEasyPay(String method) {
		return "KAKAOPAY".equals(method) ? "EASY_PAY_PROVIDER_KAKAOPAY" : null;
	}

	private String buildOrderName(SHBookingVO b) {
		return b.getOutboundDepartureIata() + "→" + b.getOutboundArrivalIata()
				+ " 항공권 " + b.getPassengerCount() + "명";
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

		if (booking == null || !"CONFIRMED".equals(booking.getStatus())) {
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