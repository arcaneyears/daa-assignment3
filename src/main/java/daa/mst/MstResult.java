package daa.mst;

import daa.graph.Edge;
import daa.metrics.Metrics;

import java.util.List;

public record MstResult(List<Edge> edges, long totalWeight, int components, Metrics metrics) {
    public boolean isSpanningTree() {
        return components == 1;
    }
}
