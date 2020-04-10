package packing.Common;

import java.util.ArrayList;

public class Graph {
    private static final String NEWLINE = System.getProperty("line.separator");
    public ArrayList<Block> bks;
    public ArrayList<Position> pos;
    public int V; // the number of node
    public ArrayList<Integer>[] xadj; // X-relation graph
    public int xE; // the number of edges of X-relation graph
    public int[] xdegree; // Degrees of nodes in the X-relationship graph
    public ArrayList<Integer>[] yadj;
    public int yE;
    public int[] ydegree;
    public ArrayList<Integer>[] zadj;
    public int zE;
    public int[] zdegree;
    public int[] keys;
    public boolean[] visited;

    public Graph() {

    }

    public Graph(ArrayList<Block> _bks, ArrayList<Position> _pos) {
        bks = _bks;
        pos = _pos;
        V = bks.size();
        this.xE = 0;
        this.yE = 0;
        this.zE = 0;
        xadj = (ArrayList<Integer>[]) new ArrayList[V];
        yadj = (ArrayList<Integer>[]) new ArrayList[V];
        zadj = (ArrayList<Integer>[]) new ArrayList[V];
        xdegree = new int[V];
        ydegree = new int[V];
        zdegree = new int[V];
        keys = new int[V];
        visited = new boolean[V];

        for (int i = 0; i < V; i++) {
            Block bk = bks.get(i);
            keys[i] = bk.id;
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
                    xadj[i].add(j);
                    xadj[j].add(i);
                    xE++;
                }
                if (pro[1] == 1) {
                    yadj[i].add(j);
                    yadj[j].add(i);
                    yE++;
                }
                if (pro[2] == 1) {
                    zadj[i].add(j);
                    zadj[j].add(i);
                    zE++;
                }
            }
        }
    }

    public void  get_degree(int v) {
        for (int i = 0; i < V; i++) {
            xdegree[i] += xadj[i].size();
            ydegree[i] += yadj[i].size();
            zdegree[i] += zadj[i].size();
        }
    }

    public boolean contains(int id) {
        for (int i = 0; i < keys.length; i++) {
            if (keys[i] == id && visited[i] == false)
                return true;
        }
        return false;
    }

    public int indexOf(int id) {
        int ret = -1;
        for (int i = 0; i < keys.length; i++) {
            if (visited[i] == false && keys[i] == id) {
                visited[i] = true;
                ret = i;
            }
        }
        return ret;
    }

    public int nameOf(int v) {
        return keys[v];
    }

    public int get_diff(Graph g) {
        int ret = 0;
        for (int i = 0; i < g.keys.length; i++) {
            if (contains(g.keys[i])) {
                int index = indexOf(g.keys[i]);

                ArrayList<Integer> l1 = xadj[index];
                for (int j = 0; j < l1.size(); j++) {
                    int id = keys[l1.get(j)];
                    if (!g.xadj[i].contains(id))
                        ret++;
                }

                ArrayList<Integer> l2 = yadj[index];
                for (int j = 0; j < l2.size(); j++) {
                    int id = keys[l2.get(j)];
                    if (!g.yadj[i].contains(id))
                        ret++;
                }

                ArrayList<Integer> l3 = zadj[index];
                for (int j = 0; j < l3.size(); j++) {
                    int id = keys[l3.get(j)];
                    if (!g.zadj[i].contains(id))
                        ret++;
                }
            }
            else
                ret += g.xadj[i].size() + g.yadj[i].size() + g.zadj[i].size();

        }

        for (int i = 0; i < keys.length; i++) {
            if (g.contains(keys[i])) {
                int index = g.indexOf(keys[i]);

                ArrayList<Integer> l1 = g.xadj[index];
                for (int j = 0; j < l1.size(); j++) {
                    int id = g.keys[j];
                    if (!xadj[i].contains(id))
                        ret++;
                }

                ArrayList<Integer> l2 = g.yadj[index];
                for (int j = 0; j < l2.size(); j++) {
                    int id = g.keys[j];
                    if (!yadj[i].contains(id))
                        ret++;
                }

                ArrayList<Integer> l3 = g.zadj[index];
                for (int j = 0; j < l3.size(); j++) {
                    int id = g.keys[j];
                    if (!zadj[i].contains(id))
                        ret++;
                }
            }

            else
                ret += xadj[i].size() + yadj[i].size() + zadj[i].size();
        }

        return ret;
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("X-relation " + V + " vertices, " + xE + " edges " + NEWLINE);
        for (int v = 0; v < V; v++) {
            s.append(v + ": ");
            for (int w : xadj[v]) {
                s.append(w + " ");
            }
            s.append(NEWLINE);
        }
        s.append(NEWLINE);
        s.append("Y-relation " + V + " vertices, " + yE + " edges " + NEWLINE);
        for (int v = 0; v < V; v++) {
            s.append(v + ": ");
            for (int w : yadj[v]) {
                s.append(w + " ");
            }
            s.append(NEWLINE);
        }
        s.append(NEWLINE);
        s.append("Y-relation " + V + " vertices, " + zE + " edges " + NEWLINE);
        for (int v = 0; v < V; v++) {
            s.append(v + ": ");
            for (int w : zadj[v]) {
                s.append(w + " ");
            }
            s.append(NEWLINE);
        }
        return s.toString();
    }
}
