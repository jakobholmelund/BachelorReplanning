/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Planner.backward;

import jTrolog.errors.PrologException;
import worldmodel.World;
import worlds.MiddleWorld;

/**
 *
 * @author Dan True
 */
public class Main {
    
    public static void main(String[] args) throws PrologException, InterruptedException  {
        World world = new MiddleWorld();//new World(30,30);
               
        BSPlanner planner = new BSPlanner(world, 1, "boxAt(a,[5;5])", null);
        
        Problem p = new Problem(1,"");
        planner.run();//findAction(p, "boxAt(a,[5;5])");
        
    }
}


