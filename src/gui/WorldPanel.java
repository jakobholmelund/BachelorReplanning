/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gui;
import controllers.AddItemController;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import worldmodel.MapObject;
import worldmodel.Tile;
import worldmodel.Wall;
import worldmodel.World;
import worldmodel.Box;

/**
 *
 * @author Dan True
 */
public class WorldPanel extends JPanel {
        private int tileSize = 10;
        private boolean init = true;
        private ArrayList<MapObject> fields = new ArrayList<MapObject>();
        public AddItemController addItemController;
        
    WorldPanel(int width, int height,AddItemController aic) {
        GridLayout boardlayout = new GridLayout(1, 1, 0, 0);
        this.setLayout(boardlayout);
        this.setPreferredSize(new Dimension(width, height));
        this.setMaximumSize( this.getPreferredSize() );
        this.setBackground(Color.black);
        this.addItemController = aic;
    }

    public void draw(World world){
        GridLayout boardlayout = new GridLayout(world.getY(), world.getX(), 0, 0);
        this.setLayout(boardlayout);
        if(!init){
            System.out.println("AFTER INITIATE");
            for(MapObject mo:fields){
                MapObject worldobject = world.getMap().get(mo.getPosition());
                if(worldobject != null){
                    if(!(worldobject instanceof Wall)){
                        mo.removeAll();
                        mo.add(worldobject);
                        mo.repaint();
                    }
                }
            }
        }else{
            init = false;
            
        //this.removeAll();
        for(int i = 0; i < world.getX(); i++) {
            for(int j = 0; j < world.getY(); j++) {
                final Tile tile = new Tile(i,j);
                tile.setPreferredSize(new Dimension(tileSize, tileSize));
                tile.setMaximumSize(tile.getPreferredSize());
                tile.setMinimumSize(tile.getPreferredSize());
                tile.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
                this.add(tile);
                MapObject worldobject = world.getMap().get(j, i);
                if(worldobject != null){
                    tile.add(worldobject);
                }
                fields.add(tile);
                tile.addMouseListener(new MouseAdapter()
                      {
                       public void mouseClicked(MouseEvent me)
                         {
                            if(addItemController.getActive() != null && tile.getComponent(0).equals(addItemController.getActive())){
                                addItemController.persist(tile.x,tile.y);
                                setBackground(Color.WHITE);
                                tile.updateUI();
                            }
                           }
                       public void mouseEntered(MouseEvent me) {
                            if(tile.getComponentCount()<1 && addItemController.getActive() != null){
                                tile.add(addItemController.getActive());
                                //tile.setBackground(Color.GREEN);
                                updateUI();
                            }else if(addItemController.getActive() != null){
                                tile.setBackground(Color.RED);
                                tile.updateUI();
                            }
                        }
                       public void mouseExited(MouseEvent e) {
                            if(tile.getComponentCount()>0 && addItemController.getActive() != null){
                                tile.remove(addItemController.getActive());
                                tile.setBackground(Color.WHITE);
                                tile.updateUI();
                            }
                        }
                       
                        });
            }
        }
        }
        this.updateUI();
    }
}
