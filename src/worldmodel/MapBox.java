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
 * @author jakobenvy
 */
public class MapBox extends MapObject {
    String name;
    public MapBox(long position){
        super(position);
        setBackground(Color.GREEN);
    }

    public MapBox(String name, int x, int y){
        super(x,y);
        setBackground(Color.GREEN);
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
    
    @Override public void paintComponent(Graphics g) {
    //... Draw X on this panel
    super.paintComponent(g);
    int w = getWidth();     // get width of panel.
    int h = getHeight();    // get its height.
    g.drawOval(0, 0, w-1, h-1); // upper left to lower right.
    char[] charA = this.name.toCharArray();
    g.setFont(new Font("Arial", Font.PLAIN, 50));
    g.drawChars(charA, 0, 1, 10, 42);
    }
}
