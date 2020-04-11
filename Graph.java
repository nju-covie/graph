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
            xadj[i] = new ArrayList<>();
            yadj[i] = new ArrayList<>();
            zadj[i] = new ArrayList<>();
        }

        for (int i = 0; i < V; i++) {
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
        get_degree();
    }

    public void  get_degree() {
        for (int i = 0; i < V; i++) {
            xdegree[i] += xadj[i].size();
            ydegree[i] += yadj[i].size();
            zdegree[i] += zadj[i].size();
        }
    }

    public boolean contains(int id) {
        for (int i = 0; i < keys.length; i++) {
            if (keys[i] == id && visited[i] == false) {
                visited[i] = true;
                return true;
            }
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

    public int get_diff(Graph g) {
        int ret = 0;
        array_init(visited);
        for (int i = 0; i < g.keys.length; i++) {
            if (contains(g.keys[i])) {
                boolean[] vcopy = new boolean[V];
                System.arraycopy(visited, 0, vcopy, 0, V);
                array_init(visited);
                int index = indexOf(g.keys[i]);
                ArrayList<Integer> l1 = xadj[index];
                array_init(g.visited);
                for (int j = 0; j < l1.size(); j++) {
                    int id = keys[l1.get(j)];
                    int index2 = g.indexOf(id);
                    if (!g.xadj[i].contains(index2))
                        ret++;
                }

                ArrayList<Integer> l2 = yadj[index];
                array_init(g.visited);
                for (int j = 0; j < l2.size(); j++) {
                    int id = keys[l2.get(j)];
                    int index2 = g.indexOf(id);
                    if (!g.yadj[i].contains(index2))
                        ret++;
                }

                ArrayList<Integer> l3 = zadj[index];
                array_init(g.visited);
                for (int j = 0; j < l3.size(); j++) {
                    int id = keys[l3.get(j)];
                    int index2 = g.indexOf(id);
                    if (!g.zadj[i].contains(index2))
                        ret++;
                }
                visited = vcopy;
            }
            else
                ret += g.xadj[i].size() + g.yadj[i].size() + g.zadj[i].size();

        }

        for (int i = 0; i < keys.length; i++) {
            if (g.contains(keys[i])) {
                boolean[] vcopy = new boolean[g.V];
                System.arraycopy(g.visited, 0, vcopy, 0, g.V);
                int index = g.indexOf(keys[i]);

                ArrayList<Integer> l1 = g.xadj[index];
                array_init(visited);
                for (int j = 0; j < l1.size(); j++) {
                    int id = g.keys[j];
                    int index1 = indexOf(id);
                    if (!xadj[i].contains(index1))
                        ret++;
                }

                ArrayList<Integer> l2 = g.yadj[index];
                array_init(visited);
                for (int j = 0; j < l2.size(); j++) {
                    int id = g.keys[j];
                    int index1 = indexOf(id);
                    if (!yadj[i].contains(index1))
                        ret++;
                }

                ArrayList<Integer> l3 = g.zadj[index];
                array_init(visited);
                for (int j = 0; j < l3.size(); j++) {
                    int id = g.keys[j];
                    int index1 = indexOf(id);
                    if (!zadj[i].contains(index1))
                        ret++;
                }
                g.visited = vcopy;
            }
            else
                ret += xadj[i].size() + yadj[i].size() + zadj[i].size();
        }

        return ret;
    }

    public boolean contains(Graph g) {
        // Check if this graph contains another one (g).
        array_init(visited);
        for (int i = 0; i < g.keys.length; i++) {
            int id1 = g.keys[i];
            if (!array_contain(id1, keys, visited))
                return false;
        }

        array_init(visited);
        for (int i = 0; i < g.keys.length; i++) {
            ArrayList<Integer> gx = g.xadj[i];
            ArrayList<Integer> gy = g.yadj[i];
            ArrayList<Integer> gz = g.zadj[i];
            int index = indexOf(g.keys[i]);
            boolean[] visited_copy = new boolean[V];
            System.arraycopy(visited, 0, visited_copy, 0, V);
            ArrayList<Integer> x = xadj[index];
            ArrayList<Integer> y = yadj[index];
            ArrayList<Integer> z = zadj[index];
            array_init(visited);
            for (int j = 0; j < gx.size(); j++) {
                int id = g.keys[gx.get(j)];
                int index1 = indexOf(id);
                if (!x.contains(index1))
                    return false;
            }
            array_init(visited);
            for (int j = 0; j < gy.size(); j++) {
                int id = g.keys[gy.get(i)];
                int index1 = indexOf(id);
                if (!y.contains(index1))
                    return false;
            }
            array_init(visited);
            for (int j = 0; j < gz.size(); j++) {
                int id = g.keys[gz.get(i)];
                int index1 = indexOf(id);
                if (!z.contains(index1))
                    return false;
            }
            visited = visited_copy;
        }
        return true;
    }

    public static boolean array_contain(int id, int[] array, boolean[] visited) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == id && visited[i] == false) {
                visited[i] = true;
                return true;
            }
        }
        return false;
    }

    public static void array_init(boolean[] array) {
        for (int i = 0; i < array.length; i++) {
            array[i] = false;
        }
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
        s.append("Z-relation " + V + " vertices, " + zE + " edges " + NEWLINE);
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