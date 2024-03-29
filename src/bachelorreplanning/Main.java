/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bachelorreplanning;

//import Planner.ReFSP.ReFSPlanner;
import Benchmark.TestBench;
import gui.MainWindow;
import gui.RouteFinder.Astar;
import jTrolog.errors.PrologException;
import worldmodel.Wall;
import worldmodel.World;

/**
 *
 * @author Dan True
 */
public class Main {
    
    public static void main(String[] args) throws PrologException, InterruptedException  {
        
        //for(int i=0;i<10;i++){
        //    world.addObject(new Wall(i,0));
        //    world.addObject(new Wall(0,i));
        //    world.addObject(new Wall(i,9));
        //    world.addObject(new Wall(9,i));
        //}
        
        World world = new World(60,60);
        MainWindow mainWindow = new MainWindow();
        mainWindow.loadNewWorld(world);
        /*
        Astar bench = new Astar();
        World world = new World(10,10);
        bench.findPlanBench(world.simpleMap(), world.getMap().keyFor(0, 0), world.getMap().keyFor(9, 9));
        world = new World(20,20);
        bench.findPlanBench(world.simpleMap(), world.getMap().keyFor(0, 0), world.getMap().keyFor(19, 19));
        world = new World(40,40);
        bench.findPlanBench(world.simpleMap(), world.getMap().keyFor(0, 0), world.getMap().keyFor(39, 39));
        world = new World(80,80);
        bench.findPlanBench(world.simpleMap(), world.getMap().keyFor(0, 0), world.getMap().keyFor(79, 79));
        world = new World(160,160);
        bench.findPlanBench(world.simpleMap(), world.getMap().keyFor(0, 0), world.getMap().keyFor(159, 159));
        world = new World(320,320);
        bench.findPlanBench(world.simpleMap(), world.getMap().keyFor(0, 0), world.getMap().keyFor(319, 319));
        world = new World(640,640);
        bench.findPlanBench(world.simpleMap(), world.getMap().keyFor(0, 0), world.getMap().keyFor(639, 639));
        world = new World(1280,1280);
        bench.findPlanBench(world.simpleMap(), world.getMap().keyFor(0, 0), world.getMap().keyFor(1279, 1279));
        //mainWindow.loadNewWorld(world);
        */
        //world.act("moveAtomic(1,E)");
        
        /*
        TestBench bench = new TestBench();
        
        World world1 = new World(10,10);
        World world2 = new World(50,50);
        World world3 = new World(100,100);
        World world4 = new World(200,200);
        World world5 = new World(300,300);
        
        world1.fillBlocks();
        world2.fillBlocks();
        world3.fillBlocks();
        world4.fillBlocks();
        world5.fillBlocks();
        
        bench.getPercepts(world1);
        bench.getPercepts(world2);
        bench.getPercepts(world3);
        bench.getPercepts(world4);
        bench.getPercepts(world5);
        */
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


