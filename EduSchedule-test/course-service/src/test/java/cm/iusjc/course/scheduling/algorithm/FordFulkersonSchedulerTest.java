package cm.iusjc.course.scheduling.algorithm;

import cm.iusjc.course.scheduling.model.FlowEdge;
import cm.iusjc.course.scheduling.model.FlowNetwork;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour FordFulkersonScheduler (Edmonds-Karp).
 *
 * Graphe de référence (cas classique) :
 *
 *        10        10
 *   S ──────► A ──────► T
 *   │         │          ▲
 *   │ 10      │ 5        │ 10
 *   ▼         ▼          │
 *   B ──────► C ─────────┘
 *        15
 *
 *  Flot max attendu = 20
 */
class FordFulkersonSchedulerTest {

    private FordFulkersonScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new FordFulkersonScheduler();
    }

    // ─── Cas 1 : graphe classique ────────────────────────────────────────────

    @Test
    void testClassicGraph_maxFlowIs20() {
        // Noeuds : S=0, A=1, B=2, C=3, T=4
        FlowNetwork graph = new FlowNetwork(5);
        graph.addEdge(0, 1, 10); // S → A
        graph.addEdge(0, 2, 10); // S → B
        graph.addEdge(1, 4, 10); // A → T
        graph.addEdge(1, 3, 5);  // A → C
        graph.addEdge(2, 3, 15); // B → C
        graph.addEdge(3, 4, 10); // C → T

        int flow = scheduler.maxFlow(graph, 0, 4);

        assertEquals(20, flow, "Le flot max doit être 20");
    }

    // ─── Cas 2 : graphe linéaire simple ─────────────────────────────────────

    @Test
    void testLinearGraph_bottleneckLimitsFlow() {
        // S=0 ──5──► A=1 ──3──► B=2 ──8──► T=3
        // Goulot = 3
        FlowNetwork graph = new FlowNetwork(4);
        graph.addEdge(0, 1, 5);
        graph.addEdge(1, 2, 3);
        graph.addEdge(2, 3, 8);

        int flow = scheduler.maxFlow(graph, 0, 3);

        assertEquals(3, flow, "Le goulot d'étranglement doit limiter le flot à 3");
    }

    // ─── Cas 3 : pas de chemin source → sink ─────────────────────────────────

    @Test
    void testNoPath_flowIsZero() {
        // S=0, A=1, T=2 — aucune arête vers T
        FlowNetwork graph = new FlowNetwork(3);
        graph.addEdge(0, 1, 10);
        // pas d'arête vers T

        int flow = scheduler.maxFlow(graph, 0, 2);

        assertEquals(0, flow, "Sans chemin vers le sink, le flot doit être 0");
    }

    // ─── Cas 4 : source == sink ───────────────────────────────────────────────

    @Test
    void testSourceEqualsSink_flowIsZero() {
        FlowNetwork graph = new FlowNetwork(3);
        graph.addEdge(0, 1, 5);
        graph.addEdge(1, 2, 5);

        int flow = scheduler.maxFlow(graph, 0, 0);

        assertEquals(0, flow, "Source == Sink doit retourner 0");
    }

    // ─── Cas 5 : graphe emploi du temps (structure réelle) ───────────────────
    //
    //  Source(0) ──2──► Cours(1) ──1──► Créneau_Lundi(2) ──1──► Salle_A(4) ──1──► Sink(5)
    //                           └──1──► Créneau_Mardi(3) ──1──► Salle_A(4)
    //
    //  2 heures/semaine pour le cours, 2 créneaux dispo, 1 salle
    //  Flot max attendu = 2 (les 2 heures sont planifiées)

    @Test
    void testTimetableGraph_allHoursScheduled() {
        // Noeuds : Source=0, Cours=1, Créneau_Lundi=2, Créneau_Mardi=3, Salle_A=4, Sink=5
        FlowNetwork graph = new FlowNetwork(6);
        graph.addEdge(0, 1, 2); // Source → Cours (2h/semaine)
        graph.addEdge(1, 2, 1); // Cours → Créneau Lundi
        graph.addEdge(1, 3, 1); // Cours → Créneau Mardi
        graph.addEdge(2, 4, 1); // Créneau Lundi → Salle A
        graph.addEdge(3, 4, 1); // Créneau Mardi → Salle A
        graph.addEdge(4, 5, 2); // Salle A → Sink (capacité 2 pour les 2 créneaux)

        int flow = scheduler.maxFlow(graph, 0, 5);

        assertEquals(2, flow, "Les 2 heures du cours doivent être planifiées");
    }

    // ─── Cas 6 : salle insuffisante (capacité bloquante) ─────────────────────

    @Test
    void testTimetableGraph_roomCapacityLimitsScheduling() {
        // Même graphe mais la salle n'a qu'une capacité de 1 vers le sink
        FlowNetwork graph = new FlowNetwork(6);
        graph.addEdge(0, 1, 2);
        graph.addEdge(1, 2, 1);
        graph.addEdge(1, 3, 1);
        graph.addEdge(2, 4, 1);
        graph.addEdge(3, 4, 1);
        graph.addEdge(4, 5, 1); // Salle → Sink limité à 1

        int flow = scheduler.maxFlow(graph, 0, 5);

        assertEquals(1, flow, "La salle saturée doit limiter le flot à 1");
    }

    // ─── Cas 7 : vérification que les arêtes résiduelles sont correctes ───────

    @Test
    void testResidualEdgesAfterFlow() {
        // S=0 ──10──► A=1 ──10──► T=2
        FlowNetwork graph = new FlowNetwork(3);
        graph.addEdge(0, 1, 10);
        graph.addEdge(1, 2, 10);

        scheduler.maxFlow(graph, 0, 2);

        // Après flot max, l'arête S→A doit être saturée (résiduel = 0)
        FlowEdge sToA = graph.getEdges(0).get(0);
        assertEquals(0, sToA.getResidualCapacity(), "L'arête S→A doit être saturée");

        // L'arête résiduelle A→S doit avoir capacité 10
        FlowEdge aToS = graph.getEdges(0).get(0).getResidual();
        assertEquals(10, aToS.getResidualCapacity(), "L'arête résiduelle A→S doit valoir 10");
    }
}
