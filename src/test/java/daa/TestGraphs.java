package daa;

import daa.graph.Graph;
import daa.graph.GraphIO;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Random;

public final class TestGraphs {
    private TestGraphs() {
    }

    public static Graph load(String name) {
        try {
            return GraphIO.read(Path.of("data", name));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Graph randomConnected(Random rnd, int n, int extra, int maxW) {
        Graph g = new Graph(n, false);
        for (int i = 1; i < n; i++) g.addEdge(rnd.nextInt(i), i, 1 + rnd.nextInt(maxW));
        for (int k = 0; k < extra; k++) g.addEdge(rnd.nextInt(n), rnd.nextInt(n), 1 + rnd.nextInt(maxW));
        return g;
    }

    public static Graph randomDirected(Random rnd, int n, int m) {
        Graph g = new Graph(n, true);
        for (int k = 0; k < m; k++) g.addEdge(rnd.nextInt(n), rnd.nextInt(n), 1 + rnd.nextInt(100));
        return g;
    }

    public static Graph randomDag(Random rnd, int n, int m, int lo, int hi) {
        int[] rank = new int[n];
        for (int i = 0; i < n; i++) rank[i] = i;
        for (int i = n - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int t = rank[i];
            rank[i] = rank[j];
            rank[j] = t;
        }
        Graph g = new Graph(n, true);
        if (n < 2) return g;
        for (int k = 0; k < m; k++) {
            int a = rnd.nextInt(n);
            int b = rnd.nextInt(n);
            if (a == b) continue;
            if (rank[a] > rank[b]) {
                int t = a;
                a = b;
                b = t;
            }
            g.addEdge(a, b, lo + rnd.nextInt(hi - lo + 1));
        }
        return g;
    }

    public static Graph path(int n, boolean closeCycle) {
        Graph g = new Graph(n, true);
        for (int i = 0; i + 1 < n; i++) g.addEdge(i, i + 1, 1);
        if (closeCycle && n > 1) g.addEdge(n - 1, 0, 1);
        return g;
    }
}
