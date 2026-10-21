package daa.paths;

import daa.metrics.Metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record PathResult(long[] dist, int[] parent, Metrics metrics) {
    public long sum() {
        long s = 0;
        for (long d : dist) s += d;
        return s;
    }

    public List<Integer> pathTo(int target) {
        List<Integer> path = new ArrayList<>();
        for (int v = target; v != -1; v = parent[v]) path.add(v);
        Collections.reverse(path);
        return path;
    }
}
