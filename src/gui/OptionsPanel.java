/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gui;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JPanel;
import worldmodel.World;

/**
 *
 * @author Dan True
 */
public class OptionsPanel extends JPanel {
    OptionsPanel() {
        this.setPreferredSize(new Dimension(1000, 100));
        this.setMaximumSize( this.getPreferredSize() );
        this.setBackground(Color.blue);
    }
    
    public void draw(World world){
        this.removeAll();
        this.add(new AddItemsPanel(world));
    }
}
