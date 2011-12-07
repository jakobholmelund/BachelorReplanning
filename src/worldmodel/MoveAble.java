/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package worldmodel;

import java.io.Serializable;

/**
 *
 * @author jakobchronos
 */
public class MoveAble extends MapObject implements Serializable {
    public MoveAble(long position){
        super(position);
    }
    public MoveAble(int x, int y){
        super(x,y);
    }
}
