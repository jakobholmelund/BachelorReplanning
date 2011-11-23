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
public class LargeWorld extends World{
    
    public LargeWorld() {
        super(30,30);
        // Agent
        this.addObject(new MapAgent(0, 1, 1));
        
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
        this.addObject(new Wall(20,0));
        this.addObject(new Wall(21,0));
        this.addObject(new Wall(22,0));
        this.addObject(new Wall(23,0));
        this.addObject(new Wall(24,0));
        this.addObject(new Wall(25,0));
        this.addObject(new Wall(26,0));
        this.addObject(new Wall(27,0));
        this.addObject(new Wall(28,0));
        this.addObject(new Wall(29,0));

        this.addObject(new Wall(0,29));
        this.addObject(new Wall(1,29));
        this.addObject(new Wall(2,29));
        this.addObject(new Wall(3,29));
        this.addObject(new Wall(4,29));
        this.addObject(new Wall(5,29));
        this.addObject(new Wall(6,29));
        this.addObject(new Wall(7,29));
        this.addObject(new Wall(8,29));
        this.addObject(new Wall(9,29));
        this.addObject(new Wall(10,29));
        this.addObject(new Wall(11,29));
        this.addObject(new Wall(12,29));
        this.addObject(new Wall(13,29));
        this.addObject(new Wall(14,29));
        this.addObject(new Wall(15,29));
        this.addObject(new Wall(16,29));
        this.addObject(new Wall(17,29));
        this.addObject(new Wall(18,29));
        this.addObject(new Wall(19,29));
        this.addObject(new Wall(20,29));
        this.addObject(new Wall(21,29));
        this.addObject(new Wall(22,29));
        this.addObject(new Wall(23,29));
        this.addObject(new Wall(24,29));
        this.addObject(new Wall(25,29));
        this.addObject(new Wall(26,29));
        this.addObject(new Wall(27,29));
        this.addObject(new Wall(28,29));
        this.addObject(new Wall(29,29));

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
        this.addObject(new Wall(0,20));
        this.addObject(new Wall(0,21));
        this.addObject(new Wall(0,22));
        this.addObject(new Wall(0,23));
        this.addObject(new Wall(0,24));
        this.addObject(new Wall(0,25));
        this.addObject(new Wall(0,26));
        this.addObject(new Wall(0,27));
        this.addObject(new Wall(0,28));
        this.addObject(new Wall(0,29));

        this.addObject(new Wall(29,1));
        this.addObject(new Wall(29,2));
        this.addObject(new Wall(29,3));
        this.addObject(new Wall(29,4));
        this.addObject(new Wall(29,5));
        this.addObject(new Wall(29,6));
        this.addObject(new Wall(29,7));
        this.addObject(new Wall(29,8));
        this.addObject(new Wall(29,9));
        this.addObject(new Wall(29,10));
        this.addObject(new Wall(29,11));
        this.addObject(new Wall(29,12));
        this.addObject(new Wall(29,13));
        this.addObject(new Wall(29,14));
        this.addObject(new Wall(29,15));
        this.addObject(new Wall(29,16));
        this.addObject(new Wall(29,17));
        this.addObject(new Wall(29,18));
        this.addObject(new Wall(29,19));
        this.addObject(new Wall(29,20));
        this.addObject(new Wall(29,21));
        this.addObject(new Wall(29,22));
        this.addObject(new Wall(29,23));
        this.addObject(new Wall(29,24));
        this.addObject(new Wall(29,25));
        this.addObject(new Wall(29,26));
        this.addObject(new Wall(29,27));
        this.addObject(new Wall(29,28));
        
        // Other walls
        this.addObject(new Wall(3,2));
        this.addObject(new Wall(3,3));
        this.addObject(new Wall(3,4));
        this.addObject(new Wall(3,5));
        this.addObject(new Wall(3,6));
        this.addObject(new Wall(4,2));
        this.addObject(new Wall(4,3));
        this.addObject(new Wall(4,4));
        this.addObject(new Wall(4,5));
        this.addObject(new Wall(4,6));
        
        this.addObject(new Wall(6,2));
        this.addObject(new Wall(6,3));
        this.addObject(new Wall(6,4));
        this.addObject(new Wall(6,5));
        this.addObject(new Wall(6,6));
        this.addObject(new Wall(7,2));
        this.addObject(new Wall(7,3));
        this.addObject(new Wall(7,4));
        this.addObject(new Wall(7,5));
        this.addObject(new Wall(7,6));
        
        // Boxes
        this.addObject(new Box("a", 2,1));
        
        // Goals
        this.addObject(new Goal("a",8,8));
        
    }
}
