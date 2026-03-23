package cm.iusjc.course.scheduling.model;

/**
 * Représente un nœud dans le réseau de flot.
 * Un nœud peut être : SOURCE, SINK, COURSE, SLOT (créneau), ROOM.
 */
public class FlowNode {

    public enum NodeType { SOURCE, SINK, COURSE, SLOT, ROOM }

    private final int index;          // index dans le FlowNetwork
    private final NodeType type;
    private final Long referenceId;   // ID métier (courseId, roomId, etc.)
    private final String label;       // ex: "INF101", "Lundi 08h-10h", "Salle A"

    public FlowNode(int index, NodeType type, Long referenceId, String label) {
        this.index = index;
        this.type = type;
        this.referenceId = referenceId;
        this.label = label;
    }

    public int getIndex()         { return index; }
    public NodeType getType()     { return type; }
    public Long getReferenceId()  { return referenceId; }
    public String getLabel()      { return label; }

    @Override
    public String toString() {
        return "[" + type + " #" + index + " | " + label + "]";
    }
}
