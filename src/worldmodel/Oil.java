/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package worldmodel;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

/**
 *
 * @author jakobchronos
 */
public class Oil extends MapObject{
    public Oil(long position){
        super(position);
        setBackground(Color.BLACK);
    }

    public Oil(int x, int y){
        super(x,y);
        setBackground(Color.BLACK);
    }
    
    @Override public void paintComponent(Graphics g) {
        //... Draw X on this panel
        super.paintComponent(g);
        int w = getWidth();     // get width of panel.
        int h = getHeight();    // get its height.
        g.drawOval(0, 0, w-1, h-1); // upper left to lower right.

    }
}
