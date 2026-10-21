package daa.topo;

import daa.graph.Edge;
import daa.graph.Graph;
import daa.metrics.Metrics;

import java.util.ArrayDeque;
import java.util.Arrays;

public final class Kahn {
    private Kahn() {
    }

    public static TopoResult run(Graph g) {
        return run(g, new Metrics());
    }

    public static TopoResult run(Graph g, Metrics m) {
        if (!g.isDirected()) throw new IllegalArgumentException("topological order needs a directed graph");
        int n = g.n();
        int[] in = g.inDegrees();
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        for (int v = 0; v < n; v++) if (in[v] == 0) queue.add(v);
        m.depth(queue.size());

        int[] order = new int[n];
        int k = 0;
        while (!queue.isEmpty()) {
            int u = queue.poll();
            order[k++] = u;
            for (Edge e : g.adj(u)) {
                m.edgeScans++;
                if (--in[e.v] == 0) queue.add(e.v);
            }
            m.depth(queue.size());
        }
        boolean cycle = k < n;
        return new TopoResult(cycle ? Arrays.copyOf(order, k) : order, cycle, m);
    }

    public static boolean isValidOrder(Graph g, int[] order) {
        int n = g.n();
        if (order.length != n) return false;
        int[] pos = new int[n];
        Arrays.fill(pos, -1);
        for (int i = 0; i < n; i++) {
            int v = order[i];
            if (v < 0 || v >= n || pos[v] != -1) return false;
            pos[v] = i;
        }
        for (Edge e : g.edges()) if (pos[e.u] >= pos[e.v]) return false;
        return true;
    }
}
