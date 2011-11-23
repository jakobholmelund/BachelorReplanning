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
public class SmallWorld extends World{
    
    public SmallWorld() {
        super(10,10);
        this.addObject(new MapAgent(0, 1 , 1));
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
        
        this.addObject(new Wall(0,1));
        this.addObject(new Wall(0,2));
        this.addObject(new Wall(0,3));
        this.addObject(new Wall(0,4));
        this.addObject(new Wall(0,5));
        this.addObject(new Wall(0,6));
        this.addObject(new Wall(0,7));
        this.addObject(new Wall(0,8));
        this.addObject(new Wall(0,9));

        this.addObject(new Wall(9,1));
        this.addObject(new Wall(9,2));
        this.addObject(new Wall(9,3));
        this.addObject(new Wall(9,4));
        this.addObject(new Wall(9,5));
        this.addObject(new Wall(9,6));
        this.addObject(new Wall(9,7));
        this.addObject(new Wall(9,8));
        this.addObject(new Wall(9,9));
        
        this.addObject(new Wall(0,9));
        this.addObject(new Wall(1,9));
        this.addObject(new Wall(2,9));
        this.addObject(new Wall(3,9));
        this.addObject(new Wall(4,9));
        this.addObject(new Wall(5,9));
        this.addObject(new Wall(6,9));
        this.addObject(new Wall(7,9));
        this.addObject(new Wall(8,9));
        this.addObject(new Wall(9,9));
        
        this.addObject(new MapBox("a", 2,1));
        this.addObject(new Goal("a",8,8));
    }
}
