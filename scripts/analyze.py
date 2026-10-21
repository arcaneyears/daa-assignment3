import csv
import math
import os
import sys

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "results")
PLOTS = os.path.join(RES, "plots")
os.makedirs(PLOTS, exist_ok=True)

ALGOS = ["Kruskal", "Prim", "Kosaraju", "Kahn", "DagShortestPath"]
COLOR = {"Kruskal": "#2a78d6", "Prim": "#eb6834", "Kosaraju": "#1baf7a",
         "Kahn": "#eda100", "DagShortestPath": "#e87ba4", "Tarjan": "#6250d6"}
STYLE = {"sparse": dict(ls="-", marker="o"), "dense": dict(ls="--", marker="s")}
HOLLOW = {"Kosaraju", "Kahn"}


def kw(a, d):
    k = dict(STYLE[d])
    if a in HOLLOW:
        k.update(ms=10, mfc="none", mew=1.8)
    else:
        k.update(ms=5)
    return k
INK, INK2, GRID = "#0b0b0b", "#52514e", "#e4e3df"


def load(name):
    with open(os.path.join(RES, name), newline="") as f:
        rows = list(csv.DictReader(f))
    for r in rows:
        for k in ("n", "m", "dsu_ops", "edge_scans", "comparisons", "max_depth", "result"):
            r[k] = int(r[k])
        r["time_ms"] = float(r["time_ms"])
        r["density"] = r["graph"].split("_")[1]
    return rows


def series(rows, algo, density, key):
    pts = sorted((r["n"], key(r)) for r in rows if r["algorithm"] == algo and r["density"] == density)
    return [p[0] for p in pts], [p[1] for p in pts]


def slope(xs, ys):
    lx = [math.log10(x) for x in xs]
    ly = [math.log10(y) for y in ys]
    mx, my = sum(lx) / len(lx), sum(ly) / len(ly)
    return sum((a - mx) * (b - my) for a, b in zip(lx, ly)) / sum((a - mx) ** 2 for a in lx)


def style_axes(ax, title, xlabel, ylabel):
    ax.set_title(title, color=INK, fontsize=12, loc="left", pad=10)
    ax.set_xlabel(xlabel, color=INK2)
    ax.set_ylabel(ylabel, color=INK2)
    ax.grid(True, which="major", color=GRID, lw=0.8)
    ax.tick_params(colors=INK2)
    for s in ("top", "right"):
        ax.spines[s].set_visible(False)
    for s in ("left", "bottom"):
        ax.spines[s].set_color(GRID)


def total_ops(r):
    return r["dsu_ops"] + r["edge_scans"] + r["comparisons"]


rows = load("results.csv")
plt.rcParams.update({"font.size": 10, "figure.facecolor": "#fcfcfb", "axes.facecolor": "#fcfcfb"})

fig, ax = plt.subplots(figsize=(8, 5.2), dpi=140)
for a in ALGOS:
    for d in ("sparse", "dense"):
        x, y = series(rows, a, d, total_ops)
        ax.plot(x, y, color=COLOR[a], lw=2, label=f"{a} ({d})", **kw(a, d))
ax.set_xscale("log")
ax.set_yscale("log")
style_axes(ax, "Operations vs n  (dsuOps + edgeScans + comparisons, log-log)", "n (vertices)", "operations")
ax.legend(fontsize=8, ncol=2, frameon=False, loc="upper left")
fig.tight_layout()
fig.savefig(os.path.join(PLOTS, "1_operations_vs_n.png"))
plt.close(fig)

fig, ax = plt.subplots(figsize=(8, 5.2), dpi=140)
for a in ALGOS:
    for d in ("sparse", "dense"):
        x, y = series(rows, a, d, lambda r: r["edge_scans"] / r["m"])
        ax.plot(x, y, color=COLOR[a], lw=2, label=f"{a} ({d})", **kw(a, d))
ax.set_xscale("log")
ax.set_ylim(0, 2.3)
ax.annotate("Prim and Kosaraju: exactly 2.0 (both ends / two passes)", (1000, 2.0), xytext=(0, 9),
            textcoords="offset points", fontsize=8, color=INK2)
ax.annotate("Kahn and DAG relaxation: exactly 1.0 (one pass)", (1000, 1.0), xytext=(0, 9),
            textcoords="offset points", fontsize=8, color=INK2)
style_axes(ax, "Edge scans per edge (edgeScans / m) - flat line = linear work", "n (vertices, log scale)", "edgeScans / m")
ax.legend(fontsize=8, ncol=2, frameon=False, loc="center left", bbox_to_anchor=(0, 0.66))
fig.tight_layout()
fig.savefig(os.path.join(PLOTS, "2_scans_per_edge.png"))
plt.close(fig)

fig, ax = plt.subplots(figsize=(8, 5.2), dpi=140)
for d in ("sparse", "dense"):
    x, y = series(rows, "Kruskal", d, lambda r: r["edge_scans"] / r["m"])
    ax.plot(x, y, color=COLOR["Kruskal"], lw=2, ms=6, label=f"Kruskal ({d}, m = {4 if d == 'sparse' else 20}n)", **STYLE[d])
    off = (0, -15) if d == "sparse" else (0, 8)
    for xi, yi in zip(x, y):
        ax.annotate(f"{yi:.0%}", (xi, yi), textcoords="offset points", xytext=off, ha="center", fontsize=8, color=INK2)
