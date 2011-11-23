/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gui;

import javax.swing.*;
import worldmodel.World;

/**
 *
 * @author Dan True
 */
public class MainWindow extends JFrame {
    private WorldPanel worldPanel;
    private OptionsPanel optionsPanel;
    private World world;
    
    
    public MainWindow() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(1000, 700);
        this.setupPanels();
        this.setVisible(true);
        
    }
    
    private void setupPanels() {
        this.getContentPane().setSize(1000, 700);
        this.getContentPane().setLayout(
            new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS)
        );
        worldPanel = new WorldPanel(1000, 600);
        optionsPanel = new OptionsPanel();
        this.getContentPane().add(worldPanel);
        this.getContentPane().add(optionsPanel);
    }
    
    public void loadNewWorld(World w){
        this.world = w;
        this.worldPanel.removeAll();
        this.worldPanel.draw(w);
        this.worldPanel.updateUI();
        this.drawOptions();
    }
    
    public void drawOptions(){
        this.optionsPanel.removeAll();
        this.optionsPanel.draw(this.world,this);
        this.optionsPanel.updateUI();
    }
    
    public void updateWorld(){
        //this.worldPanel.setVisible(false);
        this.worldPanel.draw(this.world);
        //this.worldPanel.updateUI();
    }
}
