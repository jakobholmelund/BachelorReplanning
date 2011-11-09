/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package worldmodel;

import Planner.forward.Action;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;
import java.util.regex.*;
/**
 *
 * @author jakobenvy
 */
public class World {
    private CoordinateMap<MapObject> map = new CoordinateMap<MapObject>();
    private Collection<MapObject> objects = new ArrayList<MapObject>();
    private Map<String,MapObject> objectMap = new HashMap<String,MapObject>();
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
        if(mo instanceof MapAgent){
            objectMap.put(""+((MapAgent)mo).id, mo);
        }else if(mo instanceof Box){
            objectMap.put(""+((Box)mo).name, mo);
        }
    }
    
    public void moveObject(MapObject mo, int x, int y){
        long key = map.keyFor(x, y);
        map.update(mo.getPosition(), key, mo);
        mo.setPosition(key,x,y);
        this.hasChanged = true;
    }
    
    public void moveObject(MapObject mo, long key){
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
    
        
    public void agentActionParse(String action){
        /*
        for(Long key: map.coords2value.keySet()){
            System.out.println(key);
            System.out.println(map.get(key));
            MapObject test = map.get(key);
            System.out.println("x: " + test.x + " y: " + test.y);
        }*/
        
        Pattern typeP = Pattern.compile("(^\\w+)\\((\\w*)\\,(\\w+)(\\,(\\w+))?");
        Matcher m = typeP.matcher(action);
        m.find();
        String command = m.group(1);
        System.out.println(command);
        //Pattern agentP = Pattern.compile("\\((\\w*)");
        //m = agentP.matcher("Move(1,b)");
        //m.find();
        String agent = m.group(2);
        String amovedir = m.group(3);    
        String boxcurdir = m.group(5);
        
        MapAgent mapagent = (MapAgent)objectMap.get(agent);
        
        if("Move".equals(command)){
            if("n".equals(amovedir)){
                this.moveObject(mapagent, mapagent.x, mapagent.y+1);
            }else if("s".equals(amovedir)){
                this.moveObject(mapagent, mapagent.x, mapagent.y-1);
            }else if("e".equals(amovedir)){
                this.moveObject(mapagent, mapagent.x+1, mapagent.y);
            }else if("w".equals(amovedir)){
                this.moveObject(mapagent, mapagent.x-1, mapagent.y);
            }
        }else{ 
            Box box = null;
            
            
                if("Push".equals(command)){
                    System.out.println("BOX");
                    System.out.println(map.coords2value.get(map.keyFor(mapagent.x+1, mapagent.y)));
                    
                    if("n".equals(amovedir)){
                        box = (Box)map.get(mapagent.x, mapagent.y-1);
                    }else if("s".equals(amovedir)){
                        
                        box = (Box)map.get(mapagent.x, mapagent.y+1);
                    }else if("e".equals(amovedir)){
                        System.out.println("e");
                        box = (Box)map.get(mapagent.x+1, mapagent.y);
                    }else if("w".equals(amovedir)){
                        System.out.println("w");
                        box = (Box)map.get(mapagent.x-1, mapagent.y);
                    }
                    System.out.println("x " + box.x + " y " + box.y);
                    System.out.println("x " + mapagent.x + " y " + mapagent.y);
                    if("n".equals(boxcurdir)){
                        int boxoldx = box.x;
                        int boxoldy = box.y;
                        this.moveObject(box, box.x, box.y-1);
                        this.moveObject(mapagent, boxoldx, boxoldy);
                    }else if("s".equals(boxcurdir)){
                        int boxoldx = box.x;
                        int boxoldy = box.y;
                        this.moveObject(box, box.x, box.y+1);
                        this.moveObject(mapagent, boxoldx, boxoldy);
                    }else if("e".equals(boxcurdir)){
                        int boxoldx = box.x;
                        int boxoldy = box.y;
                        this.moveObject(box, box.x+1, box.y);
                        this.moveObject(mapagent, boxoldx, boxoldy);
                    }else if("w".equals(boxcurdir)){
                        int boxoldx = box.x;
                        int boxoldy = box.y;                        
                        this.moveObject(box, box.x-1, box.y);
                        this.moveObject(mapagent, boxoldx, boxoldy);
                    }
                    
                    System.out.println("x " + box.x + " y " + box.y);
                    System.out.println("x " + mapagent.x + " y " + mapagent.y);
                    
                }else if("Pull".equals(command)){
                    if("n".equals(boxcurdir)){
                        System.out.println("n");
                        box = (Box)map.get(mapagent.x, mapagent.y+1);
                    }else if("s".equals(boxcurdir)){
                        System.out.println("s");
                        box = (Box)map.get(mapagent.x, mapagent.y-1);
                    }else if("e".equals(boxcurdir)){
                        System.out.println("e");
                        box = (Box)map.get(mapagent.x+1, mapagent.y);
                    }else if("w".equals(boxcurdir)){
                        System.out.println("w");
                        box = (Box)map.get(mapagent.x-1, mapagent.y);
                    }
                    
                    
                    if("n".equals(amovedir)){
                        int agentoldx = mapagent.x;
                        int agentoldy = mapagent.y;
                        this.moveObject(mapagent, mapagent.x, mapagent.y+1);
                        this.moveObject(box, agentoldx, agentoldy);
                    }else if("s".equals(amovedir)){
                        int agentoldx = mapagent.x;
                        int agentoldy = mapagent.y;
                        this.moveObject(mapagent, mapagent.x, mapagent.y-1);
                        this.moveObject(box, agentoldx, agentoldy);
                    }else if("e".equals(amovedir)){
                         int agentoldx = mapagent.x;
                        int agentoldy = mapagent.y;
                        this.moveObject(mapagent, mapagent.x+1, mapagent.y);
                        this.moveObject(box, agentoldx, agentoldy);
                    }else if("w".equals(amovedir)){
                        int agentoldx = mapagent.x;
                        int agentoldy = mapagent.y;
                        this.moveObject(mapagent, mapagent.x-1, mapagent.y);
                        this.moveObject(box, agentoldx, agentoldy);
                    }
                }
        }
        
        System.out.println();
    }
}
