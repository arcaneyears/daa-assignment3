package daa;

import daa.graph.Edge;
import daa.graph.Graph;
import daa.scc.Condensation;
import daa.scc.Kosaraju;
import daa.scc.SccResult;
import daa.scc.Tarjan;
import daa.topo.Kahn;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BonusTest {
    @Test
    @DisplayName("Tarjan returns the same components as Kosaraju on every data graph, with half the scans")
    void tarjanEqualsKosaraju() {
        for (String f : new String[]{"cyclic.txt", "dag.txt"}) {
            Graph g = TestGraphs.load(f);
            SccResult k = Kosaraju.run(g);
            SccResult t = Tarjan.run(g);
            assertArrayEquals(k.canonical(), t.canonical(), f);
            assertEquals(2 * t.metrics().edgeScans, k.metrics().edgeScans);
        }
    }

    @Test
    @DisplayName("the condensation of any directed graph is a DAG")
    void condensationIsDag() {
        Random rnd = new Random(4);
        for (int t = 0; t < 50; t++) {
            int n = 1 + rnd.nextInt(150);
            Graph g = TestGraphs.randomDirected(rnd, n, rnd.nextInt(3 * n + 1));
            SccResult scc = Tarjan.run(g);
            Graph c = Condensation.build(g, scc);
            assertEquals(scc.count(), c.n());
            assertFalse(Kahn.run(c).hasCycle());
        }
        Graph c = Condensation.build(TestGraphs.load("cyclic.txt"), Kosaraju.run(TestGraphs.load("cyclic.txt")));
        assertEquals(4, c.n());
        assertEquals(2, c.m());
    }

    @Test
    @DisplayName("critical path (longest path) matches brute force over all paths on small DAGs")
    void criticalPathBruteForce() {
        Random rnd = new Random(6);
        for (int t = 0; t < 100; t++) {
            int n = 1 + rnd.nextInt(12);
            Graph g = TestGraphs.randomDag(rnd, n, rnd.nextInt(3 * n + 1), 1, 20);
            Condensation.CriticalPath cp = Condensation.criticalPath(g);
            long best = 0;
            for (int s = 0; s < n; s++) best = Math.max(best, longestFrom(g, s));
            assertEquals(best, cp.length(), "graph #" + t);

            long w = 0;
            for (int i = 0; i + 1 < cp.path().size(); i++) {
                long e = Long.MIN_VALUE;
                for (Edge x : g.adj(cp.path().get(i))) if (x.v == cp.path().get(i + 1)) e = Math.max(e, x.w);
                assertTrue(e != Long.MIN_VALUE);
                w += e;
            }
            assertEquals(cp.length(), w);
        }
    }

    @Test
    @DisplayName("cyclic.txt: critical path of the condensation is {0,1,2} -> {3,4,5} -> {6}")
    void criticalPathOfCyclicFile() {
        Condensation.CriticalPath cp = Condensation.criticalPathOf(TestGraphs.load("cyclic.txt"));
        assertEquals(3, cp.path().size());
        assertEquals(4 + 7, cp.length());
    }

    private static long longestFrom(Graph g, int u) {
        long best = 0;
        for (Edge e : g.adj(u)) best = Math.max(best, e.w + longestFrom(g, e.v));
        return best;
    }
}
