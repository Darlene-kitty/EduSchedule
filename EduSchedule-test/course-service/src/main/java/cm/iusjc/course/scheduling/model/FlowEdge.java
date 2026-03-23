package cm.iusjc.course.scheduling.model;

/**
 * Arête orientée dans le réseau de flot.
 * Chaque arête a une capacité et un flot courant.
 */
public class FlowEdge {

    private final int from;
    private final int to;
    private final int capacity;
    private int flow;

    public FlowEdge(int from, int to, int capacity) {
        this.from = from;
        this.to = to;
        this.capacity = capacity;
        this.flow = 0;
    }

    public int getFrom()               { return from; }
    public int getTo()                 { return to; }
    public int getCapacity()           { return capacity; }
    public int getFlow()               { return flow; }
    public int getResidualCapacity()   { return capacity - flow; }

    public void addFlow(int delta)     { this.flow += delta; }

    @Override
    public String toString() {
        return from + " -> " + to + " [" + flow + "/" + capacity + "]";
    }
}
