package gui.RouteFinder;

import java.util.ArrayList;
import java.util.List;

public class Plan {
    ArrayList<Long> list;

    Plan() {
        list = new ArrayList<Long>();
    }

    void add(long a) {
        //int[] pos = coordsFor(a);
        //System.out.println("(" + pos[0] + " , " + pos[0] + ")");
        list.add(0, a);
    }

    public List<Long> getSolution() {
        return this.list;
    }

    void printSolution(String agent) {
        System.err.print("THIS: ");
        for (Long a : list) {
            int[] pos = coordsFor(a);
            System.err.print("move(" + agent + ",[" + pos[0] + "," + pos[1] + "]). ");
        }
        System.err.println("Is a solution");
    }

    @Override
    public String toString() {
        String ret = "Solution: \n";
        for (Long a : list) {
            ret += "  " + a.toString() + "\n";
        }
        return ret;
    }

    public boolean isEmpty() {
        return this.list.isEmpty();
    }
    
    private int[] coordsFor(long k) {
        int x = (int)(k & 0xFFFFFFFF) - 1000000000;
        int y = (int)((k >>> 32) & 0xFFFFFFFF) - 1000000000;
        return new int[]{x,y};
    }
}
