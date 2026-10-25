package src;
import java.util.*;

// Task 2.2: Custom Stack Implementation
public class Stack {
    private List<String> items = new ArrayList<>();

    public void push(String item) { items.add(item); }

    public String pop() {
        if (isEmpty()) return null;
        return items.remove(items.size() - 1);
    }

    public String peek() {
        if (isEmpty()) return null;
        return items.get(items.size() - 1);
    }

    public boolean isEmpty() { return items.isEmpty(); }

    public int size() { return items.size(); }

    @Override
    public String toString() { return items.toString(); }
}
