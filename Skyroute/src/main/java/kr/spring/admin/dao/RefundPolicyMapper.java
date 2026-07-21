package kr.spring.admin.dao;

import java.math.BigDecimal;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.spring.admin.vo.RefundPolicyVO;

@Mapper
public interface RefundPolicyMapper {

	List<RefundPolicyVO> selectRefundPolicyList();

	RefundPolicyVO selectRefundPolicy(
			@Param("policyId") long policyId
	);

	RefundPolicyVO selectRefundPolicyByDays(
			@Param("daysBefore") long daysBefore
	);

	int updateRefundPolicyFeeRate(
			@Param("policyId") long policyId,
			@Param("feeRate") BigDecimal feeRate
	);
}