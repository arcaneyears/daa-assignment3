package daa;

import daa.graph.Edge;
import daa.graph.Graph;
import daa.scc.Kosaraju;
import daa.scc.SccResult;
import daa.scc.Tarjan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SccTest {
    @Test
    @DisplayName("Kosaraju and Tarjan match brute force (BFS from every vertex, both directions), n <= 200")
    void matchesBruteForce() {
        Random rnd = new Random(31);
        for (int t = 0; t < 60; t++) {
            int n = 1 + rnd.nextInt(200);
            int m = rnd.nextInt(3 * n + 1);
            Graph g = TestGraphs.randomDirected(rnd, n, m);
            int[] expected = bruteForceCanonical(g);
            SccResult k = Kosaraju.run(g);
            SccResult tj = Tarjan.run(g);
            assertArrayEquals(expected, k.canonical(), "Kosaraju, graph #" + t);
            assertArrayEquals(expected, tj.canonical(), "Tarjan, graph #" + t);
            assertEquals(k.count(), tj.count());
            assertEquals(2L * m, k.metrics().edgeScans, "Kosaraju scans 2m");
            assertEquals(m, tj.metrics().edgeScans, "Tarjan scans m");
        }
    }

    @Test
    @DisplayName("data/cyclic.txt: components {0,1,2}, {3,4,5}, {6}, {7}")
    void cyclicFile() {
        Graph g = TestGraphs.load("cyclic.txt");
        SccResult r = Kosaraju.run(g);
        assertEquals(4, r.count());
        assertArrayEquals(new int[]{0, 0, 0, 3, 3, 3, 6, 7}, r.canonical());
        assertArrayEquals(r.canonical(), Tarjan.run(g).canonical());
    }

    @Test
    @DisplayName("Stack safety: SCC on a path of 100 000 vertices, on a thread with a 256 KB stack")
    void stackSafety() throws InterruptedException {
        int n = 100_000;
        Graph path = TestGraphs.path(n, false);
        Graph cycle = TestGraphs.path(n, true);
        AtomicReference<Throwable> error = new AtomicReference<>();
        int[] counts = new int[4];
        int[] depth = new int[1];

        Thread worker = new Thread(null, () -> {
            try {
                SccResult a = Kosaraju.run(path);
                counts[0] = a.count();
                depth[0] = a.metrics().maxDepth;
                counts[1] = Tarjan.run(path).count();
                counts[2] = Kosaraju.run(cycle).count();
                counts[3] = Tarjan.run(cycle).count();
            } catch (Throwable e) {
                error.set(e);
            }
        }, "small-stack", 256 * 1024);
        worker.start();
        worker.join();
        assertNull(error.get(), () -> "failed with " + error.get());
        assertEquals(n, counts[0]);
        assertEquals(n, counts[1]);
        assertEquals(1, counts[2]);
        assertEquals(1, counts[3]);
        assertEquals(n, depth[0]);
    }

    @Test
    @DisplayName("maxDepth is recorded and bounded by n")
    void maxDepthRecorded() {
        Graph g = TestGraphs.randomDirected(new Random(3), 500, 1500);
        SccResult r = Kosaraju.run(g);
        assertTrue(r.metrics().maxDepth >= 1 && r.metrics().maxDepth <= g.n());
    }

    static int[] bruteForceCanonical(Graph g) {
        int n = g.n();
        boolean[][] reach = new boolean[n][];
        for (int s = 0; s < n; s++) reach[s] = bfs(g, s);
        int[] canon = new int[n];
        Arrays.fill(canon, -1);
        for (int u = 0; u < n; u++) {
            if (canon[u] != -1) continue;
            for (int v = u; v < n; v++) {
                if (canon[v] == -1 && reach[u][v] && reach[v][u]) canon[v] = u;
            }
        }
        return canon;
    }

    private static boolean[] bfs(Graph g, int s) {
        boolean[] seen = new boolean[g.n()];
        ArrayDeque<Integer> q = new ArrayDeque<>();
        seen[s] = true;
        q.add(s);
        while (!q.isEmpty()) {
            int u = q.poll();
            for (Edge e : g.adj(u)) {
                if (!seen[e.v]) {
                    seen[e.v] = true;
                    q.add(e.v);
                }
            }
        }
        return seen;
    }
}
