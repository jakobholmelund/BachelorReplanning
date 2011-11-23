/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import javax.swing.JPanel;
import worldmodel.MapAgent;
import worldmodel.MapBox;
import worldmodel.World;
import javax.swing.JOptionPane;
import java.awt.*;
import java.awt.event.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import worldmodel.Goal;
import worldmodel.Wall;
/**
 *
 * @author jakopchronos
 */
public class AddItemsPanel extends JPanel{
    public AddItemsPanel(final World world,MainWindow mw){
        this.setPreferredSize(new Dimension(1000, 100));
        this.setMaximumSize( this.getPreferredSize() );
        this.setBackground(Color.blue);
        final MainWindow parent = mw;
        JButton newBut = new JButton(" SAVE ");
        this.add(newBut);
        JButton newBut2 = new JButton(" LOAD ");
        this.add(newBut2);
        newBut.addActionListener(new ActionListener() {
 
            public void actionPerformed(ActionEvent e)
            {
                try {
                    //Execute when button is pressed
                    world.save();
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(OptionsPanel.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(OptionsPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        newBut2.addActionListener(new ActionListener() {
 
            public void actionPerformed(ActionEvent e)
            {
                try {
                    World newWorld = world.load();
                    parent.loadNewWorld(newWorld);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(AddItemsPanel.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(AddItemsPanel.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(AddItemsPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
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
        
        box.addMouseListener(new MouseAdapter()
                      {
                        @Override
                       public void mouseClicked(MouseEvent me)
                         {
                            world.removeMovableObject();
                            String s = (String)JOptionPane.showInputDialog(
                                parent,
                                "Select letter for box\n",
                                "Customized Dialog",
                                JOptionPane.PLAIN_MESSAGE,
                                null,
                                null,
                                "q");
                            world.setMoveAbleObject(new MapBox(s,0,0));
                         }
                      });
        Goal goal = new Goal("Q",0,0);
        this.add(goal);
        goal.addMouseListener(new MouseAdapter()
                      {
                        @Override
                       public void mouseClicked(MouseEvent me)
                         {
                            world.removeMovableObject();
                            String s = (String)JOptionPane.showInputDialog(
                                parent,
                                "Select letter for box\n",
                                "Customized Dialog",
                                JOptionPane.PLAIN_MESSAGE,
                                null,
                                null,
                                "q");
                            world.setMoveAbleObject(new Goal(s,0,0));
                         }
                      });
        Wall wall = new Wall(0);
        this.add(wall);
        wall.addMouseListener(new MouseAdapter()
                      {
                        @Override
                       public void mouseClicked(MouseEvent me)
                         {
                            world.removeMovableObject();
                            world.setMoveAbleObject(new Wall(0));
                         }
                      });
    }
    
   
}
