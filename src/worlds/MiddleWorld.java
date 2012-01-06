/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package worlds;

import worldmodel.*;

/**
 *
 * @author Dan True
 */
public class MiddleWorld extends World{
    
    public MiddleWorld() {
        super(20,20);
        // Agent
        this.addObject(new MapAgent(0, 2, 2));
        //this.addObject(new Wall(2,10));
        
        // Main Walls
        this.addObject(new Wall(0,0));
        this.addObject(new Wall(1,0));
        this.addObject(new Wall(2,0));
        this.addObject(new Wall(3,0));
        this.addObject(new Wall(4,0));
        this.addObject(new Wall(5,0));
        this.addObject(new Wall(6,0));
        this.addObject(new Wall(7,0));
        this.addObject(new Wall(8,0));
        this.addObject(new Wall(9,0));
        this.addObject(new Wall(10,0));
        this.addObject(new Wall(11,0));
        this.addObject(new Wall(12,0));
        this.addObject(new Wall(13,0));
        this.addObject(new Wall(14,0));
        this.addObject(new Wall(15,0));
        this.addObject(new Wall(16,0));
        this.addObject(new Wall(17,0));
        this.addObject(new Wall(18,0));
        this.addObject(new Wall(19,0));

        this.addObject(new Wall(0,19));
        this.addObject(new Wall(1,19));
        this.addObject(new Wall(2,19));
        this.addObject(new Wall(3,19));
        this.addObject(new Wall(4,19));
        this.addObject(new Wall(5,19));
        this.addObject(new Wall(6,19));
        this.addObject(new Wall(7,19));
        this.addObject(new Wall(8,19));
        this.addObject(new Wall(9,19));
        this.addObject(new Wall(10,19));
        this.addObject(new Wall(11,19));
        this.addObject(new Wall(12,19));
        this.addObject(new Wall(13,19));
        this.addObject(new Wall(14,19));
        this.addObject(new Wall(15,19));
        this.addObject(new Wall(16,19));
        this.addObject(new Wall(17,19));
        this.addObject(new Wall(18,19));
        this.addObject(new Wall(19,19));

        this.addObject(new Wall(0,1));
        this.addObject(new Wall(0,2));
        this.addObject(new Wall(0,3));
        this.addObject(new Wall(0,4));
        this.addObject(new Wall(0,5));
        this.addObject(new Wall(0,6));
        this.addObject(new Wall(0,7));
        this.addObject(new Wall(0,8));
        this.addObject(new Wall(0,9));
        this.addObject(new Wall(0,10));
        this.addObject(new Wall(0,11));
        this.addObject(new Wall(0,12));
        this.addObject(new Wall(0,13));
        this.addObject(new Wall(0,14));
        this.addObject(new Wall(0,15));
        this.addObject(new Wall(0,16));
        this.addObject(new Wall(0,17));
        this.addObject(new Wall(0,18));
        this.addObject(new Wall(0,19));

        this.addObject(new Wall(19,1));
        this.addObject(new Wall(19,2));
        this.addObject(new Wall(19,3));
        this.addObject(new Wall(19,4));
        this.addObject(new Wall(19,5));
        this.addObject(new Wall(19,6));
        this.addObject(new Wall(19,7));
        this.addObject(new Wall(19,8));
        this.addObject(new Wall(19,9));
        this.addObject(new Wall(19,10));
        this.addObject(new Wall(19,11));
        this.addObject(new Wall(19,12));
        this.addObject(new Wall(19,13));
        this.addObject(new Wall(19,14));
        this.addObject(new Wall(19,15));
        this.addObject(new Wall(19,16));
        this.addObject(new Wall(19,17));
        this.addObject(new Wall(19,18));
        this.addObject(new Wall(19,19));
        
        // Other walls
        this.addObject(new Wall(3,2));
        this.addObject(new Wall(3,3));
        this.addObject(new Wall(3,4));
        this.addObject(new Wall(3,5));
        this.addObject(new Wall(3,6));
        this.addObject(new Wall(3,7));
        this.addObject(new Wall(4,2));
        this.addObject(new Wall(4,3));
        this.addObject(new Wall(4,4));
        this.addObject(new Wall(4,5));
        this.addObject(new Wall(4,6));
        this.addObject(new Wall(4,7));
        
        this.addObject(new Wall(6,2));
        this.addObject(new Wall(6,3));
        this.addObject(new Wall(6,4));
        this.addObject(new Wall(6,5));
        this.addObject(new Wall(6,6));
        this.addObject(new Wall(6,7));
        this.addObject(new Wall(7,2));
        this.addObject(new Wall(7,3));
        this.addObject(new Wall(7,4));
        this.addObject(new Wall(7,5));
        this.addObject(new Wall(7,6));
        this.addObject(new Wall(7,7));
        
        this.addObject(new Wall(9,1));
        this.addObject(new Wall(10,1));
        this.addObject(new Wall(11,1));
        this.addObject(new Wall(12,1));
        this.addObject(new Wall(13,1));
        this.addObject(new Wall(14,1));
        this.addObject(new Wall(15,1));
        this.addObject(new Wall(16,1));
        this.addObject(new Wall(17,1));
        this.addObject(new Wall(18,1));
        this.addObject(new Wall(19,1));
        
        this.addObject(new Wall(9,2));
        this.addObject(new Wall(10,2));
        this.addObject(new Wall(11,2));
        this.addObject(new Wall(12,2));
        this.addObject(new Wall(13,2));
        this.addObject(new Wall(14,2));
        this.addObject(new Wall(15,2));
        this.addObject(new Wall(16,2));
        this.addObject(new Wall(17,2));
        this.addObject(new Wall(18,2));
        this.addObject(new Wall(19,2));
        
        this.addObject(new Wall(9,3));
        this.addObject(new Wall(10,3));
        this.addObject(new Wall(11,3));
        this.addObject(new Wall(12,3));
        
        this.addObject(new Wall(9,4));
        this.addObject(new Wall(10,4));
        this.addObject(new Wall(11,4));
        this.addObject(new Wall(12,4));
        
        this.addObject(new Wall(9,6));
        this.addObject(new Wall(10,6));
        this.addObject(new Wall(11,6));
        this.addObject(new Wall(12,6));
        
        this.addObject(new Wall(9,7));
        this.addObject(new Wall(10,7));
        this.addObject(new Wall(11,7));
        this.addObject(new Wall(12,7));
        
        this.addObject(new Wall(9,8));
        this.addObject(new Wall(10,8));
        this.addObject(new Wall(11,8));
        this.addObject(new Wall(12,8));
        this.addObject(new Wall(13,8));
        this.addObject(new Wall(14,8));
        this.addObject(new Wall(15,8));
        this.addObject(new Wall(16,8));
        
        this.addObject(new Wall(9,9));
        this.addObject(new Wall(10,9));
        this.addObject(new Wall(11,9));
        this.addObject(new Wall(12,9));
        this.addObject(new Wall(13,9));
        this.addObject(new Wall(14,9));
        this.addObject(new Wall(15,9));
        this.addObject(new Wall(16,9));
        
        this.addObject(new Wall(18,1));
        this.addObject(new Wall(18,2));
        this.addObject(new Wall(18,3));
        this.addObject(new Wall(18,4));
        this.addObject(new Wall(18,6));
        this.addObject(new Wall(18,7));
        this.addObject(new Wall(18,8));
        this.addObject(new Wall(18,9));
        this.addObject(new Wall(18,10));
        this.addObject(new Wall(18,11));
        
        this.addObject(new Wall(19,3));
        this.addObject(new Wall(19,4));
        this.addObject(new Wall(19,6));
        this.addObject(new Wall(19,7));
        
        this.addObject(new Wall(20,3));
        this.addObject(new Wall(20,4));
        this.addObject(new Wall(20,6));
        this.addObject(new Wall(20,7));
        
        this.addObject(new Wall(9,11));
        this.addObject(new Wall(10,11));
        this.addObject(new Wall(11,11));
        this.addObject(new Wall(12,11));
        this.addObject(new Wall(13,11));
        this.addObject(new Wall(14,11));
        this.addObject(new Wall(15,11));
        this.addObject(new Wall(16,11));
        
        this.addObject(new Wall(1,10));
        this.addObject(new Wall(1,11));
        //this.addObject(new Wall(2,10));
        this.addObject(new Wall(3,10));
        this.addObject(new Wall(3,11));
        this.addObject(new Wall(4,10));
        this.addObject(new Wall(5,10));
        this.addObject(new Wall(5,11));
        this.addObject(new Wall(6,10));
        this.addObject(new Wall(7,10));
        this.addObject(new Wall(7,11));
        this.addObject(new Wall(7,12));
        this.addObject(new Wall(7,13));
        this.addObject(new Wall(6,13));
        this.addObject(new Wall(6,14));
        this.addObject(new Wall(6,15));
        this.addObject(new Wall(6,16));
        this.addObject(new Wall(6,17));
        
        this.addObject(new Wall(2,16));
        this.addObject(new Wall(2,17));
        this.addObject(new Wall(2,18));
        this.addObject(new Wall(2,19));
        this.addObject(new Wall(3,16));
        this.addObject(new Wall(3,17));
        this.addObject(new Wall(3,18));
        this.addObject(new Wall(3,19));
        this.addObject(new Wall(4,19));
        this.addObject(new Wall(5,19));
        this.addObject(new Wall(6,19));
        this.addObject(new Wall(7,19));
        this.addObject(new Wall(8,19));
        this.addObject(new Wall(9,19));
        
        this.addObject(new Oil("oil1",2,12));
        
        // Boxes
        this.addObject(new MapBox("a", 18,12));
        this.addObject(new MapBox("b", 15,4));
        this.addObject(new MapBox("c", 17,17));
        this.addObject(new MapBox("d", 2,15));
        this.addObject(new Goal("a",6,11));
        this.addObject(new Goal("b",2,15));
        this.addObject(new Goal("c",1,1));
        this.addObject(new Goal("d",18,13));
        // Goals
     
        
    }
}
