package daa;

import daa.graph.Graph;
import daa.graph.GraphGenerator;
import daa.mst.Kruskal;
import daa.mst.Prim;
import daa.paths.DagShortestPath;
import daa.scc.Kosaraju;
import daa.scc.SccResult;
import daa.topo.Kahn;
import daa.topo.TopoResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReferenceValuesTest {
    private static final long[][] REF = {
            {1_000, 4_000, 147_970, 27, 396_274},
            {1_000, 20_000, 30_363, 1, 252_411},
            {3_162, 12_648, 468_911, 137, 1_271_911},
            {3_162, 63_240, 97_589, 1, 723_157},
    };

    @Test
    @DisplayName("MST weight, SCC count, DAG distance sum and the 2m / m scan identities")
    void reproducesReferenceTable() {
        for (long[] r : REF) {
            int n = (int) r[0], m = (int) r[1];
            Graph u = GraphGenerator.undirected(n, m);
            assertEquals(r[2], Kruskal.run(u).totalWeight(), "Kruskal " + n + "/" + m);
            assertEquals(r[2], Prim.run(u).totalWeight(), "Prim " + n + "/" + m);

            SccResult scc = Kosaraju.run(GraphGenerator.directed(n, m));
            assertEquals(r[3], scc.count(), "SCC " + n + "/" + m);
            assertEquals(2L * m, scc.metrics().edgeScans);

            Graph dag = GraphGenerator.dag(n, m);
            TopoResult topo = Kahn.run(dag);
            assertTrue(Kahn.isValidOrder(dag, topo.order()));
            assertEquals(m, topo.metrics().edgeScans);
            assertEquals(r[4], DagShortestPath.run(dag).sum(), "DAG " + n + "/" + m);
        }
    }
}
