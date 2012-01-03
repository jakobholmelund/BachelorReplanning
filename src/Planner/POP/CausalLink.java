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
    Actions A;
    Actions B;
    String p;
    
    // Read as "A achieves p for B"
    CausalLink(Actions A, Actions B, String p) {
        this.A = A;
        this.B = B;
        this.p = p;
    }
}
