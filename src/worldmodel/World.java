
package worldmodel;

import Planner.NFSP.NFSPlanner;
import Planner.POP.POPlanner;
import gui.WorldPanel;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
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
    private ArrayList<Thread> runningAgents = new ArrayList<Thread>();
    private WorldPanel wp;
    
    
    public World(int rows, int cols){
        this.rows = rows;
        this.cols = cols;
        
    }
    
    public void setPanel(WorldPanel panel){
        this.wp = panel;
    }
    
    public void draw(){
        this.wp.update(this);
    }
    
    public int getX(){
        return this.cols;
    }
    public int getY(){
        return this.rows;
    }
    
    public void startAgentsPOP() throws InterruptedException{
        synchronized(this){
            LinkedList<String> goals = new LinkedList<String>();
            LinkedList<Integer> agents = new LinkedList<Integer>();
            int agentid = 0;
            for(MapObject mo:objects){
                if(mo instanceof MapAgent){
                    agentid = Integer.parseInt(mo.getId());
                    agents.add(agentid);
                }
                if(mo instanceof Goal){
                    Goal g = (Goal)mo;
                    int[] coords = map.coordsFor(g.getPosition());
                    goals.add("at(" + g.getName() + ",["+coords[0]+","+coords[1]+"])");
                }
            }
            //Thread[] threads = new Thread[agents.size()];
            for(int id : agents){
                POPlanner planner = new POPlanner(this, id, goals);
                Thread init = new Thread(planner);
                init.start();
            }
            
        }
    }
    
    public void startAgentsNFS() throws InterruptedException{
        synchronized(this){
            LinkedList<String> goals = new LinkedList<String>();
            int agentid = 0;
            for(MapObject mo:objects){
                if(mo instanceof MapAgent){
                    agentid = Integer.parseInt(mo.getId());
                }
                if(mo instanceof Goal){
                    Goal g = (Goal)mo;
                    int[] coords = map.coordsFor(g.getPosition());
                    goals.add("at(" + g.getName() + ",["+coords[0]+", "+coords[1]+"])");
                }
            }
        
            NFSPlanner planner = new NFSPlanner(this, agentid, goals.getFirst(), "1");
            Thread init = new Thread(planner);
            init.start();
        }
    }
    
    public CoordinateMap<MapObject> getMap(){
        return this.map;
    }
    
    public void addObjects(long position, MapObject[] mo){
        for(int i=0;i<mo.length;i++){
            this.objects.add(mo[i]);
            this.hasChanged = true;
                if(mo[i] instanceof MapAgent){
                objectMap.put(""+((MapAgent)mo[i]).id, mo[i]);
            }else if(mo[i] instanceof MapBox){
                objectMap.put(""+((MapBox)mo[i]).name, mo[i]);
            }
        }
        this.map.put(position, mo);
    }
    
    public void addObject(MapObject mo){
        
            this.objects.add(mo);
            this.hasChanged = true;
                if(mo instanceof MapAgent){
                objectMap.put(""+((MapAgent)mo).id, mo);
            }else if(mo instanceof MapBox){
                objectMap.put(""+((MapBox)mo).name, mo);
            }else if(mo instanceof Bomb){
                objectMap.put(""+((Bomb)mo).name, mo);
            }
                mo.repaint=false;
        this.map.add(mo.getPosition(),mo);
    }
    
    
    public boolean moveObject(MapObject mo, int x, int y){
        return moveObject(mo,map.keyFor(x, y));
    }
    
    public boolean moveObject(MapObject mo, long key){
        synchronized(this){
            Tile oldParent = (Tile)mo.getParent();
            if(oldParent != null){
                oldParent.repaint = true;
            }
            //Checks ir there is objects on field and if objects interfierce with plan.
            Object[] mobjects = map.get(key);
            if(mobjects != null){
                for(int i=0;i<mobjects.length;i++){
                    MapObject mobject = (MapObject)mobjects[i];
                    
                    
                    
                    //Check if field i naibour
                    if(mobject instanceof Wall || mobject instanceof MapAgent || !map.neighborsFor(key).contains(mo)){
                        
                        System.out.println("Cant move here");
                        return false;
                    }
                    
                    //Check if oilpud
                    if(mobject instanceof Oil){
                        //System.out.println("OILPUUUUUUUUD");
                        ArrayList<Long> neighbors = map.emptyNeighborsKeysFor(key);
                        Random random = new Random();
                        int test = random.nextInt(neighbors.size());
                        key = neighbors.get(test);
                    }
                    
                    if(mobject instanceof MapBox && mo instanceof MapAgent){
                        MapAgent mamo = (MapAgent)mo;
                        if(mamo.carying != null){
                            Random random = new Random();
                            int randtest = random.nextInt(100);
                            if(randtest>50){
                                return false;
                            }
                        }
                    }
                    
                }
                
            }
            
            if(mo instanceof MapAgent && ((MapAgent)mo).carying != null){
                map.update(mo.getPosition(), key, new MapObject[]{mo,((MapAgent)mo).carying});
            }else{
                map.update(mo.getPosition(), key, new MapObject[]{mo});
            }
            mo.setPosition(key);
            mo.setRepaint(true);
         
            this.hasChanged = true;
            return true;
        }
    }
    
    //test
    public void removeObject(MapObject mo){
        map.removeObjects(mo.getPosition(), new MapObject[]{mo});
        objectMap.remove(mo.getId());
        objects.remove(mo);
        this.hasChanged = true;
    }
    
    public void removeObjectSpecial(MapObject mo){
        map.removeObjects(mo.getPosition(), new MapObject[]{mo});
        objectMap.remove(mo.getId());
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
    
    public Bomb createBomb(){
        return new Bomb("q",0);
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
      Collection<MapObject> loadedObjects = ( Collection<MapObject> )inStream.readObject();
      
      //this.loadFromObjects(objects);
      World new_world = new World(this.cols,this.rows);
      new_world.loadFromObjects(loadedObjects);
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
   
    public HashSet<Long> simpleMap(){
        HashSet<Long> coords2value = new HashSet<Long>();
        for(int i = 0; i < cols; i++) {
            for(int j = 0; j < rows; j++) {
                long key = map.keyFor(j, i);
                Object[] mapobjects = map.get(key);
                if(mapobjects != null){
                    for(Object mo:mapobjects){
                            if(!((MapObject)mo instanceof MapAgent) && !((MapObject)mapobjects[0] instanceof Wall)){
                                coords2value.add(key);
                            }
                        }
                }else{
                    coords2value.add(key);
                }
            }
        }
        return coords2value;
    }
    
    public long[] parseAction(String action){
        Pattern typeP = Pattern.compile("(^\\w+)\\((\\w*)\\,\\[(\\d+\\,\\d+)\\]"); //(\\,(\\w+))?
        Matcher m = typeP.matcher(action);
        m.find();
        System.out.println(m);
        String command = m.group(1);
        String agent = m.group(2);
        String pos = m.group(3);
        String[] coords = pos.split(",");
        return new long[]{((MapAgent)objectMap.get(agent)).getPosition(),map.keyFor(Integer.parseInt(coords[0]), Integer.parseInt(coords[1])),Long.parseLong(agent)};
    }
    /*
    public String[][] splitAction(String action) {
        Pattern typeP = Pattern.compile("^(\\!?.*)\\((.[^),]*)\\)?(\\,(.*)\\))?");
        Matcher m = typeP.matcher(action);
        System.out.println(m);
        boolean matchFound = m.find();
        if (matchFound) {
            System.out.println("MATCH FOUND");
            String command = m.group(1);
            String arg1 = m.group(2);
            String arg2 = m.group(4);
                        
            System.out.println(command);
            System.out.println(arg1);
            System.out.println(m.group(3));
            System.out.println(arg2);
            return new String[][]{new String[]{command},new String[]{arg1,arg2}};
        }
        return null;
    }*/
    
    public boolean pickUp(MapAgent ma,MapObject mo){
        if(mo != null){
            ma.pickUp(mo);
            this.removeObject(mo);
            return true;
        }
        return false;
    }
    
    public boolean smash(MapAgent ma,MapObject mo){
        if(map.neighborsFor(ma.getPosition()).contains(mo)){
            //System.out.println("SMAAAAAAAAAAAAAAAAAAAAAAASH");
            this.removeObject(mo);
            return true;
        }
        return false;
    }
    
    public boolean place(MapAgent ma){
        if(ma.carying != null){
            MapObject mo = ma.getCarying();
            mo.setPosition(ma.getPosition());
            this.addObject(mo);
            ma.place();
            return true;
        }
        return false;
    }
    
   public boolean act(String action){
        Pattern typeP = Pattern.compile("(^\\w+)\\((\\w*)\\,\\s?(\\w+)\\)");
        Matcher m = typeP.matcher(action);
        boolean matchFound = m.find();
        if (matchFound) {
            MapAgent agent = (MapAgent)objectMap.get(m.group(2));
            if(m.group(1).equals("moveAtomic")){
                String amovedir = m.group(3);
                if("n".equals(amovedir)){
                    return this.moveObject(agent, agent.x,agent.y-1);
                }else if("s".equals(amovedir)){
                    return this.moveObject(agent, agent.x, agent.y+1);
                }else if("e".equals(amovedir)){
                    return this.moveObject(agent, agent.x+1, agent.y);
                }else if("w".equals(amovedir)){
                    return this.moveObject(agent, agent.x-1, agent.y);
                }
                return false;
            }else if(m.group(1).equals("pickUp")){
                MapObject object = objectMap.get(m.group(3));
                return this.pickUp(agent,object);
            }else if(m.group(1).equals("place")){
               return this.place(agent);
            }else if(m.group(1).equals("smash")){
               MapObject object = objectMap.get(m.group(3));
               return this.smash(agent,object);
            }
        }
        return false;
    }
    
    public void agentActionParse(String action){
        
        //for(Long key: map.coords2value.keySet()){
        //    System.out.println(key);
        //    System.out.println(map.get(key));
        //    MapObject test = map.get(key);
        //    System.out.println("x: " + test.x + " y: " + test.y);
        //}
        
        Pattern typeP = Pattern.compile("(^\\w+)\\((\\w*)\\,(\\w+)(\\,(\\w+))?");
        Matcher m = typeP.matcher(action);
        m.find();
        String command = m.group(1);
        System.out.println(command);
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
                        box = (MapBox)map.get(mapagent.x, mapagent.y-1)[0];
                    }else if("s".equals(amovedir)){
                        box = (MapBox)map.get(mapagent.x, mapagent.y+1)[0];
                    }else if("e".equals(amovedir)){
                        box = (MapBox)map.get(mapagent.x+1, mapagent.y)[0];
                    }else if("w".equals(amovedir)){
                        box = (MapBox)map.get(mapagent.x-1, mapagent.y)[0];
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
                        box = (MapBox)map.get(mapagent.x, mapagent.y-1)[0];
                    }else if("s".equals(boxcurdir)){
                        box = (MapBox)map.get(mapagent.x, mapagent.y+1)[0];
                    }else if("e".equals(boxcurdir)){
                        box = (MapBox)map.get(mapagent.x+1, mapagent.y)[0];
                    }else if("w".equals(boxcurdir)){
                        box = (MapBox)map.get(mapagent.x-1, mapagent.y)[0];
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
        
    }
    
    public void fillBlocks(){
        for(int i = 0; i < cols; i++) {
            for(int j = 0; j < rows; j++) {
                this.addObject(new Wall(j,i));
                
            }
        }
    }
}

