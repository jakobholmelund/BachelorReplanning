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
class OrderingConstraint {
    public Actions A;
    public Actions B;
    
    // Read as "A before B"
    OrderingConstraint(Actions A, Actions B) {
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
