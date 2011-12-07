/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gui;
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

/**
 *
 * @author Dan True
 */
public class WorldPanel extends JPanel {
        private int tileSize = 10;
        private boolean init = true;
        private ArrayList<MapObject> fields = new ArrayList<MapObject>();
        
    WorldPanel(int width, int height) {
        FlowLayout boardlayout = new FlowLayout();//new GridLayout(1, 1, 0, 0);
        this.setLayout(boardlayout);
        this.setPreferredSize(new Dimension(width, height));
        this.setMaximumSize( this.getPreferredSize() );
        this.setBackground(Color.black);
    }

    public void update(final World world){
     System.out.println("AFTER INITIATE");
            for(MapObject mo:fields){
                MapObject worldobject = world.getMap().get(mo.getPosition());
                //if(worldobject == null && mo.getComponentCount() > 0){
                //    mo.repaint();
                //}
                if(worldobject != null){
                    if(mo.getComponentCount() == 0 && worldobject instanceof Wall){
                        mo.add(worldobject);
                        mo.repaint();
                    }else if(mo.getComponentCount() == 0 || (mo.getComponentCount() > 0 && !mo.getComponent(0).equals(worldobject))){
                            Tile parent = (Tile)worldobject.getParent();
                            if(parent != null){
                                parent.removeAll();
                                parent.repaint();
                            }
                            mo.removeAll();
                            mo.add(worldobject);
                            mo.repaint();
                       }
                }
            }
    }
    
    public void draw(final World world){
        GridLayout boardlayout = new GridLayout(world.getY(), world.getX(), 0, 0);
        this.setLayout(boardlayout);
        //this.removeAll();
        for(int i = 0; i < world.getX(); i++) {
            for(int j = 0; j < world.getY(); j++) {
                final Tile tile = new Tile(j,i);
                tile.setPreferredSize(new Dimension(tileSize, tileSize));
                tile.setMaximumSize(tile.getPreferredSize());
                tile.setMinimumSize(tile.getPreferredSize());
                tile.setBorder(BorderFactory.createLineBorder(Color.RED, 0));
                this.add(tile);
                MapObject worldobject = world.getMap().get(j, i);
                if(worldobject != null){
                    tile.add(worldobject);
                }
                fields.add(tile);
                tile.addMouseListener(new MouseAdapter()
                      {
                        @Override
                       public void mouseClicked(MouseEvent me)
                         {
                            if(tile.getComponent(0) != null && tile.getComponent(0) instanceof Wall){
                                System.out.println(((Wall)tile.getComponent(0)).getPosition());
                            }
                            if(world.getMoveableObject() != null && tile.getComponent(0).equals(world.getMoveableObject())){
                                world.persistMoveableObject(tile.x,tile.y);
                                setBackground(Color.WHITE);
                                tile.updateUI();
                            }
                           }
                        @Override
                       public void mouseEntered(MouseEvent me) {
                            if(tile.getComponentCount()<1 && world.getMoveableObject() != null){
                                tile.add(world.getMoveableObject());
                                //tile.setBackground(Color.GREEN);
                                updateUI();
                            }else if(world.getMoveableObject() != null){
                                tile.setBackground(Color.RED);
                                tile.updateUI();
                            }
                        }
                        @Override
                       public void mouseExited(MouseEvent e) {
                            if(tile.getComponentCount()>0 && world.getMoveableObject() != null){
                                tile.remove(world.getMoveableObject());
                                tile.setBackground(Color.WHITE);
                                tile.updateUI();
                            }
                        }
                       
                        });
            }
        }
        this.updateUI();
    }
}
