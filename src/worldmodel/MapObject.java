/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package worldmodel;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.Serializable;
import javax.swing.JPanel;

/**
 *
 * @author jakobenvy
 */
public class MapObject extends JPanel implements Serializable {
    private Long position;
    public int x;
    public int y;
    public String id;
    boolean repaint = false;
    
    public MapObject(){
        this.setLayout(new GridLayout(1,1));
        this.setPreferredSize(new Dimension(50,50));
    }
    
    public MapObject(long position){
        this.position = position;
        int[] coords = coordsFor(position);
        this.x = coords[0];
        this.y = coords[1];
        this.setLayout(new GridLayout(1,1));
        this.setPreferredSize(new Dimension(50,50));
    }
    
    public MapObject(int x, int y){
        this.position = keyFor(x,y);
        this.x = x;
        this.y = y;
        this.setLayout(new GridLayout(1,1));
        this.setPreferredSize(new Dimension(50,50));
    }
    
    public void setPosition(long position){
        this.position = position;
        int[] coords = coordsFor(position);
        this.x = coords[0];
        this.y = coords[1];
    }
    
    public void setPosition(Long key, int x, int y){
        this.position = key;
        this.x = x;
        this.y = y;
    }
    
    public void setPosition(int x, int y){
        this.position = keyFor(x,y);
        this.x = x;
        this.y = y;
    }
    
    public Long getPosition(){
        return position;
    }
    
    public String getId(){
        return id;
    }
    
    static public Long keyFor(int x, int y) {
        int kx = x + 1000000000;
        int ky = y + 1000000000;
        return (long)kx | (long)ky << 32;
    }
    // extract the x and y from the keys
    static private int[] coordsFor(long k) {
        int x = (int)(k & 0xFFFFFFFF) - 1000000000;
        int y = (int)((k >>> 32) & 0xFFFFFFFF) - 1000000000;
        return new int[]{x,y};
    }
    
    public boolean shouldRepaint(){
        return this.repaint;
    }
    
    public void setRepaint(boolean b){
        this.repaint = b;
    }
}
