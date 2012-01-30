package Planner.NFSP;

import jTrolog.errors.PrologException;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import worldmodel.*;
import Planner.*;
import Planner.POP.POP;
import gui.RouteFinder.Astar;
import java.util.ArrayList;
public class NFSPlanner implements Runnable{ //  implements Runnable
    State state;
    TOPlan plan;
    public int iteration;
    World world;
    String statics;
    boolean done;
    int agentId;
    String goal;
    String missionId;
    Astar routeFinder;
    private ArrayList<ActionSchema> actions;
    
    public NFSPlanner(World world,int aid,String goal,String mid) {
        iteration = 0;
        this.plan = null;
        this.world = world;
        done = false;
        this.agentId = aid;
        this.missionId = mid;
        this.goal = goal;
        routeFinder = new Astar();
        this.actions = this.setActions();
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
        
        //System.err.println(theory);
        this.state = new State(theory);
        //System.out.println("\n\n" + this.state.toString() + "\n\n");
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
        NFSPProblem p;
        try {
            statics = createStatics();
            p = new NFSPProblem(agentId,missionId, this.actions);
        } catch (PrologException ex) {
            Logger.getLogger(NFSPlanner.class.getName()).log(Level.SEVERE, null, ex);
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
                ReturnInfo retInfo = this.plan.monitorPlan(state); 
                int planSucceed = retInfo.info;
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
            if(this.plan != null && !this.plan.isEmpty() && valid) {

                // If it is, do the next action
                Action next = plan.pop();
                System.err.println("Take Next Action: " + next.name);
                
                // Apply next - act() ?
                if(next.atomic) {
                    world.act(next.name);
                    //world.agentActionParse(next.name);
                }else{
                   POP popSubPlan = routeFinder.findPlan(world,next.name);
                   TOPlan subPlan = popSubPlan.getLinearization();
                   //subPlan.printSolution();
                   this.plan.prependAll(subPlan);
                }
                //world.agentActionParse(next.name);
                
            }else{
                if(this.plan != null) {
                    //System.out.println("Replan: " + !valid);
                }else{
                    //System.out.println("Started planning: ");
                }
                // Else, make a new plan ( and perform the first action ? )
                p = new NFSPProblem(agentId,missionId, this.actions);
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
            Logger.getLogger(NFSPlanner.class.getName()).log(Level.SEVERE, null, ex);
        } catch (PrologException ex) {
            Logger.getLogger(NFSPlanner.class.getName()).log(Level.SEVERE, null, ex);
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
    
    public TOPlan findPlan(NFSPProblem p) throws InterruptedException {
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
            System.out.println("Take action: " + n.toString());
            if (p.goalTest(n.s)) {
                //System.out.println("State Space Size: " + states);
                return makeSolution(n, p);
            }
            //System.out.println("# Actions: " + frontier.toString());
            
            long time1 = System.currentTimeMillis();
            
            
            ArrayList<Action> actionsGotten = p.actions(n.s);
            System.out.println("Actions expanded " + actionsGotten.size());
            System.out.println("   Actions Gotten: " + actionsGotten.toString() + "\n");
            for (Action a : actionsGotten) {
                //System.err.println("ACTION!!!!: " + a.name);
                State s1 = p.result(n.s, a);
                frontier.add(new Node(s1, n, a, n.g + p.cost(s1, a), p.heuristik(s1, a, n)));
                states++;
                long time2 = System.currentTimeMillis();
            }
            long time3 = System.currentTimeMillis();
            System.out.println("Got actions and expanded ALL: " + (time3-time1) + " ms");
        }
    }

    private Node makeInitialNode(State initial) {
        return new Node(initial, null, null, 0, 0);
    }

    private TOPlan makeSolution(Node n, NFSPProblem p) {
        TOPlan s = new TOPlan(this.goal);
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
        for(ActionSchema a : this.actions) {
            //System.out.println(a.prerequsiteString);
            actions += a.prerequsiteString;
        }
        //System.out.println(actions);
        
        //String goal = "goalAt(a, [6,11]). ";
        //String free = "f(C0) :- \\+w(C0), \\+agentAt(Agent, C0), \\+at(Box, C0). ";
        
        String neighbours = "";
            neighbours += "neighbour([X1,Y1], [X2,Y2], n) :- B is Y1 - 1, Y2 = B, X1 = X2. ";
            neighbours += "neighbour([X1,Y1], [X2,Y2], s) :- B is Y1 + 1, Y2 = B, X2 = X1. ";
            neighbours += "neighbour([X1,Y1], [X2,Y2], e) :- B is X1 + 1, X2 = B, Y1 = Y2, \\+w([X1,Y1]), \\+w([X2,Y2]). ";
            neighbours += "neighbour([X1,Y1], [X2,Y2], w) :- B is X1 - 1, X2 = B, Y1 = Y2, \\+w([X1,Y1]), \\+w([X2,Y2]). ";
        // To avoid unknown-predicate errors
        String carries = "carries(none, object). ";    
        String at = "at(none, nowhere). ";
        String agentAt = "agentAt(none, nowhere). ";
                
        String rules = actions + neighbours + carries + at + agentAt;
        
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
        
	ActionSchema move = new ActionSchema("move", prerequisites1, "move(Agent,MovePos)", argse1, effects1, true, false);
	//CurPos
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
	
	//ArrayList<String> requirements2 = new ArrayList<String>();
	//requirements2.add("f(MovePos)");

	ActionSchema moveAtomic = new ActionSchema("moveAtomic", prerequisites2, "moveAtomic(Agent,MoveDir)", argse2, effects2, false, true);
	
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
        
	ActionSchema smash = new ActionSchema("smash", prerequisites5, "smash(Agent,Object)", argse5, effects5, false, true);

        ArrayList<ActionSchema> actions = new ArrayList<ActionSchema>();
        actions.add(move);
        actions.add(moveAtomic);
        actions.add(pickUp);
        actions.add(place);
        actions.add(smash);
        
        return actions;
    }
    
}
