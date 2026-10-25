package src;
import java.util.*;

// Task 2.4: Error Handling & Recovery
public class ErrorHandler {
    private int errorCount = 0;

    public void reportError(String type, String expected, String found, int step) {
        errorCount++;
        System.err.println("\n--- ERROR DETECTED (Step " + step + ") ---");
        System.err.println("Type    : " + type);
        System.err.println("Expected: " + expected);
        System.err.println("Found   : '" + found + "'");
    }

    // Panic Mode Recovery: skip tokens or pop stack until a sync symbol is found
    public int recover(src.Stack stack, String[] tokens, int currentIdx, Set<String> syncSymbols) {
        String currentToken = tokens[currentIdx];

        if (syncSymbols != null && syncSymbols.contains(currentToken)) {
            System.out.println("Action: Found sync symbol '" + currentToken + "' — popping stack.");
            stack.pop();
            return currentIdx;
        }

        System.out.println("Action: Skipping unexpected token '" + currentToken + "'.");
        return currentIdx + 1;
    }

    public int getErrorCount() { return errorCount; }
}
