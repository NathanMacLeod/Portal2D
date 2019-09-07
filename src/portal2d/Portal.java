/*
 * File added by Nathan MacLeod 2019
 */
package portal2d;

import java.awt.Color;
import java.awt.Graphics;
/**
 *
 * @author Nathan
 */
public class Portal {
   final static double PORTAL_HEIGHT = 9;
   final static double PORTAL_INDENT_DIVIDEND = 10;
    
   private RigidBodyLine surface;
   private RigidBodyLine originalSurface;
   private RigidBody body;
   private Line portalSurface;
   private Vector portalIndent;
   private double size;
   private Color color;
   private Point center;
   private RigidBodyPoint[] indentPoints;
   private double[] drawPointsX;
   private double[] drawPointsY;
   private Vector direction;
   private Vector downVector;
   private Point downPoint;
   private SpriteImage sprite;
   private double orientation;
   
   public Portal(RigidBodyLine surface, double size, Point center, boolean bluePortal, Vector downVector) {
       this.originalSurface = surface;
       this.color = color;
       this.size = size;
       this.center = center;
       this.surface = surface;
       this.direction = surface.getNormalUnitVector();
       sprite = new SpriteImage(((bluePortal)? "bluePortal.png" : "redPortal.png"), size, new Vector(0, size * 15/32.0));
       body = surface.getBody();
       orientation = direction.getDirection() + Math.PI/2.0;
       Vector v = direction.perpendicularize().multiplyByScalar(size/2.0);
       portalIndent = direction.multiplyByScalar(-PORTAL_HEIGHT/PORTAL_INDENT_DIVIDEND);
       center.x += portalIndent.getXComp();
       center.y += portalIndent.getYComp();
       portalSurface = new Line(new Point(center.x + v.getXComp(), center.y + v.getYComp()), new Point(center.x - v.getXComp(), center.y - v.getYComp()));
       
               
       v = v.multiplyByScalar(2);
       this.downVector = downVector;
       downPoint = (new Vector(portalSurface.getPoints()[0].x - center.x, portalSurface.getPoints()[0].y - center.y).dotProduct(downVector) > 0)? portalSurface.getPoints()[0] : portalSurface.getPoints()[1];
       drawPointsX = new double[4];
       drawPointsY = new double[4];
       
       drawPointsX[0] = (portalSurface.getPoints()[1].x + direction.getXComp() * PORTAL_HEIGHT * 3/PORTAL_INDENT_DIVIDEND);
       drawPointsY[0] = (portalSurface.getPoints()[1].y + direction.getYComp() * PORTAL_HEIGHT * 3/PORTAL_INDENT_DIVIDEND);
       drawPointsX[1] = (drawPointsX[0] + v.getXComp());
       drawPointsY[1] = (drawPointsY[0] + v.getYComp());
       drawPointsX[2] = (drawPointsX[1] - direction.getXComp() * PORTAL_HEIGHT);
       drawPointsY[2] = (drawPointsY[1] - direction.getYComp() * PORTAL_HEIGHT);
       drawPointsX[3] = (drawPointsX[2] - v.getXComp());
       drawPointsY[3] = (drawPointsY[2] - v.getYComp());
   }
   
   public Point getPlacementSurface() {
       return new Point(center.x - portalIndent.getXComp(), center.y - portalIndent.getYComp());
   }
   
   public RigidBodyLine getOriginalSurface() {
       return originalSurface;
   }
   
