package daa.scc;

import daa.graph.Edge;
import daa.graph.Graph;
import daa.metrics.Metrics;

import java.util.List;

public final class Kosaraju {
    private Kosaraju() {
    }

    public static SccResult run(Graph g) {
        return run(g, new Metrics());
    }

    public static SccResult run(Graph g, Metrics m) {
        if (!g.isDirected()) throw new IllegalArgumentException("SCC needs a directed graph");
        int n = g.n();

        int[] order = new int[n];
        int[] filled = {0};
        boolean[] visited = new boolean[n];
        int[] stack = new int[n];
        int[] next = new int[n];
        for (int s = 0; s < n; s++) {
            if (!visited[s]) dfs(g, s, visited, stack, next, m, order, filled, null, 0);
        }

        Graph rev = g.reverse();
        int[] comp = new int[n];
        java.util.Arrays.fill(visited, false);
        java.util.Arrays.fill(next, 0);
        int count = 0;
        for (int i = n - 1; i >= 0; i--) {
            int s = order[i];
            if (!visited[s]) dfs(rev, s, visited, stack, next, m, null, null, comp, count++);
        }
        return new SccResult(comp, count, m);
    }

    private static void dfs(Graph g, int s, boolean[] visited, int[] stack, int[] next, Metrics m,
                            int[] order, int[] filled, int[] comp, int label) {
        int top = 0;
        stack[top++] = s;
        visited[s] = true;
        if (comp != null) comp[s] = label;
        m.depth(top);
        while (top > 0) {
            int u = stack[top - 1];
            List<Edge> out = g.adj(u);
            if (next[u] < out.size()) {
                Edge e = out.get(next[u]++);
                m.edgeScans++;
                int v = e.v;
                if (!visited[v]) {
                    visited[v] = true;
                    if (comp != null) comp[v] = label;
                    stack[top++] = v;
                    m.depth(top);
                }
            } else {
                top--;
                if (order != null) order[filled[0]++] = u;
            }
        }
    }
}
