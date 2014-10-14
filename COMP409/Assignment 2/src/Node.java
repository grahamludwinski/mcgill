import java.util.concurrent.locks.ReentrantLock;

public class Node extends ReentrantLock {
    private Integer index;

    public Node(Integer index) {
        this.index = index;
    }

    public Integer getIndex() {
        return index;
    }
}
