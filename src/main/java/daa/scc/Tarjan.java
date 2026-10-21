package daa.scc;

import daa.graph.Edge;
import daa.graph.Graph;
import daa.metrics.Metrics;

import java.util.Arrays;
import java.util.List;

public final class Tarjan {
    private Tarjan() {
    }

    public static SccResult run(Graph g) {
        return run(g, new Metrics());
    }

    public static SccResult run(Graph g, Metrics m) {
        if (!g.isDirected()) throw new IllegalArgumentException("SCC needs a directed graph");
        int n = g.n();
        int[] index = new int[n];
        int[] low = new int[n];
        Arrays.fill(index, -1);
        boolean[] onStack = new boolean[n];
        int[] active = new int[n];
        int activeTop = 0;
        int[] call = new int[n];
        int[] next = new int[n];
        int[] comp = new int[n];
        int counter = 0;
        int count = 0;

        for (int s = 0; s < n; s++) {
            if (index[s] != -1) continue;
            int top = 0;
            call[top++] = s;
            index[s] = low[s] = counter++;
            active[activeTop++] = s;
            onStack[s] = true;
            m.depth(top);

            while (top > 0) {
                int u = call[top - 1];
                List<Edge> out = g.adj(u);
                if (next[u] < out.size()) {
                    Edge e = out.get(next[u]++);
                    m.edgeScans++;
                    int v = e.v;
                    if (index[v] == -1) {
                        index[v] = low[v] = counter++;
                        active[activeTop++] = v;
                        onStack[v] = true;
                        call[top++] = v;
                        m.depth(top);
                    } else if (onStack[v]) {
                        low[u] = Math.min(low[u], index[v]);
                    }
                } else {
                    top--;
                    if (low[u] == index[u]) {
                        int w;
                        do {
                            w = active[--activeTop];
                            onStack[w] = false;
                            comp[w] = count;
                        } while (w != u);
                        count++;
                    }
                    if (top > 0) {
                        int parent = call[top - 1];
                        low[parent] = Math.min(low[parent], low[u]);
                    }
                }
            }
        }
        return new SccResult(comp, count, m);
    }
}
