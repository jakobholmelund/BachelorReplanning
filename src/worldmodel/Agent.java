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
public class Agent extends MapObject {
    public Agent(long position){
        super(position);
        setBackground(Color.RED);
    }
    public Agent(int x, int y){
        super(x,y);
        setBackground(Color.RED);
    }

}
