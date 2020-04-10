package packing.Common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Graph {
    private static final String NEWLINE = System.getProperty("line.separator");
    private ArrayList<Block> bks;
    private ArrayList<Position> pos;
    private int V; // the number of node
    //private int E; // the number of edge
    private ArrayList<Integer>[] xadj; // X-relation graph
    private ArrayList<Integer>[] yadj; // Y-relation graph
    private ArrayList<Integer>[] zadj; // Z-relation graph
    public Map<Integer, Integer> map; // (K,V) -> (Block.id, serial number)
    public int[] keys;

    public Graph() {

    }

    public Graph(ArrayList<Block> _bks, ArrayList<Position> _pos) {
        bks = _bks;
        pos = _pos;
        V = bks.size();
        //this.E = 0;
        xadj = (ArrayList<Integer>[]) new ArrayList[V];
        yadj = (ArrayList<Integer>[]) new ArrayList[V];
        zadj = (ArrayList<Integer>[]) new ArrayList[V];
        map = new HashMap<>();
        for (int i = 0; i < V; i++) {
            Block bk = bks.get(i);
            if (!map.containsKey(bk.id)) {
                map.put(bk.id, map.size());
            }
        }
        keys = new int[map.size()];
        for (int key : map.keySet()) {
            keys[map.get(key)] = key;
        }
        for (int i = 0; i < V; i++) {
            xadj[i] = new ArrayList<>();
            yadj[i] = new ArrayList<>();
            zadj[i] = new ArrayList<>();
            Block bk1 = bks.get(i);
            Position pos1 = pos.get(i);
            for (int j = i + 1; j < V; j++) {
                Block bk2 = bks.get(j);
                Position pos2 = pos.get(j);
                int[] pro = Utility.projection(bk1.cub, pos1, bk2.cub, pos2);
                if (pro[0] == 1) {
                    xadj[map.get(bk1.id)].add(map.get(bk2.id));
                    xadj[map.get(bk2.id)].add(map.get(bk1.id));
                }

            }
        }

    }

    /*public Graph copy() {
        Graph g = new Graph();
        g.V = V;
        g.E = E;
        g.adj = new ArrayList[V];
        for (int v = 0; v < V; v++) {
            g.adj[v] = new ArrayList<>(adj[v]);
        }
        g.map = map; // maybe wrong
        g.keys = new int[V];
        System.arraycopy(keys, 0, g.keys, 0, V);
        return g;
    }*/

    private void validateVertex(int v) {
        if (v < 0 || v >= V)
            throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (V-1));
    }

    /*public void addEdge(int v, int w) {
        validateVertex(v);
        validateVertex(w);
        E++;
        adj[v].add(w);
        adj[w].add(v);
    }*/

    public Iterable<Integer> adj(int v) {
        validateVertex(v);
        return adj[v];
    }

    public boolean contains(int key) {
        return map.containsKey(key);
    }

    public int indexOf(int key) {
        return map.get(key); // get(K), return V
    }

    public int nameOf(int v) {
        validateVertex(v);
        return keys[v];
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(V + " vertices, " + E + " edges " + NEWLINE);
        for (int v = 0; v < V; v++) {
            s.append(v + ": ");
            for (int w : adj[v]) {
                s.append(w + " ");
            }
            s.append(NEWLINE);
        }
        return s.toString();
    }
}
