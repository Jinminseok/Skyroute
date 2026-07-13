package kr.spring.member.booking.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/*
 * 좌석맵의 좌석 1칸
 *
 * occupied = 이미 HOLDING 또는 CONFIRMED 로 점유된 좌석
 */
@Getter
@Setter
@ToString
public class SHSeatVO {

	private Long seatId;

	private String seatNo;          // 예: 12A

	private Long seatClassId;

	private String occupiedYn;      // Y / N (매퍼에서 계산)


	public boolean isOccupied() {
		return "Y".equals(occupiedYn);
	}


	/* 12A → 12 */
	public int getRowNo() {

		if (seatNo == null) {
			return 0;
		}

		String digits = seatNo.replaceAll("[^0-9]", "");

		return digits.isEmpty()
				? 0
				: Integer.parseInt(digits);
	}


	/* 12A → A */
	public String getColCode() {

		if (seatNo == null) {
			return "";
		}

		return seatNo.replaceAll("[0-9]", "");
	}
}