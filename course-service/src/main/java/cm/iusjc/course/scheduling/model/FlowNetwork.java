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
 *
 * Chaque arête forward et son arête résiduelle backward se référencent
 * mutuellement via FlowEdge.residual — pas d'indexation fragile.
 */
public class FlowNetwork {

    private final int size;
    private final List<List<FlowEdge>> adj;

    public FlowNetwork(int size) {
        this.size = size;
        this.adj = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            adj.add(new ArrayList<>());
        }
    }

    /**
     * Ajoute une arête orientée from→to avec la capacité donnée.
     * Ajoute automatiquement l'arête résiduelle to→from (capacité 0).
     * Les deux arêtes se référencent mutuellement via FlowEdge.residual.
     */
    public void addEdge(int from, int to, int capacity) {
        FlowEdge forward  = new FlowEdge(from, to, capacity);
        FlowEdge backward = new FlowEdge(to, from, 0);

        // Liaison bidirectionnelle directe — plus d'indexation fragile
        forward.setResidual(backward);
        backward.setResidual(forward);

        adj.get(from).add(forward);
        adj.get(to).add(backward);
    }

    public List<FlowEdge> getEdges(int node) {
        return adj.get(node);
    }

    public int getSize() { return size; }
}
