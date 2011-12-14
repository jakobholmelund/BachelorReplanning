package Planner.backward;

import Planner.*;
import gui.RouteFinder.Astar;
import jTrolog.errors.PrologException;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import worldmodel.*;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
    Astar routeFinder;
    private ArrayList<ActionStruct> actions;
    
    public BSPlanner(World world,int aid,String goal,String mid) {
        iteration = 0;
        this.plan = null;
        this.world = world;
        statics = createStatics();
        done = false;
        this.agentId = aid;
        this.missionId = mid;
        this.goal = goal;
        routeFinder = new Astar();
        createActions();
    }
    
    private void getPercepts() throws PrologException {
       long time1 = System.currentTimeMillis();
       String domain = "";
       
       // add to percepts            
       for(int i = 0; i < world.getX(); i++) {
            for(int j = 0; j < world.getY(); j++) {
                long key = world.getMap().keyFor(j, i);
                Object o = world.getMap().get(key);
                if(o == null){
                    domain += "f([" + i + "," + j + "]). ";
                }else if(o instanceof MapAgent) {
                    //System.err.println("Agent found");
                    MapAgent agent = (MapAgent) o;
                    domain += "agentAt(" + agent.getNumber() + ",[" + agent.x + "," + agent.y + "]). ";
                }else if(o instanceof MapBox) {
                    //System.err.println("Box found");
                    MapBox obs = (MapBox) o;
                    domain += "at(" + obs.getName() + ",[" + obs.x + "," + obs.y + "]). "; 
                    domain += "object(" + obs.getName() + "). ";
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
            /*try {
                getPercepts();
            } catch (PrologException ex) {
                Logger.getLogger(BSPlanner.class.getName()).log(Level.SEVERE, null, ex);
            }
            Problem p;
            try {
                p = new Problem(1,"", this.actions);
                this.plan = findPlan(p, "at(a,[5,5])");
                System.out.println("Plan:\n" + this.plan);
            } catch (PrologException ex) {
                Logger.getLogger(BSPlanner.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.done = true;*/
            
            Problem p;
            try {
                //get percepts and update current state description
                getPercepts();
                //System.err.println("f(3,3): " + state.state.solveboolean("f([3,3]). "));
                //System.err.println("f(10, 10): " + state.state.solveboolean("f([10,10]). "));

                //check if plan is still valid
                boolean valid = false;
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
                if(this.plan != null) {//this.plan != null && !this.plan.isEmpty() && valid) {

                    // If it is, do the next action
                    Actions next = plan.pop();
                    System.err.println("Take Next Action: " + next.name);

                    // Apply next - act() ?
                    if(next.atomic) {
                        world.newAgentActionParse(next.name);
                    }else{
                       Plan subPlan = routeFinder.findPlan(world,next.name);
                       //subPlan.printSolution();
                       this.plan.prependAll(subPlan);
                       //System.out.println("New plan:\n" + this.plan);
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
                    this.plan = findPlan(p, this.goal);
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
            return new String[][]{new String[]{command},new String[]{arg1,arg2}};
        }
        return null;
        /*
        System.out.println(action);
        
        
        action = action.replaceAll("\\[([0-9]*),([0-9]*)\\]", "[$1;$2]");
        String[] res = action.split("\\(");
        String head = res[0];
        System.out.println("Rest: " + res[1]);
        String[] args = res[1].substring(0, res[1].length()-1).trim().split("\\,");
        
        String[][] result = new String[2][];
        String[] headT = {head};
        
        result[0] = headT;
        result[1] = args;
        
        for(String[] s:result){
            for(String s2:s){
                System.out.println(s2);
            }
        }
        return result;
         * 
         */
    }
    
    public Plan findPlan(Problem p, String goal) {
        goal = goal.replaceAll("\\[([0-9]*),([0-9]*)\\]", "[$1;$2]");
        Plan plan = new Plan();
        try {
            ArrayList<Actions> actions = findAction(p, goal);
            //System.out.println("p:");
            //System.out.println("   " + actions.toString());
            for(Actions a : actions) {
                for(String openPrecondition : a.openPreconditions) {
                    //System.out.println("Find plan for: " + openPrecondition);
                    plan.appendAll(findPlan(p, openPrecondition));
                }
            }
            plan.appendAll(actions);
        } catch (InterruptedException ex) {
            //Logger.getLogger(BSPlanner.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return plan;
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
      
        //String goal = "goalAt(a, [6,11]). ";
        //String free = "f(C0) :- \\+w(C0), \\+agentAt(Agent, C0), \\+boxAt(Box, C0). ";
        //String equals = "equals(A, B) :- B = A. ";
        
        String rules = ""; //equals; //  move + push + pull + neighbours + 

        return rules;
    }
    
    public boolean done() {
        return done;
    }
    
    void createActions() {
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
	
	ArrayList<String> requirements1 = new ArrayList<String>();
	requirements1.add("f(MovePos)");

	ActionStruct move = new ActionStruct("move", prerequisites1, "move(Agent,MovePos)", argse1, effects1, requirements1, false, false);
	
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
	
	ArrayList<String> requirements2 = new ArrayList<String>();
	requirements2.add("f(MovePos)");

	ActionStruct moveAtomic = new ActionStruct("moveAtomic", prerequisites2, "moveAtomic(Agent,MovePos)", argse2, effects2, requirements2, true, true);
	
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
	
	ArrayList<String> requirements3 = new ArrayList<String>();
	requirements3.add("at(Object,ObjPos)");
	
	ActionStruct pickUp = new ActionStruct("pickUp", prerequisites3, "pickUp(Agent,Object)", argse3, effects3, requirements3, false, true);
	
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
	
	ArrayList<String> requirements4 = new ArrayList<String>();
	requirements4.add("f(AgPos)");
	requirements4.add("carries(Agent,Object)");
	
	ActionStruct place = new ActionStruct("place", prerequisites4, "place(Agent,Object)", argse4, effects4, requirements4, false, true);

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
	
	ArrayList<String> requirements5 = new ArrayList<String>();
	
	ActionStruct useTeleporter = new ActionStruct("useTeleporter", prerequisites5, "useTeleporter(Agent,Teleporter)", argse5, effects5, requirements5, false, true);
	
        this.actions = new ArrayList<ActionStruct>();
        actions.add(move);
        actions.add(moveAtomic);
        actions.add(pickUp);
        actions.add(place);
        //actions.add(useTeleporter);
    }    
}
