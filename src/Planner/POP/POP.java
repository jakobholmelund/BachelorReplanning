/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Planner.POP;

import Planner.Actions;
import Planner.TOPlan;
import java.util.ArrayList;
import java.util.HashMap;
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
    boolean debug = false;
    
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
    
    public POP clone() {
        POP newPoP = new POP(goal);
        newPoP.openPreconditions = (HashSet<OpenPrecondition>) this.openPreconditions.clone();
        newPoP.causalLinks = (HashSet<CausalLink>) this.causalLinks.clone();
        newPoP.orderingConstraints = (HashSet<OrderingConstraint>) this.orderingConstraints.clone();
        newPoP.actions = (HashSet<Actions>) this.actions.clone();
        return newPoP;
    }
    
    public boolean hasOrderingConstraint(Actions A, Actions B) {
        return orderingConstraints.contains(new OrderingConstraint(A, B));
    }
    
    public POP addOrderingConstraint(Actions A, Actions B) {
        if(!A.equals(B)) {
            OrderingConstraint o = new OrderingConstraint(A, B);
            orderingConstraints.add(o);
        }
        return this;
    }
    
    public POP removeOrderingConstraint(Actions A, Actions B) {
        for(OrderingConstraint o : this.orderingConstraints) {
            if(o.A.equals(A) && o.B.equals(B)) {
                this.orderingConstraints.remove(o);
            }
        }
        return this;
    }
    public POP addCausalLink(Actions A, Actions B, String p) {
        CausalLink c = new CausalLink(A, B, p);
        causalLinks.add(c);
        return this;
    }
    
     public POP addOpenPrecondition(String p, Actions a) {
        OpenPrecondition op = new OpenPrecondition(p, a);
        openPreconditions.add(op);
        return this;
    }
     
    public OrderingConstraint addAndGetOrderingConstraint(Actions A, Actions B) {
        if(!A.equals(B)) {
            OrderingConstraint o = new OrderingConstraint(A, B);
            orderingConstraints.add(o);
            return o;
        }else{
            return null;
        }
    }
    
    public CausalLink addAndGetCausalLink(Actions A, Actions B, String p) {
        CausalLink c = new CausalLink(A, B, p);
        causalLinks.add(c);
        return c;
    }
    
     public OpenPrecondition addAndGetOpenPrecondition(String p, Actions a) {
        OpenPrecondition op = new OpenPrecondition(p, a);
        openPreconditions.add(op);
        return op;
    }
     
     public OpenPrecondition pollOpenPreconditions() {
         //System.out.println("Before: " + openPreconditions.toString());
         OpenPrecondition returner =  openPreconditions.iterator().next();
         openPreconditions.remove(returner);
         //System.out.println("After: " + openPreconditions.toString());
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
    
    public TOPlan getLinearization() {
        TOPlan plan = new TOPlan(this.goal);
        final HashSet<OrderingConstraint> backup = this.orderingConstraints;
        
        TOPlan linearPlan = findLinearization(startAction, plan);
        
        this.orderingConstraints = backup;
        return linearPlan;
    }
    
    private TOPlan findLinearization(Actions action, TOPlan plan) {
        TOPlan newPlan = plan;
        for(Actions a : expand(action)) {
            if(this.debug)
                System.out.println("Adding expanded: " + a.name);
            plan = findLinearization(a, plan.append(a));
            //System.out.println("PREQ ADDED: " + a.preqToString());
        }
        return newPlan;
    }
    
    private ArrayList<Actions> expand(Actions A) {
        ArrayList<Actions> actionsRet = new ArrayList<Actions>();
        ArrayList<OrderingConstraint> removers = new ArrayList<OrderingConstraint>();
        if(this.debug)
            System.out.println("   Expanding: " + A.name);
        for(OrderingConstraint o : this.orderingConstraints) {
            if(this.debug)
                System.out.println("      Looking at: " + o.A.getAction() + " < " + o.B.getAction());
            if(o.A.equals(A)) {
                boolean foundOther = false;
                for(OrderingConstraint l : this.orderingConstraints) {
                    if((!o.A.equals(l.A)) && o.B.equals(l.B)) {
                        if(this.debug)
                            System.out.println("         Found " + l.A.getAction() + " < " + l.B.getAction() + " violating: " + o.A.getAction() + " < " + o.B.getAction());
                        foundOther = true;
                        break;
                    }
                }
                if(!foundOther && !o.B.equals(finishAction)) {
                    if(this.debug)
                        System.out.println("      -> Adding: " + o.B);
                    actionsRet.add(o.B);
                    continue;
                }else{
                    removers.add(o);
                }
            }
        }
        
        this.orderingConstraints.removeAll(removers);
        return actionsRet;
    }
    
    public Actions getStart() {
        return startAction;
    }
    
    public Actions getFinish() {
        return finishAction;
    }

    void printToConsole() {
        System.out.println("Open Preconditions: " + openPreconditions.size());
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

    boolean hasCycles() {
        for(OrderingConstraint order1 : orderingConstraints) {
            for(OrderingConstraint order2 : orderingConstraints) {
                if(order1.A.equals(order2.B) && order1.B.equals(order2.A)) {
                    return true;
                }
            }
        }
        return false;
    }
}
