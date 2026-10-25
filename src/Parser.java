package src;
import java.util.*;

public class Parser {
    // Non-Terminal -> (Terminal -> Production)
    private Map<String, Map<String, List<String>>> table = new HashMap<>();
    private ErrorHandler errorHandler = new ErrorHandler();
    private Tree parseTreeRoot;
    private boolean isLL1 = true;

    // Task 1.6: LL(1) Parsing Table Construction
    public void buildTable(Grammar g, FirstFollow ff) {
        for (String A : g.nonTerminals) {
            table.put(A, new HashMap<>());
            List<List<String>> productions = g.productions.get(A);
            if (productions == null) continue;

            for (List<String> alpha : productions) {
                Set<String> firstAlpha = ff.getFirstOfSequence(alpha, g);

                for (String a : firstAlpha) {
                    if (a.equals("epsilon") || a.equals("@")) continue;
                    if (table.get(A).containsKey(a)) {
                        List<String> existing = table.get(A).get(a);
                        if (!existing.equals(alpha)) {
                            System.out.println("  [LL(1) CONFLICT] M[" + A + ", " + a + "]  "
                                    + A + " -> " + String.join(" ", existing)
                                    + "  |  " + A + " -> " + String.join(" ", alpha));
                            isLL1 = false;
                        }
                    } else {
                        table.get(A).put(a, alpha);
                    }
                }

                if (firstAlpha.contains("epsilon") || firstAlpha.contains("@")) {
                    for (String b : ff.followSets.get(A)) {
                        if (table.get(A).containsKey(b)) {
                            List<String> existing = table.get(A).get(b);
                            if (!existing.equals(alpha)) {
                                System.out.println("  [LL(1) CONFLICT] M[" + A + ", " + b + "]  "
                                        + A + " -> " + String.join(" ", existing)
                                        + "  |  " + A + " -> " + String.join(" ", alpha));
                                isLL1 = false;
                            }
                        } else {
                            table.get(A).put(b, alpha);
                        }
                    }
                }
            }
        }
        if (isLL1) System.out.println("  Grammar is LL(1): YES");
        else        System.out.println("  Grammar is LL(1): NO (conflicts detected above)");
    }

    public boolean isLL1() { return isLL1; }

    // Task 2.3 & 2.5: Parsing Algorithm with Parse Tree Generation
    public void parseString(String input, String startSymbol, FirstFollow ff) {
        errorHandler = new ErrorHandler();
        Stack customStack = new Stack();
        java.util.Stack<Tree> treeStack = new java.util.Stack<>();

        parseTreeRoot = new Tree(startSymbol);
        customStack.push("$");
        treeStack.push(new Tree("$"));
        customStack.push(startSymbol);
        treeStack.push(parseTreeRoot);

        String[] tokens = (input.trim() + " $").split("\\s+");
        int idx = 0, step = 1;

        System.out.println("\n--- Parsing Trace ---");
        System.out.printf("%-6s | %-35s | %-20s | %s\n", "Step", "Stack", "Input", "Action");
        System.out.println("-".repeat(90));

        while (!customStack.isEmpty()) {
            String X = customStack.peek();
            String a = tokens[idx];

            System.out.printf("%-6d | %-35s | %-20s | ", step, customStack.toString(), a);

            if (X.equals(a)) {
                if (X.equals("$")) {
                    System.out.println("Accept");
                    break;
                }
                customStack.pop();
                treeStack.pop();
                idx++;
                System.out.println("Match " + a);

            } else if (table.containsKey(X)) {
                List<String> prod = table.get(X).get(a);

                if (prod != null) {
                    customStack.pop();
                    Tree parentNode = treeStack.pop();

                    if (prod.get(0).equals("epsilon") || prod.get(0).equals("@")) {
                        parentNode.children.add(new Tree("epsilon"));
                        System.out.println(X + " -> epsilon");
                    } else {
                        List<Tree> childNodes = new ArrayList<>();
                        for (String sym : prod) {
                            Tree child = new Tree(sym);
                            parentNode.children.add(child);
                            childNodes.add(child);
                        }
                        for (int i = childNodes.size() - 1; i >= 0; i--) {
                            customStack.push(prod.get(i));
                            treeStack.push(childNodes.get(i));
                        }
                        System.out.println(X + " -> " + String.join(" ", prod));
                    }
                } else {
                    // Task 2.4: Panic Mode Error Recovery
                    String expected = table.get(X).keySet().toString();
                    errorHandler.reportError("Empty Table Entry", expected, a, step);
                    idx = errorHandler.recover(customStack, tokens, idx, ff.followSets.get(X));
                    syncTreeStack(treeStack, customStack);
                    if (idx >= tokens.length) break;
                }

            } else {
                // Terminal mismatch
                errorHandler.reportError("Terminal Mismatch", X, a, step);
                customStack.pop();
                if (!treeStack.isEmpty()) treeStack.pop();
            }
            step++;
        }
    }

    // Keep treeStack depth in sync with the custom stack after panic-mode pops
    private void syncTreeStack(java.util.Stack<Tree> treeStack, Stack customStack) {
        int stackDepth = customStack.toString().split(",").length;
        if (customStack.toString().equals("[]")) stackDepth = 0;
        while (treeStack.size() > stackDepth && !treeStack.isEmpty()) treeStack.pop();
    }

    public Tree getParseTree()  { return parseTreeRoot; }
    public int  getErrorCount() { return errorHandler.getErrorCount(); }

    // Task 1.6 output: LL(1) Parsing Table as formatted string
    public String getTableAsString() {
        StringBuilder sb = new StringBuilder();
        Set<String> terminals = new TreeSet<>();
        for (Map<String, List<String>> row : table.values()) terminals.addAll(row.keySet());

        sb.append(String.format("%-20s", "Non-Terminal"));
        for (String t : terminals) sb.append(String.format("| %-25s", t));
        sb.append("\n").append("-".repeat(20 + terminals.size() * 27)).append("\n");

        for (String nt : table.keySet()) {
            sb.append(String.format("%-20s", nt));
            for (String t : terminals) {
                List<String> prod = table.get(nt).get(t);
                String cell = (prod != null) ? nt + " -> " + String.join(" ", prod) : "";
                sb.append(String.format("| %-25s", cell));
            }
            sb.append("\n");
        }
        sb.append("\nGrammar is LL(1): ").append(isLL1 ? "YES" : "NO");
        return sb.toString();
    }
}
