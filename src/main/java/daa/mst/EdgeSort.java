package daa.mst;

import daa.graph.Edge;
import daa.metrics.Metrics;

public final class EdgeSort {
    private EdgeSort() {
    }

    public static void byWeight(Edge[] a, Metrics metrics) {
        int n = a.length;
        if (n < 2) return;
        Edge[] src = a;
        Edge[] dst = new Edge[n];
        for (int width = 1; width < n; width <<= 1) {
            for (int lo = 0; lo < n; lo += width << 1) {
                int mid = Math.min(lo + width, n);
                int hi = Math.min(lo + (width << 1), n);
                merge(src, dst, lo, mid, hi, metrics);
            }
            Edge[] t = src;
            src = dst;
            dst = t;
        }
        if (src != a) System.arraycopy(src, 0, a, 0, n);
    }

    private static void merge(Edge[] src, Edge[] dst, int lo, int mid, int hi, Metrics metrics) {
        int i = lo, j = mid, k = lo;
        while (i < mid && j < hi) {
            metrics.comparisons++;
            if (src[j].w < src[i].w) dst[k++] = src[j++];
            else dst[k++] = src[i++];
        }
        while (i < mid) dst[k++] = src[i++];
        while (j < hi) dst[k++] = src[j++];
    }
}
