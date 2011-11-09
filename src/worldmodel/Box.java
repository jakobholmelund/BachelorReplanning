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
public class Box extends MapObject {
    public Box(long position){
        super(position);
        setBackground(Color.GREEN);
    }
    public Box(int x, int y){
        super(x,y);
        setBackground(Color.GREEN);
    }
}
