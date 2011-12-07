
package Planner.forward;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeSet;

public class FSPlannerAStar {
    public FSPlannerAStar() {
    }

    public Plan findPlan(Problem p) throws InterruptedException {
        TreeSet<Node> frontier = new TreeSet<Node>();
        Node init = makeInitialNode(p.getInitial());
        frontier.add(init);
        int states = 0;
        while (true) {
            if (frontier.isEmpty()) {
                return null;
            }
            // System.out.println("FRONTER 1: " + frontier.toString());
            Node n = frontier.pollFirst();
            //if(n.a != null) {
            //    explored.add(n.s.shortState); //n.a.effect.toString());
            //}
            //System.out.println("FRONTER 2: " + frontier.toString());
            //System.out.println("Exploring at step: " + n.g);
            if (p.goalTest(n.s)) {
                //System.out.println("State Space Size: " + states);
                return makeSolution(n, p);
            }
            //System.out.println("EXPLORING : " + n.toString());
            //System.out.println("ACTIONS: " + p.actions(n.s));

            for (Actions a : p.actions(n.s)) {
                State s1 = p.result(n.s, a);
                frontier.add(new Node(s1, n, a, n.g + p.cost(s1, a), p.heuristik(s1, a, n)));
                states++;
                /*if(!explored.contains(s1.shortState)) { //a.effect.toString())){
                    frontier.add(new Node(s1, n, a, n.g + p.cost(s1, a), p.heuristik(s1, a, n)));
                    states++;
                }else{
                    removedstates++;
                    //System.out.println("STATE PRUNED: " + a.toString() + " num removed: " + removedstates);
                }
                /*if(frontier.size() > 50) {
                    frontier.pollLast();
                }*/
            }
            //System.err.println("Frontier size: " + frontier.size());
            //System.err.println("");
        }
    }

    private Node makeInitialNode(State initial) {
        return new Node(initial, null, null, 0, 0);
    }

    private Plan makeSolution(Node n, Problem p) {
        Plan s = new Plan();
        s.s = n.s;
        Node node = n;
        while (node != null) {
            if (node.a != null) {
                s.add(node.a);
            }

            node = node.parent;
        }
        return s;
    }
}
