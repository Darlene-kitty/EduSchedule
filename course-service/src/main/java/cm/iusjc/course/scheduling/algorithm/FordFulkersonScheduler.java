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
 * Supporte aussi le mode DFS (Ford-Fulkerson standard) via maxFlowDfs().
 */
public class FordFulkersonScheduler {

    /** Edmonds-Karp : BFS pour trouver les chemins augmentants (défaut recommandé) */
    public int maxFlow(FlowNetwork graph, int source, int sink) {
        int totalFlow = 0;
        int[] path;
        while ((path = bfs(graph, source, sink)) != null) {
            int bottleneck = findBottleneck(graph, path);
            augment(graph, path, bottleneck);
            totalFlow += bottleneck;
        }
        return totalFlow;
    }

    /**
     * BFS pour trouver un chemin augmentant de source à sink.
     * Retourne un tableau plat [node0, edgeIdx0, node1, edgeIdx1, ..., sink]
     * ou null si aucun chemin n'existe.
     */
    private int[] bfs(FlowNetwork graph, int source, int sink) {
        int n = graph.getSize();
        int[] parent     = new int[n];
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
                        return buildPath(parent, parentEdge, source, sink);
                    }
                    queue.add(next);
                }
            }
        }
        return null;
    }

    /**
     * Reconstruit le chemin sous forme de tableau plat :
     * [node0, edgeIdx0, node1, edgeIdx1, ..., sink]
     */
    private int[] buildPath(int[] parent, int[] parentEdge, int source, int sink) {
        int len = 0;
        int cur = sink;
        while (cur != source) { cur = parent[cur]; len++; }

        int[] path = new int[len * 2 + 1];
        cur = sink;
        int pos = path.length - 1;
        path[pos--] = sink;
        while (cur != source) {
            int prev = parent[cur];
            path[pos--] = parentEdge[cur];
            path[pos--] = prev;
            cur = prev;
        }
        return path;
    }

    private int findBottleneck(FlowNetwork graph, int[] path) {
        int bottleneck = Integer.MAX_VALUE;
        for (int i = 0; i < path.length - 2; i += 2) {
            FlowEdge edge = graph.getEdges(path[i]).get(path[i + 1]);
            bottleneck = Math.min(bottleneck, edge.getResidualCapacity());
        }
        return bottleneck;
    }

    private void augment(FlowNetwork graph, int[] path, int bottleneck) {
        for (int i = 0; i < path.length - 2; i += 2) {
            FlowEdge forward = graph.getEdges(path[i]).get(path[i + 1]);
            forward.addFlow(bottleneck);
            forward.getResidual().addFlow(-bottleneck);
        }
    }

    // ── Ford-Fulkerson DFS ──────────────────────────────────────────────────

    /** Ford-Fulkerson standard : DFS pour trouver les chemins augmentants */
    public int maxFlowDfs(FlowNetwork graph, int source, int sink) {
        int totalFlow = 0;
        int[] path;
        while ((path = dfs(graph, source, sink)) != null) {
            int bottleneck = findBottleneck(graph, path);
            augment(graph, path, bottleneck);
            totalFlow += bottleneck;
        }
        return totalFlow;
    }

    private int[] dfs(FlowNetwork graph, int source, int sink) {
        int n = graph.getSize();
        int[] parent     = new int[n];
        int[] parentEdge = new int[n];
        Arrays.fill(parent, -1);
        parent[source] = source;

        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(source);

        while (!stack.isEmpty()) {
            int node = stack.pop();
            List<FlowEdge> edges = graph.getEdges(node);
            for (int i = 0; i < edges.size(); i++) {
                FlowEdge edge = edges.get(i);
                int next = edge.getTo();
                if (parent[next] == -1 && edge.getResidualCapacity() > 0) {
                    parent[next]     = node;
                    parentEdge[next] = i;
                    if (next == sink) {
                        return buildPath(parent, parentEdge, source, sink);
                    }
                    stack.push(next);
                }
            }
        }
        return null;
    }
}
