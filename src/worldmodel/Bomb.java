/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package worldmodel;

/**
 *
 * @author jakobenvy
 */
public class Bomb extends Obstacle {
    private int blastRadius = 0;
    private int activisionRadius = 0;
    private int fuseTime = 0;
    
    public Bomb(long position){
        super(position);
    }
}
