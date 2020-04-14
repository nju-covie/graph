package packing.Dynamic;

import packing.Common.*;
import packing.SCLP.Node;
import java.util.ArrayList;

public class Dynamic {
    public Node node; // current state
    public ArrayList<Block> bks;
    public ArrayList<Solution> sols; // offline solutions

    public Dynamic(Node node, ArrayList<Block> bks, ArrayList<Solution> sols) {
        // node -> current state ; sols -> offline solutions
        this.node = node;
        this.bks = bks;
        this.sols = sols;
    }

    public Solution get_best_sol() {
        ArrayList<Solution> sols = new ArrayList<>();
        Graph g1 = new Graph(node.s.bks, node.s.pos);
        Node ncopy = node.copy();
        for (int i = 0; i < sols.size(); i++) {
            Solution off_sol = sols.get(i);
            Graph g2 = new Graph(off_sol.bks, off_sol.pos);
            Solution s =
        }
    }

    public static  Solution dynamic_recommend (ArrayList<Block> blist, Node node, Graph g1, Graph g2) {
        // node -> current state, g1 -> current solution graph, g2 -> offline solution graph
        while (!node.sps.isEmpty()) {
            Solution s1 = node.s;
            ArrayList<Block> placed_block = s1.bks;
            ArrayList<Position> placed_pos = s1.pos;
            ArrayList<Space> current_space = node.sps;

            int[] xdegree = g2.xdegree;
            int[] ydegree = g2.ydegree;
            int[] zdegree = g2.zdegree;

            ArrayList<Range> ranges = new ArrayList<>();
            ArrayList<Range> unplaced = new ArrayList<>();
            if (g2.contains(g1)) {
                // indexs stores the index of all the blocks that have been placed.
                int[] indexs = new int[s1.bks.size()];
                Graph.array_init(g2.visited);

                for (int i = 0; i < placed_block.size(); i++) {
                    Block bk = placed_block.get(i);
                    Position pos = placed_pos.get(i);
                    int id = placed_block.get(i).id;
                    int index = g2.indexOf(id);
                    indexs[i] = index;
                    Range placed = new Range(true, index, pos.x, pos.x + bk.cub.l, pos.y, pos.y + bk.cub.w,
                            pos.z, pos.z + bk.cub.h);
                    ranges.add(placed);
                }

                for (int i = 0; i < indexs.length; i++) {
                    int index2 = indexs[i];

                    ArrayList<Space> xsps = new ArrayList<>();
                    ArrayList<Space> ysps = new ArrayList<>();
                    ArrayList<Space> zsps = new ArrayList<>();

                    // Store all spaces that intersect the projection of the block in the x,y,z direction
                    for (int j = 0; j < current_space.size(); j++) {
                        Space sp = current_space.get(j);
                        int[] pro = Utility.projection(sp.cub, sp.pos, placed_block.get(j).cub, placed_pos.get(j));
                        if (pro[0] == 1)
                            xsps.add(sp);
                        if (pro[1] == 1)
                            ysps.add(sp);
                        if (pro[2] == 1)
                            zsps.add(sp);
                    }

                    Range range = ranges.get(i);
                    int xmin = range.xrange[0];
                    int xmax = range.xrange[1];
                    int ymin = range.yrange[0];
                    int ymax = range.yrange[1];
                    int zmin = range.zrange[0];
                    int zmax = range.zrange[1];

                    ArrayList<Integer> x = g2.xadj[index2];
                    ArrayList<Integer> y = g2.yadj[index2];
                    ArrayList<Integer> z = g2.zadj[index2];
                    for (int j = 0; j < x.size(); j++) {
                        int index3 = x.get(i);
                        if (index3 < 0)
                            continue;
                        if (!contain(indexs, index3)) {
                            int bid = g2.keys[index3];
                            Block bk = get_block(blist, bid); //index3 -> bid -> block
                            int bkl = bk.cub.l;
                            int bkw = bk.cub.w;
                            int bkh = bk.cub.h;

                            for (int k = 0; k < xsps.size(); k++) {
                                Space sp = xsps.get(i);
                                if (Utility.contains(sp.cub, bk.cub)) {

                                }
                            }
                            
                            boolean left = true;
                            boolean right = true;

                            if (xmin < bkl)
                                left = false;

                            if (binl - xmax < bkl)
                                right = false;

                            if (left || right) {
                                int y1 = range.yrange[0] - bk.cub.w; // lower bound
                                revise_range(2, y1, binl, binw, binh, bkl, bkw, bkh);
                                int y2 = range.yrange[1]; // upper bound
                                revise_range(2, y2, binl, binw, binh, bkl, bkw, bkh);

                                int z1 = range.zrange[0] - bk.cub.h;
                                revise_range(3, z1, binl, binw, binh, bkl, bkw, bkh);
                                int z2 = range.zrange[1];
                                revise_range(3, z2, binl, binw, binh, bkl, bkw, bkh);

                                if (xdegree[index3] == 1) {
                                    int _x1 = range.xrange[0] - bk.cub.l; // left
                                    int _x2 = range.xrange[1]; // right
                                    if (feasible(1, _x1, binl, binw, binh, bkl, bkw, bkh)) {
                                        unplaced.add(new Range(false, index3, _x1, _x1, y1, y2, z1, z2));
                                    }
                                    if (feasible(1, _x2, binl, binw, binh, bkl, bkw, bkh)) {
                                        unplaced.add(new Range(false, index3, _x2, _x2, y1, y2, z1, z2));
                                    }
                                }

                                else {
                                    int xmin1 = 0;
                                    int xmax1 = range.xrange[0] - bk.cub.l;
                                    int xmin2 = range.xrange[1];
                                    int xmax2 = binl - bk.cub.l;
                                    if (left == true) {
                                        unplaced.add(new Range(false, index3, xmin1, xmax1, y1, y2, z1, z2));
                                    }
                                    if (right == true) {
                                        unplaced.add(new Range(false, index3, xmin2, xmax2, y1, y2, z1, z2));
                                    }
                                }
                            }
                            else {
                                x.set(j, -1);
                                int index4 = get_index(g2.xadj[index3], index2);
                                g2.xadj[index3].set(index4, -1);
                            }
                        }
                    }

                    for (int j = 0; j < y.size(); j++) {

                    }

                    for (int j = 0; j < z.size(); j++) {

                    }
                }
            }
        }
    }