    public void indentBodySurface() {
        surface = body.getLineClosestToPoint(center, true);
        RigidBodyPoint[] originalBodyStructure = body.getNodes();
        indentPoints = new RigidBodyPoint[4];
        Vector v = direction.perpendicularize().multiplyByScalar(size);
        indentPoints[0] = new RigidBodyPoint(portalSurface.getPoints()[1].x - portalIndent.getXComp(), portalSurface.getPoints()[1].y - portalIndent.getYComp());
        indentPoints[1] = new RigidBodyPoint(portalSurface.getPoints()[1].x - portalIndent.getXComp() - direction.getXComp() * PORTAL_HEIGHT * 2/PORTAL_INDENT_DIVIDEND, portalSurface.getPoints()[1].y  - portalIndent.getYComp() - direction.getYComp() * PORTAL_HEIGHT * 2/PORTAL_INDENT_DIVIDEND);
        indentPoints[2] = new RigidBodyPoint(indentPoints[1].x + v.getXComp(), indentPoints[1].y + v.getYComp());
        indentPoints[3] = new RigidBodyPoint(portalSurface.getPoints()[0].x - portalIndent.getXComp(), portalSurface.getPoints()[0].y - portalIndent.getYComp());

        Point comparePoint = surface.getPoints()[0];
        boolean reverseDirection = new Vector(indentPoints[0].x - comparePoint.x, indentPoints[0].y - comparePoint.y).getSquaredMagnitude() > new Vector(indentPoints[3].x - comparePoint.x, indentPoints[3].y - comparePoint.y).getSquaredMagnitude();

        RigidBodyPoint[] newStructure = new RigidBodyPoint[originalBodyStructure.length + 4];
        int startIndex = 0;
        for(int i = 0; i < originalBodyStructure.length; i++) {
            newStructure[i] = originalBodyStructure[i];
            if(originalBodyStructure[i].equals(comparePoint)) {
                startIndex = i;
                break;
            }
        }

        for(int i = 0; i < 4; i++) {
             newStructure[startIndex + 1 + i] = (reverseDirection)? indentPoints[3 - i] : indentPoints[i];
        }

        for(int i = startIndex + 1; i < originalBodyStructure.length; i++) {
            newStructure[i + 4] = originalBodyStructure[i];
        }

        body.breakRest(true);
        body.resetCollisionStructure(newStructure);
   }
   
   public void removePortalFromSurface() {
       RigidBodyPoint[] points = body.getNodes();
       RigidBodyPoint[] newNodes = new RigidBodyPoint[points.length - 4];
       int newNodeIndex = 0;
       for(int i = 0; i < points.length; i++) {
           if(i > 0 && points[i - 1].equals(surface.getPoints()[0])) {
               newNodes[newNodeIndex] = (RigidBodyPoint)surface.getPoints()[1];
               i += 4;
           }
           else
               newNodes[newNodeIndex] = points[i];
           newNodeIndex++;
           if(newNodeIndex == newNodes.length)
               break;
       }
       body.resetCollisionStructure(newNodes);
   }
   
   public RigidBody getBody() {
       return body;
   }
   
   public Point getDownPoint() {
       return downPoint;
   }
   
   public Vector getDownVector() {
       return downVector;
   }
   
   public Point getCenter() {
       return center;
   }
   
   public double getSize() {
       return size;
   }
   
   public Line getPortalSurface() {
       return portalSurface;
   }
   
   public Color getColor() {
       return color;
   }
   
   public Vector getDirection() {
       return direction;
   }
   
   public void draw(Graphics g, Camera c) {
       g.setColor(color);
       int[] x = new int[4];
       int[] y = new int[4];
       Point cp = c.getPosition();
       for(int i = 0; i < 4; i++) {
           x[i] = (int) (drawPointsX[i] - cp.x + c.getWidth());
           y[i] = (int) (drawPointsY[i] - cp.y + c.getHeight());
       }
       //surface.draw(g, color, c);
       //g.fillPolygon(x, y, 4);
       sprite.drawNormally(g, c, center, orientation);
       //new Line(new Point(drawPointsX[0], drawPointsY[0]), new Point(drawPointsX[1], drawPointsY[1])).draw(g, color, c);
       //g.drawLine((int) portalSurface.getPoints()[0].x, (int) portalSurface.getPoints()[0].y, (int) portalSurface.getPoints()[1].x, (int) portalSurface.getPoints()[1].y);
       //g.drawLine((int) center.x, (int) center.y, (int) (center.x + 100 * getDirection().getXComp()), (int) (center.y + 100 * getDirection().getYComp()));
//       g.setColor(Color.white);
//       g.drawLine((int) center.x, (int) center.y, (int) (center.x + 100 * getDownVector().getXComp()), (int) (center.y + 100 * getDownVector().getYComp()));
   }

}
