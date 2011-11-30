/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.RouteFinder;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeSet;

/**
 *
 * @author jakobsuper
 */
public class Astar {
    private int movecost = 1;
        Map<Long,Long> map;
        public Astar(Map<Long,Long> c2v){
        map = c2v;
    }
    
    public Plan findPlan(long current,long goal) throws InterruptedException {
        TreeSet<Node> frontier = new TreeSet<Node>();
        Node init = makeInitialNode(current);
        frontier.add(init);
        int states = 0;
        while (true) {
            if (frontier.isEmpty()) {
                return null;
            }
            Node n = frontier.pollFirst();
            if (n.curPosition == goal) {
                return makeSolution(n);
            }
            for (long position : actions(n.curPosition)) {
                //int[] pos = coordsFor(position);
                //System.out.println("(" + pos[0] + " , " + pos[0] + ")");

                long s1 = position;
                frontier.add(new Node(s1, n, n.g + movecost, heuristik(s1,goal)));
                states++;

                //System.err.println("Checked parent " + coordsFor(n.curPosition)[0] + "," + coordsFor(n.curPosition)[1] + " h="+heuristik(n.curPosition,goal)+ "mæææg " + coordsFor(s1)[0] + "," + coordsFor(s1)[1] + " h="+heuristik(s1,goal));
            }
            //System.err.println("Frontier size: " + frontier.size());
            //System.err.println("Checked parent " + n.curPosition + " mæææg " + coordsFor(n.curPosition)[0] + "," + coordsFor(n.curPosition)[1]);
        }
    }

    public double heuristik(long cp,long goal) {
            int[] c = coordsFor(cp);
            int[] g = coordsFor(goal);          

            double h = (Math.abs(c[0] - g[0]) + Math.abs(c[1] - g[1]));

            return h;
    }
    
    public ArrayList<Long> actions(long cp) {
        ArrayList<Long> actionsReturn = new ArrayList<Long>();
        for(Long a:neighborsFor(cp)){
            if(a!=null){
              actionsReturn.add(a);
            }
        }
        return actionsReturn;
    }
    
    private Node makeInitialNode(long cp) {
        return new Node(cp, null, 0, 0);
    }

    private Plan makeSolution(Node n) {
        Plan p = new Plan();
        Node node = n;
        while (node != null) {
            p.add(node.curPosition);
            node = node.parent;
        }
        p.printSolution();
        return p;
    }
    
    private ArrayList<Long> neighborsFor(long k){    
        int x = (int)(k & 0xFFFFFFFF) - 1000000000;
        int y = (int)((k >>> 32) & 0xFFFFFFFF) - 1000000000;
        ArrayList<Long> newlist = new ArrayList<Long>();
        newlist.add(k-1);
        newlist.add(k+1);
        newlist.add(map.get(keyFor(x,y-1)));
        newlist.add(map.get(keyFor(x,y+1)));
        newlist.add(map.get(keyFor(x-1,y-1)));
        newlist.add(map.get(keyFor(x-1,y-1)));
        newlist.add(map.get(keyFor(x+1,y+1)));
        newlist.add(map.get(keyFor(x+1,y+1)));
        return newlist;
    }
    
    public Long keyFor(int x, int y) {
        int kx = x + 1000000000;
        int ky = y + 1000000000;
        return (long)kx | (long)ky << 32;
    }
    
    private int[] coordsFor(long k) {
        int x = (int)(k & 0xFFFFFFFF) - 1000000000;
        int y = (int)((k >>> 32) & 0xFFFFFFFF) - 1000000000;
        return new int[]{x,y};
    }
    
}