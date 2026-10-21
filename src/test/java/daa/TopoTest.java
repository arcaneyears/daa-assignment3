package daa;

import daa.graph.Graph;
import daa.topo.Kahn;
import daa.topo.TopoResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TopoTest {
    @Test
    @DisplayName("random DAGs: Kahn returns all n vertices with pos[u] < pos[v] for every edge")
    void validOrderOnDag() {
        Random rnd = new Random(11);
        for (int t = 0; t < 100; t++) {
            int n = 1 + rnd.nextInt(300);
            Graph g = TestGraphs.randomDag(rnd, n, rnd.nextInt(4 * n + 1), 1, 100);
            TopoResult r = Kahn.run(g);
            assertFalse(r.hasCycle());
            assertEquals(n, r.size());
            assertTrue(Kahn.isValidOrder(g, r.order()));
            assertEquals(g.m(), r.metrics().edgeScans);
        }
    }

    @Test
    @DisplayName("cycle is detected instead of returning a wrong order")
    void detectsCycle() {
        Graph g = TestGraphs.load("cyclic.txt");
        TopoResult r = Kahn.run(g);
        assertTrue(r.hasCycle());
        assertTrue(r.size() < g.n());
        assertFalse(Kahn.isValidOrder(g, r.order()));

        Random rnd = new Random(12);
        for (int t = 0; t < 50; t++) {
            int n = 2 + rnd.nextInt(100);
            Graph dag = TestGraphs.randomDag(rnd, n, 3 * n, 1, 10);
            int[] order = Kahn.run(dag).order();
            Graph cyc = new Graph(n, true);
            dag.edges().forEach(e -> cyc.addEdge(e.u, e.v, e.w));
            for (int i = 0; i + 1 < n; i++) cyc.addEdge(order[i], order[i + 1], 1);
            cyc.addEdge(order[n - 1], order[0], 1);
            assertTrue(Kahn.run(cyc).hasCycle());
        }
    }

    @Test
    @DisplayName("validator rejects a wrong order and a non-permutation")
    void validatorWorks() {
        Graph g = TestGraphs.load("dag.txt");
        int[] good = Kahn.run(g).order();
        assertTrue(Kahn.isValidOrder(g, good));
        int[] reversed = new int[good.length];
        for (int i = 0; i < good.length; i++) reversed[i] = good[good.length - 1 - i];
        assertFalse(Kahn.isValidOrder(g, reversed));
        assertFalse(Kahn.isValidOrder(g, new int[]{0, 0, 1, 2, 3, 4}));
        assertFalse(Kahn.isValidOrder(g, new int[]{0, 1, 2}));
    }

    @Test
    @DisplayName("isolated vertices and a 100 000-vertex path are ordered correctly")
    void isolatedAndLong() {
        Graph g = new Graph(5, true);
        g.addEdge(3, 1, 1);
        TopoResult r = Kahn.run(g);
        assertEquals(5, r.size());
        assertTrue(Kahn.isValidOrder(g, r.order()));

        Graph path = TestGraphs.path(100_000, false);
        TopoResult p = Kahn.run(path);
        assertFalse(p.hasCycle());
        assertTrue(Kahn.isValidOrder(path, p.order()));
    }
}
