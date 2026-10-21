package daa.graph;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class GraphIO {
    private GraphIO() {
    }

    public static Graph read(Path file) throws IOException {
        return parse(Files.readAllLines(file, StandardCharsets.UTF_8));
    }

    public static Graph parse(String text) {
        return parse(List.of(text.split("\\R")));
    }

    private static Graph parse(List<String> rawLines) {
        List<String> lines = new ArrayList<>();
        for (String l : rawLines) {
            String t = l.trim();
            if (!t.isEmpty() && !t.startsWith("#")) lines.add(t);
        }
        if (lines.isEmpty()) throw new IllegalArgumentException("empty graph file");

        String[] head = lines.get(0).split("\\s+");
        if (head.length != 3) throw new IllegalArgumentException("header must be 'n m directed|undirected'");
        int n = Integer.parseInt(head[0]);
        int m = Integer.parseInt(head[1]);
        boolean directed;
        switch (head[2].toLowerCase()) {
            case "directed" -> directed = true;
            case "undirected" -> directed = false;
            default -> throw new IllegalArgumentException("third header token must be directed|undirected");
        }
        if (lines.size() - 1 != m) {
            throw new IllegalArgumentException("header says m=" + m + " but file has " + (lines.size() - 1) + " edge lines");
        }

        Graph g = new Graph(n, directed);
        for (int i = 1; i <= m; i++) {
            String[] p = lines.get(i).split("\\s+");
            if (p.length != 3) throw new IllegalArgumentException("edge line " + i + " must be 'u v w'");
            g.addEdge(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]));
        }
        return g;
    }

    public static void write(Graph g, Path file) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(g.n()).append(' ').append(g.m()).append(' ')
                .append(g.isDirected() ? "directed" : "undirected").append('\n');
        for (Edge e : g.edges()) sb.append(e.u).append(' ').append(e.v).append(' ').append(e.w).append('\n');
        Files.writeString(file, sb.toString(), StandardCharsets.UTF_8);
    }
}
