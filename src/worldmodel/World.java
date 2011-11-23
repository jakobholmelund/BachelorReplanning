/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package worldmodel;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
    private MapObject activeObject;
    private int cols;
    private int rows;
    private String filePath;
    
    
    public World(int rows, int cols){
        this.rows = rows;
        this.cols = cols;
    }
    
    public int getX(){
        return this.cols;
    }
    public int getY(){
        return this.rows;
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
        }else if(mo instanceof MapBox){
            objectMap.put(""+((MapBox)mo).name, mo);
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
     
    public MapAgent createNewAgent(){
        
        int highestnum = 0;
        for(MapObject mo:objects){
            if(mo instanceof MapAgent && ((MapAgent)mo).getNumber() > highestnum){
                highestnum = ((MapAgent)mo).getNumber();
            }
        }
        
        return new MapAgent(highestnum+1,0);
    }
     
    public void persistMoveableObject(int x,int y){
        activeObject.setPosition(x,y);
        this.addObject(activeObject);
        this.removeMovableObject();
    }
    
    public void setMoveAbleObject(MapObject mo){
        this.activeObject = mo;
    }
    
    public MapObject getMoveableObject(){
        return this.activeObject;
    }
    
    public void removeMovableObject(){
        this.activeObject = null;
    }

    public void draw(JPanel parent){
        //parent.updateUI();
        //for(MapObject mo:objects){
        //    parent.add(mo);
        //}
    }
    
    public void save() throws FileNotFoundException, IOException{
        FileDialog fd = new FileDialog( new Frame(), 
        "Save As...", FileDialog.SAVE );
      fd.setVisible(true);
      filePath = fd.getDirectory() + fd.getFile().toString();
      FileOutputStream fos = new FileOutputStream( filePath );
      ObjectOutputStream outStream = new ObjectOutputStream( fos );
      outStream.writeObject( objects );
      outStream.flush();
      
    }
    
    public World load() throws FileNotFoundException, IOException, ClassNotFoundException{
      FileDialog fd = new FileDialog( new Frame(), 
        "Open...", FileDialog.LOAD );
      fd.setVisible(true);
      filePath = fd.getDirectory() + fd.getFile().toString();

      //  Create a stream for reading.
      FileInputStream fis = new FileInputStream( filePath );

      //  Next, create an object that can read from that file.
      ObjectInputStream inStream = new ObjectInputStream( fis );

      // Retrieve the Serializable object.
      objects = ( Collection<MapObject> )inStream.readObject();
      
      this.loadFromObjects(objects);
      World new_world = new World(this.cols,this.rows);
      new_world.loadFromObjects(objects);
      return new_world;
    }
    
    public void loadFromObjects(Collection<MapObject> array){
        map = new CoordinateMap<MapObject>();
        objects = new ArrayList<MapObject>();
        objectMap = new HashMap<String,MapObject>();
        hasChanged = false;
        activeObject = null;
        for(MapObject mo:array){
            this.addObject(mo);
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
                this.moveObject(mapagent, mapagent.x, mapagent.y-1);
            }else if("s".equals(amovedir)){
                this.moveObject(mapagent, mapagent.x, mapagent.y+1);
            }else if("e".equals(amovedir)){
                this.moveObject(mapagent, mapagent.x+1, mapagent.y);
            }else if("w".equals(amovedir)){
                this.moveObject(mapagent, mapagent.x-1, mapagent.y);
            }
        }else{ 
            MapBox box = null;
                if("Push".equals(command)){
                    if("n".equals(amovedir)){
                        box = (MapBox)map.get(mapagent.x, mapagent.y-1);
                    }else if("s".equals(amovedir)){
                        box = (MapBox)map.get(mapagent.x, mapagent.y+1);
                    }else if("e".equals(amovedir)){
                        box = (MapBox)map.get(mapagent.x+1, mapagent.y);
                    }else if("w".equals(amovedir)){
                        box = (MapBox)map.get(mapagent.x-1, mapagent.y);
                    }
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
                }else if("Pull".equals(command)){
                    if("n".equals(boxcurdir)){
                        box = (MapBox)map.get(mapagent.x, mapagent.y-1);
                    }else if("s".equals(boxcurdir)){
                        box = (MapBox)map.get(mapagent.x, mapagent.y+1);
                    }else if("e".equals(boxcurdir)){
                        box = (MapBox)map.get(mapagent.x+1, mapagent.y);
                    }else if("w".equals(boxcurdir)){
                        box = (MapBox)map.get(mapagent.x-1, mapagent.y);
                    }
                    
                    
                    if("n".equals(amovedir)){
                        int agentoldx = mapagent.x;
                        int agentoldy = mapagent.y;
                        this.moveObject(mapagent, mapagent.x, mapagent.y-1);
                        this.moveObject(box, agentoldx, agentoldy);
                    }else if("s".equals(amovedir)){
                        int agentoldx = mapagent.x;
                        int agentoldy = mapagent.y;
                        this.moveObject(mapagent, mapagent.x, mapagent.y+1);
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
