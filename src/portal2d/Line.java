package portal2d;
/*
 * File added by Nathan MacLeod 2019
 */
import java.awt.Graphics;
import java.awt.Color;
/**
 *
 * @author Nathan
 */
public class Line {
    Point p1;
    Point p2;
    
    public Line(Point p1, Point p2) {
        this.p1 = p1;
        this.p2 = p2;
    }
    
    public Point[] getPoints() {
        return new Point[] {p1, p2};
    }
    
    public double getSlope() {
        if(isHorizontel())
            return 0;
        return (p1.y - p2.y)/(p1.x - p2.x);
    }
    
    public double getYIntercept() {
        if(isHorizontel())
            return p1.y;
        return -getSlope() * p1.x + p1.y;
    }
    
    public boolean isVerticle() {
        return p1.x == p2.x;
    }
    
    public boolean isHorizontel() {
        return p1.y == p2.y;
    }
    
    public double getSmallestX() {
        if(p1.x < p2.x)
            return p1.x;
        return p2.x;
    }
    
    public double getLargestX() {
        if(p1.x > p2.x)
            return p1.x;
        return p2.x;
    }
    
    public double getSmallestY() {
        if(p1.y < p2.y)
            return p1.y;
        return p2.y;
    }
    
    public double getLargestY() {
        if(p1.y > p2.y) 
            return p1.y;
        return p2.y;
    }
    
    public boolean withinDomain(double x) {
        return x >= getSmallestX() && x <= getLargestX();
    }
    
    public boolean withinRange(double y) {
        return y >= getSmallestY() && y <= getLargestY();
    }
    
    public boolean withinDimensions(double x, double y) {
        //if(y == 298.7997886237804 && x == 229.2239227211849)
            //System.out.println("domain range check: " + withinDomain(x) + " " + withinRange(y) + " for line:" + toString());
        return withinDomain(x) && withinRange(y);
    }
    
    public boolean collisionPossible(Line l) {
        //System.out.println((l.getLargestX() < getSmallestX()) + " " + (l.getSmallestX() > getLargestX()) + " " + (l.getLargestY() < getSmallestY()) + " " + (l.getSmallestY() > getLargestY()));
        return !(l.getLargestX() < getSmallestX() || l.getSmallestX() > getLargestX() ||
                l.getLargestY() < getSmallestY() || l.getSmallestY() > getLargestY());
    }
    
    //Returning nulls indicates an intersection does not exist within the domains of the lines
    
    public Point getIntersection(Line l) {
        if(!collisionPossible(l) || l.getSlope() == getSlope())
            return null;
        Point intersect = new Point(0, 0);
        if(isVerticle())
            intersect.x = p1.x;
        else if(l.isVerticle())
            intersect.x = l.p1.x;
        else
            intersect.x = (l.getYIntercept() - getYIntercept())/(getSlope() - l.getSlope());
            
        if(isVerticle()) {
            intersect.y = l.getSlope() * intersect.x + l.getYIntercept();
        }
        else if(l.isHorizontel())
            intersect.y = l.p1.y;
        else if(isHorizontel())
            intersect.y = p1.y;
        else
            intersect.y = getSlope() * intersect.x + getYIntercept();
        
        //if(l.p1.y >= 297 && l.p1.y <= 299)
            //System.out.println(withinDimensions(intersect.x, intersect.y) + " " + l.withinDimensions(intersect.x, intersect.y) + " " + intersect);
        if(!withinDimensions(intersect.x, intersect.y) || !l.withinDimensions(intersect.x, intersect.y)) {
            return null;
        }
        return intersect;
    }
    
    public Line getIntersectingLine(Line l) {
        if(!collisionPossible(l) || l.getSlope() != getSlope() || l.getYIntercept() != getYIntercept())
            return null;
        if(isVerticle()) {
            double y1 = getSmallestY();
            if(l.getSmallestY() > y1)
                y1 = l.getSmallestY();
            double y2 = getLargestY();
            if(l.getLargestY() < y2)
                y2 = l.getLargestY();
            return new Line(new Point(y1, p1.x), new Point(y2, p1.x));
        }
        double x1 = getSmallestY();
        if(l.getSmallestX() > x1)
            x1 = l.getSmallestY();
        double x2 = getLargestX();
        if(l.getLargestY() < x2)
            x2 = l.getLargestY();
        return new Line(new Point(x1, x1 * getSlope() + getYIntercept()), new Point(x2, x2 * getSlope() + getYIntercept()));
    }
    
    public Point getMidPoint() {
        return new Point((p1.x + p2.x)/2.0, (p1.y + p2.y)/2.0);
    }
    
    public boolean closestIntersectInDomain(Point p) {
        Point intersect = null;
        if(isVerticle()) {
           intersect = new Point(p1.x, p.y);
        }
        else if(getSlope() == 0)
            intersect = new Point(p.x, p1.y);
        else {
            double xInt = ((p.x/getSlope()) + p.y - getYIntercept())/(getSlope() + 1/getSlope());
            double yInt = getSlope() * xInt + getYIntercept();
            intersect = new Point(xInt, yInt);
        }
        return withinDimensions(intersect.x, intersect.y);
    }
    
    
    public double getSquaredDistToLine(Point p) {
        //x and y int are the intersection between the tangent line to P and the line
        Point intersect = getPointOnLineClosestToPoint(p);
        return Math.pow(intersect.x - p.x, 2) + Math.pow(intersect.y - p.y, 2);
    }
    
    public Point getPointOnLineClosestToPoint(Point p) {
        if(isVerticle()) {
            return new Point(p1.x, p.y);
        }
        if(getSlope() == 0)
            return new Point(p.x, p1.y);
        double xInt = ((p.x/getSlope()) + p.y - getYIntercept())/(getSlope() + 1/getSlope());
        double yInt = getSlope() * xInt + getYIntercept();
        return new Point(xInt, yInt);
    }
    
    public static void main(String[] args) {
        Line a = new Line(new Point(0, 0), new Point(4, 2));
        Line b = new Line(new Point(2, 0), new Point(2, 4));
        System.out.println(a.getSquaredDistToLine(new Point(0, 3)));
    }
    
    public void draw(Graphics g) {
        g.setColor(Color.black);
        g.drawLine((int) p1.x, (int) p1.y, (int) p2.x, (int) p2.y);
    }
    
    public boolean equals(Object o) {
        if(!(o instanceof Line))
            return false;
        Line l = (Line) o;
        return l.p1.equals(p1) && l.p2.equals(p2);
    }
    
    public String toString() {
        return "Line, p1:" + p1.toString() + " p2:" + p2.toString();
    }
    
    public double getLength() {
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }
    
    public boolean containsPoint(Point p) {
        return p.equals(p1) || p.equals(p2);
    }
    
    public void draw(Graphics g, Color c, Camera cam) {
        g.setColor(c);
        Point cp = cam.getPosition();
        g.drawLine((int) (p1.x - cp.x + cam.getWidth()), (int) (p1.y - cp.y + cam.getHeight()), (int) (p2.x - cp.x + cam.getWidth()), (int) (p2.y - cp.y + cam.getHeight()));
                
    }
    
}
