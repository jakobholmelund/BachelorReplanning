/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Planner.POP;

import Planner.Action;
import Planner.TOPlan;
import gui.RouteFinder.Astar;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import worldmodel.World;

/**
 *
 * @author Dan True
 */
public class POP {
    HashSet<OpenPrecondition> openPreconditions;
    HashSet<CausalLink> causalLinks;
    HashSet<OrderingConstraint> orderingConstraints;
    Collection<Action> actions;
    String goal;
    Action startAction;
    Action finishAction;
    boolean debug = false;
    Astar routeFinder;
    
    public POP(String goal) {
        this.openPreconditions = new HashSet<OpenPrecondition>();
        this.causalLinks = new HashSet<CausalLink>();
        this.orderingConstraints = new HashSet<OrderingConstraint>();
        this.actions = new ArrayList<Action>();//Collections.synchronizedSet(new HashSet<Action>());

        this.startAction = new Action("Start", false, false);
        this.actions.add(this.startAction);
        this.finishAction = new Action("Finish", false, false);
        this.actions.add(this.finishAction);
        
        this.addOrderingConstraint(startAction, finishAction);
        
        this.goal = goal;
        this.addOpenPrecondition(goal, finishAction);
        
        routeFinder = new Astar();
    }
    
    @Override
    public synchronized POP clone() {
        POP newPoP = new POP(goal);
        newPoP.openPreconditions = (HashSet<OpenPrecondition>) this.openPreconditions.clone();
        newPoP.causalLinks = (HashSet<CausalLink>) this.causalLinks.clone();
        newPoP.orderingConstraints = (HashSet<OrderingConstraint>) this.orderingConstraints.clone();
        newPoP.actions = (ArrayList<Action>)((ArrayList<Action>)this.actions).clone();
        return newPoP;
    }
    
    public boolean hasOrderingConstraint(Action A, Action B) {
        return orderingConstraints.contains(new OrderingConstraint(A, B));
    }
    
    public POP addOrderingConstraint(Action A, Action B) {
        if(!A.equals(B)) {
            OrderingConstraint o = new OrderingConstraint(A, B);
            orderingConstraints.add(o);
        }
        return this;
    }
    
    public POP removeOrderingConstraint(Action A, Action B) {
        for(OrderingConstraint o : this.orderingConstraints) {
            if(o.A.equals(A) && o.B.equals(B)) {
                this.orderingConstraints.remove(o);
            }
        }
        return this;
    }
    public POP addCausalLink(Action A, Action B, String p) {
        CausalLink c = new CausalLink(A, B, p);
        causalLinks.add(c);
        return this;
    }
    
     public POP addOpenPrecondition(String p, Action a) {
        OpenPrecondition op = new OpenPrecondition(p, a);
        openPreconditions.add(op);
        return this;
    }
     
    public OrderingConstraint addAndGetOrderingConstraint(Action A, Action B) {
        if(!A.equals(B)) {
            OrderingConstraint o = new OrderingConstraint(A, B);
            orderingConstraints.add(o);
            return o;
        }else{
            return null;
        }
    }
    
    public CausalLink addAndGetCausalLink(Action A, Action B, String p) {
        CausalLink c = new CausalLink(A, B, p);
        causalLinks.add(c);
        return c;
    }
    
     public OpenPrecondition addAndGetOpenPrecondition(String p, Action a) {
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
    
    public void clearOpenPreconditions() {
        openPreconditions.clear();
    }
     
    public synchronized void addAction(Action A) {
        actions.add(A);
    }
    
    public synchronized boolean contains(Action a) {
        return actions.contains(a);
    }
    
    public boolean isSolution() {
        return openPreconditions.isEmpty();
    }
    
    public TOPlan getLinearization(World world) {
        TOPlan plan = new TOPlan(this.goal);
        final HashSet<OrderingConstraint> backup = this.orderingConstraints;
        
        TOPlan linearPlan = findLinearization(startAction, plan, world);
        
        this.orderingConstraints = backup;
        return linearPlan;
    }
    
    private TOPlan findLinearization(Action action, TOPlan plan, World world) {
        TOPlan newPlan = plan;
        for(Action a : expand(action)) {
            if(this.debug)
                System.out.println("Adding expanded: " + a.name);
            plan = findLinearization(a, plan.append(a), world);
            //System.out.println("PREQ ADDED: " + a.preqToString());
        }
        
        return newPlan; //expandToAtomic(newPlan, world);
    }
    
    private ArrayList<Action> expand(Action A) {
        ArrayList<Action> actionsRet = new ArrayList<Action>();
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
    
    public Action getStart() {
        return startAction;
    }
    
    public Action getFinish() {
        return finishAction;
    }

    public void printToConsole() {
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
    
    boolean isEmpty() {
        return (actions.size() == 2);
    }

    /*private TOPlan expandToAtomic(TOPlan newPlan, World world) {
        for(int i = 0; i < newPlan.list.size(); i++) {
            Action next = newPlan.list.get(i);
            if(!next.atomic) {
               newPlan.list.remove(i);
               System.out.println(" -- which is not atomic");
               POP subPlan = null;
                try {
                    subPlan = routeFinder.findPlan(world,next.name);
                    newPlan.addAll(i, subPlan);
                } catch (InterruptedException ex) {
                    //Logger.getLogger(POP.class.getName()).log(Level.SEVERE, null, ex);
                    return null;
                }
               //subPlan.printSolution();
                // prependAll(subPlan);
               //System.out.println("New plan:\n" + this.plan);
            }
        }
        return newPlan;
    }*/
        
}
