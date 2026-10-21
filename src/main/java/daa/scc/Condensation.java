package daa.scc;

import daa.graph.Edge;
import daa.graph.Graph;
import daa.metrics.Metrics;
import daa.paths.DagShortestPath;
import daa.paths.PathResult;
import daa.topo.Kahn;
import daa.topo.TopoResult;

public final class Condensation {
    private Condensation() {
    }

    public static Graph build(Graph g, SccResult scc) {
        Graph c = new Graph(scc.count(), true);
        int[] comp = scc.comp();
        for (Edge e : g.edges()) {
            if (comp[e.u] != comp[e.v]) c.addEdge(comp[e.u], comp[e.v], e.w);
        }
        return c;
    }

    public record CriticalPath(long length, java.util.List<Integer> path, Graph dag) {
    }

    public static CriticalPath criticalPath(Graph dag) {
        TopoResult topo = Kahn.run(dag, new Metrics());
        if (topo.hasCycle()) throw new IllegalStateException("condensation must be acyclic");
        PathResult longest = DagShortestPath.longest(dag, topo.order(), new Metrics());
        int best = 0;
        for (int v = 1; v < dag.n(); v++) if (longest.dist()[v] > longest.dist()[best]) best = v;
        long len = dag.n() == 0 ? 0 : longest.dist()[best];
        return new CriticalPath(len, dag.n() == 0 ? java.util.List.of() : longest.pathTo(best), dag);
    }

    public static CriticalPath criticalPathOf(Graph g) {
        return criticalPath(build(g, Tarjan.run(g)));
    }
}
