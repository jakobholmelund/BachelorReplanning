/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Planner.POP;

import Planner.Actions;
import Planner.Plan;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Dan True
 */
public class POP {
    HashSet<OpenPrecondition> openPreconditions;
    HashSet<CausalLink> causalLinks;
    HashSet<OrderingConstraint> orderingConstraints;
    HashSet<Actions> actions;
    String goal;
    Actions startAction;
    Actions finishAction;
    
    public POP(String goal) {
        this.openPreconditions = new HashSet<OpenPrecondition>();
        this.causalLinks = new HashSet<CausalLink>();
        this.orderingConstraints = new HashSet<OrderingConstraint>();
        this.actions = new HashSet<Actions>();

        this.startAction = new Actions("Start", false, false);
        this.actions.add(this.startAction);
        this.finishAction = new Actions("Finish", false, false);
        this.actions.add(this.finishAction);
        
        this.addOrderingConstraint(startAction, finishAction);
        
        this.goal = goal;
        this.addOpenPrecondition(goal, finishAction);
        
    }
    
    public void addOrderingConstraint(Actions A, Actions B) {
        OrderingConstraint o = new OrderingConstraint(A, B);
        orderingConstraints.add(o);
    }
    
    public void addCausalLink(Actions A, Actions B, String p) {
        CausalLink c = new CausalLink(A, B, p);
        causalLinks.add(c);
    }
    
     public void addOpenPrecondition(String p, Actions a) {
        openPreconditions.add(new OpenPrecondition(p, a));
    }
     
     public OpenPrecondition pollOpenPreconditions() {
         System.out.println("Before: " + openPreconditions.toString());
         OpenPrecondition returner =  openPreconditions.iterator().next();
         openPreconditions.remove(returner);
         System.out.println("After: " + openPreconditions.toString());
         return returner;
     }
    
    public void addActions(Actions A) {
        actions.add(A);
    }
    
    public boolean contains(Actions a) {
        return actions.contains(a);
    }
    
    public boolean isSolution() {
        return openPreconditions.isEmpty();
    }
    
    public Plan getLinearization() {
        Plan plan = new Plan(this.goal);
        // Make a linerazation
        return plan;
    }
    
    public Actions getStart() {
        return startAction;
    }
    
    public Actions getFinish() {
        return getFinish();
    }

    void printToConsole() {
        System.out.println("Actions: ");
        for (int i = 0; i < actions.toArray().length; i++) {
            System.out.println("  " + actions.toArray()[i]);
        }
        
        System.out.println("\nOrdering constraints: ");
        for (int i = 0; i < orderingConstraints.toArray().length; i++) {
            System.out.println("  " + orderingConstraints.toArray()[i].toString());
        }
        
        System.out.println("\nCausal Links: ");
        for (int i = 0; i < causalLinks.toArray().length; i++) {
            System.out.println("  " + causalLinks.toArray()[i].toString());
        }
        System.out.println("\n---------------------------------------------\n");
    }
}
