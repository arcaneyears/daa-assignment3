package daa.scc;

import daa.metrics.Metrics;

import java.util.ArrayList;
import java.util.List;

public record SccResult(int[] comp, int count, Metrics metrics) {
    public List<List<Integer>> components() {
        List<List<Integer>> res = new ArrayList<>(count);
        for (int i = 0; i < count; i++) res.add(new ArrayList<>());
        for (int v = 0; v < comp.length; v++) res.get(comp[v]).add(v);
        return res;
    }

    public int[] canonical() {
        int[] minOf = new int[count];
        java.util.Arrays.fill(minOf, Integer.MAX_VALUE);
        for (int v = 0; v < comp.length; v++) minOf[comp[v]] = Math.min(minOf[comp[v]], v);
        int[] c = new int[comp.length];
        for (int v = 0; v < comp.length; v++) c[v] = minOf[comp[v]];
        return c;
    }
}
