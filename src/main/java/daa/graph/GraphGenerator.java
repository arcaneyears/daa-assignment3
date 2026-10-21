package daa.graph;

import java.util.Random;

public final class GraphGenerator {
    public static final long SEED = 42L;

    private GraphGenerator() {
    }

    public static Graph undirected(int n, int m) {
        return undirected(n, m, new Random(SEED));
    }

    public static Graph undirected(int n, int m, Random rnd) {
        if (m < n - 1) throw new IllegalArgumentException("need m >= n - 1 for a connected graph");
        Graph g = new Graph(n, false);
        int[] perm = shuffled(n, rnd);
        for (int i = 1; i < n; i++) {
            int a = perm[rnd.nextInt(i)];
            int b = perm[i];
            g.addEdge(a, b, weight(rnd));
        }
        addRandomEdges(g, m - (n - 1), rnd, null);
        return g;
    }

    public static Graph directed(int n, int m) {
        return directed(n, m, new Random(SEED));
    }

    public static Graph directed(int n, int m, Random rnd) {
        Graph g = new Graph(n, true);
        addRandomEdges(g, m, rnd, null);
        return g;
    }

    public static Graph dag(int n, int m) {
        return dag(n, m, new Random(SEED));
    }

    public static Graph dag(int n, int m, Random rnd) {
        Graph g = new Graph(n, true);
        int[] rank = shuffled(n, rnd);
        addRandomEdges(g, m, rnd, rank);
        return g;
    }

    static int[] shuffled(int n, Random rnd) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = i;
        for (int i = n - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int t = a[i];
            a[i] = a[j];
            a[j] = t;
        }
        return a;
    }

    private static void addRandomEdges(Graph g, int count, Random rnd, int[] rank) {
        int n = g.n();
        for (int added = 0; added < count; ) {
            int a = rnd.nextInt(n);
            int b;
            do {
                b = rnd.nextInt(n);
            } while (b == a);
            if (rank != null && rank[a] > rank[b]) {
                int t = a;
                a = b;
                b = t;
            }
            g.addEdge(a, b, weight(rnd));
            added++;
        }
    }

    private static int weight(Random rnd) {
        return 1 + rnd.nextInt(1000);
    }
}
