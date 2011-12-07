/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Planner.backward;

import Planner.Logic;
import jTrolog.engine.Solution;
import jTrolog.errors.PrologException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Problem {
    State initial;
    String goal;
    Logic logic;
    int agent = 1;
    LinkedList<State> futureGoals = new LinkedList<State>();
    String currentBox;
    String currentBoxGoalPos;
    public ArrayList<ActionStruct> actions;
    
    public Problem(int aid,String missionId) throws PrologException {
        this.logic = new Logic();
        agent=aid;
        currentBox = missionId;
        currentBoxGoalPos = missionId;
        
        // move to world or agent definition.
        
        /* Move  */
        ArrayList<String> argse1 = new ArrayList<String>();
        argse1.add("Agent");
        argse1.add("MoveDir");
        argse1.add("C0");
        argse1.add("C1");
        
        ArrayList<String> effects1 = new ArrayList<String>();
        effects1.add("agentAt(Agent,C1)");
        effects1.add("!agentAt(Agent,C0)");
        
        ArrayList<String> requirements1 = new ArrayList<String>();
        requirements1.add("f(C1)");
        
        ArrayList<String> prerequisites1 = new ArrayList<String>();
        prerequisites1.add("agentAt(Agent, C0)");
        prerequisites1.add("neighbour(C0, C1, MoveDir)");
        prerequisites1.add("f(C1)");

        ActionStruct move = new ActionStruct("move", prerequisites1, "move(Agent,MoveDir)", argse1, effects1, requirements1);
        
        /* Pull  */
        ArrayList<String> argse2 = new ArrayList<String>();
        argse2.add("Agent");
        argse2.add("MoveDir");
        argse2.add("CurrDir");
        argse2.add("C0");
        argse2.add("C1");
        argse2.add("C2");
        argse2.add("Box");
        
        ArrayList<String> effects2 = new ArrayList<String>();
        effects2.add("agentAt(Agent,C1)");
        effects2.add("!agentAt(Agent,C0)");
        effects2.add("boxAt(Box,C0)");
        effects2.add("!boxAt(Box,C2)");

        ArrayList<String> requirements2 = new ArrayList<String>();
        requirements2.add("f(C1)");
        
        ArrayList<String> prerequisites2 = new ArrayList<String>();
        prerequisites2.add("agentAt(Agent, C0)");
        prerequisites2.add("neighbour(C0, C1, MoveDir)");
        prerequisites2.add("boxAt(Box, C2)");
        prerequisites2.add("neighbour(C0, C2, CurrDir)");
        prerequisites2.add("f(C1)");

        ActionStruct pull = new ActionStruct("pull", prerequisites2, "pull(Agent,MoveDir,CurrDir)", argse2, effects2, requirements2);
        
        /* Push  */
        ArrayList<String> argse3 = new ArrayList<String>();
        argse3.add("Agent");
        argse3.add("MoveDir");
        argse3.add("MovePush");
        argse3.add("C0");
        argse3.add("C1");
        argse3.add("C2");
        argse3.add("Box");
        
        ArrayList<String> effects3 = new ArrayList<String>();
        effects3.add("agentAt(Agent,C1)");
        effects3.add("!agentAt(Agent,C0)");
        effects3.add("boxAt(Box,C2)");
        effects3.add("!boxAt(Box,C1)");

        ArrayList<String> requirements3 = new ArrayList<String>();
        requirements3.add("f(C2)");
        
        ArrayList<String> prerequisites3 = new ArrayList<String>();
        prerequisites3.add("agentAt(Agent, C0)");
        prerequisites3.add("neighbour(C0, C1, MoveDir)");
        prerequisites3.add("boxAt(Box, C1)");
        prerequisites3.add("neighbour(C1, C2, MovePush)");
        prerequisites3.add("f(C2)");

        ActionStruct push = new ActionStruct("push", prerequisites3, "push(Agent,MoveDir,MovePush) ", argse3, effects3, requirements3);
        
        this.actions = new ArrayList<ActionStruct>();
        actions.add(move);
        actions.add(pull);
        actions.add(push);
    }
    
    @Override
    public String toString() {
        return "Initial: " + initial.toString() + " Goal: " + goal.toString();
    }
    
    public boolean goalTest(State s) {
        setState(s);
        boolean bol = logic.solveboolean(getGoal());
        return bol;
    }
    
    public State getInitial() {
        return initial;
    }
    
    public String getGoal() {
        return goal;
    }
    
    /*
     * Calculates the heuristik for the state.
     *
     * @param s1
     * @param a
     * @param node 
     * @return
     */

    public double heuristik(State s1, Actions a, Node node) {
        try {
            setState(s1);
            double agentToBox;
            double boxToGoal;

            Solution n = logic.solve("agentAt(" + agent + ", N).");
            Solution g = logic.solve("goalAt("+currentBoxGoalPos+", G).");
            Solution b = logic.solve("boxAt("+currentBoxGoalPos+", B).");
            String N = "" + n.getBinding("N"); //[1,3]
            String G = "" + g.getBinding("G"); //[1,3]
            String B = "" + b.getBinding("B"); //[1,3]
            N = N.replace("[", "");
            N = N.replace("]", "");
            G = G.replace("[", "");
            G = G.replace("]", "");
            B = B.replace("[", "");
            B = B.replace("]", "");
            String[] nxa = N.split(",");
            String[] goala = G.split(",");
            String[] boxa = B.split(",");
            double nx = Double.parseDouble(nxa[0]);
            double ny = Double.parseDouble(nxa[1]);
            double goalx = Double.parseDouble(goala[0]);
            double goaly = Double.parseDouble(goala[1]);
            double boxx = Double.parseDouble(boxa[0]);
            double boxy = Double.parseDouble(boxa[1]);
            //System.out.println("AgentAt: " + nx + "," + ny + " boxat: " + boxx + "," + boxy);
            agentToBox = (Math.abs(nx - boxx) + Math.abs(ny - boxy));
            boxToGoal = (Math.abs(boxx - goalx) + Math.abs(boxy - goaly));

            double h = agentToBox + boxToGoal;
            //System.err.println("AgentToBox: " + agentToBox + "   BoxToGoal: " + boxToGoal + " h: " + h);
            if (h < 0) {
                h *= -1;
            }
            return h;
        } catch (Exception ex) {
            //System.out.println(ex.);
            ex.printStackTrace();
            return 1;
        }
    }

    public double cost(State s1, Actions a) {
        return 1;
    }

    private void setState(State s) {
        //System.err.println("State size: " + s.state.length());
        if (s != null) {
            //logic.setTheory(s.state);
            logic = s.state;
            //System.err.println("real state: " + engine.getTheoryAsString());
        }
    }

    /**
     * Returns the actions applicable from this state. Needs to be made more general to allow for a parsed action set.
     *
     *
     * @param s
     * @return The actions applicable from this state
     */
    public ArrayList<Actions> actions(State s) {
        setState(s);
        //System.err.println("agent: " + agent);
        HashMap arguments = new HashMap<String,String>();
        arguments.put("Agent", "" + agent);
        //System.out.println("actions called!!!!!");
        //System.err.println("state: \n" + s.toString());
        ArrayList<Actions> actionsReturn = new ArrayList<Actions>();
        for(ActionStruct a : this.actions) {
            try {
                ArrayList<Actions> acs = a.get(logic, arguments);
                //System.err.println("Gotten: " + acs.toString());
                actionsReturn.addAll(acs);
            } catch (PrologException ex) {
                Logger.getLogger(Problem.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        //System.err.println("!!Getting Actions!");
        //System.err.println("!!Actions: ");
        for(Actions a : actionsReturn) {
            //System.out.println("action: " + a.toString());
        }
        return actionsReturn;
    }

    public State result(State s, Actions a) {
        //setState(s);
        Logic logicClone = s.state.clone();
        //System.err.println("Action : " + a.name);
        //System.out.println("\nSTATE 1: " + logic.getTheoryAsString());
        //System.out.println("Action: " + a.name);
        for (String string : a.effect) {
            try {
                logicClone.set(string);
                //System.out.println("EFFECT: " + string);
            } catch (Throwable ex) {
                Logger.getLogger(Problem.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //String s2 = logic.engine.getTheory().toString();
        //System.out.println("\nSTATE 2: " + logic.getTheoryAsString() + "\n\n\n");

        return new State(logicClone);
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public void setInitial(State init) {
        this.initial = init;

    }
}
