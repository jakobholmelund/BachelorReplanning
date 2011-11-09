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
public class MapAgent extends MapObject {
    protected int id;
    public MapAgent(int id, long position){
        super(position);
        setBackground(Color.RED);
        this.id = id;
    }
    public MapAgent(int x, int y){
        super(x,y);
        setBackground(Color.RED);
    }
}