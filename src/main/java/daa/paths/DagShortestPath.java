package daa.paths;

import daa.graph.Edge;
import daa.graph.Graph;
import daa.metrics.Metrics;
import daa.topo.Kahn;
import daa.topo.TopoResult;

public final class DagShortestPath {
    private DagShortestPath() {
    }

    public static PathResult run(Graph g) {
        TopoResult topo = Kahn.run(g, new Metrics());
        if (topo.hasCycle()) throw new IllegalArgumentException("graph has a cycle - not a DAG");
        return run(g, topo.order(), new Metrics());
    }

    public static PathResult run(Graph g, int[] topoOrder, Metrics m) {
        return relax(g, topoOrder, m, false);
    }

    public static PathResult longest(Graph g, int[] topoOrder, Metrics m) {
        return relax(g, topoOrder, m, true);
    }

    private static PathResult relax(Graph g, int[] order, Metrics m, boolean longest) {
        if (!g.isDirected()) throw new IllegalArgumentException("DAG paths need a directed graph");
        int n = g.n();
        if (order.length != n) throw new IllegalArgumentException("order must contain all n vertices");
        final long INF = Long.MAX_VALUE;
        int[] in = g.inDegrees();
        long[] dist = new long[n];
        int[] parent = new int[n];
        for (int v = 0; v < n; v++) {
            dist[v] = (in[v] == 0) ? 0 : INF;
            parent[v] = -1;
        }
        for (int u : order) {
            if (dist[u] == INF) continue;
            for (Edge e : g.adj(u)) {
                m.edgeScans++;
                long w = longest ? -(long) e.w : e.w;
                long cand = dist[u] + w;
                m.comparisons++;
                if (cand < dist[e.v]) {
                    dist[e.v] = cand;
                    parent[e.v] = u;
                }
            }
        }
        if (longest) for (int v = 0; v < n; v++) dist[v] = -dist[v];
        return new PathResult(dist, parent, m);
    }
}
