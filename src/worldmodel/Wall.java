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
public class Wall extends Obstacle {
    
    public Wall(long position){
        super(position);
        setBackground(Color.YELLOW);
    }
    public Wall(int x, int y){
        super(x,y);
        setBackground(Color.YELLOW);
    }
}
