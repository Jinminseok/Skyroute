package kr.spring.admin.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.spring.admin.dao.RefundPolicyMapper;
import kr.spring.admin.vo.RefundPolicyVO;

@Service
@Transactional(readOnly = true)
public class RefundPolicyServiceImpl
		implements RefundPolicyService {

	@Autowired
	private RefundPolicyMapper refundPolicyMapper;

	@Override
	public List<RefundPolicyVO> selectRefundPolicyList() {
		return refundPolicyMapper.selectRefundPolicyList();
	}

	@Override
	@Transactional
	public void updateFeeRate(
			long policyId,
			BigDecimal feeRate) {

		if (feeRate == null) {
			throw new IllegalArgumentException(
					"수수료율을 입력해야 합니다."
			);
		}

		BigDecimal normalizedRate =
				feeRate.setScale(2, RoundingMode.HALF_UP);

		if (normalizedRate.compareTo(BigDecimal.ZERO) < 0
				|| normalizedRate.compareTo(
						new BigDecimal("100")
				) >= 0) {

			throw new IllegalArgumentException(
					"수수료율은 0 이상 100 미만이어야 합니다."
			);
		}

		RefundPolicyVO policy =
				refundPolicyMapper.selectRefundPolicy(policyId);

		if (policy == null) {
			throw new IllegalArgumentException(
					"존재하지 않는 환불 정책입니다."
			);
		}

		if (!"Y".equals(policy.getEditableYn())
				|| "D91_PLUS".equals(
						policy.getPolicyCode()
				)) {

			throw new IllegalStateException(
					"수정할 수 없는 환불 정책입니다."
			);
		}

		int updated =
				refundPolicyMapper.updateRefundPolicyFeeRate(
						policyId,
						normalizedRate
				);

		if (updated != 1) {
			throw new IllegalStateException(
					"환불 수수료율 수정에 실패했습니다."
			);
		}
	}
}