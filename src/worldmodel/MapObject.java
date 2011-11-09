/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package worldmodel;

import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

/**
 *
 * @author jakobenvy
 */
public class MapObject extends JPanel {
    private Long position;
    public int x;
    public int y;
    
    public MapObject(long position){
        this.position = position;
        int[] coords = coordsFor(position);
        this.x = coords[0];
        this.y = coords[1];
        this.setLayout(new FlowLayout(1,1,1));
        this.setPreferredSize(new Dimension(50,50));
    }
    
    public MapObject(int x, int y){
        this.position = keyFor(x,y);
        this.x = x;
        this.y = y;
        this.setLayout(new FlowLayout(1,1,1));
        this.setPreferredSize(new Dimension(50,50));
    }
    
    public void setPosition(long position){
        this.position = position;
    }
    
    public void setPosition(Long key, int x, int y){
        this.position = key;
        this.x = x;
        this.y = y;
    }
    
    public Long getPosition(){
        return position;
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
    
}
