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
    String name;
    public Bomb(String id,long position){
        super(position);
    }
    public Bomb(String name,int x, int y){
        super(x,y);
        this.name = name;
        super.id=name;
    }
    
    @Override
    public String getName() {
        return this.name;
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
