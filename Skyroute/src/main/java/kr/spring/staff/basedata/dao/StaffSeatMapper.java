package kr.spring.staff.basedata.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

import kr.spring.staff.basedata.vo.SeatClassVO;
import kr.spring.staff.basedata.vo.SeatVO;

@Mapper
public interface StaffSeatMapper {
    
    // 등급 등록
    @Insert("INSERT INTO seat_class (aircraft_id, class_name, seat_rows, seat_columns, sort_order) "
          + "VALUES (#{aircraft_id}, #{class_name}, #{seat_rows}, #{seat_columns}, #{sort_order})")
    @Options(useGeneratedKeys = true, keyProperty = "seat_class_id", keyColumn = "seat_class_id")
    public void insertSeatClass(SeatClassVO seatClassVO);
    
    // 낱개 좌석 등록
    @Insert("INSERT INTO seat (aircraft_id, seat_class_id, seat_no) "
          + "VALUES (#{aircraft_id}, #{seat_class_id}, #{seat_no})")
    public void insertSeat(SeatVO seatVO);
}