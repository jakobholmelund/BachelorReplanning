/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import worldmodel.MapObject;
import worldmodel.World;

/**
 *
 * @author jakopchronos
 */
public class AddItemController {
    private MapObject activeObject;
    private World world;
    
    public AddItemController(World w){
        this.world = w;
    }
    
    public void persist(int x,int y){
        activeObject.setPosition(x,y);
        this.world.addObject(activeObject);
    }
    
    public void setActive(MapObject mo){
        this.activeObject = mo;
    }
    
    public MapObject getActive(){
        return this.activeObject;
    }
    
    public void removeActive(){
        this.activeObject = null;
    }
}
