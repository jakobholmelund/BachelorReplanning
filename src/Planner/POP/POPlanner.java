package Planner.POP;

import Planner.*;
import gui.RouteFinder.Astar;
import jTrolog.errors.PrologException;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import worldmodel.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class POPlanner implements Runnable { //  implements Runnable
    State state;
    TOPlan plan;
    POP popPlan;
    public int iteration;
    World world;
    String statics;
    boolean done;
    int agentId;
    LinkedList<String> goals;
    String goal;
    String missionId;
    Astar routeFinder;
    private ArrayList<ActionSchema> actions;
    
    public POPlanner(World world, int aid, LinkedList<String> goals) { //String mid
        iteration = 0;
        this.plan = null;
        this.world = world;
        statics = createStatics();
        done = false;
        this.agentId = aid;
        //this.missionId = mid;
        this.goals = goals;
        routeFinder = new Astar();
        actions = this.setActions();
    }
    
    private void getPercepts() throws PrologException {
       long time1 = System.currentTimeMillis();
       String domain = "";
       System.out.println("Get percepts --->");
       // add to percepts
       for(int i = 0; i < world.getX(); i++) {
            for(int j = 0; j < world.getY(); j++) {
                long key = world.getMap().keyFor(j, i);
                Object[] o = world.getMap().get(key);
                
                if(o == null || o.length == 0){
                    domain += "f([" + j + "," + i + "]). ";
                    //if(i == 3 && j == 2) {
                    //    System.out.println("   At 2,3 THERE IS FREE 1");
                    //}
                }else{
                    for(int k = 0; k < o.length; k++) {
                        MapObject obj = (MapObject) o[k];
                        //if(i == 3 && j == 2) {
                        //    System.out.println("   At 2,3 THERE IS SOMETHING: " + obj.toString());
                        //}
                        if(obj instanceof MapAgent) {
                            //System.err.println("Agent found");
                            MapAgent agent = (MapAgent) obj;
                            domain += "agentAt(" + agent.getNumber() + ",[" + agent.x + "," + agent.y + "]). ";
                            if(agent.getCarying() != null) {
                                domain += "carries(" + agent.getNumber() + "," + agent.getCarying().getId() + "). ";
                            }
                            //if(i == 3 && j == 2) {
                            //    System.out.println("   At 2,3 THERE IS AN AGENT");
                            //}
                        }else if(obj instanceof MapBox) {
                            //System.err.println("Box found");
                            MapBox obs = (MapBox) obj;
                            domain += "at(" + obs.getId() + ",[" + obs.x + "," + obs.y + "]). "; 
                            domain += "f([" + obs.x + "," + obs.y + "]). ";
                            domain += "item(" + obs.getId() + "). ";
                            //if(i == 3 && j == 2) {
                            //    System.out.println("   At 2,3 THERE IS A BOX");
                            //}
                        }else if(obj instanceof Bomb) {
                            //System.err.println("Box found");
                            Bomb obs = (Bomb) obj;
                            domain += "at(" + obs.getId() + ",[" + obs.x + "," + obs.y + "]). ";
                            domain += "f([" + obs.x + "," + obs.y + "]). ";
                            domain += "item(" + obs.getId() + "). ";
                            //if(i == 3 && j == 2) {
                            //    System.out.println("   At 2,3 THERE IS A BOMB");
                            //}
                        }else if(obj instanceof Goal) {
                            //System.err.println("goal found");
                            Goal obs = (Goal) obj;
                            domain += "goalAt(" + obs.getId() + ",[" + obs.x + "," + obs.y + "]). ";
                            domain += "f([" + obs.x + "," + obs.y + "]). ";
                            //if(i == 3 && j == 2) {
                            //    System.out.println("   At 2,3 THERE IS A GOAL");
                            //}
                        }else if(obj instanceof Wall) {
                            //System.err.println("wall found");
                            Wall w = (Wall) obj;
                            domain += "w([" + w.x + "," + w.y + "]). "; 
                            //if(i == 3 && j == 2) {
                            //    System.out.println("   At 2,3 THERE IS WALL");
                            //}
                        }else{
                            domain += "f([" + i + "," + j + "]). ";
                            if(i == 3 && j == 2) {
                                System.out.println("   At 2,3 THERE IS FREE 1");
                            }
                        }
                    }
                }
            }
       }
       /*
       for(MapObject o : world.getObjects()) {
           if(o instanceof MapAgent) {
                //System.err.println("Agent found");
                MapAgent agent = (MapAgent) o;
                domain += "agentAt(" + agent.getNumber() + ",[" + agent.x + "," + agent.y + "]). ";
                if(agent.getCarying() != null) {
                    domain += "carries(" + agent.getNumber() + "," + agent.getCarying().getId() + "). ";
                }
            }else if(o instanceof MapBox) {
                //System.err.println("Box found");
                MapBox obs = (MapBox) o;
                domain += "at(" + obs.getId() + ",[" + obs.x + "," + obs.y + "]). "; 
                domain += "item(" + obs.getId() + "). ";
            }else if(o instanceof Bomb) {
                //System.err.println("Box found");
                Bomb obs = (Bomb) o;
                domain += "at(" + obs.getId() + ",[" + obs.x + "," + obs.y + "]). "; 
                domain += "item(" + obs.getId() + "). ";
            }else if(o instanceof Goal) {
                //System.err.println("goal found");
                Goal obs = (Goal) o;
                domain += "goalAt(" + obs.getId() + ",[" + obs.x + "," + obs.y + "]). "; 
            }else if(o instanceof Wall) {
                //System.err.println("wall found");
                Wall w = (Wall) o;
                domain += "w([" + w.x + "," + w.y + "]). "; 
                if(w.x == 2 && w.y == 3) {
                    System.out.println("   At 2,3 THERE IS WALL");
                }
            }
        }*/
       
        String theory = getStatics() + domain;
        //System.out.println("statics:\n " + statics);
        //System.out.println("objects:\n " + world.getObjects().toString());
        //System.out.println("Num objects: " + world.getObjects().size() + "\n Theory:");
        //System.out.println(theory);
        //System.err.println(theory);
        this.state = new State(theory);
        //System.out.println("Got these percepts: \n" + this.state.toString());
        long time2 = System.currentTimeMillis();
                
       //System.out.println("Percepts gotten in: " + (time2 - time1) + " ms");
    }

    public String getStatics() {
        //if(world)
        return statics;
    }

    @Override
    public void run() {
        while(!this.done){
            System.out.println("\n");
            iteration++;
            
            POPProblem p;
            try {
                //get percepts and update current state description
                getPercepts();
                
                if(this.goal == null || this.goal.equals("") && goals.size() > 0) {
                    this.goal = goals.pop();
                    this.plan = null;
                    System.out.println("NEW GOAL: " + this.goal);
                }
                
                //check if plan is still valid
                boolean valid = false;
                if(this.plan != null && this.plan.list.isEmpty()) {
                    if(this.goals.isEmpty()) {
                        this.done = true;
                        System.err.println("DONE! ");
                        Thread.currentThread().join();
                        return;
                    } else{
                        this.goal = this.goals.pop();
                        this.plan = null;
                    }
                }
                
                if(this.plan != null) {
                    int planSucceed = this.plan.valid(state);
                    // Plan is good
                    if(planSucceed == -1) {
                        valid = true;
                    // Plan is good but goal is not fulfilled
                    }else if(planSucceed == -2) {
                        valid = false;
                        System.out.println("REPLAN -> Get new Linearization!");
                        this.plan = getTotalOrderPlan(popPlan, world);
                        this.popPlan = null;
                    // Plan is broken
                    }else{
                        System.out.println("Recieved: " + planSucceed + " -> REPLAN -> From Scratch!");
                        valid = false;
                        this.plan = null;
                        this.popPlan = null;
                    }
                    //System.out.println("PLAN VALID: " + planSucceed + "  which is: " + valid);
                }

                //System.err.println("Free: " + state.state.solveboolean("f([1,3])"));
                if(this.plan != null && !this.plan.isEmpty() && valid) { // this.plan != null) {//
                    System.out.println("Go --->");
                    // If it is, do the next action
                    Action next = plan.pop();
                    System.out.print("Take Next Action: " + next.name);

                    // Apply next - act() ?
                    if(next.atomic) {
                        System.out.println(" -- which is atomic");
                        
                        // If action succeeded
                        //boolean result = world.act(next.name);
                        
                        if(!world.act(next.name)) {
                            System.out.println("Action succeeded: " + false);
                            // Replan. Later on, try to introduce new open preconditions to popPlan instead and refine it further.
                            plan = null;
                            popPlan = null; 
                        }else{
                            System.out.println("Action succeeded: " + true);
                        }
                    }else{
                       System.out.println(" -- which is not atomic");
                       TOPlan subPlan = routeFinder.findPlan(world,next.name);
                       //subPlan.printSolution();
                       this.plan.prependAll(subPlan);
                       System.out.println("New plan:\n" + this.plan);
                    }
                    //world.agentActionParse(next.name);
                    //world.agentActionParse(next.name);
                }else{
                    if(this.plan != null) {
                        System.out.println("Replan: " + !valid);
                    }else{
                        System.out.println("Started planning for goal: " + this.goal + " -->");
                    }
                    // Else, make a new plan ( and perform the first action ? )

                    p = new POPProblem(agentId,"", this.actions);
                    p.setInitial(this.state);
                    //System.out.println(this.state);
                    p.setGoal(this.goal);

                    //System.out.println("Problem: " + p.toString());
                    
                    long time1 = System.currentTimeMillis();
                    this.popPlan = findPlan(p, this.goal);;
                    System.out.println("Pop Plan: \n");
                    //this.popPlan.printToConsole();
                    this.plan = getTotalOrderPlan(popPlan, world);
                    long time2 = System.currentTimeMillis();

                    System.out.println("Plan found in: " + (time2 - time1) + " ms");
                    
                    System.out.println("\nPlan: \n" + this.plan.toString());
                    //System.out.println("Done...");
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(POPlanner.class.getName()).log(Level.SEVERE, null, ex);
            } catch (PrologException ex) {
                Logger.getLogger(POPlanner.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                world.draw();
                Thread.sleep(75);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            } catch(NullPointerException e) {
                
            }
        }
    }
    
    public String[][] splitAction(String action) {
        Pattern typeP = Pattern.compile("^(\\!?.*)\\((.[^,]*\\)?)(\\,(.*)\\))?");
        Matcher m = typeP.matcher(action);
        boolean matchFound = m.find();
        if (matchFound) {
            String command = m.group(1);
            String arg1 = m.group(2);
            String arg2 = m.group(4);
            
            //System.out.println(command);
            //System.out.println(arg1);
            //System.out.println(arg2);
            return new String[][]{new String[]{command},new String[]{arg1,arg2}};
        }
        return null;
    }
    
    public TOPlan getTotalOrderPlan(POP pop, World world) { 
        return pop.getLinearization(world);
    }
    
    public POP findPlan(POPProblem p, String goal) { 
        // Fix format to avoid prolog-collisions. 
        goal = goal.replaceAll("\\[\\s*([0-9]*)\\s*,\\s*([0-9]*)\\s*\\]", "[$1;$2]");
        POP pop = new POP(goal);
        return refinePlan(p, pop);
    }
    
    private POP refinePlan(POPProblem p, final POP pop) {
        // PARTIAL-ORDER PLAN-SPACE SEARCHING
        if(pop == null) {
            return null;
        }
        
        if(pop.hasCycles()) {
            return null;
        }
        
        if(pop.isSolution()) {
            //pop.printToConsole();
            return pop;
        }
        
        
        OpenPrecondition oP = pop.pollOpenPreconditions();
        
        //System.out.println("OP: " + oP.toString());
        try {
            ArrayList<Action> gottenActions = findAction(p, oP.condition);
            //System.out.println("p:");
            //System.out.println("   " + actions.toString());
            for(Action A : gottenActions) {
                boolean newlyAdded = false;
                // Enforce Consistency 
                Action B = oP.action;
                CausalLink link = pop.addAndGetCausalLink(A, B, oP.condition);
                pop.addOrderingConstraint(A, B);
                if(!pop.contains(A)) {
                    pop.addOrderingConstraint(pop.getStart(), A);
                    pop.addOrderingConstraint(A, pop.getFinish());
                    newlyAdded = true;
                
                    // Add new open preconditions
                    for(String P : A.openPreconditions) {

                        //plan.appendAll(findPlan(p, openPrecondition));

                        // Fix format to avoid prolog-collisions. 
                        P = P.replaceAll("\\[\\s*([0-9]*)\\s*,\\s*([0-9]*)\\s*\\]", "[$1;$2]");
                        //System.out.println("   Adding: " + P);
                        pop.addOpenPrecondition(P, A);
                    }
                }

                // Resolve conflicts
                // Between the new causal link and all existing actions
                for(Action C : pop.actions) {
                    // If there is a (potential?) conclict between oA and link, resolve it:
                    if(conflict(link, C, pop)) {

                        // CONFLICT! Solve it

                        POP newPlan1 = refinePlan(p, pop.addOrderingConstraint(B, C));
                        if(newPlan1 != null) {
                            return newPlan1;
                        }

                        POP newPlan2 = refinePlan(p, pop.addOrderingConstraint(C, A));
                        if(newPlan2 != null) {
                            return newPlan2;
                        }
                    }
                }
             
                if(newlyAdded) {
                    // Between action A and all existing causal links      
                    //System.out.println("\nResolve - Between action A and all existing causal links");
                    for(CausalLink oL : pop.causalLinks) {
                        // If there is a conclict between A and oL, resolve it:

                        //String prop = oL.p.replace("\\+", "!");
                        //System.out.println(prop + " c? " + A.effectToString());
                        if(conflict(oL, A, pop)) {
                            // CONFLICT! Solve it
                            Action C = oL.A;
                            //pop.addOrderingConstraint(C, A);
                            POP newPlan1 = refinePlan(p, pop.addOrderingConstraint(B, C));
                            if(newPlan1 != null) {
                                return newPlan1;
                            }

                            POP newPlan2 = refinePlan(p, pop.addOrderingConstraint(C, A));
                            if(newPlan2 != null) {
                                return newPlan2;
                            }
                        }
                    }
                    pop.addActions(A);
                }
            }
        } catch (InterruptedException ex) {
            //Logger.getLogger(BSPlanner.class.getName()).log(Level.SEVERE, null, ex);
        }
        //System.out.println("Time spent so far: " + (System.currentTimeMillis() - time) + " ms. Is solution?= " + pop.isSolution() + "\n\n");

        return refinePlan(p, pop);
        
    }
    
    private boolean conflict(CausalLink aToB, Action C, POP pop) {
        String lookFor = "";
        String lookForall = "";
        String prop = aToB.p;
        boolean position = false;
        boolean not = false;
        boolean conflict = false;
        if(aToB.p.startsWith("!")) {
            lookFor = aToB.p.substring(1, aToB.p.length());
            not = true;
        }else{
            lookFor = "!" + aToB.p;
            not = false;
        }
        lookFor = lookFor.trim().replace("\\+", "!");
        lookFor = lookFor.replaceAll("\\[\\s*([0-9]*)\\s*,\\s*([0-9]*)\\s*\\]", "[$1;$2]");
        
        lookForall = lookFor.replaceAll(",.*\\)", ",_)");
        
        //System.out.println("LookFor: " + lookFor + "   LookForAll: " + lookForall);
        prop = prop.trim().replace("\\+", "!");
        prop = prop.replaceAll("\\[\\s*([0-9]*)\\s*,\\s*([0-9]*)\\s*\\]", "[$1;$2]");
        if(aToB.p.contains("agentAt")) {
            position = true;
        }
        //System.out.println("Matching: <-- " + lookFor + " --> and " + C.effectToString() + "\n");
        Pattern propPattern = Pattern.compile(lookFor);
        //Pattern agentPosPattern = Pattern.compile("agentAt(" + agentId + ",[\\s*\\d*;\\s*\\d*])");
        for(String effect : C.effects) {
            effect = effect.trim().replace("\\+", "!");
            effect = effect.replaceAll("\\[\\s*([0-9]*)\\s*,\\s*([0-9]*)\\s*\\]", "[$1;$2]");
            Matcher m = propPattern.matcher(effect);
            if(lookFor.equals(effect) || lookForall.equals(effect)) {
                conflict = true;
            }
            if(position && !not) {
                //System.out.println("   matching: " + aToB.p + " and " + effect);
                if(!effect.equals(prop) && effect.startsWith("agentAt(" + agentId)) {//if(effect.matches("agentAt(" + agentId + ",[\\s*\\d*;\\s*\\d*])")) {
                    //System.out.println("         POSITION CONFLICT!!!! between: " + prop + " and " + effect);
                    conflict = true;
                }
            }
            // Unless C before A and B before C
            if(conflict) {
                Action A = aToB.A;
                Action B = aToB.B;
                if(pop.hasOrderingConstraint(C, A) || pop.hasOrderingConstraint(B, C)) {
                    conflict = false;
                }
            }
            
            if(conflict) {
                //System.out.println("         CONFLICT!!!! between: " + prop + " and " + effect);
                return conflict;
            }
        }
        
        return false;
    }
    
    public ArrayList<Action> findAction(POPProblem p, String goal) throws InterruptedException {
        //System.out.print("FindAction: " + goal);
        goal = goal.replaceAll("\\[\\s*([0-9]*)\\s*,\\s*([0-9]*)\\s*\\]", "[$1;$2]");
        //System.out.println("   --  " + goal);
        ArrayList<Action> actionsReturn = new ArrayList<Action>();
        boolean go;
        for(ActionSchema a : p.actions) {
            if(!a.expanded) {
                go = false;
                //System.out.println("Action: " + a.name);
                HashMap arguments = new HashMap<String,String>();
                arguments.put("Agent", "" + this.agentId);
                for(String e : a.effects) {

                    String[][] action = splitAction(e);
                    String[][] goalSplittet = splitAction(goal);

                    //System.out.println("Effect : " + e);
                    //System.out.println("Goal: " + goalSplittet[0][0] + " a: " + action[0][0]);
                    if(action[0][0].equals(goalSplittet[0][0])) {
                        go = true;
                        //System.out.println("FOUND AN ACTION: " + a.name);
                        //System.out.println("Head: " + action[0][0]);  

                        for(int i = 0; i < action[1].length; i++) {
                            //System.out.print(action[1][i] + ",");
                            arguments.put(action[1][i].trim(), goalSplittet[1][i].replaceAll(";", ","));   
                        }
                        //System.out.println("args: " + arguments.toString());
                    }
                }

                if(go && !arguments.isEmpty()) {
                    //try {
                    actionsReturn.addAll(a.getSpecificActions(this.state.state, arguments));
                        //actionsReturn.addAll(a.get(this.state.state, arguments));
                    //} catch (PrologException ex) {
                    //    Logger.getLogger(BSPlanner.class.getName()).log(Level.SEVERE, null, ex);
                    //}
                }else{
                    //System.out.println("No args");
                }
                //System.out.println("\n");
            }else{
                continue;
            }
        }
        //findAction(p, "at(a,[5;5])");
        //System.out.println("Actions: " + actionsReturn.toString());
        
        return actionsReturn;
    }

    
    private Node makeInitialNode(State initial) {
        return new Node(initial, null, null, 0, 0);
    }

    private TOPlan makeSolution(Node n, POPProblem p) {
        TOPlan s = new TOPlan();
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
        /*
        for(Object o : world.getObjects()) {
            if(o instanceof Wall) {
                //System.err.println("Wall found");
                Wall wall = (Wall) o;
                staticsMk += "w([" + wall.x + "," + wall.y + "]). ";
            }
        }
        */
        return staticsMk;
    }

    private String getRules() {
      
        //String goal = "goalAt(a, [6,11]). ";
        //String free = "f(C0) :- \\+w(C0), \\+agentAt(Agent, C0), \\+boxAt(Box, C0). ";
        //String equals = "equals(A, B) :- B = A. ";
        
        
        //String rules = ""; // + carries; //equals; //  move + push + pull + neighbours + 
        
        // To avoid unknwon predicate errors
        String failsafes = "carries(none, object). at(none, nowhere). "; //carries(none, object). at(none, nowhere).
        /*
       // Forward neighbours
       String neighbours = "";
            neighbours += "neighbour([X1,Y1], [X2,Y2], n) :- B is Y1 - 1, Y2 = B, X1 = X2. ";
            neighbours += "neighbour([X1,Y1], [X2,Y2], s) :- B is Y1 + 1, Y2 = B, X2 = X1. ";
            neighbours += "neighbour([X1,Y1], [X2,Y2], e) :- B is X1 + 1, X2 = B, Y1 = Y2. ";
            neighbours += "neighbour([X1,Y1], [X2,Y2], w) :- B is X1 - 1, X2 = B, Y1 = Y2. ";
       */
        // Backward neighbours
        String neighbours = "";
            neighbours += "neighbour([X1,Y1], [X2,Y2], n) :- B is Y2 + 1, Y1 = B, X1 = X2. ";
            neighbours += "neighbour([X1,Y1], [X2,Y2], s) :- B is Y2 - 1, Y1 = B, X2 = X1. ";
            neighbours += "neighbour([X1,Y1], [X2,Y2], e) :- B is X2 - 1, X1 = B, Y1 = Y2. ";
            neighbours += "neighbour([X1,Y1], [X2,Y2], w) :- B is X2 + 1, X1 = B, Y1 = Y2. ";

        String rules = "" + neighbours + failsafes; //equals; //  move + push + pull + neighbours +     
        
        return rules;
    }
    
    public boolean done() {
        return done;
    }
    
    public ArrayList<ActionSchema> setActions() {
        /* Move  */
	ArrayList<String> argse1 = new ArrayList<String>();
	argse1.add("Agent");
	argse1.add("CurPos");
	argse1.add("MovePos");
	
	ArrayList<String> prerequisites1 = new ArrayList<String>();
	prerequisites1.add("agentAt(Agent,CurPos)");
	prerequisites1.add("f(MovePos)");
	
	ArrayList<String> effects1 = new ArrayList<String>();
	effects1.add("agentAt(Agent,MovePos)");
	effects1.add("!agentAt(Agent,CurPos)");
	
	//ArrayList<String> requirements1 = new ArrayList<String>();
	//requirements1.add("f(MovePos)");
        
	ActionSchema move = new ActionSchema("move", prerequisites1, "move(Agent,MovePos)", argse1, effects1, false, false);
	//CurPos
	/* MoveAtomic  */
	ArrayList<String> argse2 = new ArrayList<String>();
	argse2.add("Agent");
	argse2.add("CurPos");
	argse2.add("MovePos");
	
	ArrayList<String> prerequisites2 = new ArrayList<String>();
	prerequisites2.add("agentAt(Agent,CurPos)");
        prerequisites2.add("neighbour(CurPos, MovePos, _)");
	prerequisites2.add("f(MovePos)");
	
	ArrayList<String> effects2 = new ArrayList<String>();
	effects2.add("agentAt(Agent,MovePos)");
	effects2.add("!agentAt(Agent,CurPos)");
	
	//ArrayList<String> requirements2 = new ArrayList<String>();
	//requirements2.add("f(MovePos)");

	ActionSchema moveAtomic = new ActionSchema("moveAtomic", prerequisites2, "moveAtomic(Agent,MovePos)", argse2, effects2, true, true);
	
	// object(Object) :- box(Object).
	// object(Object) :- bomb(Object).	
	
	/* PickUp  */
	ArrayList<String> argse3 = new ArrayList<String>();
	argse3.add("Agent");
	//argse3.add("AgPos");
	argse3.add("CurPos");
	argse3.add("Item");
	
	ArrayList<String> prerequisites3 = new ArrayList<String>();
	prerequisites3.add("\\+carries(Agent, _)");
	prerequisites3.add("agentAt(Agent,CurPos)"); // agentAt(Agent, AgPos)
	//prerequisites3.add("equals(ObjPos,AgPos)");
	prerequisites3.add("at(Item,CurPos)");
	prerequisites3.add("item(Item)");
	
	ArrayList<String> effects3 = new ArrayList<String>();
	effects3.add("!at(Item,CurPos)");
	effects3.add("carries(Agent,Item)");
	
	//ArrayList<String> requirements3 = new ArrayList<String>();
	//requirements3.add("at(Object,ObjPos)");
	
	ActionSchema pickUp = new ActionSchema("pickUp", prerequisites3, "pickUp(Agent,Item)", argse3, effects3, false, true);
	
	/* Place  */
	ArrayList<String> argse4 = new ArrayList<String>();
	argse4.add("Agent");
	argse4.add("CurPos");
	//argse4.add("ObjPos");
	argse4.add("Item");
	
	ArrayList<String> prerequisites4 = new ArrayList<String>();
	prerequisites4.add("agentAt(Agent,CurPos)");
	//prerequisites4.add("equals(ObjPos, AgPos)");
	prerequisites4.add("carries(Agent,Item)");
	
	ArrayList<String> effects4 = new ArrayList<String>();
	effects4.add("at(Item,CurPos)");
	effects4.add("!carries(Agent,Item)");
	
	//ArrayList<String> requirements4 = new ArrayList<String>();
	//requirements4.add("f(AgPos)");
	//requirements4.add("carries(Agent,Object)");
	
	ActionSchema place = new ActionSchema("place", prerequisites4, "place(Agent,Item)", argse4, effects4, false, true);

        ArrayList<ActionSchema> actions = new ArrayList<ActionSchema>();
        actions.add(move);
        actions.add(moveAtomic);
        actions.add(pickUp);
        actions.add(place);
        
        return actions;
    }
}
