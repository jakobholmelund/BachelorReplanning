package Planner.naiveReplan;

import jTrolog.errors.PrologException;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import worldmodel.World;

public class FSPlanner { //  implements Runnable
    State state;
    Plan plan;
    public int iteration;
    World world;
    
    public FSPlanner(World world) {
        iteration = 0;
        this.plan = null;
        this.world = world;
    }

    public void getPercepts() {
        
        /*
        String largeDomain = "";
        largeDomain += "w([0,0]). w([1,0]).  w([2,0]).  w([3,0]).  w([4,0]). w([5,0]). w([6,0]). w([7,0]). w([8,0]). w([9,0]). w([10,0]). w([11,0]). w([12,0]). ";
        largeDomain += "w([0,1]).  w([0,2]).  w([0,3]).  w([0,4]).  w([0,5]).  w([0,6]).  w([0,7]).  w([0,8]).  w([0,9]).  w([0,10]). ";
        largeDomain += "w([12,1]).  w([12,2]).  w([12,3]).  w([12,4]).  w([12,5]).  w([12,6]).  w([12,7]).  w([12,8]).  w([12,9]).  w([12,10]). ";
        largeDomain += "w([0,10]).  w([1,10]).  w([2,10]).  w([3,10]). w([4,10]). w([5,10]). w([6,10]). w([7,10]). w([8,10]). w([9,10]). w([10,10]). w([11,10]). ";
        largeDomain += "boxAt(a,[1,2]). goalAt(a, [9,9]). ";
        largeDomain += "agentAt(0,[1,1]). agent(0). box(a). ";

        if(iteration > 3) {
            System.out.println("A box appears in [1,8]. ");
            largeDomain += "boxAt(b, [1,8]). ";
        }
        
        String initial = largeDomain;
        */
        String domain = "";
        
        String theory = getStatics() + domain;



        try {
            this.state = new State(theory);
            //String goal = "boxAt(a, [9,9]). ";
        } catch (PrologException ex) {
            Logger.getLogger(FSPlanner.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getStatics() {
        String boxpos = "agentAtBox(Agent, Box) :- agentAt(Agent, C0), boxAt(Box, C1), neighbour(C0, C1, MoveDirAgent). ";
        String move = "move(Agent, MoveDirAgent, C0, C1) :- agentAt(Agent, C0), neighbour(C0, C1, MoveDirAgent), f(C1). ";
        String push = "push(Agent, MoveDirAgent, MoveDirBox, C0, C1, C2, Box) :- agentAt(Agent, C0), neighbour(C0, C1, MoveDirAgent), boxAt(Box, C1), neighbour(C1, C2, MoveDirBox), f(C2). ";
        String pull = "pull(Agent, MoveDirAgent, CurrDirBox, C0, C1, C2, Box) :- agentAt(Agent, C0), neighbour(C0, C1, MoveDirAgent), boxAt(Box, C2), neighbour(C0, C2, CurrDirBox), f(C1). ";
        //String goal = "goalsSatisfied(A) :- findall(X,notAtGoal(X),L), L == []. ";
        //       goal += "notAtGoal(A) :- box(A), goalAt(A, B), boxAt(A, C), B \\== C. ";
        String goal = "";
        //String findAll = "findall(B,L,M) :- p(A),q(A,B),s(B), not(member(A,L)), !, findall(B,[A|L],M). findall(_,M,M). ";
        String findAll = "";
        String free = "f(C0) :- \\+ w(C0), \\+ agentAt(Agent, C0), \\+ boxAt(Box, C0). ";
        
        String neighbours = "";
            neighbours += "neighbour([X1,Y1], [X2,Y2], n) :- B is Y1 - 1, Y2 = B, X1 = X2. ";
            neighbours += "neighbour([X1,Y1], [X2,Y2], s) :- B is Y1 + 1, Y2 = B, X2 = X1. ";
            neighbours += "neighbour([X1,Y1], [X2,Y2], e) :- B is X1 + 1, X2 = B, Y1 = Y2. ";
            neighbours += "neighbour([X1,Y1], [X2,Y2], w) :- B is X1 - 1, X2 = B, Y1 = Y2. ";
        String statics = move + push + pull + goal + findAll + neighbours + boxpos + free;

        return statics;
    }

    public void run() {
        iteration++;
        String goal = "boxAt(a,[9,9]). ";
        try {
            // get percepts and update current state description
            getPercepts();

            // check if plan is still valid
            boolean valid = false;
            if(this.plan != null) {
                valid = this.plan.valid(state);
            }
            //System.err.println("Free: " + state.state.solveboolean("f([1,3])"));
            if(this.plan != null && !this.plan.isEmpty() && valid) {

                // If it is, do the next action
                Action next = plan.pop();
                System.out.println("Take Next Action: " + next.toString());
                // Apply next - act() ?
            }else{
                if(this.plan != null) {
                    System.out.println("Replan: " + this.plan.isEmpty() + " " + valid);
                }
                // Else, make a new plan ( and perform the first action ? )

                Problem p = new Problem();
                p.setInitial(this.state);
                //System.out.println(this.state);
                p.setGoal(goal);
                
                //System.out.println("Problem: " + p.toString());
                
                long time1 = System.currentTimeMillis();
                this.plan = this.findPlan(p);
                long time2 = System.currentTimeMillis();
                
                System.out.println("Plan found in: " + (time2 - time1) + " ms");
                System.out.println("Plan: \n" + this.plan.toString());
                //System.out.println("Done...");
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(FSPlanner.class.getName()).log(Level.SEVERE, null, ex);
        } catch (PrologException ex) {
            Logger.getLogger(FSPlanner.class.getName()).log(Level.SEVERE, null, ex);
        }
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
            frontier.clear();
            
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

            for (Action a : p.actions(n.s)) {
                //System.err.println("ACTION!!!!: " + a.name);
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