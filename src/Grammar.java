package src;
import java.util.*;
import java.io.*;

public class Grammar {
    Map<String, List<List<String>>> productions = new LinkedHashMap<>();
    List<String> nonTerminals = new ArrayList<>();

    public void loadFromFile(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split("->");
            String head = parts[0].trim();
            String[] bodies = parts[1].split("\\|");

            nonTerminals.add(head);
            List<List<String>> prodList = new ArrayList<>();
            for (String body : bodies) {
                prodList.add(new ArrayList<>(Arrays.asList(body.trim().split("\\s+"))));
            }
            productions.put(head, prodList);
        }
        reader.close();
    }

    // Task 1.2: Left Factoring — repeated until no common prefixes remain
    public void leftFactor() {
        boolean changed = true;
        while (changed) {
            changed = false;
            List<String> snapshot = new ArrayList<>(nonTerminals);
            for (String A : snapshot) {
                if (leftFactorOne(A)) changed = true;
            }
        }
    }

    private boolean leftFactorOne(String A) {
        List<List<String>> prods = productions.get(A);
        if (prods == null || prods.size() <= 1) return false;

        // Group by first symbol
        Map<String, List<List<String>>> groups = new LinkedHashMap<>();
        for (List<String> p : prods) {
            String first = p.get(0);
            groups.computeIfAbsent(first, k -> new ArrayList<>()).add(p);
        }

        boolean changed = false;
        List<List<String>> newAProds = new ArrayList<>();

        for (String prefix : groups.keySet()) {
            List<List<String>> commonProds = groups.get(prefix);
            if (commonProds.size() > 1) {
                changed = true;
                // Generate a unique name for the new non-terminal
                String newNT = A + "DoublePrime";
                while (productions.containsKey(newNT)) newNT = newNT + "Prime";

                newAProds.add(new ArrayList<>(Arrays.asList(prefix, newNT)));

                List<List<String>> newNTProds = new ArrayList<>();
                for (List<String> p : commonProds) {
                    List<String> suffix = new ArrayList<>(p.subList(1, p.size()));
                    newNTProds.add(suffix.isEmpty()
                            ? new ArrayList<>(Arrays.asList("epsilon"))
                            : suffix);
                }
                productions.put(newNT, newNTProds);
                if (!nonTerminals.contains(newNT)) nonTerminals.add(newNT);
            } else {
                newAProds.add(commonProds.get(0));
            }
        }
        productions.put(A, newAProds);
        return changed;
    }

    // Task 1.3: Indirect & Direct Left Recursion Removal
    public void removeLeftRecursion() {
        List<String> NTs = new ArrayList<>(productions.keySet());
        for (int i = 0; i < NTs.size(); i++) {
            String Ai = NTs.get(i);
            for (int j = 0; j < i; j++) {
                substitute(Ai, NTs.get(j));
            }
            eliminateDirectLeftRecursion(Ai);
        }
    }

    private void eliminateDirectLeftRecursion(String A) {
        List<List<String>> alpha = new ArrayList<>();
        List<List<String>> beta  = new ArrayList<>();
        boolean recursive = false;

        for (List<String> prod : productions.get(A)) {
            if (prod.get(0).equals(A)) {
                recursive = true;
                alpha.add(new ArrayList<>(prod.subList(1, prod.size())));
            } else {
                beta.add(prod);
            }
        }

        if (!recursive) return;

        String newNT = A + "Prime";
        while (productions.containsKey(newNT)) newNT = newNT + "Prime";

        List<List<String>> newAProds     = new ArrayList<>();
        List<List<String>> newAPrimeProds = new ArrayList<>();

        for (List<String> b : beta) {
            // If beta production is epsilon, the new production is just -> newNT
            // (epsilon . newNT simplifies to newNT)
            if (b.size() == 1 && (b.get(0).equals("epsilon") || b.get(0).equals("@"))) {
                newAProds.add(new ArrayList<>(Arrays.asList(newNT)));
            } else {
                List<String> temp = new ArrayList<>(b);
                temp.add(newNT);
                newAProds.add(temp);
            }
        }
        for (List<String> a : alpha) {
            List<String> temp = new ArrayList<>(a);
            temp.add(newNT);
            newAPrimeProds.add(temp);
        }
        newAPrimeProds.add(Arrays.asList("epsilon"));

        productions.put(A, newAProds);
        productions.put(newNT, newAPrimeProds);
        if (!nonTerminals.contains(newNT)) nonTerminals.add(newNT);
    }

    private void substitute(String Ai, String Aj) {
        List<List<String>> currentAi = productions.get(Ai);
        List<List<String>> newAi = new ArrayList<>();
        for (List<String> prod : currentAi) {
            if (prod.get(0).equals(Aj)) {
                for (List<String> ajProd : productions.get(Aj)) {
                    List<String> combined = new ArrayList<>(ajProd);
                    combined.addAll(prod.subList(1, prod.size()));
                    newAi.add(combined);
                }
            } else {
                newAi.add(prod);
            }
        }
        productions.put(Ai, newAi);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- Transformed Grammar ---\n");
        for (String nt : nonTerminals) {
            List<List<String>> prods = productions.get(nt);
            if (prods == null) continue;
            sb.append(nt).append(" -> ");
            for (int i = 0; i < prods.size(); i++) {
                sb.append(String.join(" ", prods.get(i)));
                if (i < prods.size() - 1) sb.append(" | ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
