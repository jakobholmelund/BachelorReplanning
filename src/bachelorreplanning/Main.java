/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bachelorreplanning;

import gui.MainWindow;
import gui.RouteFinder.Astar;
import jTrolog.errors.PrologException;
import worldmodel.MapAgent;
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
        World world = new World(20,20); //new MiddleWorld();//new World(30,30);
        
        //World world = new World(30,30);
        //world.addObject(new MapAgent(0,1,1));
        //Astar test = new Astar();
        //test.findPlan(world,"move(0,[28,28])");
        //4294967301294967297L,4294967421554051613L
        MainWindow mainWindow = new MainWindow();
        mainWindow.loadNewWorld(world);
        mainWindow.drawOptions();
    }
}
