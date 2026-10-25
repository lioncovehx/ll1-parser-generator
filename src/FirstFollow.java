package src;
import java.util.*;

public class FirstFollow {
    Map<String, Set<String>> firstSets  = new HashMap<>();
    Map<String, Set<String>> followSets = new HashMap<>();

    // Task 1.4: FIRST Set Computation
    public void computeFirst(Grammar g) {
        for (String nt : g.nonTerminals) firstSets.put(nt, new HashSet<>());

        boolean changed = true;
        while (changed) {
            changed = false;
            for (String A : g.nonTerminals) {
                for (List<String> prod : g.productions.get(A)) {
                    int before = firstSets.get(A).size();
                    boolean allDeriveEpsilon = true;
                    for (String symbol : prod) {
                        if (!g.productions.containsKey(symbol)) {
                            // Terminal (including "epsilon"/"@" literal)
                            firstSets.get(A).add(symbol);
                            allDeriveEpsilon = false;
                            break;
                        } else {
                            Set<String> nf = new HashSet<>(firstSets.get(symbol));
                            nf.remove("epsilon");
                            firstSets.get(A).addAll(nf);
                            if (!firstSets.get(symbol).contains("epsilon")) {
                                allDeriveEpsilon = false;
                                break;
                            }
                        }
                    }
                    if (allDeriveEpsilon) firstSets.get(A).add("epsilon");
                    if (firstSets.get(A).size() > before) changed = true;
                }
            }
        }
    }

    // Task 1.5: FOLLOW Set Computation
    public void computeFollow(Grammar g, String startSymbol) {
        for (String nt : g.nonTerminals) followSets.put(nt, new HashSet<>());
        followSets.get(startSymbol).add("$");

        boolean changed = true;
        while (changed) {
            changed = false;
            for (String A : g.nonTerminals) {
                for (List<String> prod : g.productions.get(A)) {
                    for (int i = 0; i < prod.size(); i++) {
                        String B = prod.get(i);
                        if (!g.productions.containsKey(B)) continue;
                        int before = followSets.get(B).size();

                        if (i + 1 < prod.size()) {
                            Set<String> firstBeta = getFirstOfSequence(
                                    prod.subList(i + 1, prod.size()), g);
                            Set<String> temp = new HashSet<>(firstBeta);
                            temp.remove("epsilon");
                            followSets.get(B).addAll(temp);
                            if (firstBeta.contains("epsilon"))
                                followSets.get(B).addAll(followSets.get(A));
                        } else {
                            followSets.get(B).addAll(followSets.get(A));
                        }
                        if (followSets.get(B).size() > before) changed = true;
                    }
                }
            }
        }
    }

    Set<String> getFirstOfSequence(List<String> seq, Grammar g) {
        Set<String> result = new HashSet<>();
        for (String s : seq) {
            if (!g.productions.containsKey(s)) {
                result.add(s);
                return result;
            }
            result.addAll(firstSets.get(s));
            if (!firstSets.get(s).contains("epsilon")) {
                result.remove("epsilon");
                break;
            }
        }
        return result;
    }
}
