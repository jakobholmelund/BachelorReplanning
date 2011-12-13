/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Planner.backward;

import Planner.Node;
import Planner.State;
import Planner.*;
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
    
    public Problem(int aid,String missionId, ArrayList<ActionStruct> actions) throws PrologException {
        this.logic = new Logic();
        agent=aid;
        currentBox = missionId;
        currentBoxGoalPos = missionId;
        this.actions = actions;
        // move to world or agent definition.
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
           if(!a.expanded) {
                try {
                    ArrayList<Actions> acs = a.get(logic, arguments);
                    //System.err.println("Gotten: " + acs.toString());
                    actionsReturn.addAll(acs);
                } catch (PrologException ex) {
                    Logger.getLogger(Problem.class.getName()).log(Level.SEVERE, null, ex);
                }
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