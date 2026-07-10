package kr.spring.staff.schedule.vo;

import groovy.transform.ToString;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ToString
public class ScheduleVO {
	private int flight_id;              // 운항 스케줄 ID
    private String flight_no;           // 항공편 번호 (예: HJ-1207)
    private int route_id;               // 노선 ID
    private int aircraft_id;            // 항공기 ID
    private int departure_gate_id;      // 출발 게이트 ID
    private int arrival_gate_id;        // 도착 게이트 ID
    private String departure_time;      // 출발 시각 (HTML datetime-local과 맞추기 위해 String 사용)
    private String arrival_time;        // 도착 시각
    private String flight_status;       // 상태 (SCHEDULED 등)
    private int delay_minutes;          // 지연 시간(분)
    private String is_deleted;          // 삭제 여부 (Y/N)

    private String route_name;          // 예: ICN → KIX
    private String aircraft_name;       // 예: HL1234 (A320)
    private String dep_gate_code;       // 예: 10A
    private String arr_gate_code;       // 예: 12B
}
