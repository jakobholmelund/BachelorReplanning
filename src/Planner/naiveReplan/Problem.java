/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Planner.naiveReplan;

import Planner.Logic;
import jTrolog.engine.Solution;
import jTrolog.errors.PrologException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jakobsuper
 */
public class Problem {
    State initial;
    String goal;
    Logic logic;
    int agent = 0;
    LinkedList<State> futureGoals = new LinkedList<State>();
    String currentBox;
    String currentBoxGoalPos;

    public Problem() throws PrologException {
        this.logic = new Logic();
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

    /**
     * Calculates the heuristik for the state.
     *
     * @param s1
     * @param a
     * @param node 
     * @return
     */

    public double heuristik(State s1, Action a, Node node) {
        
        try {
            setState(s1);
            double agentToBox;
            double boxToGoal;

            Solution n = logic.solve("agentAt(0, N).");
            Solution g = logic.solve("goalAt(a, G).");
            Solution b = logic.solve("boxAt(a, B).");
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
        setState(s);
        //System.out.println("actions called!!!!!");
        //System.err.println("state: \n" + s.toString());
        ArrayList<Action> actionsReturn = new ArrayList<Action>();

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
                Action act = new Action("Move(" + agent + "," + MoveDirAgent + ")");
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

        ArrayList<Solution> sol2 = logic.solveAll("push(" + agent + ", MoveDirAgent, CurrDirBox, C0, C1, C2, B). ");

        for (Solution si1 : sol2) {
            //System.err.println("push: " + si1.bindingsToString());
            try {
                String MoveDirAgent = "" + si1.getBinding("MoveDirAgent");
                //System.err.println("MoveDirAgent: " + MoveDirAgent);
                MoveDirAgent = MoveDirAgent.replace("'", "");
                String CurrDirBox = "" + si1.getBinding("CurrDirBox");
                CurrDirBox = CurrDirBox.replace("'", "");
                String C0 = "" + si1.getBinding("C0");
                //System.err.println("C0: " + C0);
                String C1 = "" + si1.getBinding("C1");
                //System.err.println("C1: " + C1);
                String C2 = "" + si1.getBinding("C2");
                //System.err.println("C2: " + C2);
                String B = "" + si1.getBinding("B");
                //System.err.println("B: " + B);
                Action act = new Action("Push(" + agent + "," + MoveDirAgent + "," + CurrDirBox + ") ");
                act.addEffect("agentAt(" + agent + "," + C1 + ")");
                act.addEffect("!agentAt(" + agent + "," + C0 + ")");
                act.addEffect("boxAt(" + B + "," + C2 + ")");
                act.addEffect("!boxAt(" + B + "," + C1 + ")");

                act.addRequirement("f(" + C2 + ")");

                act.box = B;
                act.moveAgentDir = MoveDirAgent;
                //System.err.println(act.toString());
                actionsReturn.add(act);

            } catch (Exception e) {
                                e.printStackTrace();
            }
        }

        ArrayList<Solution> sol3 = logic.solveAll("pull(" + agent + ", MoveDirAgent, CurrDirBox, C0, C1, C2, B). ");

        for (Solution si2 : sol3) {
            //System.err.println("pull: " + si2.bindingsToString());
            try {
                String MoveDirAgent = "" + si2.getBinding("MoveDirAgent");
                MoveDirAgent = MoveDirAgent.replace("'", "");
                String CurrDirBox = "" + si2.getBinding("CurrDirBox");
                CurrDirBox = CurrDirBox.replace("'", "");
                String C0 = "" + si2.getBinding("C0");
                String C1 = "" + si2.getBinding("C1");
                String C2 = "" + si2.getBinding("C2");
                String B = "" + si2.getBinding("B");

                Action act = new Action("Pull(" + agent + "," + MoveDirAgent + "," + CurrDirBox + ")");
                act.addEffect("agentAt(" + agent + "," + C1 + ")");
                act.addEffect("!agentAt(" + agent + "," + C0 + ")");
                act.addEffect("boxAt(" + B + "," + C0 + ")");
                act.addEffect("!boxAt(" + B + "," + C2 + ")");

                act.addRequirement("f(" + C1 + ")");
                act.box = B;
                act.moveAgentDir = MoveDirAgent;
                //System.err.println("Pull(" + MoveDirAgent + "," + CurrDirBox + ")" + "   - PULL: " + C0 + " , " + C1 + " , " + C2 + " is C1 ");
                //System.err.println(engine.getTheoryAsString());
                //System.err.println(act.toString());
                actionsReturn.add(act);

               // Action noOp = new Action("NoOp");
               // noOp.box = "";
               // noOp.moveAgentDir = "";
               // actionsReturn.add(noOp);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //System.err.println("No More Actions. Num of actions : " + actionsReturn.size());
        //System.err.println("Actionstime: " + (time2 - time1) / 1000 + " actions_size: " + actionsReturn.size() + " s-size " + engine.getTheoryAsString().length());
        return actionsReturn;
    }

    public State result(State s, Action a) {
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
