/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gui;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import worldmodel.MapObject;
import worldmodel.Tile;
import worldmodel.World;

/**
 *
 * @author Dan True
 */
public class WorldPanel extends JPanel {
        private int tileSize = 10;
        private int cols = 10;
        private int rows = 10;
        
    WorldPanel(int width, int height) {
        GridLayout boardlayout = new GridLayout(cols, rows, 0, 0);
        this.setLayout(boardlayout);
        this.setPreferredSize(new Dimension(width, height));
        this.setMaximumSize( this.getPreferredSize() );
        this.setBackground(Color.black);
    }

    public void draw(World world){
        this.removeAll();
        for(int i = 0; i < cols; i++) {
            for(int j = 0; j < rows; j++) {
                Tile tile = new Tile(i,j);
                tile.setPreferredSize(new Dimension(tileSize, tileSize));
                tile.setMaximumSize(tile.getPreferredSize());
                tile.setMinimumSize(tile.getPreferredSize());
                tile.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
                this.add(tile);
                MapObject worldobject = world.getMap().get(i, j);
                if(worldobject != null){
                    tile.add(worldobject);
                }
            }
        }
    }
}
