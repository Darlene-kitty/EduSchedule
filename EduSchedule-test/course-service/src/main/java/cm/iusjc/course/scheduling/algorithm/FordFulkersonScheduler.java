package cm.iusjc.course.scheduling.algorithm;

import cm.iusjc.course.scheduling.model.FlowEdge;
import cm.iusjc.course.scheduling.model.FlowNetwork;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

/**
 * Implémentation d'Edmonds-Karp (Ford-Fulkerson + BFS).
 * Complexité : O(V * E²)
 *
 * Utilisé pour maximiser l'affectation cours → créneaux → salles.
 */
public class FordFulkersonScheduler {

    /**
     * Calcule le flot maximum entre source et sink dans le réseau donné.
     * Modifie le flot des arêtes du réseau en place.
     *
     * @return valeur du flot maximum
     */
    public int maxFlow(FlowNetwork graph, int source, int sink) {
        int totalFlow = 0;
        int[] path;

        // Tant qu'un chemin augmentant existe (BFS)
        while ((path = bfs(graph, source, sink)) != null) {
            int bottleneck = findBottleneck(graph, path, source, sink);
            augment(graph, path, source, sink, bottleneck);
            totalFlow += bottleneck;
        }

        return totalFlow;
    }

    /**
     * BFS pour trouver un chemin augmentant de source à sink.
     * Retourne le tableau parent[] ou null si aucun chemin.
     */
    private int[] bfs(FlowNetwork graph, int source, int sink) {
        int n = graph.getSize();
        int[] parent    = new int[n];
        int[] parentEdge = new int[n];
        Arrays.fill(parent, -1);
        parent[source] = source;

        Deque<Integer> queue = new ArrayDeque<>();
        queue.add(source);

        while (!queue.isEmpty()) {
            int node = queue.poll();
            List<FlowEdge> edges = graph.getEdges(node);

            for (int i = 0; i < edges.size(); i++) {
                FlowEdge edge = edges.get(i);
                int next = edge.getTo();

                if (parent[next] == -1 && edge.getResidualCapacity() > 0) {
                    parent[next]     = node;
                    parentEdge[next] = i;
                    if (next == sink) {
                        // Reconstruit le chemin via parentEdge
                        return buildPath(parent, parentEdge, source, sink, n);
                    }
                    queue.add(next);
                }
            }
        }
        return null; // pas de chemin augmentant
    }

    /**
     * Reconstruit le chemin sous forme de tableau plat :
     * [node0, edgeIdx0, node1, edgeIdx1, ..., sink]
     */
    private int[] buildPath(int[] parent, int[] parentEdge, int source, int sink, int n) {
        // Compte la longueur du chemin
        int len = 0;
        int cur = sink;
        while (cur != source) { cur = parent[cur]; len++; }

        int[] path = new int[len * 2 + 1];
        cur = sink;
        int pos = path.length - 1;
        path[pos--] = sink;

        while (cur != source) {
            int prev = parent[cur];
            path[pos--] = parentEdge[cur]; // index de l'arête dans prev
            path[pos--] = prev;
            cur = prev;
        }
        return path;
    }

    /** Trouve le goulot d'étranglement (capacité résiduelle minimale) sur le chemin */
    private int findBottleneck(FlowNetwork graph, int[] path, int source, int sink) {
        int bottleneck = Integer.MAX_VALUE;
        for (int i = 0; i < path.length - 2; i += 2) {
            int node    = path[i];
            int edgeIdx = path[i + 1];
            FlowEdge edge = graph.getEdges(node).get(edgeIdx);
            bottleneck = Math.min(bottleneck, edge.getResidualCapacity());
        }
        return bottleneck;
    }

    /** Augmente le flot sur le chemin de la valeur bottleneck */
    private void augment(FlowNetwork graph, int[] path, int source, int sink, int bottleneck) {
        for (int i = 0; i < path.length - 2; i += 2) {
            int node    = path[i];
            int edgeIdx = path[i + 1];
            FlowEdge forward  = graph.getEdges(node).get(edgeIdx);
            FlowEdge backward = graph.getResidual(node, edgeIdx);
            forward.addFlow(bottleneck);
            backward.addFlow(-bottleneck);
        }
    }
}
