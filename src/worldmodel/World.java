/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package worldmodel;

import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JPanel;

/**
 *
 * @author jakobenvy
 */
public class World {
    private CoordinateMap<MapObject> map = new CoordinateMap<MapObject>();
    private Collection<MapObject> objects = new ArrayList<MapObject>();
    private boolean hasChanged = false;
    
    public World(){
    
    }
    
    public CoordinateMap<MapObject> getMap(){
        return this.map;
    }
    
    public void addObject(MapObject mo){
        this.objects.add(mo);
        this.map.put(mo.getPosition(), mo);
        this.hasChanged = true;
    }
    
    public void moveObject(MapObject mo, int x, int y){
        long key = map.keyFor(x, y);
        map.update(mo.getPosition(), key, mo);
        mo.setPosition(key);
        this.hasChanged = true;
    }
    //test
    public void removeObject(MapObject mo){
        map.remove(mo.getPosition(), mo);
        this.hasChanged = true;
    }
    
    public boolean hasChanged(){
        this.hasChanged = !this.hasChanged;
        return !hasChanged;
    }
    
     public Collection<MapObject> getObjects(){
        return this.objects;
    }

    public void draw(JPanel parent){
        parent.removeAll();
        for(MapObject mo:objects){
            parent.add(mo);
        }
    }
}