x, y = series(rows, "Prim", "dense", lambda r: r["edge_scans"] / (2 * r["m"]))
ax.plot(x, y, color=COLOR["Prim"], lw=2, ms=5, label="Prim (any density): reads every adjacency entry", ls=":", marker="^")
ax.set_xscale("log")
ax.set_ylim(0, 1.12)
ax.yaxis.set_major_formatter(matplotlib.ticker.PercentFormatter(1.0))
style_axes(ax, "Share of the sorted edges Kruskal examines before the tree is complete", "n (vertices, log scale)", "edges examined / m")
ax.legend(fontsize=8, frameon=False, loc="lower left")
fig.tight_layout()
fig.savefig(os.path.join(PLOTS, "3_kruskal_edge_share.png"))
plt.close(fig)

lg = lambda v: math.log2(v)
FORMULAS = {
    "Kruskal": [("dsuOps / E", lambda r: r["dsu_ops"] / r["m"]),
                ("dsuOps / edgeScans", lambda r: r["dsu_ops"] / r["edge_scans"]),
                ("comparisons / (E log2 E)", lambda r: r["comparisons"] / (r["m"] * lg(r["m"])))],
    "Prim": [("edgeScans / E", lambda r: r["edge_scans"] / r["m"]),
             ("comparisons / (E log2 V)", lambda r: r["comparisons"] / (r["m"] * lg(r["n"])))],
    "Kosaraju": [("edgeScans / (V + E)", lambda r: r["edge_scans"] / (r["n"] + r["m"])),
                 ("edgeScans / E", lambda r: r["edge_scans"] / r["m"])],
    "Kahn": [("edgeScans / (V + E)", lambda r: r["edge_scans"] / (r["n"] + r["m"])),
             ("edgeScans / E", lambda r: r["edge_scans"] / r["m"])],
    "DagShortestPath": [("edgeScans / (V + E)", lambda r: r["edge_scans"] / (r["n"] + r["m"])),
                        ("comparisons / E", lambda r: r["comparisons"] / r["m"])],
}

out = []
sizes = sorted({r["n"] for r in rows})
for d in ("sparse", "dense"):
    out.append(f"\n**{d} graphs (m = {4 if d == 'sparse' else 20}n)**\n")
    out.append("| algorithm | ratio | " + " | ".join(f"n={n:,}".replace(",", " ") for n in sizes) + " | spread |")
    out.append("|---|---|" + "---:|" * len(sizes) + "---:|")
    for a in ALGOS:
        for label, f in FORMULAS[a]:
            vals = [f(r) for n in sizes for r in rows if r["algorithm"] == a and r["density"] == d and r["n"] == n]
            spread = (max(vals) - min(vals)) / (sum(vals) / len(vals))
            out.append(f"| {a} | {label} | " + " | ".join(f"{v:.3f}" for v in vals) + f" | {spread:.1%} |")

out.append("\n**log-log slopes (least squares over the five sizes)**\n")
out.append("| algorithm | counter | slope sparse | slope dense |")
out.append("|---|---|---:|---:|")
for a in ALGOS:
    ks = [("edgeScans", lambda r: r["edge_scans"])]
    if a == "Kruskal":
        ks += [("total work (sort comparisons + dsuOps + scans)", total_ops)]
    if a == "Prim":
        ks += [("total work (PQ comparisons + scans)", total_ops)]
    for label, f in ks:
        s = [slope(*series(rows, a, d, f)) for d in ("sparse", "dense")]
        out.append(f"| {a} | {label} | {s[0]:.3f} | {s[1]:.3f} |")

out.append("\n**median time (ms), 5 runs**\n")
out.append("| algorithm | " + " | ".join(f"{n}/{d[0]}" for n in sizes for d in ("sparse", "dense")) + " |")
out.append("|---|" + "---:|" * (2 * len(sizes)))
for a in ALGOS:
    vals = [r["time_ms"] for n in sizes for d in ("sparse", "dense") for r in rows
            if r["algorithm"] == a and r["n"] == n and r["density"] == d]
    out.append(f"| {a} | " + " | ".join(f"{v:.1f}" for v in vals) + " |")

if os.path.exists(os.path.join(RES, "bonus_tarjan.csv")):
    b = load("bonus_tarjan.csv")
    out.append("\n**Bonus A: Kosaraju vs Tarjan edge scans**\n")
    out.append("| graph | Kosaraju scans | Tarjan scans | Kosaraju ms | Tarjan ms | SCCs |")
    out.append("|---|---:|---:|---:|---:|---:|")
    for i in range(0, len(b), 2):
        k, t = b[i], b[i + 1]
        out.append(f"| {k['graph']} | {k['edge_scans']} | {t['edge_scans']} | {k['time_ms']:.1f} | {t['time_ms']:.1f} | {t['result']} |")

text = "\n".join(out) + "\n"
with open(os.path.join(RES, "analysis.md"), "w") as f:
    f.write(text)
print(text)
print("plots written to", PLOTS, file=sys.stderr)
