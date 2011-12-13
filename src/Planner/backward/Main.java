/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Planner.backward;

import gui.MainWindow;
import jTrolog.errors.PrologException;
import worldmodel.World;
import worlds.MiddleWorld;
import worlds.SmallWorld;

/**
 *
 * @author Dan True
 */
public class Main {
    
    public static void main(String[] args) throws PrologException, InterruptedException  {
        World world = new MiddleWorld();//new World(30,30);
        MainWindow mainWindow = new MainWindow();
        mainWindow.loadNewWorld(world);
        BSPlanner planner = new BSPlanner(world, 0, "at(a,[15;15])", null); // at(a,[5;5]) // agentAt(0,[5;5])
        Thread init = new Thread(planner);
        init.start();
        //planner.run();//findAction(p, "boxAt(a,[5;5])");
    }
}


