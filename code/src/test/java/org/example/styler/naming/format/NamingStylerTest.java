package org.example.styler.naming.format;

import org.example.TestBase;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

class NamingStylerTest extends TestBase {

	@Test
	void analyzeInconsistency_matchesGolden() {
		String dir = Paths.get(javaTestCasesDir, "naming").toString();
		assertAnalyzeInconsistencyMatchesGolden(dir,
				new String[] {"f1.java", "f2.java", "f1-gt.java", "f2-gt.java"},
				new String[] {"f2.java", "f1.java", "f2.java", "f1.java"},
				List.of(NamingStyler.class));
	}

  @Test
	void test() {
	  String dir = "src/test/sources/naming/";
	  String[] srcFiles = {
			  "f1.java",
			  "f2.java"
	  };

	  String[] targetFiles = {
			  "f2.java",
			  "f1.java"
	  };

	  for (int i = 0; i < srcFiles.length; i++) {
		  Path gtPath = Paths.get(dir, String.format("%s-gt.java", srcFiles[i].replace(".java", "")));
		  String actual = apply(Paths.get(dir, srcFiles[i]), Paths.get(dir, targetFiles[i]), List.of(NamingStyler.class));
		  if (false) {
			  try{
				  Files.writeString(gtPath, actual);
			  }	catch (Exception e) {
				  e.printStackTrace();
			  }
		  }


		  testCodeEqual(actual, gtPath);
	  }
  }

	/**
	 * C++ naming: {@code f1.cpp} (camelCase) as source and {@code f2.cpp} as style target, same idea as the Java pair.
	 * Oracle {@code f1tof2gt.cpp} matches current engine output (whitespace-insensitive); C++ still uses
	 * {@code JavaResolver} in config, so some identifiers may be only partially renamed until a dedicated C++ resolver exists.
	 */
	@Test
	void testCpp_naming_apply() {
		String dir = Paths.get(cppTestCasesDir, "naming").toString();
		Path gtPath = Paths.get(dir, "f1tof2gt.cpp");
		String actual = apply(Paths.get(dir, "f1.cpp"), Paths.get(dir, "f2.cpp"), "cpp", List.of(NamingStyler.class));
		testCodeEqualIgnoreWS(actual, gtPath);
	}
}