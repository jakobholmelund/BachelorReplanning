/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Planner.NFSP;

import jTrolog.engine.Solution;
import jTrolog.errors.PrologException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import Planner.*;
/**
 *
 * @author jakobsuper
 */
public class NFSPProblem {
    State initial;
    String goal;
    Logic logic;
    int agent = 1;
    LinkedList<State> futureGoals = new LinkedList<State>();
    String currentBox;
    String currentBoxGoalPos;
    ArrayList<ActionSchema> actions;
    
    public NFSPProblem(int aid,String missionId, ArrayList<ActionSchema> actions) throws PrologException {
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
        //System.out.println("Goal: " + getGoal() + " is: " + bol);
        return bol;
    }

    public State getInitial() {
        return initial;
    }

    public String getGoal() {
        return goal;
    }

    public double heuristik(State s1, Action a, Node node) {
        /*
        try {
            setState(s1);
            if(logic.solveboolean(getGoal())) {
                return -10;
            }
            
            boolean carries = logic.solveboolean("carries(" + agent + ", N).");
            //System.err.println("Carries: " + carries + " for action " + a.getAction());
            
            if(!carries) {
                double agentToObject;
                Solution n = logic.solve("agentAt(" + agent + ", N).");
                String N = "" + n.getBinding("N"); //[1,3]
                //if(N.equals("[6, 12]")) {
                    //System.out.println(s1.toString());
                //}
                
                N = N.replace("[", "");
                N = N.replace("]", "");
                String[] nxa = N.split(",");

                double agentx = Double.parseDouble(nxa[0]);
                double agenty = Double.parseDouble(nxa[1]);
                
                Solution b = logic.solve("at(" + currentBoxGoalPos + ", B).");
                String B = "" + b.getBinding("B"); //[1,3]
                if(B.equals("null")) {
                    //System.err.println(s1.toString());
                }
                
                System.out.println(B);
                
                B = B.replace("[", "");
                B = B.replace("]", "");
                //System.err.println("B: " + B);
                String[] boxa = B.split(",");
                double objectx = Double.parseDouble(boxa[0]);
                double objecty = Double.parseDouble(boxa[1]);
                agentToObject = (Math.abs(agentx - objectx) + Math.abs(agenty - objecty));
                //System.out.println("Agent At: " + N + " objectAt: " + B + " H: " + agentToObject);
                
                return agentToObject;
            }else{
                double agentToGoal;
                if(a.getAction().equals("pickUp(" + agent + "," + currentBox + ")")) {
                    return -1;
                }
                //System.out.println("Trying to find: " + "pickUp(" + agent + "," + currentBox + ")");
                
                Solution n = logic.solve("agentAt(" + agent + ", N).");
                String N = "" + n.getBinding("N"); //[1,3]
                N = N.replace("[", "");
                N = N.replace("]", "");
                String[] nxa = N.split(",");

                double agentx = Double.parseDouble(nxa[0]);
                double agenty = Double.parseDouble(nxa[1]);
                
                Solution g = logic.solve("goalAt(" + currentBoxGoalPos + ", G).");
                String G = "" + g.getBinding("G"); //[1,3]
                G = G.replace("[", "");
                G = G.replace("]", "");
                String[] goala = G.split(",");
                double goalx = Double.parseDouble(goala[0]);
                double goaly = Double.parseDouble(goala[1]);
                
                agentToGoal = (Math.abs(agentx - goalx) + Math.abs(agenty - goaly));
                //System.out.println("Agent At: " + N + " objectAt: " + G + " H: " + agentToGoal);
                
                return agentToGoal;
            }
        } catch (Exception ex) {
            //System.out.println(ex.);
            ex.printStackTrace();
            return 1;
        }*/
        return 1;
    }

    public double cost(State s1, Action a) {
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
    public ArrayList<Action> actions(State s) {
        long time1 = System.currentTimeMillis();
        setState(s);
        
        //System.err.println("agent: " + agent);
        HashMap arguments = new HashMap<String,String>();
        arguments.put("Agent", "" + agent);
        //System.out.println("actions called!!!!!");
        //System.out.println("state: \n" + s.toString());
        ArrayList<Action> actionsReturn = new ArrayList<Action>();
        //System.out.println("    # action schemas : " + this.actions.size());
        for(ActionSchema a : this.actions) {
            if(!a.expanded) {
                //System.out.println("       Action: " + a.name);
                //System.out.println("       is not expanded");
                try {
                    ArrayList<Action> acs = a.get(logic, arguments);
                    //System.out.println("          Got: " + acs.toString());
                    //System.err.println("Gotten: " + acs.toString());
                    actionsReturn.addAll(acs);
                } catch (PrologException ex) {
                    Logger.getLogger(NFSPProblem.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        long time2 = System.currentTimeMillis();
        //System.out.println("Got actions in: " + (time2-time1) + " ms");
        //System.out.println("       # of actions: " + actionsReturn.size());
        return actionsReturn;
    }

    public State result(State s, Action a) {
        //setState(s);
        Logic logicClone = s.state.clone();
        //System.err.println("Action : " + a.name);
        //System.out.println("\nSTATE 1: " + logic.getTheoryAsString());
        //System.out.println("Action: " + a.name);
        for (String string : a.effects) {
            try {
                logicClone.set(string);
                //System.out.println("EFFECT: " + string);
            } catch (Throwable ex) {
                Logger.getLogger(NFSPProblem.class.getName()).log(Level.SEVERE, null, ex);
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
