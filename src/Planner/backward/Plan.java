package Planner.backward;

import java.util.ArrayList;
import java.util.List;

public class Plan {

    State s;
    ArrayList<Actions> list;

    Plan() {
        list = new ArrayList<Actions>();
    }

    void add(Actions a) {
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

    void printSolution() {
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

    boolean valid(State state) {
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
