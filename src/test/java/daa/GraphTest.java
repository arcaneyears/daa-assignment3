package daa;

import daa.graph.Edge;
import daa.graph.Graph;
import daa.graph.GraphGenerator;
import daa.graph.GraphIO;
import daa.mst.Kruskal;
import daa.mst.Prim;
import daa.paths.DagShortestPath;
import daa.scc.Kosaraju;
import daa.scc.Tarjan;
import daa.topo.Kahn;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphTest {
    @Test
    @DisplayName("data files are parsed: header, direction and edge count")
    void readsDataFiles() {
        Graph a = TestGraphs.load("small_connected.txt");
        assertEquals(5, a.n());
        assertEquals(8, a.m());
        assertFalse(a.isDirected());

        Graph b = TestGraphs.load("disconnected.txt");
        assertEquals(7, b.n());
        assertEquals(5, b.m());

        Graph c = TestGraphs.load("cyclic.txt");
        assertTrue(c.isDirected());
        assertEquals(9, c.m());

        Graph d = TestGraphs.load("dag.txt");
        assertEquals(6, d.n());
        assertEquals(9, d.m());
    }

    @Test
    @DisplayName("undirected edge appears in both adjacency lists, a self-loop once")
    void undirectedAdjacency() {
        Graph g = new Graph(3, false);
        g.addEdge(0, 1, 5);
        g.addEdge(2, 2, 1);
        assertEquals(1, g.adj(0).size());
        assertEquals(1, g.adj(1).size());
        assertEquals(1, g.adj(2).size());
        Edge e = g.adj(1).get(0);
        assertEquals(1, e.other(0));
        assertEquals(0, e.other(1));
    }

    @Test
    @DisplayName("write + read round trip keeps every edge")
    void roundTrip() throws IOException {
        Graph g = TestGraphs.load("dag.txt");
        Path tmp = Files.createTempFile("graph", ".txt");
        try {
            GraphIO.write(g, tmp);
            Graph h = GraphIO.read(tmp);
            assertEquals(g.n(), h.n());
            assertEquals(g.m(), h.m());
            assertEquals(g.isDirected(), h.isDirected());
            for (int i = 0; i < g.m(); i++) {
                assertEquals(g.edges().get(i).u, h.edges().get(i).u);
                assertEquals(g.edges().get(i).v, h.edges().get(i).v);
                assertEquals(g.edges().get(i).w, h.edges().get(i).w);
            }
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    @Test
    @DisplayName("malformed input is rejected with a clear exception")
    void badInput() {
        assertThrows(IllegalArgumentException.class, () -> GraphIO.parse("3 1 sideways\n0 1 1"));
        assertThrows(IllegalArgumentException.class, () -> GraphIO.parse("3 2 directed\n0 1 1"));
        assertThrows(IllegalArgumentException.class, () -> GraphIO.parse("3 1 directed\n0 5 1"));
        assertThrows(IllegalArgumentException.class, () -> GraphIO.parse(""));
    }

    @Test
    @DisplayName("self-loops, parallel edges, isolated vertices and empty graphs crash nothing")
    void edgeCasesDoNotCrash() {
        Graph und = GraphIO.parse("6 4 undirected\n0 0 3\n0 1 2\n0 1 1\n2 3 4");
        assertDoesNotThrow(() -> Kruskal.run(und));
        assertDoesNotThrow(() -> Prim.run(und));
        assertEquals(Kruskal.run(und).totalWeight(), Prim.run(und).totalWeight());

        Graph dir = GraphIO.parse("5 4 directed\n0 0 3\n0 1 2\n0 1 1\n1 2 4");
        assertDoesNotThrow(() -> Kosaraju.run(dir));
        assertDoesNotThrow(() -> Tarjan.run(dir));
        assertTrue(Kahn.run(dir).hasCycle());

        Graph dag = GraphIO.parse("5 3 directed\n0 1 2\n0 1 1\n1 2 4");
        assertDoesNotThrow(() -> DagShortestPath.run(dag));

        for (boolean directed : new boolean[]{true, false}) {
            Graph empty = new Graph(0, directed);
            Graph single = new Graph(1, directed);
            if (directed) {
                assertEquals(0, Kosaraju.run(empty).count());
                assertEquals(1, Kosaraju.run(single).count());
                assertEquals(1, Kahn.run(single).size());
                assertEquals(0, DagShortestPath.run(single).sum());
            } else {
                assertEquals(0, Kruskal.run(empty).totalWeight());
                assertEquals(1, Prim.run(single).components());
                assertEquals(1, Kruskal.run(single).components());
            }
        }
    }

    @Test
    @DisplayName("generator: exact m, connected undirected graph, no self-loops, DAG has no cycle")
    void generatorShape() {
        Graph u = GraphGenerator.undirected(500, 2000);
        Graph d = GraphGenerator.directed(500, 2000);
        Graph a = GraphGenerator.dag(500, 2000);
        assertEquals(2000, u.m());
        assertEquals(2000, d.m());
        assertEquals(2000, a.m());
        for (Edge e : d.edges()) assertTrue(e.u != e.v);
        for (Edge e : u.edges()) assertTrue(e.w >= 1 && e.w <= 1000);
        assertEquals(1, Kruskal.run(u).components());
        assertFalse(Kahn.run(a).hasCycle());

        Graph again = GraphGenerator.directed(500, 2000);
        for (int i = 0; i < d.m(); i++) {
            assertEquals(d.edges().get(i).u, again.edges().get(i).u);
            assertEquals(d.edges().get(i).w, again.edges().get(i).w);
        }
    }
}
