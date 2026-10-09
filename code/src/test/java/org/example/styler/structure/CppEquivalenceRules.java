package org.example.styler.structure;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Loads enabled C++ structure equivalence rules from {@code cpp_equivalences.xml} (classpath).
 */
public final class CppEquivalenceRules {

	public record EnabledRule(int id, int codeVariantCount) {}

	/** Maps rule id → {@code src/test/sources-cpp/structure/<subdir>/} (one {@code f1.cpp} per id, not shared across ids). */
	private static final Map<Integer, String> RULE_ID_TO_TEST_SUBDIR = Map.ofEntries(
			Map.entry(1, "inc"),
			Map.entry(2, "dec"),
			Map.entry(3, "assignment"),
			Map.entry(4, "if"),
			Map.entry(5, "cascading-if"),
			Map.entry(6, "loop-empty"),
			Map.entry(7, "loop"),
			Map.entry(9, "loop3"),
			Map.entry(12, "loop6"),
			Map.entry(13, "check-then-return"),
			Map.entry(14, "check-then-assign"),
			Map.entry(15, "check-then-assign2"),
			Map.entry(18, "op-not-false"),
			Map.entry(19, "literal-position"),
			Map.entry(22, "continue"),
			Map.entry(23, "continue2"),
			Map.entry(24, "redundant1"),
			Map.entry(25, "redundant2"));

	private CppEquivalenceRules() {
	}

	public static Map<Integer, String> ruleIdToTestSubdir() {
		return RULE_ID_TO_TEST_SUBDIR;
	}

	public static List<EnabledRule> loadEnabledRules() {
		try (InputStream in = CppEquivalenceRules.class.getClassLoader().getResourceAsStream("cpp_equivalences.xml")) {
			if (in == null) {
				throw new IllegalStateException("classpath resource cpp_equivalences.xml not found");
			}
			SAXReader reader = new SAXReader();
			Document doc = reader.read(in);
			List<EnabledRule> out = new ArrayList<>();
			for (Object o : doc.getRootElement().elements("rule")) {
				Element rule = (Element) o;
				if ("false".equalsIgnoreCase(rule.attributeValue("enable"))) {
					continue;
				}
				int id = Integer.parseInt(rule.attributeValue("id"));
				Element codes = rule.element("codes");
				int n = codes == null ? 0 : codes.elements("code").size();
				out.add(new EnabledRule(id, n));
			}
			return out;
		} catch (Exception e) {
			throw new RuntimeException("Failed to parse cpp_equivalences.xml", e);
		}
	}
}
