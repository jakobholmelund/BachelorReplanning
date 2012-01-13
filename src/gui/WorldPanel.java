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
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import worldmodel.MapAgent;
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
        private ArrayList<Tile> fields = new ArrayList<Tile>();
        private HashMap<Long,Tile> tileMap = new HashMap<Long,Tile>();
        
    WorldPanel(int width, int height) {
        FlowLayout boardlayout = new FlowLayout();//new GridLayout(1, 1, 0, 0);
        this.setLayout(boardlayout);
        this.setPreferredSize(new Dimension(width, height));
        this.setMaximumSize( this.getPreferredSize() );
        this.setBackground(Color.black);
    }

    public void cleanTile(long key){
        tileMap.get(key).removeAll();
    }
    
    public void fillTile(long key,MapObject[] objects){
        Tile t = tileMap.get(key);
        for(int i=0;i<objects.length;i++){
            t.add(objects[i]);
        }
        
    }
    
    public void update(final World world){
        for(Tile t:fields){
            Object[] worldobjects = world.getMap().get(t.getPosition());
            if(worldobjects != null){
            MapObject test = (MapObject)worldobjects[0];
                if(test.shouldRepaint()||t.shouldRepaint()){
                    test.setRepaint(false);
                    t.removeAll();
                    t.add(test);
                    t.repaint();
                }
            }else if(t.shouldRepaint()){
                t.setRepaint(false);
                t.removeAll();
                t.repaint();
            }
        }
        
        //System.out.println("AFTER INITIATE");
        /*
        for(MapObject mo:fields){
            
            
            Object[] worldobjects = world.getMap().get(mo.getPosition());
            //if(worldobject == null && mo.getComponentCount() > 0){
            //    mo.repaint();
            //}
            if(worldobjects != null){
                MapObject test = (MapObject)worldobjects[0];
                if(mo.getComponentCount() == 0 && test instanceof Wall){
                    mo.add(test);
                    mo.repaint();
                }else if(mo.getComponentCount() == 0 || (mo.getComponentCount() > 0 && !mo.getComponent(0).equals(test))){
                        Tile parent = (Tile)test.getParent();
                        if(parent != null){
                            parent.removeAll();
                            parent.repaint();
                        }
                        mo.removeAll();
                        mo.add(test);
                        mo.repaint();
                   }
            }
        }
         * */
         
    }
    
    public void draw(final World world){
        fields.clear();
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
                //System.out.println(world.getMap().get(j, i).toString());
                Object[] worldobject = world.getMap().get(j, i);
                if(worldobject != null){
                    final MapObject mobject = (MapObject)worldobject[0];
                    tile.add(mobject);
                    ((MapObject)worldobject[0]).addMouseListener(new MouseAdapter()
                      {
                        @Override
                       public void mouseClicked(MouseEvent me)
                         {
                             if(me.getButton() == MouseEvent.BUTTON3){
                                 tile.removeAll();
                                 world.removeObject(mobject);
                                 world.setMoveAbleObject(mobject);
                             }
                         }
                      });
                }
                fields.add(tile);
                tile.addMouseListener(new MouseAdapter()
                      {
                        @Override
                       public void mouseClicked(MouseEvent me)
                         {
                             if(me.getButton() == MouseEvent.BUTTON1){
                            if(tile.getComponent(0) != null && tile.getComponent(0) instanceof Wall){
                                //System.out.println(((Wall)tile.getComponent(0)).getPosition());
                            }
                            if(world.getMoveableObject() != null && tile.getComponent(0).equals(world.getMoveableObject())){
                                world.persistMoveableObject(tile.x,tile.y);
                                world.removeMovableObject();
                                setBackground(Color.WHITE);
                                tile.updateUI();
                            }
                             }
                           }
                        @Override
                       public void mouseEntered(MouseEvent me) {
                            if(tile.getComponentCount()<1 && world.getMoveableObject() != null){
                                tile.add(world.getMoveableObject());
                                //tile.setBackground(Color.GREEN);                                
                                tile.repaint();
                            }else if(world.getMoveableObject() != null){
                                tile.setBackground(Color.RED);
                                tile.repaint();
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
