/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package worldmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jakobenvy
 */
public class CoordinateMap<V> {
    Map<Long,V> coords2value = new HashMap<Long,V>();
    Map<V,Long> value2coords = new HashMap<V,Long>();
    
    public void put( int x, int y, V value )
    {
            long key = keyFor(x,y);
            coords2value.put(key, value);
            value2coords.put(value, key);
    }
    
    public void put( long key, V value )
    {
            coords2value.put(key, value);
            value2coords.put(value, key);
    }
    
    public void update(long oldC,long newC, V value){
        value2coords.remove(value);
        coords2value.remove(oldC);
        this.put(newC, value);
    }
    
    public void remove(long position, V value){
        value2coords.remove(value);
        coords2value.remove(position);
    }
    
    public V get(long key){
        return coords2value.get(key);
    }
    
    public V get(int x,int y){
        return coords2value.get(keyFor(x,y));
    }
    
    public long get(V key){
        return value2coords.get(key);
    }
    
    public Long keyFor(int x, int y) {
        int kx = x + 1000000000;
        int ky = y + 1000000000;
        return (long)kx | (long)ky << 32;
    }

    // extract the x and y from the keys
    private int[] coordsFor(long k) {
        int x = (int)(k & 0xFFFFFFFF) - 1000000000;
        int y = (int)((k >>> 32) & 0xFFFFFFFF) - 1000000000;
        return new int[]{x,y};
    }
    
    private ArrayList<V> neighborsFor(long k){    
        int x = (int)(k & 0xFFFFFFFF) - 1000000000;
        int y = (int)((k >>> 32) & 0xFFFFFFFF) - 1000000000;
        ArrayList<V> newlist = new ArrayList<V>();
        newlist.add(this.get(keyFor(x-1,y)));
        newlist.add(this.get(keyFor(x+1,y)));
        newlist.add(this.get(keyFor(x,y-1)));
        newlist.add(this.get(keyFor(x,y-1)));
        return newlist;
    }
}
