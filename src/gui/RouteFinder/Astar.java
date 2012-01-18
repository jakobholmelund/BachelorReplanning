/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.RouteFinder;

import Planner.Action;
import Planner.POP.POP;
import Planner.TOPlan;
import java.util.ArrayList;
import java.util.TreeSet;
import worldmodel.World;

/**
 *
 * @author jakobsuper
 */
public class Astar {
    final private int movecost = 1;
    private ArrayList<Long> map;
    private long agent;
    
    public POP findPlan(World w,String action) throws InterruptedException {
        map = w.simpleMap();
        long[] startogoal = w.parseAction(action);
        long current = startogoal[0];//w.getAgentPos(agent);
        long goal = startogoal[1];//w.getAgentPos(agent);
        agent = startogoal[2];

        TreeSet<Node> frontier = new TreeSet<Node>();
        Node init = makeInitialNode(current);
        frontier.add(init);
        System.out.println(frontier);
        
        int states = 0;
        while (true) {
            //System.out.println(frontier.size());
            if (frontier.isEmpty()) {
                return null;
            }
            
            Node n = frontier.pollFirst();
            if (n.curPosition == goal) {
                return makeSolution(n, w);
            }else if(n.f > map.size()){
                return null;
            }
            
            for (long position : actions(n.curPosition)) {
                long s1 = position;
                frontier.add(new Node(s1, n, n.g + movecost, heuristik(s1,goal)));
                states++;
            }
          
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

    private POP makeSolution(Node n, World w) {
        TOPlan p = new TOPlan();
        POP pop = new POP("");
        pop.clearOpenPreconditions();
        Node node = n;
        Action laterAction = pop.getFinish();
        Action firstAction = null;
        while (node != null && node.parent != null) {
            int[] cords = coordsFor(node.curPosition);
            int[] cordsParent = coordsFor(node.parent.curPosition);
            //Action action = new Action("moveAtomic(" + this.agent + ", [" + cords[0] + "," + cords[1] + "])", true, true);
            String direction = "Error";
            
            if(cords[0] == cordsParent[0] && (cords[1] == cordsParent[1] - 1)) {
                direction = "N";
            }
            if(cords[0] == cordsParent[0] && (cords[1] == cordsParent[1] + 1)) {
                direction = "S";
            }
            if((cords[0] == cordsParent[0] + 1) && cords[1] == cordsParent[1]) {
                direction = "E";
            }
            if((cords[0] == cordsParent[0] - 1) && cords[1] == cordsParent[1]) {
                direction = "W";
            }
            
            Action action = new Action("moveAtomic(" + this.agent + ", " + direction + ")", true, true);
            action.addEffect("agentAt(" + this.agent + ",[" + cords[0] + "," + cords[1] + "])");
            action.addEffect("!agentAt(" + this.agent + ",[" + cordsParent[0] + "," + cordsParent[1] + "])");
            
            action.addPrecondition("f([" + cords[0] + "," + cords[1] + "])");
            action.addPrecondition("agentAt(" + this.agent + ",[" + cordsParent[0] + "," + cordsParent[1] + "])");
            
            //p.add(node.curPosition);
            p.add(action);
            pop.addAction(action);
            System.out.println("   Adding ordering: " + action.getAction() + " <<< " + laterAction.getAction());
            pop.addOrderingConstraint(action, laterAction);
            
            laterAction = action;
            firstAction = action;
            node = node.parent;
        }
        pop.addOrderingConstraint(pop.getStart(), firstAction);
        //TOPlan top = pop.getLinearization(w);
        
        //p.printSolution();
        //pop.printToConsole();
        //System.out.println("\n\n");
        //top.printSolution();
        return pop;
    }
    
    private ArrayList<Long> neighborsFor(long k){    
        int x = (int)(k & 0xFFFFFFFF) - 1000000000;
        int y = (int)((k >>> 32) & 0xFFFFFFFF) - 1000000000;
        ArrayList<Long> newlist = new ArrayList<Long>();
        long key1 = keyFor(x-1,y);
        if(map.contains(key1)){
            newlist.add(key1);
        }
        long key2 = keyFor(x+1,y);
        if(map.contains(key2)){
            newlist.add(key2);
        }
        long key3 = keyFor(x,y-1);
        if(map.contains(key3)){
            newlist.add(key3);
        }
        long key4 = keyFor(x,y+1);
        if(map.contains(key4)){
            newlist.add(key4);
        }
        
        //newlist.add(map.get());
        //newlist.add(map.get());
        //newlist.add(map.get());
        //newlist.add(map.get(keyFor(x-1,y-1)));
        //newlist.add(map.get(keyFor(x-1,y-1)));
        //newlist.add(map.get(keyFor(x+1,y+1)));
        //newlist.add(map.get(keyFor(x+1,y+1)));
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
