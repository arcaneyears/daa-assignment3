# DAA Assignment 3 — City Network & Job Scheduler

Yersaiyn Taubay · SE-2524

Graph algorithms written from scratch in Java (only `java.util` collections are used):
Disjoint Set Union, Kruskal, Prim, Kosaraju, Kahn, shortest paths in a DAG, plus the bonus
tasks Tarjan's SCC and condensation + critical path. Every algorithm counts its own work
(`dsuOps`, `edgeScans`, `comparisons`, `maxDepth`), and a self-checking benchmark reproduces
the reference table exactly. The analysis is in [REPORT.md](REPORT.md).

## Requirements

- JDK 17 or newer (tested with OpenJDK 21)
- Maven 3.8+
- Optional, only to redraw the plots: Python 3 with `matplotlib`

## Build and test

```bash
mvn clean test          # compiles and runs all JUnit 5 tests (32 tests)
mvn clean package       # same + builds target/daa-assignment3-1.0.jar
```

## Run the benchmark (one command)

```bash
mvn -q compile exec:exec
```

or, after `mvn package`:

```bash
java -Xmx3g -jar target/daa-assignment3-1.0.jar            # writes results/
java -Xmx3g -jar target/daa-assignment3-1.0.jar out 3      # other folder, 3 runs instead of 5
```

The benchmark generates the 10 graphs with `new Random(42)`, runs every algorithm 5 times
(median time), and **fails with an exception** if:

- Kruskal and Prim return different MST weights,
- any MST weight, SCC count or DAG distance sum differs from the reference table,
- Kosaraju does not scan exactly 2m edges, or Kahn / the DAG relaxation not exactly m,
- Kahn's order is not a valid topological order of all n vertices,
- Tarjan finds a different partition than Kosaraju,
- the counters differ between the 5 runs.

Expected console output (about 25 s, ~3 GB heap for the 2 000 000-edge graphs):

```
n          m                  MST weight      SCC count   DAG dist sum   status
1000       4000                   147970             27         396274       OK
1000       20000                   30363              1         252411       OK
...
100000     2000000               3056681              1       23512087       OK
All checks passed: ...
```

Outputs:

| File | Content |
|---|---|
| `results/results.csv` | 50 rows: 5 algorithms × 10 graphs, columns `algorithm,graph,n,m,time_ms,dsu_ops,edge_scans,comparisons,max_depth,result` |
| `results/bonus_tarjan.csv` | Kosaraju vs Tarjan on the 10 directed graphs |
| `results/bonus_critical_path.csv` | condensation size and critical paths |

## Plots and ratio tables

```bash
pip install matplotlib
python3 scripts/analyze.py      # reads results/results.csv
```

writes `results/plots/1_operations_vs_n.png`, `2_scans_per_edge.png`, `3_kruskal_edge_share.png`
and `results/analysis.md` (counter/formula ratios, log-log slopes, times).

## Project layout

```
src/main/java/daa/
  graph/      Edge, Graph (adjacency list, directed + undirected), GraphIO (file format), GraphGenerator
  dsu/        DSU (union by rank + path compression, counters)
  mst/        Kruskal, Prim, EdgeSort (merge sort with comparison counter), MstResult
  scc/        Kosaraju (iterative), Tarjan (bonus A), Condensation + critical path (bonus B), SccResult
  topo/       Kahn (+ order validator), TopoResult
  paths/      DagShortestPath (shortest + longest), PathResult (parent[] path restoring)
  metrics/    Metrics (dsuOps, edgeScans, comparisons, maxDepth)
  benchmark/  Benchmark (self-checking, writes the CSVs)
src/test/java/daa/   JUnit 5 tests
data/                small input graphs used by the tests
results/             results.csv, bonus CSVs, analysis.md, plots/
scripts/analyze.py   plots + ratio tables
```

### Input format

```
n m directed|undirected
u v w        (m lines, vertices 0..n-1, int weights)
```

Lines starting with `#` are comments. Files in `data/`:

| File | Kind | Used for |
|---|---|---|
| `small_connected.txt` | undirected, self-loop + parallel edge | MST = 11 |
| `disconnected.txt` | undirected, 2 parts + 2 isolated vertices | spanning forest, 4 components |
| `cyclic.txt` | directed with cycles | SCCs, cycle detection in Kahn, condensation |
| `dag.txt` | DAG with negative weights | shortest paths, path restoring |

## Tests

| Test class | What it checks |
|---|---|
| `GraphTest` | file input, malformed input, self-loops / parallel edges / isolated / empty graphs, generator |
| `DsuTest` | random unions vs a naive reference, `components()`, counters, long chains |
| `MstTest` | 100 random connected graphs: Kruskal == Prim, n − 1 edges, acyclic; brute force on tiny graphs; forests |
| `SccTest` | Kosaraju and Tarjan vs brute force (BFS from every vertex, both directions), n ≤ 200; **stack safety on a 100 000-vertex path in a 256 KB-stack thread** |
| `TopoTest` | valid order on random DAGs, cycle detection, validator |
| `DagShortestPathTest` | vs Bellman-Ford on random DAGs with negative weights, path restoring, super-source |
| `BonusTest` | Tarjan == Kosaraju, condensation is a DAG, critical path vs brute force |
| `ReferenceValuesTest` | reference table for the four smallest generated graphs |

## Git

Branches `feature/dsu`, `feature/mst`, `feature/scc`, `feature/paths`, `feature/metrics` were merged into
`main` with `--no-ff`; the release is tagged `v1.0`.

## AI use acknowledgement

I used Claude (Anthropic) as an assistant while working on this assignment: to discuss the design,
to help write and review the Java code, tests and benchmark, and to help draft the report text
and plotting script. I reviewed every file, ran the tests and the benchmark myself, and I can explain each
algorithm, the counters and the complexity table at the defense. All algorithms are implemented in this
repository; no graph library is used.
