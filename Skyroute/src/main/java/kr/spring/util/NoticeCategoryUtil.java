package kr.spring.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class NoticeCategoryUtil {

	private static final Map<String, String> CATEGORY_MAP;

	static {
		Map<String, String> map = new LinkedHashMap<String, String>();

		map.put("SKY_NEWS", "스카이 소식");
		map.put("FUEL_SURCHARGE", "유류할증료");
		map.put("MEMBERSHIP_CLUB", "멤버십클럽");
		map.put("PARTNER_NEWS", "제휴사 소식");
		map.put("ETC", "기타");

		CATEGORY_MAP = Collections.unmodifiableMap(map);
	}

	private NoticeCategoryUtil() {}

	public static Map<String, String> getCategoryMap() {
		return CATEGORY_MAP;
	}

	public static boolean isValid(String category) {
		return category != null && CATEGORY_MAP.containsKey(category);
	}

	public static String getLabel(String category) {
		if (category == null) {
			return "기타";
		}

		String label = CATEGORY_MAP.get(category);

		if (label == null) {
			return "기타";
		}

		return label;
	}
}