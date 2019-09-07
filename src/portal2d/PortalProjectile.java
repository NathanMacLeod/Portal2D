/*
 * File added by Nathan MacLeod 2019
 */
package portal2d;

import java.util.ArrayList;
import java.awt.Graphics;
import java.awt.Color;
/**
 *
 * @author Nathan
 */
public class PortalProjectile {
    private PortalPair portalSystem;
    private int portalNumber;
    private Color color;
    private Point position;
    private double portalSize;
    private Vector velocity;
    
    public PortalProjectile(PortalPair portalSystem, Point startPosition, Vector velocity, double portalSize, int portalNumber) {
        position = startPosition;
        this.velocity = velocity;
        this.portalSystem = portalSystem;
        this.portalSize = portalSize;
        this.portalNumber = portalNumber;
        color = portalSystem.getPortalColor(portalNumber);
    }
    
    public boolean moveForwardInTime(double dt, ArrayList<RigidBody> bodies) {
        Point futurePos = new Point(position.x + velocity.getXComp() * dt, position.y + velocity.getYComp() * dt);
        Line lineOfTravel = new Line(position, futurePos);
        ArrayList<RigidBodyLine> intersectedLines = new ArrayList<>();
        ArrayList<Point> intersections = new ArrayList<>();
        for(RigidBody b: bodies) {
            if((b.getFixed() || b instanceof Door) && b.lineIntersectsBodyFrame(lineOfTravel)) {
                for(RigidBodyLine l: b.getLines()) {
                    Point p = l.getIntersection(lineOfTravel);
                    if(p != null) {
                        intersectedLines.add(l);
                        intersections.add(p);
                    }
                }
            }
        }
        RigidBodyLine surface = null;
        Point center = null;
        Vector downVector;
        do {
            double closestValue = 0;    
            for(int i = 0; i < intersections.size(); i++) {
                Point p = intersections.get(i);
                Vector v = new Vector(p.x - position.x, p.y - position.y);
                double distVal = v.getSquaredMagnitude();
                if(surface == null || distVal < closestValue) {
                    surface = intersectedLines.get(i);
                    center = p;
                    closestValue = distVal;
                }
            }
            
            position = futurePos;

            if(center == null) {
                return false;
            }
            
            if(!surface.getBody().getPortable()) {
                return true;
            }
            
            Vector perpendicularVector = surface.getNormalUnitVector().perpendicularize();
            Vector downDirectionVector = (Math.abs(surface.getSlope()) < 0.6)? new Vector(-velocity.getXComp(), 0) : new Vector(0, 1);
            downVector = (perpendicularVector.dotProduct(downDirectionVector) > 0)? perpendicularVector : perpendicularVector.multiplyByScalar(-1);

            Vector downPointVector;
            Vector upPointVector;

            Point p1 = surface.getPoints()[0];
            Point p2 = surface.getPoints()[1];

            Vector p1Vector = new Vector(p1.x - center.x, p1.y - center.y);
            Vector p2Vector = new Vector(p2.x - center.x, p2.y - center.y);
            Vector p1p2Vector = new Vector(p1.x - p2.x, p1.y - p2.y);

            if(p1Vector.dotProduct(downVector) > 0) {
                downPointVector = p1Vector;
                upPointVector = p2Vector;
            }
            else {
                downPointVector = p2Vector;
                upPointVector = p1Vector;
            }

            double downVectorMag = downPointVector.getMagnitude();
            double upVectorMag = upPointVector.getMagnitude();

            if(p1p2Vector.getMagnitude() < portalSize) {
                intersections.remove(center);
                intersectedLines.remove(surface);
                return true;
                //continue;
            }
            else if(downVectorMag < portalSize/2.0) {
                Vector translation = downVector.multiplyByScalar(-(portalSize/2.0 - downVectorMag + 3.5));
                center.x += translation.getXComp();
                center.y += translation.getYComp();
            }
            else if(upVectorMag < portalSize/2.0) {
                Vector translation = downVector.multiplyByScalar((portalSize/2.0 - upVectorMag + 3.5));
                center.x += translation.getXComp();
                center.y += translation.getYComp();
            }
            
        } while(false);
        
        for(int i = 0; i < 2; i++) {
            if(i == portalNumber)
                continue;
            Portal  p = portalSystem.getPortals()[i];
            if(p != null && p.getOriginalSurface() == surface) {
                double centerDist = Math.pow(p.getCenter().x - center.x, 2) + Math.pow(p.getCenter().y - center.y, 2);
                if(centerDist < Math.pow(p.getSize(), 2))
                    return true;
            }
        }
        
        portalSystem.openPortal(portalNumber, new Portal(surface, portalSize, center, portalNumber == 0, downVector));
        return true;
    }
    
    public void draw(Graphics g, Camera c) {
        g.setColor(color);
        g.fillOval((int) (position.x - 8 - c.getPosition().x + c.getWidth()), (int) (position.y - 8 - c.getPosition().y + c.getHeight()), 16, 16);
    }
}
