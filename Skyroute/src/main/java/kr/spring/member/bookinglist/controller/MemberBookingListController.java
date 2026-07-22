package kr.spring.member.bookinglist.controller;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.spring.member.booking.exception.SHRefundReconciliationException;
import kr.spring.member.booking.payment.SHBookingCancelFacade;
import kr.spring.member.booking.payment.SHBookingCancelResult;
import kr.spring.member.bookinglist.service.MemberBookingListService;
import kr.spring.member.bookinglist.vo.MemberBookingCancelRequest;
import kr.spring.member.vo.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import kr.spring.member.booking.payment.BookingCancelQuote;
import kr.spring.member.booking.service.SHBookingService;

@Slf4j
@Controller
@RequestMapping("/bookinglist")
@RequiredArgsConstructor
public class MemberBookingListController {

    private final MemberBookingListService bookingListService;

    private final SHBookingCancelFacade bookingCancelFacade;

    private final SHBookingService shBookingService;

    /**
     * 로그인 회원의 예약 목록 조회.
     */
    @GetMapping("/lookup")
    @PreAuthorize("hasAuthority('USER')")
    public String bookingLookup(
            @RequestParam(
                    value = "passengerName",
                    required = false
            )
            String passengerName,

            @RequestParam(
                    value = "bookingNo",
                    required = false
            )
            String bookingNo,

            @RequestParam(
                    value = "arrivalKeyword",
                    required = false
            )
            String arrivalKeyword,

            @RequestParam(
                    value = "departureDate",
                    required = false
            )
            String departureDate,

            @AuthenticationPrincipal
            PrincipalDetails principal,

            Model model) {

        Long memberId = getMemberId(principal);

        log.info(
                "<<예약 조회 진입>> "
                + "memberId={}, passengerName={}, bookingNo={}",
                memberId,
                passengerName,
                bookingNo
        );

        Map<String, Object> paramMap =
                new HashMap<>();

        paramMap.put("memberId", memberId);
        paramMap.put(
                "passengerName",
                passengerName
        );
        paramMap.put(
                "bookingNo",
                bookingNo
        );
        paramMap.put(
                "arrivalKeyword",
                arrivalKeyword
        );
        paramMap.put(
                "departureDate",
                departureDate
        );

        List<Map<String, Object>> bookingRows =
                bookingListService.selectMyBookingList(paramMap);

        List<Map<String, Object>> bookingList =
                groupBookingList(bookingRows);

        model.addAttribute("bookingList", bookingList);

        /*
         * 검색 입력값 보존
         */
        model.addAttribute(
                "passengerName",
                passengerName
        );
        model.addAttribute(
                "bookingNo",
                bookingNo
        );
        model.addAttribute(
                "arrivalKeyword",
                arrivalKeyword
        );
        model.addAttribute(
                "departureDate",
                departureDate
        );

        return "thviews/member/member_booking";
    }

    
    // 로그인 회원 예약 전체 취소 및 환불정책에 따른 환불
    @PostMapping("/cancel/quote")
    @ResponseBody
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<Map<String, Object>> cancelQuote(
    		@RequestBody MemberBookingCancelRequest request,
    		@AuthenticationPrincipal PrincipalDetails principal) {

    	if (principal == null || principal.getMemberVO() == null) {
    		return errorResponse(
    				HttpStatus.UNAUTHORIZED,
    				"UNAUTHORIZED",
    				"로그인이 필요합니다."
    		);
    	}

    	if (request == null || request.bookingId() == null) {
    		return errorResponse(
    				HttpStatus.BAD_REQUEST,
    				"INVALID_REQUEST",
    				"예약 ID가 필요합니다."
    		);
    	}

    	Long memberId = principal.getMemberVO().getMember_id();

    	try {
    		BookingCancelQuote quote = shBookingService.calculateCancellationQuote(request.bookingId(), memberId);

    		Map<String, Object> body = new LinkedHashMap<>();

    		body.put("result", "SUCCESS");
    		body.put("bookingId", quote.bookingId());
    		body.put("originalAmount", quote.originalAmount());
    		body.put("feeAmount", quote.totalFeeAmount());
    		body.put("refundAmount", quote.totalRefundAmount());
    		body.put("fullRefund", quote.totalFeeAmount() == 0L);

    		return ResponseEntity.ok(body);

    	} catch (IllegalArgumentException | IllegalStateException e) {
    		log.warn(
    				"<<예약 취소 예상금액 조회 거절>> memberId={}, bookingId={}, message={}",
    				memberId,
    				request.bookingId(),
    				e.getMessage()
    		);

    		return errorResponse(
    				HttpStatus.CONFLICT,
    				"CANCEL_NOT_ALLOWED",
    				e.getMessage()
    		);

    	} catch (Exception e) {
    		log.error(
    				"<<예약 취소 예상금액 조회 오류>> memberId={}, bookingId={}",
    				memberId,
    				request.bookingId(),
    				e
    		);

    		return errorResponse(
    				HttpStatus.INTERNAL_SERVER_ERROR,
    				"INTERNAL_ERROR",
    				"예상 환불금액을 계산하는 중 오류가 발생했습니다."
    		);
    	}
    }

