/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Planner;

import java.util.ArrayList;

/**
 *
 * @author Dan True
 */
public class ReturnInfo {
    public int info;
    public ArrayList<String> precondition;
    
    ReturnInfo(int info, ArrayList<String>  precondition) {
        this.info = info;
        this.precondition = precondition;        
    }
}
