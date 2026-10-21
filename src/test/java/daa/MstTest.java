package daa;

import daa.dsu.DSU;
import daa.graph.Edge;
import daa.graph.Graph;
import daa.mst.Kruskal;
import daa.mst.MstResult;
import daa.mst.Prim;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MstTest {
    @Test
    @DisplayName("100 random connected graphs: Kruskal == Prim and the tree has exactly n - 1 edges")
    void kruskalEqualsPrim() {
        Random rnd = new Random(2024);
        for (int t = 0; t < 100; t++) {
            int n = 1 + rnd.nextInt(150);
            Graph g = TestGraphs.randomConnected(rnd, n, rnd.nextInt(4 * n + 1), 1 + rnd.nextInt(50));
            MstResult k = Kruskal.run(g);
            MstResult p = Prim.run(g);
            assertEquals(k.totalWeight(), p.totalWeight(), "graph #" + t);
            assertEquals(n - 1, k.edges().size());
            assertEquals(n - 1, p.edges().size());
            assertEquals(1, k.components());
            assertEquals(1, p.components());
            assertSpanningForest(g, k);
            assertSpanningForest(g, p);
        }
    }

    @Test
    @DisplayName("brute force on tiny graphs: the MST weight is the minimum over all spanning trees")
    void bruteForce() {
        Random rnd = new Random(5);
        for (int t = 0; t < 60; t++) {
            int n = 2 + rnd.nextInt(5);
            Graph g = TestGraphs.randomConnected(rnd, n, rnd.nextInt(8), 9);
            long best = bruteForceMst(g);
            assertEquals(best, Kruskal.run(g).totalWeight());
            assertEquals(best, Prim.run(g).totalWeight());
        }
    }

    @Test
    @DisplayName("data/small_connected.txt: weight 11 despite self-loop and parallel edge")
    void smallConnectedFile() {
        Graph g = TestGraphs.load("small_connected.txt");
        assertEquals(11, Kruskal.run(g).totalWeight());
        assertEquals(11, Prim.run(g).totalWeight());
    }

    @Test
    @DisplayName("disconnected graph returns a minimum spanning forest and the component count, no exception")
    void disconnectedForest() {
        Graph g = TestGraphs.load("disconnected.txt");
        MstResult k = assertDoesNotThrow(() -> Kruskal.run(g));
        MstResult p = assertDoesNotThrow(() -> Prim.run(g));
        assertEquals(5, k.totalWeight());
        assertEquals(5, p.totalWeight());
        assertEquals(4, k.components());
        assertEquals(4, p.components());
        assertEquals(g.n() - 4, k.edges().size());
        assertEquals(g.n() - 4, p.edges().size());

        Random rnd = new Random(99);
        for (int t = 0; t < 30; t++) {
            int parts = 1 + rnd.nextInt(5);
            int size = 1 + rnd.nextInt(20);
            Graph f = new Graph(parts * size, false);
            for (int c = 0; c < parts; c++) {
                Graph piece = TestGraphs.randomConnected(rnd, size, rnd.nextInt(30), 20);
                for (Edge e : piece.edges()) f.addEdge(c * size + e.u, c * size + e.v, e.w);
            }
            MstResult kk = Kruskal.run(f);
            MstResult pp = Prim.run(f);
            assertEquals(kk.totalWeight(), pp.totalWeight());
            assertEquals(parts, kk.components());
            assertEquals(parts, pp.components());
        }
    }

    @Test
    @DisplayName("Kruskal stops early: on a dense graph it examines only part of the edges")
    void kruskalEarlyStop() {
        Graph g = TestGraphs.randomConnected(new Random(1), 200, 8000, 1000);
        MstResult k = Kruskal.run(g);
        assertTrue(k.metrics().edgeScans < g.m(), "expected an early stop");
        assertTrue(k.metrics().edgeScans >= g.n() - 1);
        assertTrue(k.metrics().dsuOps > 0);
        assertTrue(k.metrics().comparisons > 0);
    }

    private static void assertSpanningForest(Graph g, MstResult r) {
        DSU dsu = new DSU(g.n());
        long sum = 0;
        for (Edge e : r.edges()) {
            assertTrue(dsu.union(e.u, e.v), "tree contains a cycle");
            sum += e.w;
        }
        assertEquals(r.totalWeight(), sum);
    }

    private static long bruteForceMst(Graph g) {
        List<Edge> edges = g.edges();
        int n = g.n(), m = edges.size();
        long best = Long.MAX_VALUE;
        for (int mask = 0; mask < (1 << m); mask++) {
            if (Integer.bitCount(mask) != n - 1) continue;
            DSU dsu = new DSU(n);
            long w = 0;
            boolean ok = true;
            for (int i = 0; i < m && ok; i++) {
                if ((mask >> i & 1) == 0) continue;
                ok = dsu.union(edges.get(i).u, edges.get(i).v);
                w += edges.get(i).w;
            }
            if (ok && dsu.components() == 1) best = Math.min(best, w);
        }
        return best;
    }
}
