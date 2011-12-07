package Planner.backward;

import Planner.*;
import jTrolog.errors.PrologException;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import worldmodel.*;
import java.util.HashMap;

public class BSPlanner implements Runnable { //  implements Runnable
    State state;
    Plan plan;
    public int iteration;
    World world;
    String statics;
    boolean done;
    int agentId;
    String goal;
    String missionId;
    
    public BSPlanner(World world,int aid,String goal,String mid) {
        iteration = 0;
        this.plan = null;
        this.world = world;
        statics = createStatics();
        done = false;
        this.agentId = aid;
        this.missionId = mid;
        this.goal = goal;
    }

    public void getPercepts() {
        System.err.println("Getting percepts");
       long time1 = System.currentTimeMillis();
        String domain = "";
        
        for(Object o : world.getObjects()) {
            // add to percepts            
            if(o instanceof MapAgent) {
                //System.err.println("Agent found");
                MapAgent agent = (MapAgent) o;
                domain += "agentAt(" + agent.getNumber() + ",[" + agent.x + "," + agent.y + "]). ";
            }else if(o instanceof MapBox) {
                //System.err.println("Box found");
                MapBox obs = (MapBox) o;
                domain += "boxAt(" + obs.getName() + ",[" + obs.x + "," + obs.y + "]). "; 
            }else if(o instanceof Goal) {
                //System.err.println("goal found");
                Goal obs = (Goal) o;
                domain += "goalAt(" + obs.getName() + ",[" + obs.x + "," + obs.y + "]). "; 
            }else if(o instanceof Wall) {
                //System.err.println("wall found");
                Wall w = (Wall) o;
                domain += "w([" + w.x + "," + w.y + "]). "; 
            }
        }
        
        String theory = getStatics() + domain;
        //System.out.println("statics:\n " + statics);
        //System.out.println("objects:\n " + world.getObjects().toString());
        //System.out.println("Num objects: " + world.getObjects().size() + "\n Theory:");
        //System.out.println(theory);
        //System.err.println(theory);
        try {
            this.state = new State(theory);
            //String goal = "boxAt(a, [9,9]). ";
        } catch (PrologException ex) {
            Logger.getLogger(BSPlanner.class.getName()).log(Level.SEVERE, null, ex);
        }
       long time2 = System.currentTimeMillis();
                
       System.out.println("Percepts gotten in: " + (time2 - time1) + " ms");
    }

    public String getStatics() {
        //if(world)
        return statics;
    }