    /**
     * 로그인 회원 예약 전체 취소 및 전액 환불.
     */
    @PostMapping("/cancel")
    @ResponseBody
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<Map<String, Object>>
            cancelBooking(

            @RequestBody
            MemberBookingCancelRequest request,

            @AuthenticationPrincipal
            PrincipalDetails principal) {

        /*
         * 인증 정보 자체가 없는 경우
         */
        if (principal == null
                || principal.getMemberVO() == null) {

            return errorResponse(
                    HttpStatus.UNAUTHORIZED,
                    "UNAUTHORIZED",
                    "로그인이 필요합니다."
            );
        }

        /*
         * 요청값 검증
         */
        if (request == null
                || request.bookingId() == null) {

            return errorResponse(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_REQUEST",
                    "예약 ID가 필요합니다."
            );
        }

        Long memberId =
                principal
                        .getMemberVO()
                        .getMember_id();

        try {

            SHBookingCancelResult result =
                    bookingCancelFacade
                            .cancelFullBooking(
                                    request.bookingId(),
                                    memberId,
                                    request.reason()
                            );

            Map<String, Object> body =
                    new LinkedHashMap<>();

            body.put(
                    "result",
                    "SUCCESS"
            );

            body.put(
                    "code",
                    result.alreadyCompleted()
                            ? "ALREADY_COMPLETED"
                            : "CANCELLED"
            );

            body.put(
                    "message",
                    result.alreadyCompleted()
                            ? "이미 취소 및 환불이 완료된 예약입니다."
                            : "예약 취소 및 환불이 완료되었습니다."
            );

            body.put(
                    "bookingId",
                    result.bookingId()
            );

            body.put(
                    "refundAmount",
                    result.refundAmount()
            );

            body.put(
                    "alreadyCompleted",
                    result.alreadyCompleted()
            );

            log.info(
                    "<<회원 예약 취소 완료>> "
                    + "memberId={}, bookingId={}, "
                    + "refundAmount={}, alreadyCompleted={}",
                    memberId,
                    result.bookingId(),
                    result.refundAmount(),
                    result.alreadyCompleted()
            );

            return ResponseEntity.ok(body);

        } catch (
                SHRefundReconciliationException e
        ) {

            /*
             * PortOne 환불은 성공했지만
             * DB 상태 반영이 실패한 경우.
             */
            log.error(
                    "<<환불 정합성 오류>> "
                    + "memberId={}, bookingId={}",
                    memberId,
                    request.bookingId(),
                    e
            );

            return errorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "REFUND_RECONCILIATION_REQUIRED",
                    e.getMessage()
            );

        } catch (
                IllegalArgumentException
                | IllegalStateException e
        ) {

            /*
             * 취소 불가 상태, 출발 시각 경과,
             * 체크인 완료, 소유권 불일치 등.
             */
            log.warn(
                    "<<예약 취소 거절>> "
                    + "memberId={}, bookingId={}, message={}",
                    memberId,
                    request.bookingId(),
                    e.getMessage()
            );

            return errorResponse(
                    HttpStatus.CONFLICT,
                    "CANCEL_NOT_ALLOWED",
                    e.getMessage()
            );

        } catch (Exception e) {

            log.error(
                    "<<예약 취소 처리 오류>> "
                    + "memberId={}, bookingId={}",
                    memberId,
                    request.bookingId(),
                    e
            );

            return errorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "INTERNAL_ERROR",
                    "예약 취소 처리 중 오류가 발생했습니다."
            );
        }
    }

    
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> groupBookingList(
            List<Map<String, Object>> bookingRows) {

        Map<Long, Map<String, Object>> grouped =
                new LinkedHashMap<>();

        for (Map<String, Object> row : bookingRows) {
            Number bookingIdNumber =
                    (Number) row.get("BOOKING_ID");

            if (bookingIdNumber == null) {
                continue;
            }

            Long bookingId =
                    bookingIdNumber.longValue();

            Map<String, Object> booking =
                    grouped.get(bookingId);

            if (booking == null) {
                booking = new LinkedHashMap<>();
                booking.put("BOOKING_ID", bookingId);
                booking.put("BOOKING_NO", row.get("BOOKING_NO"));
                booking.put("BOOKING_STATUS", row.get("BOOKING_STATUS"));
                booking.put("FLIGHTS", new ArrayList<Map<String, Object>>());
                booking.put("PASSENGERS", new ArrayList<Map<String, Object>>());
                booking.put("TOTAL_AMOUNT", 0L);
                booking.put("CANCEL_AVAILABLE", true);

                grouped.put(bookingId, booking);
            }

            List<Map<String, Object>> flights =
                    (List<Map<String, Object>>) booking.get("FLIGHTS");

            Map<String, Object> targetFlight = null;

            for (Map<String, Object> flight : flights) {
                if (sameValue(
                        flight.get("FLIGHT_ID"),
                        row.get("FLIGHT_ID"))
                        && sameValue(
                        flight.get("LEG_TYPE"),
                        row.get("LEG_TYPE"))) {

                    targetFlight = flight;
                    break;
                }
            }

            if (targetFlight == null) {
                targetFlight = new LinkedHashMap<>();
                targetFlight.put("FLIGHT_ID", row.get("FLIGHT_ID"));
                targetFlight.put("LEG_TYPE", row.get("LEG_TYPE"));
                targetFlight.put("FLIGHT_STATUS", row.get("FLIGHT_STATUS"));
                targetFlight.put("DELAY_MINUTES", row.get("DELAY_MINUTES"));
                targetFlight.put("IS_DEPARTED", row.get("IS_DEPARTED"));
                targetFlight.put("FLIGHT_NO", row.get("FLIGHT_NO"));
                targetFlight.put("DEPARTURE_TIME", row.get("DEPARTURE_TIME"));
                targetFlight.put("DEP_IATA", row.get("DEP_IATA"));
                targetFlight.put("DEP_NAME", row.get("DEP_NAME"));
                targetFlight.put("ARR_IATA", row.get("ARR_IATA"));
                targetFlight.put("ARR_NAME", row.get("ARR_NAME"));
                targetFlight.put("TICKETS", new ArrayList<Map<String, Object>>());

                flights.add(targetFlight);
            }

            List<Map<String, Object>> tickets =
                    (List<Map<String, Object>>) targetFlight.get("TICKETS");

            Map<String, Object> ticket =
                    new LinkedHashMap<>();

            ticket.put("TICKET_ID", row.get("TICKET_ID"));
            ticket.put("BOOKING_PASSENGER_ID", row.get("BOOKING_PASSENGER_ID"));
            ticket.put("PASSENGER_NAME", row.get("PASSENGER_NAME"));
            ticket.put("PASSENGER_TYPE", row.get("PASSENGER_TYPE"));
            ticket.put("CLASS_NAME", row.get("CLASS_NAME"));
            ticket.put("SEAT_NO", row.get("SEAT_NO"));
            ticket.put("FARE_AMOUNT", row.get("FARE_AMOUNT"));
            ticket.put("CHECKIN_STATUS", row.get("CHECKIN_STATUS"));
            ticket.put("CHECKED_IN_AT", row.get("CHECKED_IN_AT"));
            ticket.put("BOARDED_AT", row.get("BOARDED_AT"));

            tickets.add(ticket);

            List<Map<String, Object>> passengers =
                    (List<Map<String, Object>>) booking.get("PASSENGERS");

            boolean passengerExists = false;

            for (Map<String, Object> passenger : passengers) {
                if (sameValue(
                        passenger.get("BOOKING_PASSENGER_ID"),
                        row.get("BOOKING_PASSENGER_ID"))) {

                    passengerExists = true;
                    break;
                }
            }

            if (!passengerExists) {
                Map<String, Object> passenger =
                        new LinkedHashMap<>();

                passenger.put(
                        "BOOKING_PASSENGER_ID",
                        row.get("BOOKING_PASSENGER_ID")
                );
                passenger.put(
                        "PASSENGER_NAME",
                        row.get("PASSENGER_NAME")
                );
                passenger.put(
                        "PASSENGER_TYPE",
                        row.get("PASSENGER_TYPE")
                );

                passengers.add(passenger);
            }

            Number fareAmount =
                    (Number) row.get("FARE_AMOUNT");

            if (fareAmount != null) {
                long totalAmount =
                        ((Number) booking.get("TOTAL_AMOUNT"))
                                .longValue();

                booking.put(
                        "TOTAL_AMOUNT",
                        totalAmount + fareAmount.longValue()
                );
            }

            String bookingStatus =
                    String.valueOf(row.get("BOOKING_STATUS"));

            String checkinStatus =
                    row.get("CHECKIN_STATUS") == null
                            ? null
                            : String.valueOf(row.get("CHECKIN_STATUS"));

            String flightStatus =
                    row.get("FLIGHT_STATUS") == null
                            ? null
                            : String.valueOf(row.get("FLIGHT_STATUS"));

            boolean rowCancelable =
                    "CONFIRMED".equals(bookingStatus)
                            && (checkinStatus == null
                            || "NOT_CHECKED_IN".equals(checkinStatus))
                            && ("SCHEDULED".equals(flightStatus)
                            || "DELAYED".equals(flightStatus))
                            && "N".equals(String.valueOf(row.get("IS_DEPARTED")));

            booking.put(
                    "CANCEL_AVAILABLE",
                    Boolean.TRUE.equals(
                            booking.get("CANCEL_AVAILABLE")
                    ) && rowCancelable
            );
        }

        for (Map<String, Object> booking : grouped.values()) {
            List<Map<String, Object>> flights =
                    (List<Map<String, Object>>) booking.get("FLIGHTS");

            List<Map<String, Object>> passengers =
                    (List<Map<String, Object>>) booking.get("PASSENGERS");

            boolean hasInbound = false;

            for (Map<String, Object> flight : flights) {
                if ("INBOUND".equals(
                        String.valueOf(flight.get("LEG_TYPE")))) {

                    hasInbound = true;
                    break;
                }
            }

            booking.put(
                    "TRIP_TYPE",
                    hasInbound ? "ROUND_TRIP" : "ONE_WAY"
            );

            booking.put(
                    "TRIP_TYPE_LABEL",
                    hasInbound ? "왕복" : "편도"
            );

            booking.put(
                    "PASSENGER_COUNT",
                    passengers.size()
            );
        }

        return new ArrayList<>(grouped.values());
    }

    private boolean sameValue(
            Object left,
            Object right) {

        if (left == null || right == null) {
            return left == right;
        }

        return String.valueOf(left)
                .equals(String.valueOf(right));
    }
    

    private Long getMemberId(
            PrincipalDetails principal) {

        if (principal == null
                || principal.getMemberVO() == null) {

            throw new IllegalStateException(
                    "로그인이 필요합니다."
            );
        }

        return principal
                .getMemberVO()
                .getMember_id();
    }


    private ResponseEntity<Map<String, Object>>
            errorResponse(

            HttpStatus status,
            String code,
            String message) {

        Map<String, Object> body =
                new LinkedHashMap<>();

        body.put(
                "result",
                "FAIL"
        );

        body.put(
                "code",
                code
        );

        body.put(
                "message",
                message == null || message.isBlank()
                        ? "요청 처리에 실패했습니다."
                        : message
        );

        return ResponseEntity
                .status(status)
                .body(body);
    }
}