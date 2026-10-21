package daa.topo;

import daa.metrics.Metrics;

public record TopoResult(int[] order, boolean hasCycle, Metrics metrics) {
    public int size() {
        return order.length;
    }
}
