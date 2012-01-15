/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Planner.POP;

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

public class POPProblem {
    State initial;
    String goal;
    Logic logic;
    int agent = 1;
    LinkedList<State> futureGoals = new LinkedList<State>();
    String currentBox;
    String currentBoxGoalPos;
    public ArrayList<ActionSchema> actions;
    
    public POPProblem(int aid,String missionId, ArrayList<ActionSchema> actions) throws PrologException {
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
    
    private void setState(State s) {
        //System.err.println("State size: " + s.state.length());
        if (s != null) {
            //logic.setTheory(s.state);
            logic = s.state;
            //System.err.println("real state: " + engine.getTheoryAsString());
        }
    }
    
    /*
    public double cost(State s1, Action a) {
        return 1;
    }

    public ArrayList<Action> actions(State s) {
        setState(s);
        //System.err.println("agent: " + agent);
        HashMap arguments = new HashMap<String,String>();
        arguments.put("Agent", "" + agent);
        //System.out.println("actions called!!!!!");
        //System.err.println("state: \n" + s.toString());
        ArrayList<Action> actionsReturn = new ArrayList<Action>();
        for(ActionSchema a : this.actions) {
           if(!a.expanded) {
                try {
                    ArrayList<Action> acs = a.get(logic, arguments);
                    //System.err.println("Gotten: " + acs.toString());
                    actionsReturn.addAll(acs);
                } catch (PrologException ex) {
                    Logger.getLogger(POPProblem.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        //System.err.println("!!Getting Actions!");
        //System.err.println("!!Actions: ");
        for(Action a : actionsReturn) {
            //System.out.println("action: " + a.toString());
        }
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
                Logger.getLogger(POPProblem.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //String s2 = logic.engine.getTheory().toString();
        //System.out.println("\nSTATE 2: " + logic.getTheoryAsString() + "\n\n\n");

        return new State(logicClone);
    }
*/
    public void setGoal(String goal) {
        this.goal = goal;
    }

    public void setInitial(State init) {
        this.initial = init;

    }
}
