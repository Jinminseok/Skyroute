package kr.spring.staff.content.vo;

import java.sql.Timestamp;
import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(exclude = {"upload"})
public class EventVO {
	private long event_id;
	private String title;
	private String content;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date start_date;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date end_date;

	private String image_url;
	private String is_visible;
	private String is_ended;
	private long created_by;
	private Timestamp created_at;
	private int display_order;
	private MultipartFile upload;
	private int participation_count;

	private int winner_count;
	private String result_status;
	private Timestamp result_announced_at;
	private String display_visibility;
	private String progress_status;
	private String event_status;
	private boolean drawRequired;
}