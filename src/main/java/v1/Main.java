package v1;

import java.nio.charset.Charset;
import java.util.List;

public class Main {
	static int randomSeed = -1;
	static String modelFile;
	static int numOfIterations = 1;
	static String seedFile;
	static String outputFile;
	static int strength = 2; // default strength

	// Character encoding of the input (model) file, detected with ICU4J.
	// The output is produced in this same encoding. It defaults to the
	// platform default until the model file has been read.
	static String charset = Charset.defaultCharset().name();

	static final int MAX_LEVEL = 63;

	static final int MAX_ITERATIONS = 100000;
	static final int MAX_STRENGTH = 5;	
	static final int Max_RandomSeed = 65535;
	// static final int Max_RandomSeed = 10;

	static boolean debugMode = false;

	enum Language {
		JP, EN
	}

	static Language language = Language.JP;

	// Start the whole process
	public static void main(String[] args) {

		long start = System.currentTimeMillis();

		try {
			// コマンド引数処理
			String errorMessage = processCommandArgument(args);

			// エラー出力先設定
			Error.setOutputFile(outputFile);

			// コマンド引数でのエラー出力
			if (errorMessage != null)
				Error.printError(errorMessage);

			// モデル読み込み
			// System.err.println("starting reading model");
			InputFileData inputfiledata = Inputer.readModel(modelFile);

			// 制約処理 BDD作成
			// System.err.println("starting building bdd");

			// old version where all parameters are considered in BDD
			//			ConstraintHandler conhndl = new ConstraintHandler(
			//					inputfiledata.parameterList, inputfiledata.constraintList);

			// newer version
			ConstraintHandler conhndl = new ConstraintHandler(
					inputfiledata.parameterList, inputfiledata.constraintList, inputfiledata.constrainedParameters);
			// DEBUG: BDDの表示
			/* conhndl.printConstraintBDD(); */

			// 　シード読み込み
			List<Testcase> seed = Inputer.readSeed(seedFile, inputfiledata);

			// テストケース生成
			// System.err.println("starting test suite construction");
			List<Testcase> testSet = null;
			if (strength == -1) {
				// 全網羅
				try {
					testSet = GeneratorAll.generate(new ParameterModel(
							inputfiledata.parameterList), conhndl);
				} catch (OutOfMaxNumOfTestcasesException e) {
					Error.printError(Main.language == Main.Language.JP ? "テストケース数が上限"
							+ Generator.MaxNumOfTestcases + "を超えました"
							: "The number of test cases exceeded the upper bound "
							+ Generator.MaxNumOfTestcases);
				}

				new Outputer(outputFile).outputResult(testSet, inputfiledata,
						modelFile, outputFile);
			} else { // strength >= 2
				Generator generator = GeneratorFactor.newGenerator(
						new ParameterModel(inputfiledata.parameterList),
						inputfiledata.groupList, conhndl, seed, randomSeed,
						strength);
				try {
					testSet = generator.generate();
				} catch (OutOfMaxNumOfTestcasesException e) {
					testSet = null;
				}

				if (debugMode)
					System.err.println("random seed: " + randomSeed);
				// 繰り返す場合
				for (int i = 2; i < numOfIterations; i++) {
					int nextRandomSeed = (int) Math.floor(Math.random()
							* (Max_RandomSeed + 1));
					generator = GeneratorFactor.newGenerator(
							new ParameterModel(inputfiledata.parameterList),
							inputfiledata.groupList, conhndl, seed,
							nextRandomSeed, strength);

					if (debugMode)
						System.err.println("random seed: " + nextRandomSeed);

					List<Testcase> nextTestSet = null;
					try {
						nextTestSet = generator.generate();
					} catch (OutOfMaxNumOfTestcasesException e) {
						nextTestSet = null;
					}

					if (testSet != null && nextTestSet != null) {
						if (nextTestSet.size() < testSet.size()) {
							testSet = nextTestSet;
							randomSeed = nextRandomSeed;
						}
					} else if (testSet == null && nextTestSet != null) {
						testSet = nextTestSet;
						randomSeed = nextRandomSeed;
					}
				}
				if (testSet == null)
					Error.printError(Main.language == Main.Language.JP ? "テストケース数が上限"
							+ Generator.MaxNumOfTestcases + "を超えました"
							: "The number of test cases exceeded the upper bound "
							+ Generator.MaxNumOfTestcases);

				new Outputer(outputFile).outputResult(testSet, inputfiledata,
						randomSeed, modelFile, seedFile, outputFile, strength,
						numOfIterations);
			}

			/* debug */
			if (debugMode) {
				System.err.println("test set size: " + testSet.size());
			}
		} catch (OutOfMemoryError e) {
			Error.printError(Main.language == Main.Language.JP ? "メモリ不足です．"
					: "Out of memory");
		} catch (Exception e) {
			Error.printError(Main.language == Main.Language.JP ? "プログラムが異常終了しました．"
					: "Abnormal termination");
		}

		//		long end = System.currentTimeMillis();
		//		System.err.println("time: " + (end - start) + "ms");
	}

