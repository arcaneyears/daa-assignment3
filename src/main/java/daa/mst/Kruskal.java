package daa.mst;

import daa.dsu.DSU;
import daa.graph.Edge;
import daa.graph.Graph;
import daa.metrics.Metrics;

import java.util.ArrayList;
import java.util.List;

public final class Kruskal {
    private Kruskal() {
    }

    public static MstResult run(Graph g) {
        return run(g, new Metrics());
    }

    public static MstResult run(Graph g, Metrics m) {
        if (g.isDirected()) throw new IllegalArgumentException("MST needs an undirected graph");
        int n = g.n();
        Edge[] sorted = g.edges().toArray(new Edge[0]);
        EdgeSort.byWeight(sorted, m);

        DSU dsu = new DSU(n, m);
        List<Edge> tree = new ArrayList<>(Math.max(0, n - 1));
        long total = 0;
        for (Edge e : sorted) {
            if (tree.size() == n - 1) break;
            m.edgeScans++;
            if (dsu.find(e.u) != dsu.find(e.v)) {
                dsu.union(e.u, e.v);
                tree.add(e);
                total += e.w;
            }
        }

        int components = (n == 0) ? 0 : n - tree.size();
        return new MstResult(tree, total, components, m);
    }
}
