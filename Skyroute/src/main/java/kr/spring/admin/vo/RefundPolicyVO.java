package kr.spring.admin.vo;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class RefundPolicyVO {
	private long policyId;
	private String policyCode;
	private String policyName;
	private Integer minDaysBefore;
	private Integer maxDaysBefore;
	private BigDecimal feeRate;
	private String editableYn;
	private int displayOrder;
}