	// コマンド引数処理
	private static String processCommandArgument(String[] args) {
		if (args.length == 0) {
			Error.printError("usage: java -jar Program.jar [-i input] [-o output] [-policy] ...");
		}

		// policyの表示
		if (args.length == 1 && args[0].equals("-policy")) {
			System.out.println("""
				This software (CIT-BACH 1.2) is licensed under the MIT License.
					
					 Copyright (c) 2026 Tatsuhiro Tsuchiya
					
					 This software includes components from JDD, a Java BDD library developed
					 by Arash Vahidi. JDD is free software distributed under the zlib license.
					 The JDD license notice is included in the distribution.
					
					 Permission is hereby granted, free of charge, to any person obtaining a copy
					 of this software and associated documentation files (the “Software”), to deal
					 in the Software without restriction, including without limitation the rights
					 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
					 copies of the Software, and to permit persons to whom the Software is
					 furnished to do so, subject to the conditions of the MIT License.
					
					 The above copyright notice and this permission notice shall be included in
					 all copies or substantial portions of the Software.
			""");
			System.exit(0);
		}

		// エラー表示を出力ファイルが指定されるまで遅らせる
		String errorMessage = null;

		for (int i = 0; i + 1 < args.length; i += 2) {
			String option = args[i];
			String str = args[i + 1];
			if (option.equals("-i")) {
				modelFile = str;
			} else if (option.equals("-o")) {
				outputFile = str;
			} else if (option.equals("-random")) {
				try {
					randomSeed = Integer.parseInt(str);
				} catch (NumberFormatException e) {
					// Error.printError("invalid number");
					errorMessage = Main.language == Main.Language.JP ? "ランダムシードに無効な値が指定されています．"
							: "Invalid random seed";
					continue;
				}
				randomSeed = Math.abs(randomSeed) % (Max_RandomSeed + 1);
			} else if (option.equals("-c")) {
				if (str.equals("all")) {
					// 全網羅
					strength = -1;
				} else {
					try {
						strength = Integer.parseInt(str);
					} catch (NumberFormatException e) {
						// Error.printError("invalid number");
						errorMessage = Main.language == Main.Language.JP ? "網羅度に無効な値が指定されています．"
								: "Invalid strength";
						continue;
					}
					if (strength < 2 || MAX_STRENGTH < strength) {
						// Error.printError("invalid strength");
						errorMessage = Main.language == Main.Language.JP ? "網羅度に無効な値が指定されています．"
								: "Invalid strength";
						continue;
					}
				}
			}
			// 繰り返し数
			else if (option.equals("-repeat")) {
				try {
					numOfIterations = Integer.parseInt(str);
				} catch (NumberFormatException e) {
					// Error.printError("invalid repeating number");
					errorMessage = Main.language == Main.Language.JP ? "くり返し数に無効な値が指定されています．"
							: "Invalid number of repetition times";
					continue;
				}
				if (numOfIterations <= 0 || numOfIterations > MAX_ITERATIONS) {
					// Error.printError("invalid repeating number");
					errorMessage = Main.language == Main.Language.JP ? "くり返し数に無効な値が指定されています．"
							: "Invalid number of repetition times";
					continue;
				}
			} else if (option.equals("-s")) {
				seedFile = str;
			} else if (option.equals("-debug")) {
				debugMode = true;
				// 次の引数はダミー
			} else if (option.equals("-lang")) {
				if (str.matches("JP|jp")) {
					Main.language = Main.Language.JP;
				} else if (str.matches("EN|en")) {
					Main.language = Main.Language.EN;
				} else {
					errorMessage = "無効な言語が指定されています (Invalid Language)";
					continue;
				}
			} else {
				// Error.printError("Invalid option");
				errorMessage = Main.language == Main.Language.JP ? "無効なオプションが指定されています．"
						: "Invalid option";
				continue;
			}
		}

		if (randomSeed == -1) {
			randomSeed = (int) Math.floor(Math.random() * (Max_RandomSeed + 1));
		}

		return errorMessage;
	}
}

