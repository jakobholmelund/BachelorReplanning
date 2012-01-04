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
    private ArrayList<ActionStruct> actions;
    
    public POPlanner(World world, int aid, LinkedList<String> goals, String mid) {
        iteration = 0;
        this.plan = null;
        this.world = world;
        statics = createStatics();
        done = false;
        this.agentId = aid;
        this.missionId = mid;
        this.goals = goals;
        routeFinder = new Astar();
        actions = this.setActions();
    }
    
    private void getPercepts() throws PrologException {
       long time1 = System.currentTimeMillis();
       String domain = "";
       
       // add to percepts            
       for(int i = 0; i < world.getX(); i++) {
            for(int j = 0; j < world.getY(); j++) {
                long key = world.getMap().keyFor(j, i);
                Object o = world.getMap().get(key);
                if(!(o instanceof Wall)){
                    domain += "f([" + i + "," + j + "]). ";
                }
            }
       }
       
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
                domain += "object(" + obs.getId() + "). ";
            }else if(o instanceof Goal) {
                //System.err.println("goal found");
                Goal obs = (Goal) o;
                domain += "goalAt(" + obs.getId() + ",[" + obs.x + "," + obs.y + "]). "; 
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
        this.state = new State(theory);
        //System.out.println(this.state.toString());
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
            System.out.println("\n");
            iteration++;
            
            Problem p;
            try {
                //get percepts and update current state description
                getPercepts();
                if(this.goal == null || this.goal.equals("")) {
                    this.goal = goals.pop();
                }
                //check if plan is still valid
                boolean valid = false;
                if(this.plan != null && this.plan.list.isEmpty()) {
                    if(!this.goals.isEmpty()) {
                        this.goal = this.goals.pop();
                        this.plan = null;
                    } else{
                        this.done = true;
                        System.err.println("DONE! ");
                        Thread.currentThread().join();
                        return; 
                    }
                }
                if(this.plan != null) {
                    int planSucceed = this.plan.validForPOP(state);
                    // Plan is good
                    if(planSucceed == -1) {
                        valid = true;
                    // Plan is good but goal is not fulfilled
                    }else if(planSucceed == -2) {
                        valid = false;
                    // Plan is broken
                    }else{
                        valid = false;
                    }
                    System.out.println("PLAN VALID: " + planSucceed + "  which is: " + valid);
                }

                //System.err.println("Free: " + state.state.solveboolean("f([1,3])"));
                if(this.plan != null && !this.plan.isEmpty() && valid) { // this.plan != null) {//
                    System.out.println("Go --->\n");
                    // If it is, do the next action
                    Actions next = plan.pop();
                    System.out.print("Take Next Action: " + next.name);

                    // Apply next - act() ?
                    if(next.atomic) {
                        System.out.println(" -- which is atomic");
                        
                        // If action succeeded
                        if(world.act(next.name)) {
                            // Replan. Later on, try to introduce new open preconditions to popPlan instead and refine it further.
                            plan = null;
                            popPlan = null; 
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
                        System.out.println("Started planning: ");
                    }
                    // Else, make a new plan ( and perform the first action ? )

                    p = new Problem(agentId,"", this.actions);
                    p.setInitial(this.state);
                    //System.out.println(this.state);
                    p.setGoal(this.goal);

                    //System.out.println("Problem: " + p.toString());

                    long time1 = System.currentTimeMillis();
                    this.popPlan = findPlan(p, this.goal);;
                    this.plan = getTotalOrderPlan(popPlan);
                    long time2 = System.currentTimeMillis();

                    System.out.println("Plan found in: " + (time2 - time1) + " ms");
                    System.out.println("Plan: \n" + this.plan.toString());
                    //System.out.println("Done...");
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(POPlanner.class.getName()).log(Level.SEVERE, null, ex);
            } catch (PrologException ex) {
                Logger.getLogger(POPlanner.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                world.draw();
                Thread.sleep(200);
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
    
    public TOPlan getTotalOrderPlan(POP pop) { 
        return pop.getLinearization();
    }
    
    public POP findPlan(Problem p, String goal) { 
        // Fix format to avoid prolog-collisions. 
        goal = goal.replaceAll("\\[\\s*([0-9]*)\\s*,\\s*([0-9]*)\\s*\\]", "[$1;$2]");
        POP pop = new POP(goal);
        return refinePlan(p, pop);
    }
    
    private POP refinePlan(Problem p, final POP pop) {
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
            ArrayList<Actions> gottenActions = findAction(p, oP.condition);
            //System.out.println("p:");
            //System.out.println("   " + actions.toString());
            for(Actions A : gottenActions) {
                boolean newlyAdded = false;
                // Enforce Consistency 
                Actions B = oP.action;
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
                //System.out.println("\nResolve - Between the new causal link and all existing actions");
                // Between the new causal link and all existing actions
                for(Actions C : pop.actions) {
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
                            Actions C = oL.A;
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
    
    private boolean conflict(CausalLink aToB, Actions C, POP pop) {
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
                Actions A = aToB.A;
                Actions B = aToB.B;
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
    
    public ArrayList<Actions> findAction(Problem p, String goal) throws InterruptedException {
        //System.out.print("FindAction: " + goal);
        goal = goal.replaceAll("\\[\\s*([0-9]*)\\s*,\\s*([0-9]*)\\s*\\]", "[$1;$2]");
        //System.out.println("   --  " + goal);
        ArrayList<Actions> actionsReturn = new ArrayList<Actions>();
        boolean go;
        for(ActionStruct a : p.actions) {
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

    private TOPlan makeSolution(Node n, Problem p) {
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
      
        //String goal = "goalAt(a, [6,11]). ";
        //String free = "f(C0) :- \\+w(C0), \\+agentAt(Agent, C0), \\+boxAt(Box, C0). ";
        //String equals = "equals(A, B) :- B = A. ";
        
        String rules = ""; //equals; //  move + push + pull + neighbours + 

        return rules;
    }
    
    public boolean done() {
        return done;
    }
    
    public ArrayList<ActionStruct> setActions() {
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

	ActionStruct move = new ActionStruct("move", prerequisites1, "move(Agent,MovePos)", argse1, effects1, false, false);
	
	/* MoveAtomic  */
	ArrayList<String> argse2 = new ArrayList<String>();
	argse2.add("Agent");
	argse2.add("CurPos");
	argse2.add("MovePos");
	
	ArrayList<String> prerequisites2 = new ArrayList<String>();
	prerequisites2.add("agentAt(Agent,CurPos)");
	prerequisites2.add("f(MovePos)");
	
	ArrayList<String> effects2 = new ArrayList<String>();
	effects2.add("agentAt(Agent,MovePos)");
	effects2.add("!agentAt(Agent,CurPos)");
	
	//ArrayList<String> requirements2 = new ArrayList<String>();
	//requirements2.add("f(MovePos)");

	ActionStruct moveAtomic = new ActionStruct("moveAtomic", prerequisites2, "moveAtomic(Agent,MovePos)", argse2, effects2, true, true);
	
	// object(Object) :- box(Object).
	// object(Object) :- bomb(Object).	
	
	/* PickUp  */
	ArrayList<String> argse3 = new ArrayList<String>();
	argse3.add("Agent");
	//argse3.add("AgPos");
	argse3.add("ObjPos");
	argse3.add("Object");
	
	ArrayList<String> prerequisites3 = new ArrayList<String>();
	prerequisites3.add("\\+carries(Agent, _)");
	prerequisites3.add("agentAt(Agent,ObjPos)"); // agentAt(Agent, AgPos)
	//prerequisites3.add("equals(ObjPos,AgPos)");
	prerequisites3.add("at(Object,ObjPos)");
	prerequisites3.add("object(Object)");
	
	ArrayList<String> effects3 = new ArrayList<String>();
	effects3.add("!at(Object,ObjPos)");
	effects3.add("carries(Agent,Object)");
	
	//ArrayList<String> requirements3 = new ArrayList<String>();
	//requirements3.add("at(Object,ObjPos)");
	
	ActionStruct pickUp = new ActionStruct("pickUp", prerequisites3, "pickUp(Agent,Object)", argse3, effects3, false, true);
	
	/* Place  */
	ArrayList<String> argse4 = new ArrayList<String>();
	argse4.add("Agent");
	argse4.add("AgPos");
	//argse4.add("ObjPos");
	argse4.add("Object");
	
	ArrayList<String> prerequisites4 = new ArrayList<String>();
	prerequisites4.add("agentAt(Agent,AgPos)");
	//prerequisites4.add("equals(ObjPos, AgPos)");
	prerequisites4.add("carries(Agent,Object)");
	
	ArrayList<String> effects4 = new ArrayList<String>();
	effects4.add("at(Object,AgPos)");
	effects4.add("!carries(Agent,Object)");
	
	//ArrayList<String> requirements4 = new ArrayList<String>();
	//requirements4.add("f(AgPos)");
	//requirements4.add("carries(Agent,Object)");
	
	ActionStruct place = new ActionStruct("place", prerequisites4, "place(Agent,Object)", argse4, effects4, false, true);

	/* Use Teleporter  */
	ArrayList<String> argse5 = new ArrayList<String>();
	argse5.add("Agent");
	argse5.add("AgPos");
	argse5.add("TeleporterPos");
	argse5.add("Teleporter");
	argse5.add("To");
	
	ArrayList<String> prerequisites5 = new ArrayList<String>();
	prerequisites5.add("agentAt(Agent,TeleporterPos)");
	//prerequisites5.add("equals(AgPos,TeleporterPos)");
	// prerequisites5.add("at(Teleporter, TeleporterPos)"); // ? Necessary ?
	prerequisites5.add("teleporter(Teleporter,TeleporterPos, To)");
	
	ArrayList<String> effects5 = new ArrayList<String>();
	effects5.add("agentAt(Agent,To)");
	effects5.add("!agentAt(Agent,TeleporterPos)");
	
	//ArrayList<String> requirements5 = new ArrayList<String>();
	
	ActionStruct useTeleporter = new ActionStruct("useTeleporter", prerequisites5, "useTeleporter(Agent,Teleporter)", argse5, effects5, false, true);
	
        ArrayList<ActionStruct> actions = new ArrayList<ActionStruct>();
        actions.add(move);
        actions.add(moveAtomic);
        actions.add(pickUp);
        actions.add(place);
        //actions.add(useTeleporter);
        
        return actions;
    }    
}
