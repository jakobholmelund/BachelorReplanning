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
        Pattern typeP = Pattern.compile("(^\\w+)\\((\\w*)\\,(\\w+)(\\,(\\w+))?");
        Matcher m = typeP.matcher("Move(1,b)");
        m.find();
        String command = m.group(1);
        System.out.println(command);
        //Pattern agentP = Pattern.compile("\\((\\w*)");
        //m = agentP.matcher("Move(1,b)");
        //m.find();
        String agent = m.group(2);
        String amovedir = m.group(3);    
        String bmovedir = m.group(4);
        
        MapAgent mapagent = (MapAgent)objectMap.get(agent);
        
        if("Move".equals(command)){
            if("n".equals(amovedir)){
                this.moveObject(mapagent, mapagent.getX(), mapagent.getY()+1);
            }else if("s".equals(amovedir)){
                this.moveObject(mapagent, mapagent.getX(), mapagent.getY()-1);
            }else if("e".equals(amovedir)){
                this.moveObject(mapagent, mapagent.getX()+1, mapagent.getY());
            }else if("w".equals(amovedir)){
                this.moveObject(mapagent, mapagent.getX()-1, mapagent.getY());
            }
        }else{ 
            Box box = null;
            if("n".equals(amovedir)){
                box = (Box)map.get(mapagent.getX()+1, mapagent.getX());
            }else if("s".equals(amovedir)){
                box = (Box)map.get(mapagent.getX()+1, mapagent.getX());
            }else if("e".equals(amovedir)){
                box = (Box)map.get(mapagent.getX(), mapagent.getX()+1);
            }else if("w".equals(amovedir)){
                box = (Box)map.get(mapagent.getX(), mapagent.getX()-1);
            }
                if("Push".equals(command)){
                    if("n".equals(bmovedir)){
                        int boxoldx = box.getX();
                        int boxoldy = box.getY();
                        this.moveObject(box, box.getX()-1, box.getY());
                        this.moveObject(mapagent, boxoldx, boxoldy);
                    }else if("s".equals(bmovedir)){
                        int boxoldx = box.getX();
                        int boxoldy = box.getY();
                        this.moveObject(box, box.getX()-1, box.getY());
                        this.moveObject(mapagent, boxoldx, boxoldy);
                    }else if("e".equals(bmovedir)){
                        int boxoldx = box.getX();
                        int boxoldy = box.getY();
                        this.moveObject(box, box.getX()-1, box.getY());
                        this.moveObject(mapagent, boxoldx, boxoldy);
                    }else if("w".equals(bmovedir)){
                        int boxoldx = box.getX();
                        int boxoldy = box.getY();                        
                        this.moveObject(box, box.getX()-1, box.getY());
                        this.moveObject(mapagent, boxoldx, boxoldy);
                    }
                }else if("Pull".equals(command)){
                    if("n".equals(bmovedir)){
                        int agentoldx = mapagent.getX();
                        int agentoldy = mapagent.getY();
                        this.moveObject(mapagent, mapagent.getX()+1, mapagent.getY());
                        this.moveObject(box, agentoldx, agentoldy);
                    }else if("s".equals(bmovedir)){
                        int agentoldx = mapagent.getX();
                        int agentoldy = mapagent.getY();
                        this.moveObject(mapagent, mapagent.getX()-1, mapagent.getY());
                        this.moveObject(box, agentoldx, agentoldy);
                    }else if("e".equals(bmovedir)){
                         int agentoldx = mapagent.getX();
                        int agentoldy = mapagent.getY();
                        this.moveObject(mapagent, mapagent.getX(), mapagent.getY()+1);
                        this.moveObject(box, agentoldx, agentoldy);
                    }else if("w".equals(bmovedir)){
                        int agentoldx = mapagent.getX();
                        int agentoldy = mapagent.getY();
                        this.moveObject(mapagent, mapagent.getX(), mapagent.getY()-1);
                        this.moveObject(box, agentoldx, agentoldy);
                    }
                }
        }
        
    }
}
