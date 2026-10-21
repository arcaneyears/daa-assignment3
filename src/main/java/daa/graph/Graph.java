package daa.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Graph {
    private final int n;
    private final boolean directed;
    private final List<Edge> edges = new ArrayList<>();
    private final List<List<Edge>> adj;

    public Graph(int n, boolean directed) {
        if (n < 0) throw new IllegalArgumentException("n must be >= 0, got " + n);
        this.n = n;
        this.directed = directed;
        this.adj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
    }

    public void addEdge(int u, int v, int w) {
        checkVertex(u);
        checkVertex(v);
        Edge e = new Edge(u, v, w);
        edges.add(e);
        adj.get(u).add(e);
        if (!directed && u != v) adj.get(v).add(e);
    }

    public List<Edge> adj(int u) {
        return adj.get(u);
    }

    public List<Edge> edges() {
        return Collections.unmodifiableList(edges);
    }

    public int n() {
        return n;
    }

    public int m() {
        return edges.size();
    }

    public boolean isDirected() {
        return directed;
    }

    public Graph reverse() {
        if (!directed) throw new IllegalStateException("reverse() is defined for directed graphs");
        Graph r = new Graph(n, true);
        for (Edge e : edges) r.addEdge(e.v, e.u, e.w);
        return r;
    }

    public int[] inDegrees() {
        int[] in = new int[n];
        for (Edge e : edges) in[e.v]++;
        return in;
    }

    private void checkVertex(int x) {
        if (x < 0 || x >= n) throw new IllegalArgumentException("vertex " + x + " not in 0.." + (n - 1));
    }

    @Override
    public String toString() {
        return (directed ? "directed" : "undirected") + " graph n=" + n + " m=" + m();
    }
}
