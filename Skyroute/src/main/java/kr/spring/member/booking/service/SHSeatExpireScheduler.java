package kr.spring.member.booking.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * 만료된 좌석 HOLD 반납
 *
 * 조회 쿼리에서 expired_at 을 함께 보고 있으므로
 * 스케줄러가 늦어도 좌석이 잘못 팔리지는 않는다.
 * 다만 좌석맵의 점유 표시와 TICKET 행 상태를 실제로 정리하려면 필요하다.
 *
 * ※ SkyRouteApplication 에 @EnableScheduling 을 붙여야 동작한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SHSeatExpireScheduler {

	private final SHBookingService shBookingService;


	/* 1분마다 */
	@Scheduled(fixedDelay = 60_000, initialDelay = 30_000)
	public void releaseExpiredSeats() {

		try {
			shBookingService.releaseExpiredSeats();

		} catch (Exception e) {
			/* 스케줄러가 죽으면 이후 실행이 멈추므로 예외를 삼킨다 */
			log.error("<<만료 좌석 반납 실패>>", e);
		}
	}
}