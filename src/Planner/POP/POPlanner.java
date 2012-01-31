package Planner.POP;

import Planner.*;
import gui.RouteFinder.Astar;
import jTrolog.errors.PrologException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import worldmodel.*;
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
    Action lastAtomicOnSubPlan = null;
    boolean init = true;
    private boolean planRepair;
    
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
        this.planRepair = true;
    }
    
    private void getPercepts() throws PrologException {
       long time1 = System.currentTimeMillis();
       String domain = "";
       //System.out.println("Get percepts --->");
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
                            domain += "f([" + agent.x + "," + agent.y + "]). ";
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
                            domain += "box(" + obs.getId() + "). ";
                            //domain += "f([" + obs.x + "," + obs.y + "]). ";
                            //domain += "item(" + obs.getId() + "). ";
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
                            domain += "f([" + j + "," + i + "]). ";
                            //if(i == 3 && j == 2) {
                            //    System.out.println("   At 2,3 THERE IS FREE 1");
                            //}
                        }
                    }
                }
            }
       
       }
     
        String theory = getStatics() + domain;
        //System.out.println("statics:\n " + statics);
        //System.out.println("objects:\n " + world.getObjects().toString());
        //System.out.println("Num objects: " + world.getObjects().size() + "\n Theory:");
        
        //System.err.println(theory);
        this.state = new State(theory);
        //System.out.println("\n\n" + this.state.toString() + "\n\n");
        //System.out.println("Got these percepts: \n" + this.state.toString());
        //long time2 = System.currentTimeMillis();       
       //System.out.println("Percepts gotten in: " + (time2 - time1) + " ms");
    }

    public String getStatics() {
        //if(world)
        return statics;
    }

    @Override
    public void run() {
        //this.goals = new LinkedList<String>();
        //this.goals.add("agentAt(1,[1,7])");
        
        while(!this.done) {
            long timeTest1 = System.nanoTime();
            //System.out.println("\n");
            iteration++;

            try {
                //get percepts and update current state description
                getPercepts();
                long timeTest3 = System.nanoTime();
                //System.out.println("from start to after percepts: " + (timeTest3 - timeTest1) + " nanoseconds / " + (timeTest3 - timeTest1)/1000000 + " ms");
                    
                
                
                if(this.goal == null || this.goal.equals("") && !this.goals.isEmpty()) {
                    try {
                        this.goal = goals.pop();
                        this.plan = null;
                        //System.out.println("NEW GOAL: " + this.goal);
                    }
                    catch(NoSuchElementException e) {
                        this.done = true;
                        //System.err.println("DONE! ");
                        Thread.currentThread().join();
                        return;
                    }
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
                    ReturnInfo retInfo = this.plan.monitorPlan(state);
                    long timeTest2 = System.nanoTime();
                    //System.out.println("from start to after monitor: " + (timeTest2 - timeTest1) + " nanoseconds / " + (timeTest2 - timeTest1)/1000000 + " ms");
                    int planSucceed = retInfo.info;
                    // Plan is good
                    if(planSucceed == -1) {
                        valid = true;
                    }else if(planSucceed == -2) {
                        // Plan is good but goal is not fulfilled
                        System.out.println("REPLAN -> From Scratch");
                        
                        this.popPlan = null;  
                        this.plan = null;
                        valid = false;
                    }else{
                        if(this.planRepair) { 
                            System.out.println("   Recieved: " + planSucceed + " -> REPLAN -> Try to repair plan!");
                            // Plan is broken
                            Action failedAction = this.plan.list.get(planSucceed);

                            long time1 = System.nanoTime();
                            this.popPlan = this.popPlan.dropAllActionsEarlierThan(failedAction);
                            this.plan = getTotalOrderPlan(this.popPlan);

                            this.popPlan = repairPlan(this.popPlan, failedAction, retInfo.precondition, this.state);
                            //getTotalOrderPlan(this.popPlan, world).printSolution();
                            //this.popPlan.printToConsole();
                            //System.out.println("      Plan attempted repaired!");
                            if(this.popPlan == null || this.popPlan.isEmpty()) {
                                System.out.println("      Could not repair plan!");
                                valid = false;
                                this.plan = null;
                                this.popPlan = null;
                            }else{
                                valid = true;
                                System.out.println("      Plan repaired");
                                this.popPlan.printToConsole();
                                this.plan = getTotalOrderPlan(this.popPlan);
                                long time2 = System.nanoTime();
                                System.out.println("Plan repaired in: " + (time2 - time1) + " nanoseconds / " + (float)(time2 - time1)/1000000 + " ms");
                                //this.popPlan.printToConsole();
                                this.plan.printSolution();
                                //System.out.println("      New Plan:\n   " + this.plan + "\n");
                                continue;
                            }
                        }else{
                            System.out.println("REPLAN -> From Scratch");
                            this.plan = null;
                            this.popPlan = null;
                            valid = false;
                        }
                    }
                    //System.out.println("PLAN VALID: " + planSucceed + "  which is: " + valid);
                }
                
                //System.err.println("Free: " + state.state.solveboolean("f([1,3])"));
                if(this.plan != null && !this.plan.isEmpty() && valid) { // this.plan != null) {//
                    //System.out.println("Go --->");
                    // If it is, do the next action
                    Action next = this.plan.peep();
                    
                    //System.out.print("Take Next Action: " + next.name);
                    
                    // Apply next - act() ?
                    if(next.atomic) {
                        //System.out.println(" -- which is atomic");
                        
                        // If action succeeded
                        //boolean result = world.act(next.name);
                        
                        if(!world.act(next.name)) {
                            //System.out.println("Action " + next.name + " succeeded: " + false);
                            // Replan. Later on, try to introduce new open preconditions to popPlan instead and refine it further.
                            this.plan = null; 
                            this.popPlan = null;
                            valid = false;
                        }else{
                            this.plan.pop();
                            this.popPlan.safelyDeleteAction(next);
                    
                            //this.popPlan.printToConsole();
                            //if(next.equals(lastAtomicOnSubPlan)) {
                                //popPlan.actions.remove(next);
                            //}
                            //System.out.println("Action " + next.name + " succeeded: " + true);
                            //System.out.println(this.popPlan.printActions());
                        }
                    }else{
                       //System.out.println(" -- which is not atomic");

                       Action removeAction = plan.pop();
                       
                       POP popSubPlan = routeFinder.findPlan(world,next.name);
                       //TOPlan subPlan = getTotalOrderPlan(popSubPlan, world);
            
                       if(popSubPlan == null || this.popPlan.isEmpty()) {
                            //System.out.println("IMPOSSIBLE GOAL FOUND! SKIPPING");
                            if(this.goals.isEmpty()) {
                                this.goal = "";
                                //System.err.println("DONE! ");
                                //Thread.currentThread().join();
                                //return;
                            }else{
                                this.goal = this.goals.pop();
                            }
                       }else{
                            //System.out.println("\n   Before Merge:\n");
                            //this.popPlan.printToConsole();
                            this.popPlan = popPlan.insertPOPAt(popSubPlan, removeAction);
                            //System.out.println("\n   Plans Merged:\n");
                            
                            //this.popPlan.printToConsole();
                            this.plan = getTotalOrderPlan(this.popPlan);
                            //System.out.println("\nNew plan:\n" + this.plan + "\n");
                            //System.out.println("\n\n");
                            //lastAtomicOnSubPlan = subPlan.peepLast();
                            //subPlan.printSolution();
                            //this.plan.prependAll(subPlan);
                            //System.out.println("Sub-plan:\n" + this.plan);
                       }
                    }
                }else{
                    if(this.plan != null) {
                        //System.out.println("Replan: " + !valid);
                    }else{
                        System.out.println("Started planning for goal: " + this.goal + " -->");
                    }
                    // Else, make a new plan ( and perform the first action ? )

                    long time1 = System.nanoTime();
                    this.popPlan = findPlan(this.goal);
                    if(this.popPlan == null || this.popPlan.isEmpty()) {
                        System.out.println("IMPOSSIBLE GOAL FOUND! SKIPPING");
                        try {
                            this.goal = this.goals.pop();
                        }
                        catch(NoSuchElementException e) {
                            this.goal = null;
                        }
                    }else{
                        //System.out.println("Pop Plan: \n");
                        //this.popPlan.printToConsole();
                        //System.out.println("\n");
                        this.plan = getTotalOrderPlan(this.popPlan);
                        long time2 = System.nanoTime();
                        
                        System.out.println("Plan found in: " + (time2 - time1) + " nanoseconds / " + (float)(time2 - time1)/1000000 + " ms");
                        //this.popPlan.printToConsole();
                        System.out.println("\nPlan: \n" + this.plan.toString());
                        //System.out.println("Done...");
                    }
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(POPlanner.class.getName()).log(Level.SEVERE, null, ex);
            } catch (PrologException ex) {
                Logger.getLogger(POPlanner.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                world.draw();
                //Thread.sleep(5);
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            } catch(NullPointerException e) {
                
            }
            long timeTest2 = System.nanoTime();
            //System.out.println("Loop Took: " + (timeTest2 - timeTest1) + " nanoseconds / " + (timeTest2 - timeTest1)/1000000 + " ms");
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
            
            if(arg1 != null) {
                arg1 = arg1.replace(")", "");
            }
            
            if(arg2 != null) {
                arg2 = arg2.replace(")", "");
            }

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
    
    public POP findPlan(String goal) { 
        // Fix format to avoid prolog-collisions. 
        goal = goal.replaceAll("\\[\\s*([0-9]*)\\s*,\\s*([0-9]*)\\s*\\]", "[$1;$2]");
        POP pop = new POP(goal);
        return refinePlan(pop);
    }
        
    public POP repairPlan(POP pop, Action actionThatFailed, ArrayList<String> preqsThatFailed, State state) {
        
        
        for(String preqThatFailed : preqsThatFailed) {
            pop.addOpenPrecondition(preqThatFailed, actionThatFailed);
            System.out.println("   New OP: " + preqThatFailed + " for: " + actionThatFailed);
        }
        
        return refinePlan(pop);
    }
    
    private POP refinePlan(final POP pop) {
        // PARTIAL-ORDER PLAN-SPACE SEARCHING
        if(pop == null) {
            return null;
        }
        
        if(pop.hasCycles()) {
            return null;
        }
        
        if(pop.isSolution(state)) {
            //pop.printToConsole();
            return pop;
        }
        long time1 = System.nanoTime();
        
        OpenPrecondition oP = pop.pollOpenPreconditions();
        if(oP == null) {
            return null;
        }
        //System.out.println("OP: " + oP.toString());
        try {
            /*boolean found = false;
            ArrayList<Action> gottenActions = new ArrayList<>();
            for(Action bP : pop.actions) {
                for(String effect : bP.effects) {
                    if(oP.condition.trim().equals(effect.trim())) {
                        found = true;
                        gottenActions.add(bP);
                        break;
                    }
                }
            }
            if(!found) {
                gottenActions = findAction(oP.condition);
            }*/
            ArrayList<Action> gottenActions = findAction(oP.condition);
            //System.out.println("p:");
            //System.out.println("   " + actions.toString());
            for(Action A : gottenActions) {
                boolean newlyAdded = false;
                // Enforce Consistency 
                Action B = oP.action;
                CausalLink link = pop.addAndGetCausalLink(A, B, oP.condition);
                //System.out.println("      Adding ordering-1: " + A + " < " + B);
                pop.addOrderingConstraint(A, B);
                if(!pop.contains(A)) {
                    pop.addAction(A);
                    //System.out.println("      Adding ordering-2: " + pop.getStart() + " < " + A);
                    pop.addOrderingConstraint(pop.getStart(), A);
                    //pop.addOrderingConstraint(A, pop.getFinish());
                    newlyAdded = true;
                    
                    // Add new open preconditions
                    for(String P : A.openPreconditions) {
                        // Fix format to avoid prolog-collisions. 
                        P = P.replaceAll("\\[\\s*([0-9]*)\\s*,\\s*([0-9]*)\\s*\\]", "[$1;$2]");
                        pop.addOpenPrecondition(P, A);
                    }
                }
                
                // Resolve conflicts
                // Between the new causal link and all existing actions
                // copy actions to avoid concurrency
                Collection<Action> actionbackup = new ArrayList<Action>();
                for(Action C : pop.actions) {
                    actionbackup.add(C);
                }
                for(Action C : actionbackup) {
                    // If there is a (potential?) conclict between oA and link, resolve it:
                    if(conflict(link, C, pop)) {
                        
                        // CONFLICT! Solve it
                        //System.out.println("      link: " + link);
                        //System.out.println("      Adding ordering-3: " + B + " < " + C);
                        POP newPlan1 = refinePlan(pop.addOrderingConstraint(B, C));
                        if(newPlan1 != null) {
                            return newPlan1;
                        }
                        //System.out.println("      Adding ordering-4: " + C + " < " + A);
                        POP newPlan2 = refinePlan(pop.addOrderingConstraint(C, A));
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
                            //System.out.println("      Adding ordering-5: " + B + " < " + C);
                            POP newPlan1 = refinePlan(pop.addOrderingConstraint(B, C));
                            if(newPlan1 != null) {
                                return newPlan1;
                            }
                            //System.out.println("      Adding ordering-6: " + C + " < " + A);
                            POP newPlan2 = refinePlan(pop.addOrderingConstraint(C, A));
                            if(newPlan2 != null) {
                                return newPlan2;
                            }
                        }
                    }
                    //pop.addAction(A);
                    POP newPlan3 = refinePlan(pop);
                    if(newPlan3 != null) {
                        return newPlan3;
                    }
                }
            }
        } catch (InterruptedException ex) {
            //Logger.getLogger(BSPlanner.class.getName()).log(Level.SEVERE, null, ex);
        }
        //System.out.println("Time spent so far: " + (System.currentTimeMillis() - time) + " ms. Is solution?= " + pop.isSolution() + "\n\n");
        long time2 = System.nanoTime();
                        
        //System.out.println("   Open Precondition closed in : " + (time2 - time1) + " nanoseconds / " + (float)(time2 - time1)/1000000 + " ms");
        return refinePlan(pop);
        
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
    
    public ArrayList<Action> findAction(String goal) throws InterruptedException {
        //System.out.print("FindAction: " + goal);
        goal = goal.replaceAll("\\[\\s*([0-9]*)\\s*,\\s*([0-9]*)\\s*\\]", "[$1;$2]");
        //System.out.println("   --  " + goal);
        ArrayList<Action> actionsReturn = new ArrayList<Action>();
        boolean go;
        for(ActionSchema a : this.actions) {
            if(!a.expanded) {
                go = false;
                //System.out.println("Action: " + a.name);
                HashMap arguments = new HashMap<String,String>();
                arguments.put("Agent", "" + this.agentId);
                for(String e : a.effects) {

                    String[][] action = splitAction(e);
                    String[][] goalSplittet = splitAction(goal);

                    //System.out.println("   Effect : " + e);
                    //System.out.println("   Goal: " + goalSplittet[0][0] + " a: " + action[0][0]);
                    if(action[0][0].equals(goalSplittet[0][0])) {
                        go = true;
                        //System.out.println("      FOUND AN ACTION: " + a.name);
                        //System.out.println("      Head: " + action[0][0]);  
                        //System.out.println("      a: " + action[1].length);
                        for(int i = 0; i < action[1].length; i++) {
                            //System.out.println("         Contains: " + i + " -> " + action[1][i]);
                        }
                        
                        for(int i = 0; i < action[1].length; i++) {
                            if(action[1][i] != null) {
                                //System.out.print("         " + action[1][i] + ",");
                                arguments.put(action[1][i].trim(), goalSplittet[1][i].replaceAll(";", ","));
                            }
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
            neighbours += "neighbour([X1,Y1], [X2,Y2], n) :- B is Y1 - 1, Y2 = B, X1 = X2, f([X1,Y1]), f([X2,Y2]). ";
            neighbours += "neighbour([X1,Y1], [X2,Y2], s) :- B is Y1 + 1, Y2 = B, X2 = X1, f([X1,Y1]), f([X2,Y2]). ";
            neighbours += "neighbour([X1,Y1], [X2,Y2], e) :- B is X1 + 1, X2 = B, Y1 = Y2, f([X1,Y1]), f([X2,Y2]). ";
            neighbours += "neighbour([X1,Y1], [X2,Y2], w) :- B is X1 - 1, X2 = B, Y1 = Y2, f([X1,Y1]), f([X2,Y2]). ";
        */
        // Backward neighbours
        String neighbours = "";
            neighbours += "neighbour([X1,Y1], [X2,Y2], n) :- B is Y2 + 1, Y1 = B, X1 = X2, \\+w([X1,Y1]), \\+w([X2,Y2]). ";
            neighbours += "neighbour([X1,Y1], [X2,Y2], s) :- B is Y2 - 1, Y1 = B, X2 = X1, \\+w([X1,Y1]), \\+w([X2,Y2]). ";
            neighbours += "neighbour([X1,Y1], [X2,Y2], e) :- B is X2 - 1, X1 = B, Y1 = Y2, \\+w([X1,Y1]), \\+w([X2,Y2]). ";
            neighbours += "neighbour([X1,Y1], [X2,Y2], w) :- B is X2 + 1, X1 = B, Y1 = Y2, \\+w([X1,Y1]), \\+w([X2,Y2]). ";

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
        
	ActionSchema move1 = new ActionSchema("move", prerequisites1, "move(Agent,MovePos)", argse1, effects1, false, false);

	/* MoveAtomic  */
	ArrayList<String> argse2 = new ArrayList<String>();
	argse2.add("Agent");
	argse2.add("CurPos");
	argse2.add("MovePos");
	argse2.add("MoveDir");
        
	ArrayList<String> prerequisites2 = new ArrayList<String>();
	prerequisites2.add("agentAt(Agent,CurPos)");
        prerequisites2.add("neighbour(CurPos, MovePos, MoveDir)");
	prerequisites2.add("f(MovePos)");
	
	ArrayList<String> effects2 = new ArrayList<String>();
	effects2.add("agentAt(Agent,MovePos)");
	effects2.add("!agentAt(Agent,CurPos)");

	ActionSchema moveAtomic = new ActionSchema("moveAtomic", prerequisites2, "moveAtomic(Agent,MoveDir)", argse2, effects2, true, true);
	
	/* PickUp  */
	ArrayList<String> argse3 = new ArrayList<String>();
	argse3.add("Agent");
	argse3.add("CurPos");
	argse3.add("Item");
	
	ArrayList<String> prerequisites3 = new ArrayList<String>();
	prerequisites3.add("\\+carries(Agent, _)");
	prerequisites3.add("agentAt(Agent,CurPos)"); // agentAt(Agent, AgPos)
	prerequisites3.add("at(Item,CurPos)");
	prerequisites3.add("item(Item)");
	
	ArrayList<String> effects3 = new ArrayList<String>();
	effects3.add("!at(Item,CurPos)");
	effects3.add("carries(Agent,Item)");
	
	ActionSchema pickUp1 = new ActionSchema("pickUp", prerequisites3, "pickUp(Agent,Item)", argse3, effects3, false, true);
	
	/* Place  */
	ArrayList<String> argse4 = new ArrayList<String>();
	argse4.add("Agent");
	argse4.add("CurPos");
	argse4.add("Item");
	
	ArrayList<String> prerequisites4 = new ArrayList<String>();
	prerequisites4.add("agentAt(Agent,CurPos)");
	prerequisites4.add("carries(Agent,Item)");
	
	ArrayList<String> effects4 = new ArrayList<String>();
	effects4.add("at(Item,CurPos)");
	effects4.add("!carries(Agent,Item)");

	ActionSchema place1 = new ActionSchema("place", prerequisites4, "place(Agent,Item)", argse4, effects4, false, true);
     
    /* Smash  */
	ArrayList<String> argse5 = new ArrayList<String>();
	argse5.add("Agent");
	argse5.add("AgPosition");
	argse5.add("Object");
        argse5.add("BoxPosition");
        argse5.add("AnyDirection");
	
	ArrayList<String> prerequisites5 = new ArrayList<String>();
        prerequisites5.add("agentAt(Agent,AgPosition)");
        prerequisites5.add("f(AgPosition)");
        prerequisites5.add("neighbour(AgPosition, BoxPosition, AnyDirection)");
	prerequisites5.add("box(Object)");
        prerequisites5.add("at(Object,BoxPosition)");
	
        ArrayList<String> effects5 = new ArrayList<String>();
	effects5.add("f(BoxPosition)");
	effects5.add("!at(Object, BoxPosition)");
	effects5.add("!box(Object)");
        
	ActionSchema smash1 = new ActionSchema("smash", prerequisites5, "smash(Agent,Object)", argse5, effects5, false, true);

	ArrayList<ActionSchema> actions = new ArrayList<ActionSchema>();
	
	actions.add(move1);
	actions.add(moveAtomic);
	actions.add(pickUp1);
	actions.add(place1);
	actions.add(smash1);

	return actions;
}
}
