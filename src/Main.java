package src;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Main {

    public static void main(String[] args) throws IOException {
        Files.createDirectories(Paths.get("output"));

        // Clear output files from any previous run
        String[] outFiles = {
            "output/grammar_transformed.txt", "output/first_follow_sets.txt",
            "output/parsing_table.txt",       "output/parsing_trace1.txt",
            "output/parsing_trace2.txt",      "output/parse_trees.txt"
        };
        for (String f : outFiles) new PrintWriter(f).close();

        // Grammar 1 — simple nullable (output to standard files)
        System.out.println("\n========== GRAMMAR 1 ==========");
        runGrammar("input/grammar1.txt",
                   new String[]{"a b", "b"},          // valid strings
                   new String[]{"c b", "a a"},         // error strings
                   "output/grammar_transformed.txt",
                   "output/first_follow_sets.txt",
                   "output/parsing_table.txt",
                   "output/parsing_trace1.txt",
                   "output/parsing_trace2.txt",
                   "output/parse_trees.txt");

        // Grammar 2 — arithmetic expression (append to same output files)
        System.out.println("\n========== GRAMMAR 2 ==========");
        runGrammar("input/grammar2.txt",
                   new String[]{"id + id", "id * id", "( id + id ) * id"},
                   new String[]{"id + * id", "+ id"},
                   "output/grammar_transformed.txt",
                   "output/first_follow_sets.txt",
                   "output/parsing_table.txt",
                   "output/parsing_trace1.txt",
                   "output/parsing_trace2.txt",
                   "output/parse_trees.txt");

        // Grammar 3 — if-then-else (has LL(1) conflict — dangling else, will be reported)
        System.out.println("\n========== GRAMMAR 3 ==========");
        runGrammar("input/grammar3.txt",
                   new String[]{"a", "if b then a"},
                   new String[]{"if b a", "if b then"},
                   "output/grammar_transformed.txt",
                   "output/first_follow_sets.txt",
                   "output/parsing_table.txt",
                   "output/parsing_trace1.txt",
                   "output/parsing_trace2.txt",
                   "output/parse_trees.txt");

        // Grammar 4 — indirect left recursion
        System.out.println("\n========== GRAMMAR 4 ==========");
        runGrammar("input/grammar4.txt",
                   new String[]{"b", "a"},
                   new String[]{"d", "a b c"},
                   "output/grammar_transformed.txt",
                   "output/first_follow_sets.txt",
                   "output/parsing_table.txt",
                   "output/parsing_trace1.txt",
                   "output/parsing_trace2.txt",
                   "output/parse_trees.txt");
    }

    private static void runGrammar(
            String grammarFile,
            String[] validInputs,
            String[] errorInputs,
            String transFile, String setsFile, String tableFile,
            String trace1File, String trace2File, String treeFile)
            throws IOException {

        // 1. Load & transform grammar
        Grammar g = new Grammar();
        g.loadFromFile(grammarFile);
        g.leftFactor();
        g.removeLeftRecursion();

        String transformedStr = g.toString();
        appendToFile(transFile, "\n===== " + grammarFile + " =====\n" + transformedStr);
        System.out.print(transformedStr);

        // 2. FIRST / FOLLOW sets
        FirstFollow ff = new FirstFollow();
        ff.computeFirst(g);
        String startSymbol = g.nonTerminals.get(0);
        ff.computeFollow(g, startSymbol);

        String setsStr = "FIRST Sets:\n" + ff.firstSets + "\n\nFOLLOW Sets:\n" + ff.followSets;
        appendToFile(setsFile, "\n===== " + grammarFile + " =====\n" + setsStr + "\n");
        System.out.println("FIRST : " + ff.firstSets);
        System.out.println("FOLLOW: " + ff.followSets);

        // 3. Build LL(1) parsing table
        Parser parser = new Parser();
        System.out.println("Building table...");
        parser.buildTable(g, ff);
        String tableStr = "===== " + grammarFile + " =====\n" + parser.getTableAsString();
        appendToFile(tableFile, "\n" + tableStr + "\n");

        if (!parser.isLL1()) {
            System.out.println("  Note: grammar has LL(1) conflicts; parsing attempted anyway.\n");
        }

        // 4. Parse valid strings — trace goes to trace1 file
        final Parser p1 = parser;
        final FirstFollow ff1 = ff;
        final String start = startSymbol;
        final String grammarLabel = grammarFile;

        appendToFile(trace1File, "\n===== " + grammarFile + " — Valid Inputs =====\n");
        for (String input : validInputs) {
            redirectStreams(trace1File, () -> {
                System.out.println("\nInput: \"" + input + "\"");
                p1.parseString(input, start, ff1);
                if (p1.getErrorCount() == 0 && p1.getParseTree() != null) {
                    System.out.println("\n--- Parse Tree ---");
                    p1.getParseTree().print("", true);
                }
            });
        }

        // Save final parse tree (last valid parse) to tree file
        appendToFile(treeFile, "\n===== " + grammarFile + " =====\n");
        redirectStreams(treeFile, () -> {
            if (p1.getParseTree() != null) p1.getParseTree().print("", true);
        });

        // 5. Parse error strings — trace goes to trace2 file
        appendToFile(trace2File, "\n===== " + grammarFile + " — Error Inputs =====\n");
        for (String input : errorInputs) {
            redirectStreams(trace2File, () -> {
                System.out.println("\nInput: \"" + input + "\"");
                p1.parseString(input, start, ff1);
            });
        }
    }

    private static void appendToFile(String path, String content) throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(path, true))) {
            out.print(content);
        }
    }

    private static void redirectStreams(String path, Runnable action) {
        PrintStream origOut = System.out;
        PrintStream origErr = System.err;
        try (PrintStream fs = new PrintStream(new FileOutputStream(path, true))) {
            System.setOut(fs);
            System.setErr(fs);
            action.run();
        } catch (FileNotFoundException e) {
            System.err.println("Could not open: " + path);
        } finally {
            System.setOut(origOut);
            System.setErr(origErr);
        }
    }
}
