package kr.spring.admin.service;

import java.math.BigDecimal;
import java.util.List;

import kr.spring.admin.vo.RefundPolicyVO;

public interface RefundPolicyService {

	List<RefundPolicyVO> selectRefundPolicyList();

	void updateFeeRate(
			long policyId,
			BigDecimal feeRate
	);
}