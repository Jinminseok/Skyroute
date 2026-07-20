package kr.spring.member.bookinglist.controller;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

@Slf4j
@Controller
@RequestMapping("/bookinglist")
@RequiredArgsConstructor
public class MemberBookingListController {

    private final MemberBookingListService bookingListService;

    private final SHBookingCancelFacade bookingCancelFacade;


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

        List<Map<String, Object>> bookingList =
                bookingListService
                        .selectMyBookingList(
                                paramMap
                        );

        model.addAttribute(
                "bookingList",
                bookingList
        );

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