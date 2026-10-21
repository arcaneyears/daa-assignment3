package daa.mst;

import daa.graph.Edge;
import daa.graph.Graph;
import daa.metrics.Metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public final class Prim {
    private Prim() {
    }

    private record Item(int w, int to, Edge edge) {
    }

    public static MstResult run(Graph g) {
        return run(g, new Metrics());
    }

    public static MstResult run(Graph g, Metrics m) {
        if (g.isDirected()) throw new IllegalArgumentException("MST needs an undirected graph");
        int n = g.n();
        boolean[] inTree = new boolean[n];
        List<Edge> tree = new ArrayList<>(Math.max(0, n - 1));
        long total = 0;
        int components = 0;

        PriorityQueue<Item> pq = new PriorityQueue<>((a, b) -> {
            m.comparisons++;
            return Integer.compare(a.w, b.w);
        });

        for (int start = 0; start < n; start++) {
            if (inTree[start]) continue;
            components++;
            visit(g, start, inTree, pq, m);
            while (!pq.isEmpty()) {
                m.depth(pq.size());
                Item it = pq.poll();
                if (inTree[it.to]) continue;
                inTree[it.to] = true;
                tree.add(it.edge);
                total += it.w;
                if (tree.size() == n - 1) {
                    pq.clear();
                    break;
                }
                visit(g, it.to, inTree, pq, m);
            }
        }
        return new MstResult(tree, total, components, m);
    }

    private static void visit(Graph g, int u, boolean[] inTree, PriorityQueue<Item> pq, Metrics m) {
        inTree[u] = true;
        for (Edge e : g.adj(u)) {
            m.edgeScans++;
            int to = e.other(u);
            if (!inTree[to]) pq.add(new Item(e.w, to, e));
        }
    }
}
