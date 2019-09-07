/*
 * File added by Nathan MacLeod 2019
 */
package portal2d;

import java.awt.Graphics;

/**
 *
 * @author Nathan
 */
public interface Asset {
    
    public void update(double dt, PhysicsPanel panel);
    
    public Vector getVectorToBase();
    
    public void draw(Graphics g, Camera c, Point position, double orientation);
    
    public RigidBody getBoundingBox();
    
}
