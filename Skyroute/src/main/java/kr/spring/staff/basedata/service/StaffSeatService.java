package kr.spring.staff.basedata.service;

import kr.spring.staff.basedata.vo.SeatClassVO;

public interface StaffSeatService {
    public void generateSeats(SeatClassVO seatClassVO, int startRow);
}