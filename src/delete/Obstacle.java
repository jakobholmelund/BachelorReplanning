/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package worldmodel;

import java.io.Serializable;

/**
 *
 * @author jakobenvy
 */
public class Obstacle extends MapObject implements Serializable {
    public Obstacle(long position){
        super(position);
    }
    public Obstacle(int x, int y){
        super(x,y);
    }
}
