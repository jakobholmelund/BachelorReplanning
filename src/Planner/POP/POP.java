/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Planner.POP;

import Planner.Actions;
import Planner.Plan;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Dan True
 */
public class POP {
    Set<String> openPreconditions;
    Set<CausalLink> causalLinks;
    Set<OrderingConstraint> orderingConstraints;
    Set<Actions> actions;
    String goal;
    Actions startAction;
    
    public POP(String goal) {
        this.openPreconditions = new HashSet<String>();
        this.causalLinks = new HashSet<CausalLink>();
        this.orderingConstraints = new HashSet<OrderingConstraint>();
        this.actions = new HashSet<Actions>();
        this.goal = goal;
        
        this.startAction = new Actions("Start", false, false);
        this.actions.add(this.startAction);
    }
    
    public void addOrderingConstraint(Actions A, Actions B) {
        OrderingConstraint o = new OrderingConstraint(A, B);
        orderingConstraints.add(o);
    }
    
    
    public void addCausalLink(Actions A, Actions B, String p) {
        CausalLink c = new CausalLink(A, B, p);
        causalLinks.add(c);
    }
    
     public void addOpenPreconditions(String p) {
        openPreconditions.add(p);
    }
    
    public void addActions(Actions A) {
        actions.add(A);
    }
    
    public boolean isSolution() {
        return openPreconditions.isEmpty();
    }
    
    public Plan getLinearization() {
        Plan plan = new Plan(this.goal);
        // Make a linerazation
        return plan;
    }
}
