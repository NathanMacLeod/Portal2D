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
public class SegmentableRay {
    private Ray baseRay;
    private ArrayList<Ray> segments;
    private Portal firstPortal = null;
    
    public SegmentableRay(Ray baseRay) {
        this.baseRay = baseRay;
        segments = new ArrayList<>();
        segments.add(baseRay);
    }
    
    public Ray getBaseRay() {
        return baseRay;
    }
    
    public Ray getTerminalRay() {
        if(segments != null)
            return segments.get(0);
        return baseRay;
    }
    
    public ArrayList<Ray> getSegments() {
        return segments;
    }
    
    public Portal getFirstPortal() {
        return firstPortal;
    }
    
    public void segmentRay(PortalPair portals) {
        segments = new ArrayList<>();
        int maxRays = 5;
        boolean segmentAdded = true;
        segments.add(baseRay);
        if(!portals.portalSystemActive())
            return;
        while(segments.size() <= maxRays && segmentAdded) {
            segmentAdded = false;
            Ray r = segments.get(0);
            Line rLine = new Line(r.origin, new Point(r.origin.x + r.vector.getXComp(), r.origin.y + r.vector.getYComp()));
            Point intersection = null;
            Point intersection1 = rLine.getIntersection(portals.getPortals()[0].getPortalSurface());
            Point intersection2 = rLine.getIntersection(portals.getPortals()[1].getPortalSurface());
            if(intersection1 == null && intersection2 == null)
                continue;
            else if(intersection1 != null && intersection2 != null) {
                double distTo1 = new Vector(r.origin.x - intersection1.x, r.origin.y - intersection1.y).getSquaredMagnitude();
                double distTo2 = new Vector(r.origin.x - intersection2.x, r.origin.y - intersection2.y).getSquaredMagnitude();
                if(distTo1 < distTo2)
                    intersection = intersection1;
                else
                    intersection = intersection2;
            }
            else if(intersection1 != null)
                intersection = intersection1;
            else
                intersection = intersection2;
            
            Portal p = (intersection.equals(intersection1))? portals.getPortals()[0] : portals.getPortals()[1];
            if(firstPortal == null)
                firstPortal = p;
            
            r.vector = new Vector(intersection.x - r.origin.x, intersection.y - r.origin.y);
            Vector oldVector = new Vector(rLine.getPoints()[1].x - intersection.x, rLine.getPoints()[1].y - intersection.y);
            Portal otherPortal = (portals.getPortals()[0] == p)? portals.getPortals()[1] : portals.getPortals()[0];
            Point newOrigin = portals.teleportPointToOtherPortal(intersection, p, otherPortal);
            Vector newVector = portals.redirectVector(oldVector, p, otherPortal);

            segments.add(0, new Ray(newVector, newOrigin));
            segmentAdded = true;
            break;
        }
    }
    
    public void draw(Graphics g, Color c, Camera cam) {
        if(segments == null) {
            Ray r = baseRay;
            Line rLine = new Line(r.origin, new Point(r.origin.x + r.vector.getXComp(), r.origin.y + r.vector.getYComp()));
            rLine.draw(g, c, cam);
        }
        else {
            for(Ray r : segments) {
                Line rLine = new Line(r.origin, new Point(r.origin.x + r.vector.getXComp(), r.origin.y + r.vector.getYComp()));
                rLine.draw(g, c, cam);
            }
        }
    }
}
