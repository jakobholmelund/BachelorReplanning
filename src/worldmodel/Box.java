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
    String name;
    public Box(long position){
        super(position);
        setBackground(Color.GREEN);
    }

    public Box(String name, int x, int y){
        super(x,y);
        setBackground(Color.GREEN);
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
}
