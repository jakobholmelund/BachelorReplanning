/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Planner.POP;

import Planner.Actions;

/**
 *
 * @author Dan True
 */
public class OpenPrecondition {
    public String condition;
    public Actions action;
    
    OpenPrecondition(String condition, Actions action) {
        this.condition = condition;
        this.action = action;
    }
    
    public String toString() {
        return condition + " for " + action.getAction();
    }
    
    public boolean equals(OpenPrecondition o) {
        return o.condition.equals(this.condition) && o.action.equals(this.action);
    }
}
