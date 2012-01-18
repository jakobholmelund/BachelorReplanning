package Planner;

import java.util.ArrayList;

public class Action implements Comparable<Action>{
    public String name;
    public ArrayList<String> effects;
    public ArrayList<String> preconditions;
    public ArrayList<String> openPreconditions;
    public boolean expanded;
    public boolean atomic;
    
    public Action(String s, boolean expanded, boolean atomic) {
        this.name = s;
        this.effects = new ArrayList<String>();
        this.preconditions = new ArrayList<String>();
        this.openPreconditions = new ArrayList<String>(); 
        this.expanded = expanded;
        this.atomic = atomic;
    }

    public void addEffect(String s) {
        this.effects.add(s);
    }

    public void addPrecondition(String s) {
        //System.out.println("Requirement added: " + s);
        preconditions.add(s);
    }

    public void addOpenPrecondition(String s) {
        //System.out.println("Requirement added: " + s);
        openPreconditions.add(s);
    }
    
    @Override
    public int compareTo(Action a) {
        //System.err.println(this.s + " -- " + a.s);
        //if (!this.effect.containsAll(a.effect)) {
        //    return 1;
        //} else {
        //    return 0;
        //}
        if(this.name.equals(a.name)) {
            return 1;
        }else{
            return 0;
        }
    }

    public String getAction() {
            return name;
    }

    public String effectToString() {
        String str = effects.toString();
        return str;
    }
    
    public String openPreconditionsToString() {
        String str = openPreconditions.toString();
        return str;
    }
 
    public String preqToString() {
        String str = preconditions.toString();
        return str;
    }

    @Override
    public String toString() {
        return name + " " + effectToString() + " " + openPreconditionsToString();
    }
    
    public boolean equals(Action a) {
        return name.equals(a.name) && effects.equals(a.effects) && preconditions.equals(a.preconditions);
    }
}
