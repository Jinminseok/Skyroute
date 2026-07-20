package kr.spring.member.booking.exception;

/**
 * PortOne 환불은 성공했지만
 * 내부 DB 반영이 실패한 정합성 오류.
 */
public class SHRefundReconciliationException
        extends RuntimeException {

    public SHRefundReconciliationException(
            String message,
            Throwable cause) {

        super(message, cause);
    }
}