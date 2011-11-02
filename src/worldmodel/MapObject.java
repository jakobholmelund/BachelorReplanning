/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package worldmodel;

import javax.swing.JPanel;

/**
 *
 * @author jakobenvy
 */
public class MapObject extends JPanel {
    private Long position;
    
    public MapObject(long position){
        this.position = position;
    }
    
    public MapObject(int x, int y){
        this.position = keyFor(x,y);
    }
    
    public void setPosition(long position){
        this.position = position;
    }
    
    public Long getPosition(){
        return position;
    }
    
    static public Long keyFor(int x, int y) {
        int kx = x + 1000000000;
        int ky = y + 1000000000;
        return (long)kx | (long)ky << 32;
    }
}
