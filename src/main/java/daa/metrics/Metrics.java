package daa.metrics;

public final class Metrics {
    public long dsuOps;
    public long edgeScans;
    public long comparisons;
    public int maxDepth;

    public void depth(int current) {
        if (current > maxDepth) maxDepth = current;
    }

    public void reset() {
        dsuOps = 0;
        edgeScans = 0;
        comparisons = 0;
        maxDepth = 0;
    }

    public boolean sameCounts(Metrics o) {
        return dsuOps == o.dsuOps && edgeScans == o.edgeScans
                && comparisons == o.comparisons && maxDepth == o.maxDepth;
    }

    @Override
    public String toString() {
        return "dsuOps=" + dsuOps + " edgeScans=" + edgeScans + " comparisons=" + comparisons + " maxDepth=" + maxDepth;
    }
}
