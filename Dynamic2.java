package packing.Dynamic;

import packing.Common.*;
import packing.Data.Instance;
import packing.SCLP.Fitness;
import packing.SCLP.Node;
import packing.SCLP.TreeSearch;

import java.util.ArrayList;
import java.util.Collections;

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
        ArrayList<Solution> bs = new ArrayList<>();

        for (int i = 0; i < sols.size(); i++) {
            Node ncopy = node.copy();
            Solution off_sol = sols.get(i);
            Graph g2 = new Graph(off_sol.bks, off_sol.pos);
            Solution s = dynamic_recommend(bks, ncopy, g2, off_sol);
            bs.add(s);
        }

        Collections.sort(bs);
        return bs.get(0);
    }

    // get the unplaced block in sol.bks;
    public static ArrayList<Block> get_unplaced(Node node, Solution sol) {
        ArrayList<Block> ret = new ArrayList<>();
        ArrayList<Block> placed_block = node.bks;
        boolean[] checked = new boolean[placed_block.size()];

        for (int i = 0; i < sol.bks.size(); i++) {
            boolean unplaced = true;
            Block bk = sol.bks.get(i);
            for (int j = 0; j < placed_block.size(); j++) {
                Block placed = placed_block.get(j);
                if (bk.id == placed.id && checked[j] == false) {
                    checked[j] = true;
                    unplaced = false;
                }
            }
            if (unplaced)
                ret.add(bk);
        }
        return ret;
    }

    // Calculate the degree between the block to be selected and the block already placed
    public static int max_degree_index(Graph g, int[] indexs, boolean[] feasible) {
        int ret = -1;
        int max = -1;
        ArrayList[] xadj = g.xadj;
        ArrayList[] yadj = g.yadj;
        ArrayList[] zadj = g.zadj;

        for (int i = 0; i < g.keys.length; i++) {
            int count = 0;
            if (!contain(indexs, i) && feasible[i]) {
                for (int j = 0; j < indexs.length; j++) {
                    int _index = indexs[j];
                    ArrayList<Integer> x = (ArrayList<Integer>) xadj[_index];
                    ArrayList<Integer> y = (ArrayList<Integer>) yadj[_index];
                    ArrayList<Integer> z = (ArrayList<Integer>) zadj[_index];
                    if (x.contains(i))
                        count++;
                    if (y.contains(i))
                        count++;
                    if (z.contains(i))
                        count++;
                }
                if (count > max) {
                    max = count;
                    ret = i;
                }
            }
        }
        return ret;
    }

    public static int feasible_degree(Graph g, int[] indexs, int index, Cuboid cub, Position pos, ArrayList<Block> blocks, ArrayList<Position> poss) {
        // g -> offline solution graph, indexs -> index of the placed block
        // index -> index of the block to be placed
        // blocks -> placed blocks, poss -> positions of the placed blocks
        int ret = 0;

        for (int i = 0; i < blocks.size(); i++) {
            Block _bk = blocks.get(i);
            Position _pos = poss.get(i);
            int _index = indexs[i];
            ArrayList<Integer> x = g.xadj[_index];
            ArrayList<Integer> y = g.yadj[_index];
            ArrayList<Integer> z = g.zadj[_index];
            int[] _pro = Utility.projection(cub, pos, _bk.cub, _pos);
            if (_pro[0] == 1 && x.contains(index))
                ret++;
            if (_pro[1] == 1 && y.contains(index))
                ret++;
            if (_pro[2] == 1 && z.contains(index))
                ret++;
        }
        return ret;
    }

    public static  Solution dynamic_recommend (ArrayList<Block> blist, Node node, Graph g2, Solution off_line) {
        // blist stores all previously generated blocks
        // node -> current state, g1 -> current solution graph, g2 -> offline solution graph
        while (!node.sps.isEmpty() && !node.bks.isEmpty()) {
            Solution s1 = node.s;
            ArrayList<Block> placed_block = s1.bks;
            ArrayList<Position> placed_pos = s1.pos;
            Graph g1 = new Graph(placed_block, placed_pos);

            Instance inst = node.inst;
            int[] type_num = new int[inst.type_num];
            System.arraycopy(node.s.num, 0, type_num, 0, inst.type_num);
            // feasible block in off_line solution
            Object[] _feasible = feasible(inst, node.sps, off_line, g2, type_num);
            boolean[] feas_index = (boolean[]) _feasible[0];
            ArrayList<Block> feas_blocks = (ArrayList<Block>) _feasible[1];

            //g2 includes all the blocks in g1.
            if (g2.contains2(g1)) {
                // indexs stores the index of all the blocks that have been placed.
                int[] indexs = new int[s1.bks.size()];

                Graph.array_init(g2.visited);
                for (int i = 0; i < placed_block.size(); i++) {
                    int id = placed_block.get(i).id;
                    int index = g2.indexOf(id);
                    indexs[i] = index;
                }

                int max_degree_index = max_degree_index(g2, indexs, feas_index);
                if (max_degree_index != -1) {
                    int block_id = g2.keys[max_degree_index];
                    Block bk = get_block(blist, block_id);
                    Space target = new Space();

                    int max = -1;
                    for (int i = 0; i < node.sps.size(); i++) {
                        Space _sp = node.sps.get(i);
                        if (_sp.contains(bk)) {
                            Position p1 = _sp.get_corner(bk.cub, _sp.cor);
                            int degree = feasible_degree(g2, indexs, max_degree_index, bk.cub, p1, placed_block, placed_pos);
                            if (degree > max) {
                                max = degree;
                                target = _sp;
                            }
                        }
                    }
                    if (max != -1) {
                        node.add_block(target, bk);
                    }
                    else {
                        TreeSearch ts = new TreeSearch(inst);
                        Solution fs = ts.partial_solve(node, 1, 25, 2);
                        return fs;
                    }
                }

                else {
                    TreeSearch ts = new TreeSearch(inst);
                    Solution fs = ts.partial_solve(node, 1, 25, 2);
                    return fs;
                }

            }

            else { // g2 does not contain g1
                int[] keys1 = g1.keys;
                int[] keys2 = g2.keys;
                ArrayList<Integer> intersect = new ArrayList<>(); //Storing the parts contained in both arrays together

                boolean[] _checked = new boolean[keys2.length];
                for (int i = 0; i < keys1.length; i++) {
                    int id1 = keys1[i];
                    if (Graph.array_contain(id1, keys2, _checked)) {
                        intersect.add(id1);
                    }
                }

                if (intersect.isEmpty()) {
                    /*boolean feas = false;
                    for (int i = 0; i < feas_index.length; i++) {
                        if (feas_index[i] == true) {
                            feas = true;
                            break;
                        }
                    }*/

                    if (feas_blocks.isEmpty()) {
                        /*Space _sp = node.sps.get(0);
                        Fitness ft = new Fitness(node.inst);
                        Block _bk = best_block(ft, node.s, _sp, 5, node.bks);
                        node.add_block(_sp, _bk);*/
                        TreeSearch ts = new TreeSearch(inst);
                        Solution fs = ts.partial_solve(node, 1, 25, 2);
                        return fs;
                    }

                    else {
                        for (int i = 0; i < node.sps.size(); i++) {
                            Space _sp = node.sps.get(i);
                            Fitness ft = new Fitness(node.inst);
                            Block _bk = best_block(ft, node.s, _sp, 5, feas_blocks);
                            if (_bk != null) {
                                node.add_block(_sp, _bk);
                                break;
                            }
                        }
                    }
                }
                
                else {
                    int[] indexs = new int[intersect.size()];
                    Graph.array_init(g2.visited);
                    for (int i = 0; i < intersect.size(); i++) {
                        int _id = intersect.get(i);
                        int _index = g2.indexOf(_id);
                        indexs[i] = _index;
                    }

                    ArrayList<Block> placed = new ArrayList<>();
                    ArrayList<Position> pla_pos = new ArrayList<>();

                    boolean[] marked = new boolean[intersect.size()];
                    for (int i = 0; i < placed_block.size(); i++) {
                        Block _bk = placed_block.get(i);
                        Position _pos = placed_pos.get(i);
                        for (int j = 0; j < intersect.size(); j++) {
                            int _id = intersect.get(j);
                            if (_bk.id == _id && !marked[j]) {
                                placed.add(_bk);
                                pla_pos.add(_pos);
                                marked[j] = true;
                            }
                        }
                    }

                    int max_degree_index = max_degree_index(g2, indexs, feas_index);

                    if (max_degree_index == -1) {
                        /*Block _bk = null;
                        for (int i = 0; i < node.sps.size(); i++) {
                            Space _sp = node.sps.get(i);
                            Fitness ft = new Fitness(node.inst);
                            _bk = best_block(ft, node.s, _sp, 5, node.bks);
                            if (_bk != null) {
                                node.add_block(_sp, _bk);
                                break;
                            }
                        }
                        if (_bk == null) {
                            node.sps = null;
                        }*/
                        TreeSearch ts = new TreeSearch(inst);
                        Solution fs = ts.partial_solve(node, 1, 25, 2);
                        return fs;
                    }

                    else {
                        int block_id = g2.keys[max_degree_index];
                        Block bk = get_block(blist, block_id);
                        Space target = new Space();
                        for (int i = 0; i < node.sps.size(); i++) {
                            int max = -1;
                            Space _sp = node.sps.get(i);
                            if (_sp.contains(bk)) {
                                Position p1 = _sp.get_corner(bk.cub, _sp.cor);
                                int degree = feasible_degree(g2, indexs, max_degree_index, bk.cub, p1, placed, pla_pos);
                                if (degree > max) {
                                    max = degree;
                                    target = _sp;
                                }
                            }
                        }
                        node.add_block(target, bk);
                    }
                }
            }
        }
        return node.s;
    }

    public static Object[] feasible(Instance inst, ArrayList<Space> sps, Solution off_sol, Graph g2, int[] type_num) {
        // g2 -> the graph of offline solution
        Object[] ret = new Object[2];
        ArrayList<Block> blocks = new ArrayList<>();
        boolean[] feasible = new boolean[g2.keys.length];

        for (int i = 0; i < off_sol.bks.size(); i++) {
            Block _bk = off_sol.bks.get(i);
            boolean feas = true;
            for (int j = 0; j < _bk.type_set.length; j++) {
                int t = _bk.type_set[j];
                if (type_num[t] + _bk.type_num[j] > inst.item_num[t]) {
                    feas = false;
                    break;
                }
            }

            boolean feas2 = false;

            if (feas) {
                for (int j = 0; j < sps.size(); j++) {
                    Space _sp = sps.get(j);
                    if (_sp.contains(_bk)) {
                        feas2 = true;
                        break;
                    }
                }
            }

            if (feas2) {
                blocks.add(_bk);
                feasible[i] = true;
            }
        }
        ret[0] = feasible;
        ret[1] = blocks;
        return ret;
    }

    public static Block best_block(Fitness ft, Solution s, Space sp, int _factor, ArrayList<Block> bks) {
        ft.kp(s, sp, _factor);
        ArrayList<Block> bks2 = new ArrayList<>();
        for(int i = 0; i < bks.size(); i++){
            Block b = bks.get(i);
            if(sp.contains(b)){
                b.srv = ft.value(sp, b);
                bks2.add(b);
            }
        }
        Collections.sort(bks2);
        if (bks2.isEmpty())
            return null;
        else {
            Block best = bks2.get(0);
            return best;
        }
    }

    public static boolean contain(int[] array, int index) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == index)
                return true;
        }
        return false;
    }

    public static Block get_block(ArrayList<Block> bks, int id) {
        for (Block block : bks) {
            if (block.id == id)
                return block;
        }
        return null;
    }
}