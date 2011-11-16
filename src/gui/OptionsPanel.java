/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gui;

import controllers.AddItemController;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseListener;
import javax.swing.JPanel;

/**
 *
 * @author Dan True
 */
public class OptionsPanel extends JPanel {
    private AddItemController addItemController;
    OptionsPanel(AddItemController aic) {
        this.setPreferredSize(new Dimension(1000, 100));
        this.setMaximumSize( this.getPreferredSize() );
        this.setBackground(Color.blue);
        this.addItemController = aic;
    }
    
    public void draw(){
    }
}
