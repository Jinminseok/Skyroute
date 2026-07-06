package kr.spring.staff.content.vo;

import java.sql.Date;
import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class EventParticipationVO {
	private long participation_id;
	private long event_id;
	private long member_id;
	private Timestamp participated_at;

	private String title;
	private String image_url;
	private Date start_date;
	private Date end_date;

	private String name;
	private String login_id;
	private String email;
}