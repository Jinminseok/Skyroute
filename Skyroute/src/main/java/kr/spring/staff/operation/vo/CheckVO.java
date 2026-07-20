package kr.spring.staff.operation.vo;

import lombok.Data;

@Data
public class CheckVO {
	private Long ticketId;           // 티켓 ID 
    private Long bookingId;          // 예약 ID
    private String pnrCode;          // 예약번호 
    private String passengerName;    // 승객명
    private String seatName;         // 지정좌석 
    
    private String holdStatus;       // 예약상태 
    private String paymentStatus;    // 결제상태 
    private String checkinStatus;    // 체크인/탑승 상태 
    
    private String checkedInByName;  // 체크인 처리자 이름/사번
    private Long checkedInBy;        // 체크인 처리자 ID
    
    private String flightCode;       // 항공편 코드 
}
