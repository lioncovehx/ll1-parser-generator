package src;
import java.util.*;

public class Tree {
    public String label;
    public List<Tree> children = new ArrayList<>();

    public Tree(String label) {
        this.label = label;
    }

    /**
     * Task 2.5: Prints the tree in an indented ASCII format
     * @param prefix The current indentation string
     * @param isTail Whether this node is the last child of its parent
     */
    public void print(String prefix, boolean isTail) {
        System.out.println(prefix + (isTail ? "└── " : "├── ") + label);
        for (int i = 0; i < children.size() - 1; i++) {
            children.get(i).print(prefix + (isTail ? "    " : "│   "), false);
        }
        if (children.size() > 0) {
            children.get(children.size() - 1).print(prefix + (isTail ? "    " : "│   "), true);
        }
    }
}