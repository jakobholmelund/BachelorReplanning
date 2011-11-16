/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bachelorreplanning;

import Planner.Logic;
import Planner.forward.FSPlanner;
import Planner.forward.Plan;
import Planner.forward.Problem;
import Planner.forward.State;
import gui.MainWindow;
import jTrolog.errors.PrologException;
import jTrolog.parser.Parser;
import jTrolog.engine.*;
import jTrolog.terms.*;
import worldmodel.Box;
import worldmodel.Goal;
import worldmodel.MapAgent;
import worldmodel.Wall;
import worldmodel.World;
/**
 *
 * @author Dan True
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws PrologException, Throwable {
        World world = new World();
        world.addObject(new MapAgent(0, 1 ,1));
        world.addObject(new Wall(0,0));
        world.addObject(new Wall(1,0));
        world.addObject(new Wall(2,0));
        world.addObject(new Wall(3,0));
        world.addObject(new Wall(4,0));
        world.addObject(new Wall(5,0));
        world.addObject(new Wall(6,0));
        world.addObject(new Wall(7,0));
        world.addObject(new Wall(8,0));
        world.addObject(new Wall(9,0));
        
        world.addObject(new Wall(0,1));
        world.addObject(new Wall(0,2));
        world.addObject(new Wall(0,3));
        world.addObject(new Wall(0,4));
        world.addObject(new Wall(0,5));
        world.addObject(new Wall(0,6));
        world.addObject(new Wall(0,7));
        world.addObject(new Wall(0,8));
        world.addObject(new Wall(0,9));

        world.addObject(new Wall(9,1));
        world.addObject(new Wall(9,2));
        world.addObject(new Wall(9,3));
        world.addObject(new Wall(9,4));
        world.addObject(new Wall(9,5));
        world.addObject(new Wall(9,6));
        world.addObject(new Wall(9,7));
        world.addObject(new Wall(9,8));
        world.addObject(new Wall(9,9));
        
        world.addObject(new Wall(0,9));
        world.addObject(new Wall(1,9));
        world.addObject(new Wall(2,9));
        world.addObject(new Wall(3,9));
        world.addObject(new Wall(4,9));
        world.addObject(new Wall(5,9));
        world.addObject(new Wall(6,9));
        world.addObject(new Wall(7,9));
        world.addObject(new Wall(8,9));
        world.addObject(new Wall(9,9));
        
        world.addObject(new Box("a", 2,1));
        world.addObject(new Goal("a",8,8));
        
        MainWindow mainWindow = new MainWindow();
        
        FSPlanner agent = new FSPlanner(world);
        
        while(!agent.done()) {
            if(agent.iteration == 3) {
                world.addObject(new Box("a", 5,8));
            }
            mainWindow.drawWorld(world);
            Thread.sleep(1000);
            agent.run();
        } 
    }
}
