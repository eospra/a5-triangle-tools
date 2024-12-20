/*
 * @(#)Compiler.java                       
 * 
 * Revisions and updates (c) 2022-2024 Sandy Brownlee. alexander.brownlee@stir.ac.uk
 * 
 * Original release:
 *
 * Copyright (C) 1999, 2003 D.A. Watt and D.F. Brown
 * Dept. of Computing Science, University of Glasgow, Glasgow G12 8QQ Scotland
 * and School of Computer and Math Sciences, The Robert Gordon University,
 * St. Andrew Street, Aberdeen AB25 1HG, Scotland.
 * All rights reserved.
 *
 * This software is provided free for educational use only. It may
 * not be used for commercial purposes without the prior written permission
 * of the authors.
 */

package triangle;

import com.sampullara.cli.Argument;

import triangle.abstractSyntaxTrees.Program;
import triangle.codeGenerator.Emitter;
import triangle.codeGenerator.Encoder;
import triangle.contextualAnalyzer.Checker;
import triangle.optimiser.ConstantFolder;
import triangle.optimiser.Hoister;
import triangle.optimiser.SummaryVisitor;
import triangle.syntacticAnalyzer.Parser;
import triangle.syntacticAnalyzer.Scanner;
import triangle.syntacticAnalyzer.SourceFile;
import triangle.treeDrawer.Drawer;

/**
 * The main driver class for the Triangle compiler.
 *
 * @version 2.1 7 Oct 2003
 * @author Deryck F. Brown
 */
public class Compiler {

	// the cli parser library lets us make instance variables with annotations like
	// this
	// that specify command line arguments for the program

	/** The filename for the object program, normally obj.tam. */
	@Argument(alias = "on", description = "name of the source file", required = true)
	static String objectName = "obj.tam";

	@Argument(alias = "st", description = "whether to show the tree produced", required = false)
	static boolean showTree = false;

	@Argument(alias = "f", description = "whether folding will be done", required = false)
	static boolean folding = false;

	@Argument(alias = "sta", description = "whether to show the tree after folding", required = false)
	static boolean showTreeAfter = false;

	@Argument(alias = "ss", description = "whether to show the visitor statistics", required = false)
	static boolean showStats = false;

	@Argument(alias = "h", description = "whether hoisting will be donw", required = false)
	static boolean hoisting = false;

	private static Scanner scanner;
	private static Parser parser;
	private static Checker checker;
	private static Encoder encoder;
	private static Emitter emitter;
	private static ErrorReporter reporter;
	private static Drawer drawer;

	/** The AST representing the source program. */
	private static Program theAST;

	/**
	 * Compile the source program to TAM machine code.
	 *
	 * @param sourceName   the name of the file containing the source program.
	 * @param objectName   the name of the file containing the object program.
	 * @param showingAST   true if the AST is to be displayed after contextual
	 *                     analysis
	 * @param showingTable true if the object description details are to be
	 *                     displayed during code generation (not currently
	 *                     implemented).
	 * @return true if the source program is free of compile-time errors, otherwise
	 *         false.
	 */
	static boolean compileProgram(String sourceName, String objectName, boolean showingAST, boolean showingTable,
			boolean folding, boolean showingASTAfter, boolean showingStats, boolean hoisting) {

		System.out.println("********** " + "Triangle Compiler (Java Version 2.1)" + " **********");

		System.out.println("Syntactic Analysis ...");
		SourceFile source = SourceFile.ofPath(sourceName);

		if (source == null) {
			System.out.println("Can't access source file " + sourceName);
			System.exit(1);
		}

		scanner = new Scanner(source);
		reporter = new ErrorReporter(false);
		parser = new Parser(scanner, reporter);
		checker = new Checker(reporter);
		emitter = new Emitter(reporter);
		encoder = new Encoder(emitter, reporter);
		drawer = new Drawer();

		// scanner.enableDebugging();
		theAST = parser.parseProgram(); // 1st pass
		if (reporter.getNumErrors() == 0) {
			// if (showingAST) {
			// drawer.draw(theAST);
			// }
			System.out.println("Contextual Analysis ...");
			checker.check(theAST); // 2nd pass
			if (showingAST) {
				drawer.draw(theAST);
			}
			if (folding) {
				theAST.visit(new ConstantFolder());
				if (showingASTAfter) {
					drawer.draw(theAST);
				}
			}
			if (hoisting) {
				theAST.visit(new Hoister());
				drawer.draw(theAST);
			}
			if (showingStats) {
				SummaryVisitor sv = new SummaryVisitor();
				theAST.visit(sv);
				System.out.println(sv.getCounts());
			}

			if (reporter.getNumErrors() == 0) {
				System.out.println("Code Generation ...");
				encoder.encodeRun(theAST, showingTable); // 3rd pass
			}
		}

		boolean successful = (reporter.getNumErrors() == 0);
		if (successful) {
			emitter.saveObjectProgram(objectName);
			System.out.println("Compilation was successful.");
		} else {
			System.out.println("Compilation was unsuccessful.");
		}
		return successful;
	}

	/**
	 * Triangle compiler main program.
	 *
	 * @param args the only command-line argument to the program specifies the
	 *             source filename.
	 */
	public static void main(String[] args) {

		if (args.length < 1) {
			System.out.println(
					"Usage: tc filename [-o=outputfilename] [tree] [folding] [treeAfter] [showStats] [hoisting]");
			System.exit(1);
		}

		parseArgs(args);

		String sourceName = args[0];

		var compiledOK = compileProgram(sourceName, objectName, showTree, false, folding, showTreeAfter, showStats,
				hoisting);

		if (!showTree) {
			System.exit(compiledOK ? 0 : 1);
		}
	}

	private static void parseArgs(String[] args) {
		for (String s : args) {
			var sl = s.toLowerCase();
			if (sl.equals("tree")) {
				showTree = true;
			} else if (sl.startsWith("-o=")) {
				objectName = s.substring(3);
			} else if (sl.equals("folding")) {
				folding = true;
			} else if (sl.equals("treeafter")) {
				showTreeAfter = true;
			} else if (sl.equals("showstats")) {
				showStats = true;
			} else if (sl.equals("hoisting")) {
				hoisting = true;
			}
		}
	}
}
