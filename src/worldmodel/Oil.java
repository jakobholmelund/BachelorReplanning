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
    public Oil(String name,long position){
        super(position);
        setBackground(Color.BLACK);
        super.id=name;
    }

    public Oil(String name, int x, int y){
        super(x,y);
        setBackground(Color.BLACK);
        super.id=name;
    }
    
    @Override public void paintComponent(Graphics g) {
        //... Draw X on this panel
        super.paintComponent(g);
        int w = getWidth();     // get width of panel.
        int h = getHeight();    // get its height.
        g.drawOval(0, 0, w-1, h-1); // upper left to lower right.
        char[] charA = this.getId().toCharArray();
        g.setFont(new Font("Arial", Font.PLAIN, 50));
        g.drawChars(charA, 0, 1, 10, 42);
    }
}