    @Override
    public void run() {
        while(!this.done){
            System.out.println("New iteration");
            iteration++;
            getPercepts();
            Problem p;
            try {
                p = new Problem(1,"");
                findAction(p, "boxAt(a,[5;5])");
            } catch (InterruptedException ex) {
                Logger.getLogger(BSPlanner.class.getName()).log(Level.SEVERE, null, ex);
            } catch (PrologException ex) {
                Logger.getLogger(BSPlanner.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.done = true;
            
            //try {
                // get percepts and update current state description
                //getPercepts();
                //System.err.println("f(3,3): " + state.state.solveboolean("f([3,3]). "));
                //System.err.println("f(10, 10): " + state.state.solveboolean("f([10,10]). "));

                // check if plan is still valid
                /*boolean valid = false;
                if(this.plan != null && this.plan.list.isEmpty()) {
                    this.done = true;
                    System.err.println("DONE! ");
                    Thread.currentThread().join();
                    return; 
                }
                if(this.plan != null) {
                    valid = this.plan.valid(state);
                }

                //System.err.println("Free: " + state.state.solveboolean("f([1,3])"));
                if(this.plan != null && !this.plan.isEmpty() && valid) {

                    // If it is, do the next action
                    Actions next = plan.pop();
                    System.err.println("Take Next Action: " + next.name);

                    // Apply next - act() ?
                    world.agentActionParse(next.name);
                }else{
                    if(this.plan != null) {
                        System.out.println("Replan: " + !valid);
                    }else{
                        System.out.println("Started planning: ");
                    }
                    // Else, make a new plan ( and perform the first action ? )

                    Problem p = new Problem(agentId,missionId);
                    p.setInitial(this.state);
                    //System.out.println(this.state);
                    p.setGoal(this.goal);

                    //System.out.println("Problem: " + p.toString());

                    long time1 = System.currentTimeMillis();
                    //this.plan = this.findPlan(p, this.goal);
                    long time2 = System.currentTimeMillis();

                    System.out.println("Plan found in: " + (time2 - time1) + " ms");
                    System.out.println("Plan: \n" + this.plan.toString());
                    //System.out.println("Done...");
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(BSPlanner.class.getName()).log(Level.SEVERE, null, ex);
            } catch (PrologException ex) {
                Logger.getLogger(BSPlanner.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                world.draw();
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }*/
        }
    }
    
    public String[][] splitAction(String action) {
        String[] res = action.split("\\(");
        String head = res[0];
        //System.out.println("Rest: " + res[1]);
        String[] args = res[1].substring(0, res[1].length()-1).trim().split("\\,");
        
        String[][] result = new String[2][];
        String[] headT = {head};
        
        result[0] = headT;
        result[1] = args;
        
        return result;
    }
    
    public Plan findAction(Problem p, String goal) throws InterruptedException {
        ArrayList<Actions> actionsReturn = new ArrayList<Actions>();
        for(ActionStruct a : p.actions) {
            //System.out.println("Action: " + a.name);
            HashMap arguments = new HashMap<String,String>();
            for(String e : a.effects) {
                
                String[][] action = splitAction(e);
                String[][] goalSplittet = splitAction(goal);
                
                //System.out.println("Effect : " + e);
                //System.out.println("Goal: " + goalSplittet[0][0] + " a: " + action[0][0]);
                if(action[0][0].equals(goalSplittet[0][0])) {
                    System.out.println("FOUND AN ACTION: " + a.name);
                    System.out.println("Head: " + action[0][0]);  
                    
                    
                    for(int i = 0; i < action[1].length; i++) {
                        System.out.print(action[1][i] + ",");
                        arguments.put(action[1][i], goalSplittet[1][i].replaceAll(";", ","));
                        
                    }
                    System.out.println("args: " + arguments.toString());
                }
            }
            
            if(!arguments.isEmpty()) {
                //try {
                    a.getSpecificAction(this.state.state, arguments);
                    //actionsReturn.addAll(a.get(this.state.state, arguments));
                //} catch (PrologException ex) {
                //    Logger.getLogger(BSPlanner.class.getName()).log(Level.SEVERE, null, ex);
                //}
            }else{
                System.out.println("No args");
            }
            System.out.println("\n");
        }
        System.out.println("Actions: " + actionsReturn.toString());
        
        return null;
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
        String move = "move(Agent, MoveDir, C0, C1) :- agentAt(Agent, C0), neighbour(C0, C1, MoveDir), f(C1). ";
        String push = "push(Agent, MoveDir, MovePush, C0, C1, C2, Box) :- agentAt(Agent, C0), neighbour(C0, C1, MoveDir), boxAt(Box, C1), neighbour(C1, C2, MovePush), f(C2). ";
        String pull = "pull(Agent, MoveDir, CurrDir, C0, C1, C2, Box) :- agentAt(Agent, C0), neighbour(C0, C1, MoveDir), boxAt(Box, C2), neighbour(C0, C2, CurrDir), f(C1). ";

        //String goal = "goalAt(a, [6,11]). ";
        String free = "f(C0) :- \\+w(C0), \\+agentAt(Agent, C0), \\+boxAt(Box, C0). ";
        
        String neighbours = "";
            /*neighbours += "neighbour([X1, Y1], [X2, Y2], n):- is(Y1, Y2 + 1), X1 = X2. ";
            neighbours += "neighbour([X1, Y1], [X2, Y2], s):- is(Y1,Y2 - 1), X2 = X1. ";
            neighbours += "neighbour([X1, Y1], [X2, Y2], e):- is(X1,X2 - 1), Y1 = Y2. ";
            neighbours += "neighbour([X1, Y1], [X2, Y2], w):- is(X1,X2 + 1), Y1 = Y2. ";

            neighbours += "neighbour([X1, Y1], [X2, Y2], n):- is(Y2,Y1 - 1), X1 = X2. ";
            neighbours += "neighbour([X1, Y1], [X2, Y2], s):- is(Y2,Y1 + 1), X2 = X1. ";
            neighbours += "neighbour([X1, Y1], [X2, Y2], e):- is(X2,X1 + 1), Y1 = Y2. ";
            neighbours += "neighbour([X1, Y1], [X2, Y2], w):- is(X2,X1 - 1), Y1 = Y2. ";
            */
            neighbours += "neighbour([X1, Y1], [X2, Y2], n):- Y1 = Y2 + 1, X1 = X2. ";
            neighbours += "neighbour([X1, Y1], [X2, Y2], s):- Y1 = Y2 - 1, X2 = X1. ";
            neighbours += "neighbour([X1, Y1], [X2, Y2], e):- X1 = X2 - 1, Y1 = Y2. ";
            neighbours += "neighbour([X1, Y1], [X2, Y2], w):- X1 = X2 + 1, Y1 = Y2. ";

            neighbours += "neighbour([X1, Y1], [X2, Y2], n):- Y2 = Y1 - 1, X1 = X2. ";
            neighbours += "neighbour([X1, Y1], [X2, Y2], s):- Y2 = Y1 + 1, X2 = X1. ";
            neighbours += "neighbour([X1, Y1], [X2, Y2], e):- X2 = X1 + 1, Y1 = Y2. ";
            neighbours += "neighbour([X1, Y1], [X2, Y2], w):- X2 = X1 - 1, Y1 = Y2. ";

            //neighbours += "neighbour([X1,Y1], [X2,Y2], n) :- B is Y1 - 1, Y2 = B, X1 = X2. ";
            //neighbours += "neighbour([X1,Y1], [X2,Y2], s) :- B is Y1 + 1, Y2 = B, X2 = X1. ";
            //neighbours += "neighbour([X1,Y1], [X2,Y2], e) :- B is X1 + 1, X2 = B, Y1 = Y2. ";
            //neighbours += "neighbour([X1,Y1], [X2,Y2], w) :- B is X1 - 1, X2 = B, Y1 = Y2. ";
            
        String rules = move + push + pull + neighbours + free;

        return rules;
    }
    
    public boolean done() {
        return done;
    }
    
}