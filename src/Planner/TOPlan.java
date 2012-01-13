package Planner;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class TOPlan {

    public State s;
    public ArrayList<Action> list;
    private String goal;
    
    public TOPlan() {
        list = new ArrayList<Action>();
    }
    
    public TOPlan(String goal) {
        list = new ArrayList<Action>();
        this.goal = goal.replaceAll("\\[([0-9]*);([0-9]*)\\]", "[$1,$2]");
    }
    
    public void setGoal(String goal) {
        this.goal = goal.replaceAll("\\[([0-9]*);([0-9]*)\\]", "[$1,$2]");
    }
    
    public String getGoal() {
        return this.goal;
    }

    public TOPlan appendAll(TOPlan p1) {
        this.list.addAll(p1.list);
        return this;
    }
    
    public TOPlan prependAll(TOPlan p1) {
        this.list.addAll(0, p1.list);
        return this;
    }
    
    public TOPlan addAll(int index, ArrayList<Action> list) {
        this.list.addAll(index, list);
        return this;
    }
    
    public TOPlan addAll(int index, TOPlan plan) {
        this.list.addAll(index, plan.list);
        return this;
    }
    
    public TOPlan appendAll(ArrayList<Action> list) {
        this.list.addAll(list);
        return this;
    }
    
    public TOPlan prependAll(ArrayList<Action> list) {
        this.list.addAll(0, list);
        return this;
    }
    
    public TOPlan append(Action act) {
        this.list.add(act);
        return this;
    }
    
    public TOPlan prepend(Action act) {
        this.list.add(0, act);
        return this;
    }

    public TOPlan add(int index, Action a) {
        list.add(index, a);
        return this;
    }
    
    public TOPlan add(Action a) {
        list.add(0, a);
        return this;
    }
    
    public Action pop() {
        Action ret = list.get(0);
        list.remove(0);
        return ret;
    }

    public List<Action> getSolution() {
        return this.list;
    }

    public void printSolution() {
        System.err.print("THIS: ");
        for (Action a : list) {
            System.err.print(a.toString() + " , ");
        }
        System.err.println("Is a solution");
    }

    @Override
    public String toString() {
        String ret = "Solution: \n";
        for (Action a : list) {
            ret += "  " + a.toString() + "\n";
        }
        return ret;
    }

    public boolean isEmpty() {
        return this.list.isEmpty();
    }
    
    /**
     * 
     * @param state
     * @return 
     *  i if the plan is broken at action i
     *  -1 if the plan is valid
     *  -2 if the plan is valid but does not fulfill the goal
     */
    public int valid(State state) {
        Logic s = state.state.clone();
        //System.out.println("Initial check state: \n " + s.getTheoryAsString());
        long time1 = System.currentTimeMillis();

        for(int i = 0; i < list.size(); i++) {
            Action action = list.get(i);
            //System.out.println("   Action: " + action.toString());
            if(action.atomic) {
                //System.out.println("   -> Atomic");
                for(String preq : action.preconditions) {
                    Boolean result =  s.solveboolean(preq);
                    //System.out.println("      Checking for: " + preq + "  Result: " + result);

                    if(!result) {
                        //System.out.println("      >>> Plan failed at number " + i + "\n" + action.toString());
                        //System.out.println("State when fail: \n " + s.getTheoryAsString());
                        return i;
                    }
                }
                
                for (String effect : action.effects) {
                    try {
                        s.set(effect);
                        //System.out.println("      Added: " + effect);
                    } catch (Throwable ex) {
                        //System.out.println("      Failed at adding: " + effect);
                    }
                }
            }else{
                //System.out.println("   -> Non-Atomic");
                for (String effect : action.effects) {
                    try {
                        s.set(effect);
                        //System.out.println("      Added: " + effect);
                    } catch (Throwable ex) {
                        //System.out.println("      Failed at adding: " + effect);
                    }
                }
            }
        }
        boolean bol = s.solveboolean(getGoal());
        
        long time2 = System.currentTimeMillis();
        //System.out.println("Plan monitoring took: " + (time2-time1) + " ms  -- goal is: " + bol);
        //System.out.println("Goal: " + getGoal() + " - valid: " + bol);
        if(bol) {
            return -1;
        }else{
            return -2;
        }
    }
    
    public int validForPOP(State state) {
        Logic s = state.state.clone();
        long time1 = System.currentTimeMillis();
        
        for(int i = 0; i < list.size(); i++) {
            Action action = list.get(i);
            System.out.println("   Action: " + action.toString());
            System.out.println("      PREQ: " + action.preqToString());
            
            for (String effect : action.effects) {
                try {
                    //System.out.println("Setting: " + string);
                    s.set(effect);
                } catch (Throwable ex) {

                }
            }
        }
        boolean bol = s.solveboolean(getGoal());
        
        long time2 = System.currentTimeMillis();
        //System.out.println("Plan monitoring took: " + (time2-time1) + " ms  -- goal is: " + bol);
        //System.out.println("Goal: " + getGoal() + " - valid: " + bol);
        if(bol) {
            return -1;
        }else{
            return -2;
        }
    }
}
