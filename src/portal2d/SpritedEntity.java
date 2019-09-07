/*
 * File added by Nathan MacLeod 2019
 */
package portal2d;

import java.awt.Graphics;
import java.util.ArrayList;

/**
 *
 * @author Nathan
 */
public interface SpritedEntity extends Asset {
    
    public boolean getLastDrawPriority();
    
    public void flipSprite();
    
    public SpriteImage getSprite();
    
    public Point getDrawPosition();
    
    public void draw(Graphics g, Camera c, PortalPair portals, ArrayList<RigidBody> obstacles);
}
