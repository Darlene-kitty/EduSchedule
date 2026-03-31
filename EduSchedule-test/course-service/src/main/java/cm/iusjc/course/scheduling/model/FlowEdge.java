package cm.iusjc.course.scheduling.model;

/**
 * Arête orientée dans le réseau de flot.
 * Chaque arête connaît directement son arête résiduelle (référence directe).
 */
public class FlowEdge {

    private final int from;
    private final int to;
    private final int capacity;
    private int flow;

    /** Référence directe vers l'arête résiduelle (évite les bugs d'indexation) */
    private FlowEdge residual;

    public FlowEdge(int from, int to, int capacity) {
        this.from = from;
        this.to = to;
        this.capacity = capacity;
        this.flow = 0;
    }

    public int getFrom()             { return from; }
    public int getTo()               { return to; }
    public int getCapacity()         { return capacity; }
    public int getFlow()             { return flow; }
    public int getResidualCapacity() { return capacity - flow; }

    public void addFlow(int delta)   { this.flow += delta; }

    public FlowEdge getResidual()            { return residual; }
    public void setResidual(FlowEdge residual) { this.residual = residual; }

    @Override
    public String toString() {
        return from + " -> " + to + " [" + flow + "/" + capacity + "]";
    }
}
