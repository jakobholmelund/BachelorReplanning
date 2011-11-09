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
    int number;
    
    public MapAgent(long position){
        super(position);
        setBackground(Color.RED);
    }
    public MapAgent(int num, int x, int y){
        super(x,y);
        setBackground(Color.RED);
        this.number = num;
    }
    
    public int getNumber() {
        return this.number;
    }
}
