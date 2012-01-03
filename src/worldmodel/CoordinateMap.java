/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package worldmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jakobenvy
 */
public class CoordinateMap<V> {
    Map<Long,V[]> coords2value = new HashMap<Long,V[]>();
    //Map<V,Long> value2coords = new HashMap<V,Long>();
    
    public void put( int x, int y, V[] values )
    {
            long key = keyFor(x,y);
            coords2value.put(key, values);
            //value2coords.put(value, key);
    }
    
    public void add(long key, V value )
    {
            V[] vals = coords2value.get(key);
            List<V> newvals = new ArrayList<V>();
            newvals.add(value); 
            if(vals != null){
                newvals.addAll(Arrays.asList(vals));
            }
            coords2value.put(key,(V[])newvals.toArray());
            //value2coords.put(value, key);
    }
    
    public void addObjects(long key, V[] values )
    {
        V[] vals = coords2value.get(key);
        List<V> newvals = new ArrayList<V>();
        
        newvals.addAll(Arrays.asList(values));
        if(vals!=null){
            newvals.addAll(Arrays.asList(vals));
        }
        
        

        put(key,(V[])newvals.toArray());
    }
    
    public void put( long key, V[] values )
    {
            coords2value.put(key, values);
            //value2coords.put(value, key);
    }
    
    public void update(long oldC,long newC, V[] values){
        //value2coords.remove(value);
        removeObjects(oldC,values);
        addObjects(newC, values);
    }
    
    public void removeObjects(long key, V[] values){
        V[] vals = coords2value.get(key);
        List<V> newvals = new ArrayList<V>();
        newvals.addAll(Arrays.asList(vals));
        
        for(int i=0;i<values.length;i++){
                newvals.remove(values[i]);
        }
        if(newvals.size()<1){
            coords2value.remove(key);
        }else{
            put(key,(V[])newvals.toArray());
        }
        
    }
    
    public void remove(long position, V[] values){
        //value2coords.remove(value);
        coords2value.remove(position);
    }
    
    public V[] get(long key){
        return coords2value.get(key);
    }
    
    public V[] get(int x,int y){
        return coords2value.get(keyFor(x,y));
    }
    
    //public long get(V key){
    //    return value2coords.get(key);
    //}
    
    public Long keyFor(int x, int y) {
        int kx = x + 1000000000;
        int ky = y + 1000000000;
        return (long)kx | (long)ky << 32;
    }

    // extract the x and y from the keys
    public int[] coordsFor(long k) {
        int x = (int)(k & 0xFFFFFFFF) - 1000000000;
        int y = (int)((k >>> 32) & 0xFFFFFFFF) - 1000000000;
        return new int[]{x,y};
    }
    
    public ArrayList<V[]> neighborsFor(long k){    
        int x = (int)(k & 0xFFFFFFFF) - 1000000000;
        int y = (int)((k >>> 32) & 0xFFFFFFFF) - 1000000000;
        ArrayList<V[]> newlist = new ArrayList<V[]>();
        newlist.add(this.get(keyFor(x-1,y)));
        newlist.add(this.get(keyFor(x+1,y)));
        newlist.add(this.get(keyFor(x,y-1)));
        newlist.add(this.get(keyFor(x,y-1)));
        return newlist;
    }
    
    public void checkNeighbor(long key,V[] values,ArrayList<Long> list, int x, int y){
        for(int i=0;i<values.length;i++){
            if(!(values[i] instanceof Wall)){
                list.add(key);
            }
        }
    }
    
    public ArrayList<Long> emptyNeighborsKeysFor(long k){    
        int x = (int)(k & 0xFFFFFFFF) - 1000000000;
        int y = (int)((k >>> 32) & 0xFFFFFFFF) - 1000000000;
        ArrayList<Long> newlist = new ArrayList<Long>();
      
        long key1 = keyFor(x-1,y);
        V[] vals1 = this.get(key1);
        checkNeighbor(key1,vals1,newlist,x,y);
        
        long key2 = keyFor(x+1,y);
        V[] vals2 = this.get(key2);
        checkNeighbor(key2,vals2,newlist,x,y);
        
        long key3 = keyFor(x,y+1);
        V[] vals3 = this.get(key3);
        checkNeighbor(key3,vals3,newlist,x,y);
        
        long key4 = keyFor(x,y-1);
        V[] vals4 = this.get(key4);
        checkNeighbor(key4,vals4,newlist,x,y);
        
        return newlist;
    }
}
