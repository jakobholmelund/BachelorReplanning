package Planner;

import java.util.ArrayList;
import java.util.List;

public class Plan {

    public State s;
    public ArrayList<Actions> list;

    public Plan() {
        list = new ArrayList<Actions>();
    }

    public void appendAll(Plan p1) {
        this.list.addAll(p1.list);
    }
    
    public void prependAll(Plan p1) {
        this.list.addAll(0, p1.list);
    }
    
    public void addAll(int index, ArrayList<Actions> list) {
        this.list.addAll(index, list);
    }
    
    public void appendAll(ArrayList<Actions> list) {
        this.list.addAll(list);
    }
    
    public void prependAll(ArrayList<Actions> list) {
        this.list.addAll(0, list);
    }
    
    public void append(Actions act) {
        this.list.add(act);
    }
    
    public void prepend(Actions act) {
        this.list.add(0, act);
    }

    public void add(int index, Actions a) {
        list.add(index, a);
    }
    
    public void add(Actions a) {
        list.add(0, a);
    }
    
    public Actions pop() {
        Actions ret = list.get(0);
        list.remove(0);
        return ret;
    }

    public List<Actions> getSolution() {
        return this.list;
    }

    public void printSolution() {
        System.err.print("THIS: ");
        for (Actions a : list) {
            System.err.print(a.toString() + " , ");
        }
        System.err.println("Is a solution");
    }

    @Override
    public String toString() {
        String ret = "Solution: \n";
        for (Actions a : list) {
            ret += "  " + a.toString() + "\n";
        }
        return ret;
    }

    public boolean isEmpty() {
        return this.list.isEmpty();
    }

    public boolean valid(State state) {
        boolean valid = true;
        //System.err.println("state: " + state);
        for (Actions a : list) {
            for (String st : a.requirements) {
                
                boolean res = state.state.solveboolean(st);
                //System.out.println("Solving: " + st + " res: " + res);
                if (!res) {
                    valid = false;
                    break;
                }
            }
            if (!valid) {
                break;
            }
        }

        return valid;
    }
}
