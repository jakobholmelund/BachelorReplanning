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
import controllers.AddItemController;
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
        World world = new LargeWorld();
        
        Box test = new Box("a",0,0);
        AddItemController addItemController = new AddItemController(world);
        addItemController.setActive(test);
        MainWindow mainWindow = new MainWindow(addItemController);
        mainWindow.loadNewWorld(world);
        mainWindow.drawWorld();
        FSPlanner agent = new FSPlanner(world);
        Thread init = new Thread(agent);
        while(!agent.done()) {
            if(agent.iteration == 3) {
                world.addObject(new Box("b", 5,8));
            }
            mainWindow.drawWorld();
            init.run();
        }
        init.join();
    }
}
