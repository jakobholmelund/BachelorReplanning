package Planner;

import java.util.ArrayList;

public class Actions implements Comparable<Actions>{
    public String name;
    public ArrayList<String> effect;
    public ArrayList<String> requirements;
    public boolean expanded;
    public boolean atomic;
    
    public Actions(String s, boolean expanded, boolean atomic) {
        this.name = s;
        effect = new ArrayList<String>();
        requirements = new ArrayList<String>();
        this.expanded = expanded;
        this.atomic = atomic;
    }

    public void addEffect(String s) {
        effect.add(s);
    }

    public void addRequirement(String s) {
        //System.out.println("Requirement added: " + s);
        requirements.add(s);
    }

    @Override
    public int compareTo(Actions a) {
        //System.err.println(this.s + " -- " + a.s);
        if (!this.effect.containsAll(a.effect)) {
            return 1;
        } else {
            return 0;
        }
    }

    public String getAction() {
            return name;
    }

    public String effectToString() {
        String str = effect.toString();
        return str;
    }

    public String reqToString() {
        String str = requirements.toString();
        return str;
    }

    @Override
    public String toString() {
        return name + effectToString() + " " + reqToString();
    }
}
