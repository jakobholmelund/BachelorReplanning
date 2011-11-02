/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bachelorreplanning;

import Planner.Logic;
import Planner.naiveReplan.FSPlanner;
import Planner.naiveReplan.Plan;
import Planner.naiveReplan.Problem;
import Planner.naiveReplan.State;
import gui.MainWindow;
import jTrolog.errors.PrologException;
import jTrolog.parser.Parser;
import jTrolog.engine.*;
import jTrolog.terms.*;
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
        world.addObject(new MapAgent(0,0));
        world.addObject(new Wall(1,0));
        world.addObject(new Wall(2,0));
        world.addObject(new Wall(3,0));
        world.addObject(new Wall(4,0));
        world.addObject(new Wall(5,0));
        world.addObject(new Wall(6,0));
        world.addObject(new MapAgent(9,9));
        MainWindow mainWindow = new MainWindow();
        mainWindow.drawWorld(world);
        
        /*
        FSPlanner agent = new FSPlanner();
        agent.run();
        agent.run();
        agent.run();
        agent.run();
        agent.run();
        agent.run();*/
    }
}
