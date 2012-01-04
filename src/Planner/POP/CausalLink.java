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
class CausalLink {
    public Actions A;
    public Actions B;
    public String p;
    
    // Read as "A achieves p for B"
    CausalLink(Actions A, Actions B, String p) {
        this.A = A;
        this.B = B;
        this.p = p;
    }
    
    public String toString() {
       return A.getAction() + " achives " + p + " for " + B.getAction(); 
    }
    
    public boolean equals(CausalLink o) {
        return o.A.equals(this.A) && o.B.equals(this.B) && o.p.equals(this.p);
    }
}
