package kr.spring.admin.vo;

import lombok.Data;

@Data
public class SeasonVO {
	private int seasonId;
    private String seasonName;
    private String startDate;
    private String endDate;
    private String isActive;
    
    public int getSeasonId() { return this.seasonId; }
    public String getSeasonName() { return this.seasonName; }
}
