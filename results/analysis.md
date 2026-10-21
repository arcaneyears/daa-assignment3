
**sparse graphs (m = 4n)**

| algorithm | ratio | n=1 000 | n=3 162 | n=10 000 | n=31 623 | n=100 000 | spread |
|---|---|---:|---:|---:|---:|---:|---:|
| Kruskal | dsuOps / E | 2.290 | 2.519 | 2.682 | 2.732 | 2.748 | 17.7% |
| Kruskal | dsuOps / edgeScans | 2.972 | 2.848 | 2.776 | 2.757 | 2.751 | 7.9% |
| Kruskal | comparisons / (E log2 E) | 0.896 | 0.928 | 0.940 | 0.926 | 0.949 | 5.7% |
| Prim | edgeScans / E | 1.999 | 2.000 | 2.000 | 2.000 | 2.000 | 0.0% |
| Prim | comparisons / (E log2 V) | 1.857 | 2.027 | 2.140 | 2.169 | 2.167 | 15.1% |
| Kosaraju | edgeScans / (V + E) | 1.600 | 1.600 | 1.600 | 1.600 | 1.600 | 0.0% |
| Kosaraju | edgeScans / E | 2.000 | 2.000 | 2.000 | 2.000 | 2.000 | 0.0% |
| Kahn | edgeScans / (V + E) | 0.800 | 0.800 | 0.800 | 0.800 | 0.800 | 0.0% |
| Kahn | edgeScans / E | 1.000 | 1.000 | 1.000 | 1.000 | 1.000 | 0.0% |
| DagShortestPath | edgeScans / (V + E) | 0.800 | 0.800 | 0.800 | 0.800 | 0.800 | 0.0% |
| DagShortestPath | comparisons / E | 1.000 | 1.000 | 1.000 | 1.000 | 1.000 | 0.0% |

**dense graphs (m = 20n)**

| algorithm | ratio | n=1 000 | n=3 162 | n=10 000 | n=31 623 | n=100 000 | spread |
|---|---|---:|---:|---:|---:|---:|---:|
| Kruskal | dsuOps / E | 0.580 | 0.606 | 0.565 | 0.754 | 0.894 | 48.4% |
| Kruskal | dsuOps / edgeScans | 2.697 | 2.657 | 2.723 | 2.497 | 2.403 | 12.3% |
| Kruskal | comparisons / (E log2 E) | 0.935 | 0.922 | 0.946 | 0.953 | 0.942 | 3.3% |
| Prim | edgeScans / E | 1.998 | 1.999 | 2.000 | 2.000 | 2.000 | 0.1% |
| Prim | comparisons / (E log2 V) | 0.809 | 0.794 | 0.705 | 0.902 | 1.034 | 38.8% |
| Kosaraju | edgeScans / (V + E) | 1.905 | 1.905 | 1.905 | 1.905 | 1.905 | 0.0% |
| Kosaraju | edgeScans / E | 2.000 | 2.000 | 2.000 | 2.000 | 2.000 | 0.0% |
| Kahn | edgeScans / (V + E) | 0.952 | 0.952 | 0.952 | 0.952 | 0.952 | 0.0% |
| Kahn | edgeScans / E | 1.000 | 1.000 | 1.000 | 1.000 | 1.000 | 0.0% |
| DagShortestPath | edgeScans / (V + E) | 0.952 | 0.952 | 0.952 | 0.952 | 0.952 | 0.0% |
| DagShortestPath | comparisons / E | 1.000 | 1.000 | 1.000 | 1.000 | 1.000 | 0.0% |

**log-log slopes (least squares over the five sizes)**

| algorithm | counter | slope sparse | slope dense |
|---|---|---:|---:|
| Kruskal | edgeScans | 1.055 | 1.119 |
| Kruskal | total work (sort comparisons + dsuOps + scans) | 1.093 | 1.088 |
| Prim | edgeScans | 1.000 | 1.000 |
| Prim | total work (PQ comparisons + scans) | 1.133 | 1.140 |
| Kosaraju | edgeScans | 1.000 | 1.000 |
| Kahn | edgeScans | 1.000 | 1.000 |
| DagShortestPath | edgeScans | 1.000 | 1.000 |

**median time (ms), 5 runs**

| algorithm | 1000/s | 1000/d | 3162/s | 3162/d | 10000/s | 10000/d | 31623/s | 31623/d | 100000/s | 100000/d |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| Kruskal | 0.6 | 2.8 | 1.8 | 12.5 | 10.8 | 45.6 | 28.3 | 197.1 | 107.1 | 918.7 |
| Prim | 0.7 | 2.4 | 2.6 | 10.7 | 11.6 | 41.7 | 56.5 | 202.8 | 256.9 | 1020.0 |
| Kosaraju | 0.4 | 1.5 | 1.1 | 5.8 | 5.4 | 18.0 | 22.1 | 98.1 | 122.3 | 592.5 |
| Kahn | 0.2 | 0.3 | 0.4 | 1.2 | 1.1 | 3.5 | 4.4 | 13.9 | 17.8 | 42.5 |
| DagShortestPath | 0.3 | 0.3 | 0.2 | 1.2 | 1.0 | 3.0 | 3.8 | 12.5 | 27.8 | 36.6 |

**Bonus A: Kosaraju vs Tarjan edge scans**

| graph | Kosaraju scans | Tarjan scans | Kosaraju ms | Tarjan ms | SCCs |
|---|---:|---:|---:|---:|---:|
| n1000_sparse | 8000 | 4000 | 0.4 | 0.5 | 27 |
| n1000_dense | 40000 | 20000 | 1.5 | 0.2 | 1 |
| n3162_sparse | 25296 | 12648 | 1.1 | 0.2 | 137 |
| n3162_dense | 126480 | 63240 | 5.8 | 1.5 | 1 |
| n10000_sparse | 80000 | 40000 | 5.4 | 1.4 | 373 |
| n10000_dense | 400000 | 200000 | 18.0 | 4.6 | 1 |
| n31623_sparse | 252984 | 126492 | 22.1 | 7.0 | 1188 |
| n31623_dense | 1264920 | 632460 | 98.1 | 22.6 | 1 |
| n100000_sparse | 800000 | 400000 | 122.3 | 31.2 | 3923 |
| n100000_dense | 4000000 | 2000000 | 592.5 | 99.9 | 1 |
