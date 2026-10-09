package org.example.styler.structure;

import org.example.TestBase;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * Per equivalence <strong>rule id</strong>: every code variant index {@code 0..n-1} is exercised as the
 * structure target (one dynamic test per {@code (id, targetIndex)}).
 * <p>
 * Source layout: <strong>each id has its own directory and its own single {@code f1.cpp}</strong>
 * (e.g. {@code structure/inc/f1.cpp} for id=1, {@code structure/dec/f1.cpp} for id=2). That file must embed
 * all {@code n} variants for that id; all tests for that id reuse it. Different ids do <strong>not</strong>
 * share one source file across directories.
 * <p>
 * Oracle: {@code f1-<id>-<targetIndex>-gt.cpp} next to that id's {@code f1.cpp}.
 */
public class CPPStructureStylerTest extends TestBase {

	static {
		StructureStyler.TEST_MODE = true;
	}

	@TestFactory
	Stream<DynamicTest> cppStructure_eachEquivalenceTargetIndex() {
		List<CppEquivalenceRules.EnabledRule> rules = CppEquivalenceRules.loadEnabledRules();
		Map<Integer, String> dirMap = CppEquivalenceRules.ruleIdToTestSubdir();
		List<DynamicTest> tests = new ArrayList<>();
		for (CppEquivalenceRules.EnabledRule r : rules) {
			String sub = dirMap.get(r.id());
			if (sub == null) {
				continue;
			}
			Path caseDir = Paths.get(cppTestCasesDir, "structure", sub);
			if (!Files.isRegularFile(caseDir.resolve("f1.cpp"))) {
				continue;
			}
			String dir = caseDir.toString();
			for (int idx = 0; idx < r.codeVariantCount(); idx++) {
				final int ruleId = r.id();
				final int targetIndex = idx;
				tests.add(dynamicTest(String.format("cpp structure id=%d targetIndex=%d (%s)", ruleId, targetIndex, sub),
						() -> {
							// One-time extracted style XML for the large id=14 fixture (see original test).
							boolean saveStyle = ruleId == 14 && targetIndex == 0;
							testCppStructureStyle(dir, new String[] {"f1.cpp"}, createStructureStyle(ruleId, targetIndex),
									saveStyle);
						}));
			}
		}
		return tests.stream();
	}
}
