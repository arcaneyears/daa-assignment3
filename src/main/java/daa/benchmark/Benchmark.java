package daa.benchmark;

import daa.graph.Graph;
import daa.graph.GraphGenerator;
import daa.metrics.Metrics;
import daa.mst.Kruskal;
import daa.mst.MstResult;
import daa.mst.Prim;
import daa.paths.DagShortestPath;
import daa.paths.PathResult;
import daa.scc.Condensation;
import daa.scc.Kosaraju;
import daa.scc.SccResult;
import daa.scc.Tarjan;
import daa.topo.Kahn;
import daa.topo.TopoResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public final class Benchmark {
    static final int[] SIZES = {1_000, 3_162, 10_000, 31_623, 100_000};
    static final int[] DENSITIES = {4, 20};

    static final long[][] REFERENCE = {
            {1_000, 4_000, 147_970, 27, 396_274},
            {1_000, 20_000, 30_363, 1, 252_411},
            {3_162, 12_648, 468_911, 137, 1_271_911},
            {3_162, 63_240, 97_589, 1, 723_157},
            {10_000, 40_000, 1_486_139, 373, 3_941_828},
            {10_000, 200_000, 309_550, 1, 2_381_342},
            {31_623, 126_492, 4_715_515, 1_188, 12_551_891},
            {31_623, 632_460, 968_517, 1, 7_376_612},
            {100_000, 400_000, 15_012_094, 3_923, 40_096_539},
            {100_000, 2_000_000, 3_056_681, 1, 23_512_087},
    };

    private static final String HEADER = "algorithm,graph,n,m,time_ms,dsu_ops,edge_scans,comparisons,max_depth,result";

    private Benchmark() {
    }

    record Row(String algorithm, String graph, int n, int m, double timeMs, Metrics metrics, long result) {
        String csv() {
            return String.format(Locale.ROOT, "%s,%s,%d,%d,%.3f,%d,%d,%d,%d,%d",
                    algorithm, graph, n, m, timeMs, metrics.dsuOps, metrics.edgeScans,
                    metrics.comparisons, metrics.maxDepth, result);
        }
    }

    record Run<T>(long value, T full) {
    }

    public static void main(String[] args) throws IOException {
        Path out = Path.of(args.length > 0 ? args[0] : "results");
        int runs = args.length > 1 ? Integer.parseInt(args[1]) : 5;
        Files.createDirectories(out);

        warmUp();

        List<Row> rows = new ArrayList<>();
        List<Row> bonus = new ArrayList<>();
        List<String> critical = new ArrayList<>();

        System.out.printf("%-10s %-14s %14s %14s %14s %8s%n", "n", "m", "MST weight", "SCC count", "DAG dist sum", "status");
        for (long[] ref : REFERENCE) {
            int n = (int) ref[0];
            int m = (int) ref[1];
            String name = "n" + n + "_" + (m == 4 * n ? "sparse" : "dense");

            Graph und = GraphGenerator.undirected(n, m);
            Graph dir = GraphGenerator.directed(n, m);
            Graph dag = GraphGenerator.dag(n, m);
            check(und.m() == m && dir.m() == m && dag.m() == m, name + ": generator produced a wrong edge count");

            Row kr = measure("Kruskal", name, n, m, runs, met -> {
                MstResult r = Kruskal.run(und, met);
                return new Run<>(r.totalWeight(), r);
            });
            Row pr = measure("Prim", name, n, m, runs, met -> {
                MstResult r = Prim.run(und, met);
                return new Run<>(r.totalWeight(), r);
            });
            check(kr.result == pr.result, name + ": Kruskal weight " + kr.result + " != Prim weight " + pr.result);
            check(kr.result == ref[2], name + ": MST weight " + kr.result + " != reference " + ref[2]);
            check(kr.metrics.dsuOps > 0 && kr.metrics.edgeScans >= n - 1, name + ": Kruskal counters look wrong");

            SccResult[] kosa = new SccResult[1];
            Row ko = measure("Kosaraju", name, n, m, runs, met -> {
                SccResult r = Kosaraju.run(dir, met);
                kosa[0] = r;
                return new Run<>(r.count(), r);
            });
            check(ko.result == ref[3], name + ": SCC count " + ko.result + " != reference " + ref[3]);
            check(ko.metrics.edgeScans == 2L * m, name + ": Kosaraju scanned " + ko.metrics.edgeScans + " edges, expected 2m = " + 2L * m);

            TopoResult[] topo = new TopoResult[1];
            Row ka = measure("Kahn", name, n, m, runs, met -> {
                TopoResult r = Kahn.run(dag, met);
                topo[0] = r;
                return new Run<>(r.size(), r);
            });
            check(!topo[0].hasCycle() && ka.result == n, name + ": Kahn reported a cycle on a DAG");
            check(Kahn.isValidOrder(dag, topo[0].order()), name + ": Kahn order violates pos[u] < pos[v]");
            check(ka.metrics.edgeScans == m, name + ": Kahn scanned " + ka.metrics.edgeScans + " edges, expected m = " + m);

            int[] order = topo[0].order();
            Row sp = measure("DagShortestPath", name, n, m, runs, met -> {
                PathResult r = DagShortestPath.run(dag, order, met);
                return new Run<>(r.sum(), r);
            });
            check(sp.result == ref[4], name + ": DAG distance sum " + sp.result + " != reference " + ref[4]);
            check(sp.metrics.edgeScans == m, name + ": DAG relaxation scanned " + sp.metrics.edgeScans + " edges, expected m = " + m);

            rows.addAll(List.of(kr, pr, ko, ka, sp));

            SccResult[] tar = new SccResult[1];
            Row ta = measure("Tarjan", name, n, m, runs, met -> {
                SccResult r = Tarjan.run(dir, met);
                tar[0] = r;
                return new Run<>(r.count(), r);
            });
            check(Arrays.equals(tar[0].canonical(), kosa[0].canonical()), name + ": Tarjan and Kosaraju disagree on the components");
            check(ta.metrics.edgeScans == m, name + ": Tarjan scanned " + ta.metrics.edgeScans + " edges, expected m");
            bonus.add(ko);
            bonus.add(ta);

            Graph cond = Condensation.build(dir, kosa[0]);
            Condensation.CriticalPath cpCond = Condensation.criticalPath(cond);
            Condensation.CriticalPath cpDag = Condensation.criticalPath(dag);
            critical.add(String.format(Locale.ROOT, "%s,%d,%d,%d,%d,%d,%d,%d",
                    name, n, m, cond.n(), cond.m(), cpCond.length(), cpDag.length(), cpDag.path().size()));

            System.out.printf(Locale.ROOT, "%-10d %-14d %14d %14d %14d %8s%n", n, m, kr.result, ko.result, sp.result, "OK");
        }

        writeCsv(out.resolve("results.csv"), HEADER, rows.stream().map(Row::csv).toList());
        writeCsv(out.resolve("bonus_tarjan.csv"), HEADER, bonus.stream().map(Row::csv).toList());
        writeCsv(out.resolve("bonus_critical_path.csv"),
                "graph,n,m,condensation_vertices,condensation_edges,condensation_critical_path,dag_critical_path,dag_critical_path_vertices",
                critical);

        System.out.println();
        System.out.println("All checks passed: Kruskal == Prim, reference values reproduced, Kosaraju scans = 2m,");
        System.out.println("Kahn / DAG relaxation scan = m, topological orders valid, Tarjan == Kosaraju.");
        System.out.println("Wrote " + rows.size() + " rows to " + out.resolve("results.csv"));
    }

    private static <T> Row measure(String algo, String graph, int n, int m, int runs, Function<Metrics, Run<T>> body) {
        double[] times = new double[runs];
        Metrics first = null;
        long value = 0;
        for (int i = 0; i < runs; i++) {
            Metrics met = new Metrics();
            long t0 = System.nanoTime();
            Run<T> r = body.apply(met);
            times[i] = (System.nanoTime() - t0) / 1e6;
            if (first == null) {
                first = met;
                value = r.value();
            } else {
                check(first.sameCounts(met) && value == r.value(), algo + " on " + graph + ": counters differ between runs");
            }
        }
        Arrays.sort(times);
        return new Row(algo, graph, n, m, times[runs / 2], first, value);
    }

    private static void check(boolean ok, String message) {
        if (!ok) throw new IllegalStateException("BENCHMARK CHECK FAILED - " + message);
    }

    private static void writeCsv(Path file, String header, List<String> lines) throws IOException {
        StringBuilder sb = new StringBuilder(header).append('\n');
        for (String l : lines) sb.append(l).append('\n');
        Files.writeString(file, sb.toString(), StandardCharsets.UTF_8);
    }

    private static void warmUp() {
        Graph u = GraphGenerator.undirected(5_000, 40_000);
        Graph d = GraphGenerator.directed(5_000, 40_000);
        Graph a = GraphGenerator.dag(5_000, 40_000);
        for (int i = 0; i < 5; i++) {
            Kruskal.run(u);
            Prim.run(u);
            Kosaraju.run(d);
            Tarjan.run(d);
            DagShortestPath.run(a);
        }
    }
}
