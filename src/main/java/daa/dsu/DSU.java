package daa.dsu;

import daa.metrics.Metrics;

public final class DSU {
    private int[] parent;
    private int[] rank;
    private int sets;
    private long findCalls;
    private long unionCalls;
    private final Metrics metrics;

    public DSU(int n) {
        this(n, new Metrics());
    }

    public DSU(int n, Metrics metrics) {
        this.metrics = metrics;
        makeSet(n);
    }

    public void makeSet(int n) {
        parent = new int[n];
        rank = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;
        sets = n;
    }

    public int find(int x) {
        findCalls++;
        metrics.dsuOps++;
        int root = x;
        while (parent[root] != root) root = parent[root];
        while (parent[x] != root) {
            int next = parent[x];
            parent[x] = root;
            x = next;
        }
        return root;
    }

    public boolean union(int x, int y) {
        int rx = find(x);
        int ry = find(y);
        if (rx == ry) return false;
        if (rank[rx] < rank[ry]) {
            int t = rx;
            rx = ry;
            ry = t;
        }
        parent[ry] = rx;
        if (rank[rx] == rank[ry]) rank[rx]++;
        sets--;
        unionCalls++;
        metrics.dsuOps++;
        return true;
    }

    public boolean connected(int x, int y) {
        return find(x) == find(y);
    }

    public int components() {
        return sets;
    }

    public int size() {
        return parent.length;
    }

    public long findCalls() {
        return findCalls;
    }

    public long unionCalls() {
        return unionCalls;
    }
}
