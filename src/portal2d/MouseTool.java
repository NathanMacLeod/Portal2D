/*
 * File added by Nathan MacLeod 2019
 */
package portal2d;


import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.Graphics;
/**
 *
 * @author Nathan
 */
public interface MouseTool extends MouseListener, MouseMotionListener {
    
    public void draw(Graphics g, Camera c);
    
}
