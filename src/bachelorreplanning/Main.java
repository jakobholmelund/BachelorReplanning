/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bachelorreplanning;

import Planner.forward.FSPlanner;
import gui.MainWindow;
import jTrolog.errors.PrologException;
import worldmodel.MapBox;
import worldmodel.World;
import worlds.*;

/**
 *
 * @author Dan True
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws PrologException, Throwable {
        World world = new World(30,30);
        
        MainWindow mainWindow = new MainWindow();
        mainWindow.loadNewWorld(world);
        mainWindow.drawWorld();
        mainWindow.drawOptions();
        FSPlanner agent = new FSPlanner(world);
        Thread init = new Thread(agent);
        
        /*
        while(!agent.done()) {
            if(agent.iteration == 3) {
                world.addObject(new MapBox("b", 5,8));
            }
            mainWindow.drawWorld();
            init.run();
        }
        init.join();
         * 
         */
    }
}
