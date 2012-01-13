/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bachelorreplanning;

//import Planner.ReFSP.ReFSPlanner;
import gui.MainWindow;
import jTrolog.errors.PrologException;
import worldmodel.World;

/**
 *
 * @author Dan True
 */
public class Main {
    
    public static void main(String[] args) throws PrologException, InterruptedException  {
        World world = new World(20,20);
        MainWindow mainWindow = new MainWindow();
        mainWindow.loadNewWorld(world);
        //world.parseAction("move(a,[1,1])");
        //LinkedList<String> goals = new LinkedList<String>();
        //world.addObject(new MapBox("a", 18,13));
        //world.addObject(new MapBox("b", 15,4));
        //world.addObject(new MapBox("c", 17,17));
        //world.addObject(new MapBox("d", 2,14));
        
        //world.addObject(new Goal("a",6,11));
        //world.addObject(new Goal("b",2,15));
        //world.addObject(new Goal("c",1,1));
        //world.addObject(new Goal("d",18,5));
        
        //goals.add("at(a,[9,2])");
        //goals.add("at(b,[2,15])");
        //goals.add("at(c,[1,1])");
        //goals.add("at(d,[18,5])");
        
        //goals.add("at(a,[15;15])");
        //goals.add("agentAt(0,[1;1])");
        //goals.add("agentAt(0,[13;13])");
        //goals.add("agentAt(0,[13;4])");
        //POPlanner planner = new POPlanner(world, 0, goals); // at(a,[5;5]) // agentAt(0,[5;5]) //null
        //NFSPlanner planner = new NFSPlanner(world, 0, goals.getFirst(), "a"); // at(a,[5;5]) // agentAt(0,[5;5])
        
        //Thread init = new Thread(planner);
        //init.start();
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


