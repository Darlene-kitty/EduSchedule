package cm.iusjc.course.scheduling.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Réseau de flot représenté par liste d'adjacence.
 *
 * Structure du graphe pour l'emploi du temps :
 *   Source (0)
 *     └─► Nœud Cours  (capacité = hoursPerWeek)
 *           └─► Nœud Créneau  (capacité = 1, si enseignant dispo)
 *                 └─► Nœud Salle  (capacité = 1)
 *                       └─► Puits (n-1)
 */
public class FlowNetwork {

    private final int size;
    private final List<List<FlowEdge>> adj;
    // Pour chaque arête forward, on stocke l'index de son arête résiduelle
    private final List<List<Integer>> residualIndex;

    public FlowNetwork(int size) {
        this.size = size;
        this.adj = new ArrayList<>();
        this.residualIndex = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            adj.add(new ArrayList<>());
            residualIndex.add(new ArrayList<>());
        }
    }

    /**
     * Ajoute une arête orientée from→to avec la capacité donnée.
     * Ajoute automatiquement l'arête résiduelle to→from (capacité 0).
     */
    public void addEdge(int from, int to, int capacity) {
        FlowEdge forward  = new FlowEdge(from, to, capacity);
        FlowEdge backward = new FlowEdge(to, from, 0);

        int fwdIdx = adj.get(from).size();
        int bwdIdx = adj.get(to).size();

        adj.get(from).add(forward);
        adj.get(to).add(backward);

        residualIndex.get(from).add(bwdIdx);
        residualIndex.get(to).add(fwdIdx);
    }

    public List<FlowEdge> getEdges(int node) {
        return adj.get(node);
    }

    /** Retourne l'arête résiduelle correspondant à l'arête à l'index edgeIdx du nœud node */
    public FlowEdge getResidual(int node, int edgeIdx) {
        int rIdx = residualIndex.get(node).get(edgeIdx);
        FlowEdge edge = adj.get(node).get(edgeIdx);
        return adj.get(edge.getTo()).get(rIdx);
    }

    public int getSize() { return size; }
}