    public static int get_index(ArrayList<Integer> list, int i) {
        for (int j = 0; j < list.size(); j++) {
            if (list.get(j) == i)
                return j;
        }
        return -1;
    }

    public static boolean contain(int[] array, int index) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == index)
                return true;
        }
        return false;
    }

    public static boolean feasible(int o, int value, int l, int w, int h, int bl, int bw, int bh) {
        if (o == 1 && value >= 0 && value + bl <= l)
            return true;
        if (o == 2 && value >= 0 && value + bw <= w)
            return true;
        if (o == 3 && value >= 0 && value + bh <= h)
            return true;
        return false;
    }

    public static void revise_range(int o, int value, int l, int w, int h, int bl, int bw, int bh) {
        if (o == 1 && value >= 0 && value + bl <= l)
            return;
        if (o == 2 && value >= 0 && value + bw <= w)
            return;
        if (o == 3 && value >= 0 && value + bh <= h)
            return;

        int diff1 = 0 - value;
        if (o == 1) {
            int diff2 = l - value - bl;
            if (diff1 > 0)
                value = 0;
            if (diff2 < 0)
                value = l - bl;
        }
        else if (o == 2) {
            int diff2 = w - value - bw;
            if (diff1 > 0)
                value = 0;
            if (diff2 < 0)
                value = w - bw;
        }
        else {
            int diff2 = h - value - bh;
            if (diff1 > 0)
                value = 0;
            if (diff2 < 0)
                value = h - bh;
        }
    }

    public static void revise_range(Range r, ArrayList<Block> placed_block, ArrayList<Position> poss) {

    }

    public static Block get_block(ArrayList<Block> bks, int id) {
        for (Block block : bks) {
            if (block.id == id)
                return block;
        }
        return null;
    }

}