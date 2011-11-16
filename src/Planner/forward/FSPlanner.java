package Planner.forward;

import jTrolog.errors.PrologException;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import worldmodel.*;

public class FSPlanner { //  implements Runnable
    State state;
    Plan plan;
    public int iteration;
    World world;
    String statics;
    boolean done;
    
    public FSPlanner(World world) {
        iteration = 0;
        this.plan = null;
        this.world = world;
        statics = createStatics();
        done = false;
    }

    public void getPercepts() {
       long time1 = System.currentTimeMillis();
        String domain = "";
        
        for(Object o : world.getObjects()) {
            // add to percepts            
            if(o instanceof MapAgent) {
                //System.err.println("Agent found");
                MapAgent agent = (MapAgent) o;
                domain += "agentAt(" + agent.getNumber() + ",[" + agent.x + "," + agent.y + "]). ";
            }
            
            if(o instanceof Box) {
                //System.err.println("Box found");
                Box obs = (Box) o;
                domain += "boxAt(" + obs.getName() + ",[" + obs.x + "," + obs.y + "]). "; 
            }
        }
        
        String theory = getStatics() + domain;
        //System.out.println("statics:\n " + statics);
        //System.out.println("objects:\n " + world.getObjects().toString());
        //System.out.println("Num objects: " + world.getObjects().size() + "\n Theory:");
        //System.out.println(theory);
        
        try {
            this.state = new State(theory);
            //String goal = "boxAt(a, [9,9]). ";
        } catch (PrologException ex) {
            Logger.getLogger(FSPlanner.class.getName()).log(Level.SEVERE, null, ex);
        }
       long time2 = System.currentTimeMillis();
                
       System.out.println("Percepts gotten in: " + (time2 - time1) + " ms");
    }

    public String getStatics() {
        return statics;
    }

    public void run() {
        System.out.println("New iteration");
        iteration++;
        String goal = "boxAt(a,[8,8]). ";
        try {
            // get percepts and update current state description
            getPercepts();

            // check if plan is still valid
            boolean valid = false;
            if(this.plan != null && this.plan.list.isEmpty()) {
                this.done = true;
                System.err.println("DONE! ");
                return; 
            }
            if(this.plan != null) {
                valid = this.plan.valid(state);
            }
            
            //System.err.println("Free: " + state.state.solveboolean("f([1,3])"));
            if(this.plan != null && !this.plan.isEmpty() && valid) {

                // If it is, do the next action
                Action next = plan.pop();
                System.out.println("Take Next Action: " + next.name);
                
                // Apply next - act() ?
                world.agentActionParse(next.name);
            }else{
                if(this.plan != null) {
                    System.out.println("Replan: " + !valid);
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
            
            Node n = frontier.pollFirst();
            //frontier.clear();
            
            if (p.goalTest(n.s)) {
                //System.out.println("State Space Size: " + states);
                return makeSolution(n, p);
            }
            
            for (Action a : p.actions(n.s)) {
                //System.err.println("ACTION!!!!: " + a.name);
                State s1 = p.result(n.s, a);
                frontier.add(new Node(s1, n, a, n.g + p.cost(s1, a), p.heuristik(s1, a, n)));
                states++;
            }
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

    private String createStatics() {
        String staticsMk = getRules();
        for(Object o : world.getObjects()) {
            if(o instanceof Wall) {
                //System.err.println("Wall found");
                Wall wall = (Wall) o;
                staticsMk += "w([" + wall.x + "," + wall.y + "]). ";
            }
        }
        return staticsMk;
    }

    private String getRules() {
        String move = "move(Agent, MoveDirAgent, C0, C1) :- agentAt(Agent, C0), neighbour(C0, C1, MoveDirAgent), f(C1). ";
        String push = "push(Agent, MoveDirAgent, MoveDirBox, C0, C1, C2, Box) :- agentAt(Agent, C0), neighbour(C0, C1, MoveDirAgent), boxAt(Box, C1), neighbour(C1, C2, MoveDirBox), f(C2). ";
        String pull = "pull(Agent, MoveDirAgent, CurrDirBox, C0, C1, C2, Box) :- agentAt(Agent, C0), neighbour(C0, C1, MoveDirAgent), boxAt(Box, C2), neighbour(C0, C2, CurrDirBox), f(C1). ";
        
        String goal = "goalAt(a, [8,8]). ";
        String free = "f(C0) :- \\+ w(C0), \\+ agentAt(Agent, C0), \\+ boxAt(Box, C0). ";
        
        String neighbours = "";
            neighbours += "neighbour([X1,Y1], [X2,Y2], n) :- B is Y1 - 1, Y2 = B, X1 = X2. ";
            neighbours += "neighbour([X1,Y1], [X2,Y2], s) :- B is Y1 + 1, Y2 = B, X2 = X1. ";
            neighbours += "neighbour([X1,Y1], [X2,Y2], e) :- B is X1 + 1, X2 = B, Y1 = Y2. ";
            neighbours += "neighbour([X1,Y1], [X2,Y2], w) :- B is X1 - 1, X2 = B, Y1 = Y2. ";
        String rules = move + push + pull + goal + neighbours + free;

        return rules;
    }

    public boolean done() {
        return done;
    }
    
}