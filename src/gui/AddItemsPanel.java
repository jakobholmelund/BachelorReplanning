/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import worldmodel.MapAgent;
import worldmodel.MapBox;
import worldmodel.World;

/**
 *
 * @author jakopchronos
 */
public class AddItemsPanel extends JPanel{
    public AddItemsPanel(final World world){
        this.setPreferredSize(new Dimension(1000, 100));
        this.setMaximumSize( this.getPreferredSize() );
        this.setBackground(Color.blue);
        
        MapAgent agent = new MapAgent(1,0);
        this.add(agent);
        agent.addMouseListener(new MouseAdapter()
                      {
                        @Override
                       public void mouseClicked(MouseEvent me)
                         {
                            world.removeMovableObject();
                            world.setMoveAbleObject(world.createNewAgent());
                         }
                      });
        
        MapBox box = new MapBox("Q",0,0);
        this.add(box);
    }
}
