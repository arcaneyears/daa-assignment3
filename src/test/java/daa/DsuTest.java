package daa;

import daa.dsu.DSU;
import daa.metrics.Metrics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DsuTest {
    private static final class NaiveSets {
        final int[] label;

        NaiveSets(int n) {
            label = new int[n];
            for (int i = 0; i < n; i++) label[i] = i;
        }

        boolean union(int a, int b) {
            int la = label[a], lb = label[b];
            if (la == lb) return false;
            for (int i = 0; i < label.length; i++) if (label[i] == lb) label[i] = la;
            return true;
        }

        boolean same(int a, int b) {
            return label[a] == label[b];
        }

        int sets() {
            Set<Integer> s = new HashSet<>();
            for (int l : label) s.add(l);
            return s.size();
        }
    }

    @Test
    @DisplayName("after random unions find() agrees with a naive reference and components() matches")
    void agreesWithReference() {
        Random rnd = new Random(7);
        for (int trial = 0; trial < 50; trial++) {
            int n = 1 + rnd.nextInt(300);
            DSU dsu = new DSU(n);
            NaiveSets ref = new NaiveSets(n);
            int ops = rnd.nextInt(2 * n + 1);
            for (int k = 0; k < ops; k++) {
                int a = rnd.nextInt(n), b = rnd.nextInt(n);
                assertEquals(ref.union(a, b), dsu.union(a, b), "union result differs");
            }
            for (int a = 0; a < n; a++) {
                for (int b = 0; b < n; b += 1 + rnd.nextInt(5)) {
                    assertEquals(ref.same(a, b), dsu.find(a) == dsu.find(b), "find differs for " + a + "," + b);
                }
            }
            assertEquals(ref.sets(), dsu.components());
        }
    }

    @Test
    @DisplayName("counters: every find call and every successful union is counted")
    void countersInsideOperations() {
        Metrics m = new Metrics();
        DSU dsu = new DSU(4, m);
        dsu.find(0);
        assertTrue(dsu.union(0, 1));
        assertFalse(dsu.union(1, 0));
        assertEquals(5, dsu.findCalls());
        assertEquals(1, dsu.unionCalls());
        assertEquals(6, m.dsuOps);
        assertEquals(3, dsu.components());
    }

    @Test
    @DisplayName("makeSet resets to singletons; long chain of unions stays shallow (rank + compression)")
    void makeSetAndChain() {
        int n = 200_000;
        DSU dsu = new DSU(n);
        for (int i = 1; i < n; i++) dsu.union(i - 1, i);
        assertEquals(1, dsu.components());
        int root = dsu.find(0);
        for (int i = 0; i < n; i++) assertEquals(root, dsu.find(i));

        assertTrue(dsu.findCalls() < 5L * n);
        dsu.makeSet(10);
        assertEquals(10, dsu.components());
        assertEquals(10, dsu.size());
    }
}
