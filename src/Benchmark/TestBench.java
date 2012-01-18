/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Benchmark;

import Planner.State;
import jTrolog.errors.PrologException;
import java.util.ArrayList;
import worldmodel.Bomb;
import worldmodel.Goal;
import worldmodel.MapAgent;
import worldmodel.MapBox;
import worldmodel.MapObject;
import worldmodel.Wall;
import worldmodel.World;

/**
 *
 * @author jakobchronos
 */
public class TestBench {
    
    public void getPercepts(World world) throws PrologException {
       long time1 = System.currentTimeMillis();
       String domain = "";
       //System.out.println("Get percepts --->");
       // add to percepts
       for(int i = 0; i < world.getX(); i++) {
            for(int j = 0; j < world.getY(); j++) {
                long key = world.getMap().keyFor(j, i);
                Object[] o = world.getMap().get(key);
                
                if(o == null || o.length == 0){
                    domain += "f([" + j + "," + i + "]). ";
                    //if(i == 3 && j == 2) {
                    //    System.out.println("   At 2,3 THERE IS FREE 1");
                    //}
                }else{
                    for(int k = 0; k < o.length; k++) {
                        MapObject obj = (MapObject) o[k];
                        //if(i == 3 && j == 2) {
                        //    System.out.println("   At 2,3 THERE IS SOMETHING: " + obj.toString());
                        //}
                        if(obj instanceof MapAgent) {
                            //System.err.println("Agent found");
                            MapAgent agent = (MapAgent) obj;
                            domain += "agentAt(" + agent.getNumber() + ",[" + agent.x + "," + agent.y + "]). ";
                            domain += "f([" + agent.x + "," + agent.y + "]). ";
                            if(agent.getCarying() != null) {
                                domain += "carries(" + agent.getNumber() + "," + agent.getCarying().getId() + "). ";
                            }
                            //if(i == 3 && j == 2) {
                            //    System.out.println("   At 2,3 THERE IS AN AGENT");
                            //}
                        }else if(obj instanceof MapBox) {
                            //System.err.println("Box found");
                            MapBox obs = (MapBox) obj;
                            domain += "at(" + obs.getId() + ",[" + obs.x + "," + obs.y + "]). "; 
                            domain += "box(" + obs.getId() + "). ";
                            //domain += "f([" + obs.x + "," + obs.y + "]). ";
                            //domain += "item(" + obs.getId() + "). ";
                            //if(i == 3 && j == 2) {
                            //    System.out.println("   At 2,3 THERE IS A BOX");
                            //}
                        }else if(obj instanceof Bomb) {
                            //System.err.println("Box found");
                            Bomb obs = (Bomb) obj;
                            domain += "at(" + obs.getId() + ",[" + obs.x + "," + obs.y + "]). ";
                            domain += "f([" + obs.x + "," + obs.y + "]). ";
                            domain += "item(" + obs.getId() + "). ";
                            //if(i == 3 && j == 2) {
                            //    System.out.println("   At 2,3 THERE IS A BOMB");
                            //}
                        }else if(obj instanceof Goal) {
                            //System.err.println("goal found");
                            Goal obs = (Goal) obj;
                            domain += "goalAt(" + obs.getId() + ",[" + obs.x + "," + obs.y + "]). ";
                            domain += "f([" + obs.x + "," + obs.y + "]). ";
                            //if(i == 3 && j == 2) {
                            //    System.out.println("   At 2,3 THERE IS A GOAL");
                            //}
                        }else if(obj instanceof Wall) {
                            //System.err.println("wall found");
                            Wall w = (Wall) obj;
                            domain += "w([" + w.x + "," + w.y + "]). "; 
                            //if(i == 3 && j == 2) {
                            //    System.out.println("   At 2,3 THERE IS WALL");
                            //}
                        }else{
                            domain += "f([" + i + "," + j + "]). ";
                            if(i == 3 && j == 2) {
                                System.out.println("   At 2,3 THERE IS FREE 1");
                            }
                        }
                    }
                }
            }
       }
      
        String theory = domain;
        //System.out.println("statics:\n " + statics);
        //System.out.println("objects:\n " + world.getObjects().toString());
        //System.out.println("Num objects: " + world.getObjects().size() + "\n Theory:");
        
        //System.err.println(theory);
        new State(theory);
        //System.out.println("\n\n" + this.state.toString() + "\n\n");
        //System.out.println("Got these percepts: \n" + this.state.toString());
        long time2 = System.currentTimeMillis();
                
       System.out.println("Percepts gotten in: " + (time2 - time1) + " ms for " + world.getX() + "," + world.getY());
    }
    
    
    
}
