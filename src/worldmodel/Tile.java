/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package worldmodel;

import java.awt.Color;

/**
 *
 * @author jakobenvy
 */
public class Tile extends MapObject{
    
    
    public Tile(long position){
        super(position);
        setBackground(Color.WHITE);
    }
    public Tile(int x, int y){
        super(x,y);
        setBackground(Color.WHITE);
    }

    
}
