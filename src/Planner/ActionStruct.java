/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Planner;

import Planner.forward.Actions;
import jTrolog.engine.Solution;
import jTrolog.errors.PrologException;
import java.util.ArrayList;

/**
 *
 * @author Dan True
 */
public class ActionStruct {
    String name;
    String prerequsite;
    String[] args;
    String[] effects;
    String[] requirements;
    
    public ActionStruct(String name, String prerequsite, String[] args, String[] effects, String[] requirements) {
        this.name = name.toLowerCase();
        this.args = args;
        this.prerequsite = prerequsite; // "move(Agent, MoveDirAgent, C0, C1) :- 
                                        // agentAt(Agent, C0), neighbour(C0, C1, MoveDirAgent), f(C1). ";
        this.effects = effects;
        this.requirements = requirements;
    }
    
    void get() throws PrologException {
        Logic logic = new Logic();
        int agent = 0;
        ArrayList<Actions> actionsReturn = new ArrayList<Actions>();
        
        ArrayList<Solution> sol = logic.solveAll("move(" + agent + ", MoveDirAgent, C0, C1). ");
       
        for (Solution si : sol) {
            //System.err.println("move: " + si.bindingsToString());
            try {
                //System.out.println("SI: " + si.toString()  + "  bindings: " + si.getBindings());
                String MoveDirAgent = "" + si.getBinding("MoveDirAgent");
                //System.out.println("Dir: " + MoveDirAgent);
                MoveDirAgent = MoveDirAgent.replace("'", "");
                String C0 = "" + si.getBinding("C0");
                //System.out.println("C0: " + C0);
                String C1 = "" + si.getBinding("C1");
                //System.out.println("C1: " + C1);
                Actions act = new Actions("Move(" + agent + "," + MoveDirAgent + ")");
                act.addEffect("agentAt(" + agent + "," + C1 + ")");
                act.addEffect("!agentAt(" + agent + "," + C0 + ")");

                act.addRequirement("f(" + C1 + ")");
                
                act.box = "";
                act.moveAgentDir = MoveDirAgent;
                //System.err.println(act.toString());

                actionsReturn.add(act);

            } catch (Exception e) {
                                e.printStackTrace();
            }
        }
     }
}
