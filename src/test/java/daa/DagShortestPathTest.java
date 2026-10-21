package daa;

import daa.graph.Edge;
import daa.graph.Graph;
import daa.metrics.Metrics;
import daa.paths.DagShortestPath;
import daa.paths.PathResult;
import daa.topo.Kahn;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DagShortestPathTest {
    @Test
    @DisplayName("matches Bellman-Ford (with a super-source) on random DAGs, negative weights included")
    void matchesBellmanFord() {
        Random rnd = new Random(77);
        for (int t = 0; t < 200; t++) {
            int n = 1 + rnd.nextInt(60);
            Graph g = TestGraphs.randomDag(rnd, n, rnd.nextInt(4 * n + 1), -50, 50);
            PathResult r = DagShortestPath.run(g);
            assertArrayEquals(bellmanFord(g), r.dist(), "graph #" + t);
            assertRestoredPathsAreConsistent(g, r);
        }
    }

    @Test
    @DisplayName("one pass: exactly m edge scans and m comparisons")
    void onePass() {
        Graph g = TestGraphs.randomDag(new Random(8), 1000, 5000, -10, 10);
        int[] order = Kahn.run(g).order();
        PathResult r = DagShortestPath.run(g, order, new Metrics());
        assertEquals(g.m(), r.metrics().edgeScans);
        assertEquals(g.m(), r.metrics().comparisons);
    }

    @Test
    @DisplayName("data/dag.txt: distances 0 5 3 10 7 5, path to 4 is 0 -> 2 -> 4")
    void dagFile() {
        Graph g = TestGraphs.load("dag.txt");
        PathResult r = DagShortestPath.run(g);
        assertArrayEquals(new long[]{0, 5, 3, 10, 7, 5}, r.dist());
        assertEquals(30, r.sum());
        assertEquals(List.of(0, 2, 4), r.pathTo(4));
        assertEquals(List.of(0), r.pathTo(0));
    }

    @Test
    @DisplayName("every in-degree-0 vertex is a source with distance 0 (super-source)")
    void superSource() {
        Graph g = new Graph(5, true);
        g.addEdge(0, 2, 10);
        g.addEdge(1, 2, 3);
        g.addEdge(2, 3, -4);
        PathResult r = DagShortestPath.run(g);
        assertArrayEquals(new long[]{0, 0, 3, -1, 0}, r.dist());
        assertEquals(List.of(1, 2, 3), r.pathTo(3));
    }

    @Test
    @DisplayName("a cyclic graph is refused instead of producing wrong distances")
    void refusesCycle() {
        assertThrows(IllegalArgumentException.class, () -> DagShortestPath.run(TestGraphs.load("cyclic.txt")));
    }

    static long[] bellmanFord(Graph g) {
        int n = g.n();
        long inf = Long.MAX_VALUE / 4;
        long[] d = new long[n];
        int[] in = g.inDegrees();
        for (int v = 0; v < n; v++) d[v] = in[v] == 0 ? 0 : inf;
        for (int it = 0; it < n; it++) {
            boolean changed = false;
            for (Edge e : g.edges()) {
                if (d[e.u] < inf && d[e.u] + e.w < d[e.v]) {
                    d[e.v] = d[e.u] + e.w;
                    changed = true;
                }
            }
            if (!changed) break;
        }
        return d;
    }

    private static void assertRestoredPathsAreConsistent(Graph g, PathResult r) {
        int[] in = g.inDegrees();
        for (int v = 0; v < g.n(); v++) {
            List<Integer> p = r.pathTo(v);
            assertEquals(0, in[p.get(0)], "path must start at a source");
            long w = 0;
            for (int i = 0; i + 1 < p.size(); i++) w += cheapest(g, p.get(i), p.get(i + 1));
            assertEquals(r.dist()[v], w, "path weight to " + v);
        }
    }

    private static long cheapest(Graph g, int u, int v) {
        long best = Long.MAX_VALUE;
        for (Edge e : g.adj(u)) if (e.v == v) best = Math.min(best, e.w);
        if (best == Long.MAX_VALUE) throw new AssertionError("no edge " + u + " -> " + v);
        return best;
    }
}
