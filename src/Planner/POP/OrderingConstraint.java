/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Planner.POP;

import Planner.Action;

/**
 *
 * @author Dan True
 */
class OrderingConstraint {
    public Action A;
    public Action B;
    
    // Read as "A before B"
    OrderingConstraint(Action A, Action B) {
        this.A = A;
        this.B = B;
    }
    
    public String toString() {
       return A.getAction() + " before " + B.getAction(); 
    }
    
    public boolean equals(OrderingConstraint o) {
        return o.A.equals(this.A) && o.B.equals(this.B);
    }
}
