/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bachelorreplanning;

import Planner.backward.BSPlanner;
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
        BSPlanner planner = new BSPlanner(world, 0, "agentAt(0,[15;15])", null); // at(a,[5;5]) // agentAt(0,[5;5])
        Thread init = new Thread(planner);
        init.start();
        //planner.run();//findAction(p, "boxAt(a,[5;5])");
        /*
            World world = new World(100,100); //new MiddleWorld();//new World(30,30);
            world.addObject(new MapAgent(1,1,1));
            //world.newAgentActionParse("moveAtomic(0,[5,10])");
            //world.newAgentActionParse("pickUp(1,a)");
            //world.newAgentActionParse("place(1,a)");
            //World world = new World(30,30);
            //world.addObject(new MapAgent(0,1,1));
            //Astar test = new Astar();
            //test.findPlan(world,"move(0,[28,28])");
            //4294967301294967297L,4294967421554051613L
            //MainWindow mainWindow = new MainWindow();
            //mainWindow.loadNewWorld(world);
           //mainWindow.drawOptions();
         */
    }
}


