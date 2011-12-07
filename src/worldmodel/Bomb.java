/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package worldmodel;

import java.awt.Graphics;

/**
 *
 * @author jakobenvy
 */
public class Bomb extends MoveAble {
    protected int id;
    public Bomb(int id,long position){
        super(position);
        this.id=id;
    }
    public Bomb(int id,int x, int y){
        super(x,y);
        this.id=id;
    }
    public int getNumber(){
        return id;
    }
    @Override public void paintComponent(Graphics g) {
    //... Draw X on this panel
    super.paintComponent(g);
    int w = getWidth();     // get width of panel.
    int h = getHeight();    // get its height.
    g.fillOval(0, 0, w-1, h-1); // upper left to lower right.
    //char[] charA = String.valueOf(this.id).toCharArray();
    //g.setFont(new Font("Arial", Font.PLAIN, 50));
    //g.drawChars(charA, 0, 0, 0, 0);
}
}
