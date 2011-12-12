package Planner.forward;

import jTrolog.errors.PrologException;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import worldmodel.*;
import Planner.*;
import gui.RouteFinder.Astar;
import java.util.ArrayList;
public class FSPlanner implements Runnable{ //  implements Runnable
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
    
    public FSPlanner(World world,int aid,String goal,String mid) {
        iteration = 0;
        this.plan = null;
        this.world = world;
        done = false;
        this.agentId = aid;
        this.missionId = mid;
        this.goal = "agentAt(1, [5,5]). "; //goal;
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
        }
        /*
        for(Object o : world.getObjects()) {    
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
            }*/
            /*if(o instanceof MapAgent) {
                //System.err.println("Agent found");
                MapAgent agent = (MapAgent) o;
                domain += "agentAt(" + agent.getNumber() + ",[" + agent.x + "," + agent.y + "]). ";
            }else if(o instanceof Bomb) {
                //System.err.println("Agent found");
                Bomb bomb = (Bomb) o;
                domain += "At(" + bomb.getNumber() + ",[" + bomb.x + "," + bomb.y + "]). ";
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
        }*/
        
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
        Problem p;
        try {
            p = new Problem(agentId,missionId, this.actions);
            statics = createStatics();
        } catch (PrologException ex) {
            Logger.getLogger(FSPlanner.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        while(!this.done){
        System.out.println("New iteration");
        this.iteration++;

        try {
            // get percepts and update current state description
            this.getPercepts();
            //System.err.println("f(3,3): " + state.state.solveboolean("f([3,3]). "));
            //System.err.println("f(10, 10): " + state.state.solveboolean("f([10,10]). "));
            
            // check if plan is still valid
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
            if(this.plan != null && !this.plan.isEmpty() && valid) {

                // If it is, do the next action
                Actions next = plan.pop();
                System.err.println("Take Next Action: " + next.name);
                
                // Apply next - act() ?
                if(next.atomic) {
                    world.agentActionParse(next.name);
                }else{
                   Plan subPlan = routeFinder.findPlan(world,next.name);
                   subPlan.printSolution();
                   this.plan = subPlan;
                }
                //world.agentActionParse(next.name);
                
            }else{
                if(this.plan != null) {
                    //System.out.println("Replan: " + !valid);
                }else{
                    //System.out.println("Started planning: ");
                }
                // Else, make a new plan ( and perform the first action ? )
                p = new Problem(agentId,missionId, this.actions);
                statics = createStatics();
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
        try {
            world.draw();
            Thread.sleep(200);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
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
            System.out.println("First action: " + n.toString());
            if (p.goalTest(n.s)) {
                //System.out.println("State Space Size: " + states);
                return makeSolution(n, p);
            }
            System.err.println("# Actions: " + p.actions(n.s).size());
            for (Actions a : p.actions(n.s)) {
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
        //String move = "move(Agent, MoveDirAgent, C0, C1) :- agentAt(Agent, C0), neighbour(C0, C1, MoveDirAgent), f(C1). ";
        //String push = "push(Agent, MoveDirAgent, MoveDirBox, C0, C1, C2, Box) :- agentAt(Agent, C0), neighbour(C0, C1, MoveDirAgent), boxAt(Box, C1), neighbour(C1, C2, MoveDirBox), f(C2). ";
        //String pull = "pull(Agent, MoveDirAgent, CurrDirBox, C0, C1, C2, Box) :- agentAt(Agent, C0), neighbour(C0, C1, MoveDirAgent), boxAt(Box, C2), neighbour(C0, C2, CurrDirBox), f(C1). ";
        
        String actions = "";
        for(ActionStruct a : this.actions) {
            actions += a.prerequsiteString;
        }
        
        //String goal = "goalAt(a, [6,11]). ";
        String free = "f(C0) :- \\+w(C0), \\+agentAt(Agent, C0), \\+boxAt(Box, C0). ";
        
        //String neighbours = "";
        //    neighbours += "neighbour([X1,Y1], [X2,Y2], n) :- B is Y1 - 1, Y2 = B, X1 = X2. ";
        //    neighbours += "neighbour([X1,Y1], [X2,Y2], s) :- B is Y1 + 1, Y2 = B, X2 = X1. ";
        //    neighbours += "neighbour([X1,Y1], [X2,Y2], e) :- B is X1 + 1, X2 = B, Y1 = Y2. ";
        //    neighbours += "neighbour([X1,Y1], [X2,Y2], w) :- B is X1 - 1, X2 = B, Y1 = Y2. ";
        String rules = actions + free; //neighbours;
        
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
	prerequisites1.add("agentAt(Agent, CurPos)");
	prerequisites1.add("f(MovePos)");
	
	ArrayList<String> effects1 = new ArrayList<String>();
	effects1.add("agentAt(Agent,MovePos)");
	effects1.add("!agentAt(Agent,CurPos)");
	
	ArrayList<String> requirements1 = new ArrayList<String>();
	requirements1.add("f(MovePos)");

	ActionStruct move = new ActionStruct("move", prerequisites1, "Move(Agent,MovePos)", argse1, effects1, requirements1, false, false);
	
	/* MoveAtomic  */
	ArrayList<String> argse2 = new ArrayList<String>();
	argse2.add("Agent");
	argse2.add("CurPos");
	argse2.add("MovePos");
	
	ArrayList<String> prerequisites2 = new ArrayList<String>();
	prerequisites2.add("agentAt(Agent, CurPos)");
	prerequisites2.add("f(MovePos)");
	
	ArrayList<String> effects2 = new ArrayList<String>();
	effects2.add("agentAt(Agent,MovePos)");
	effects2.add("!agentAt(Agent,CurPos)");
	
	ArrayList<String> requirements2 = new ArrayList<String>();
	requirements2.add("f(MovePos)");

	ActionStruct moveAtomic = new ActionStruct("moveAtomic", prerequisites2, "MoveAtomic(Agent,MovePos)", argse2, effects2, requirements2, true, true);
	
	// object(Object) :- box(Object).
	// object(Object) :- bomb(Object).	
	
	/* PickUp  */
	ArrayList<String> argse3 = new ArrayList<String>();
	argse3.add("Agent");
	argse3.add("AgPos");
	argse3.add("ObjectPos");
	argse3.add("Object");
	
	ArrayList<String> prerequisites3 = new ArrayList<String>();
	prerequisites3.add("\\+carries(Agent, OtherObject)");
	prerequisites3.add("agentAt(Agent, AgPos)");
	prerequisites3.add("ObjectPos = AgPos");
	prerequisites3.add("at(Object, ObjectPos)");
	prerequisites3.add("object(Object)");
	
	ArrayList<String> effects3 = new ArrayList<String>();
	effects3.add("!at(Object, ObjectPos)");
	effects3.add("carries(Agent, Object)");
	
	ArrayList<String> requirements3 = new ArrayList<String>();
	requirements3.add("at(Object, ObjectPos)");
	
	ActionStruct pickUp = new ActionStruct("pickUp", prerequisites3, "PickUp(Agent,Object)", argse3, effects3, requirements3, false, true);
	
	/* Place  */
	ArrayList<String> argse4 = new ArrayList<String>();
	argse4.add("Agent");
	argse4.add("AgPos");
	argse4.add("ObjectPos");
	argse4.add("Object");
	
	ArrayList<String> prerequisites4 = new ArrayList<String>();
	prerequisites4.add("agentAt(Agent, AgPos)");
	prerequisites4.add("ObjectPos = AgPos");
	prerequisites4.add("carries(Agent, Object)");
	
	ArrayList<String> effects4 = new ArrayList<String>();
	effects4.add("at(Object, ObjectPos)");
	effects4.add("!carries(Agent, Object)");
	
	ArrayList<String> requirements4 = new ArrayList<String>();
	requirements4.add("f(ObjectPos)");
	requirements4.add("carries(Agent, Object)");
	
	ActionStruct place = new ActionStruct("place", prerequisites4, "Place(Agent,Object)", argse4, effects4, requirements4, false, true);

	/* Use Teleporter  */
	ArrayList<String> argse5 = new ArrayList<String>();
	argse5.add("Agent");
	argse5.add("AgPos");
	argse5.add("TeleporterPos");
	argse5.add("Teleporter");
	argse5.add("To");
	
	ArrayList<String> prerequisites5 = new ArrayList<String>();
	prerequisites5.add("agentAt(Agent, AgPos)");
	prerequisites5.add("AgPos = TeleporterPos");
	// prerequisites5.add("at(Teleporter, TeleporterPos)"); // ? Necessary ?
	prerequisites5.add("teleporter(Teleporter, TeleporterPos, To)");
	
	ArrayList<String> effects5 = new ArrayList<String>();
	effects5.add("agentAt(Agent, To)");
	effects5.add("!agentAt(Agent, AgPos)");
	
	ArrayList<String> requirements5 = new ArrayList<String>();
	
	ActionStruct useTeleporter = new ActionStruct("useTeleporter", prerequisites5, "UseTeleporter(Agent,Teleporter)", argse5, effects5, requirements5, false, true);
	
        this.actions = new ArrayList<ActionStruct>();
        actions.add(move);
        actions.add(moveAtomic);
        actions.add(pickUp);
        actions.add(place);
        //actions.add(useTeleporter);
    }
    
}