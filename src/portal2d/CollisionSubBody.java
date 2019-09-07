package portal2d;
/*
 * File added by Nathan MacLeod 2019
 */
import java.util.ArrayList;
import java.awt.Graphics;
import java.awt.Color;
import java.util.Iterator;
/**
 *
 * @author Nathan
 */
public class CollisionSubBody implements Iterable<RigidBodyPoint> {
    private PointNode head;
    private int size;
    private Line[] subBodyLines;
    
    public CollisionSubBody() {
        head = null;
        size = 0;
    }
    
    public CollisionSubBody(Point p) {
        this.head = new PointNode(p);
        size = 1;
    }
    
    public CollisionSubBody(RigidBodyPoint[] points) {
        for(RigidBodyPoint p : points) {
            add(p);
        }
        size = points.length;
        close();
    }
    
    public PointNode getPointNode(Point p) {
        PointNode current = head;
        while(current != null) {
            if(current.point.equals(p))
                return current;
            current = current.next;
        }
        return null;
    } 
    
    public SubBodyIterator iterator() {
        return new SubBodyIterator(head, size);
    }
    
    public class SubBodyIterator implements Iterator<RigidBodyPoint> {
        private PointNode current;
        private int size;
        private int currentIndex;
        
        public SubBodyIterator(PointNode head, int size) {
            this.current = head;
            this.size = size;
            currentIndex = 0;
        }
        
        public RigidBodyPoint next() {
            RigidBodyPoint returnVal = new RigidBodyPoint(current.point.x, current.point.y);
            current = current.next;
            currentIndex++;
            return returnVal;
        }
        
        public boolean hasNext() {
            return currentIndex <= size;
        }
    }
    
    public void add(RigidBodyPoint point) {
        size++;
        if(head == null) {
            head = new PointNode(point);
            return;
        }
        PointNode current = head;
        while(current.next != null)
            current = current.next;
        current.next = new PointNode(point);
        current.next.previous = current;
    }
    
    public void close() {
        if(head == null)
            return;
        PointNode current = head;
        subBodyLines = new Line[size];
        int i = 1;
        while(current.next != null) {
            current = current.next;
            subBodyLines[i] = new Line(current.previous.point, current.point);
            i++;
        }
        current.next = head;
        head.previous = current;
        subBodyLines[0] = new Line(head.previous.point, head.point);
    }
    
    public RigidBodyPoint[] getNodes() {
        RigidBodyPoint[] points = new RigidBodyPoint[size];
        PointNode current = head;
        for(int i = 0; i < size; i++) {
            Point p = current.point;
            points[i] = new RigidBodyPoint(p.x, p.y);
            current = current.next;
        }
        return points;
    }
    
    public Line[] getLines() {
        return subBodyLines;
    }
    
    public boolean pointInsideBody(Point p) {
        //Creates a horizontel line centered around P and checks how it intersects with 
        //the lines in the body, if the number of intersects to the left and right of p are odd numbers p is inside the body
        Line l = new Line(new Point(-Integer.MAX_VALUE, p.y), new Point(Integer.MAX_VALUE, p.y));
        int intersectsLeft = 0;
        int intersectsRight = 0;
        for(int i = 0; i < subBodyLines.length; i++) {
            Point intersect = subBodyLines[i].getIntersection(l);
            if(intersect == null)
                continue;
            
            int acc = 10000;
            if((int)(intersect.x * acc) == (int)(p.x * acc))
                return true;
            else if(intersect.x < p.x)
                intersectsLeft++;
            else if(intersect.x > p.x)
                intersectsRight++;
        }
        return (intersectsLeft % 2) == 1 && (intersectsRight % 2) == 1;
    }
    
    public static CollisionSubBody clipBody(CollisionSubBody body, Line line, Vector desiredSide) {
        Point[] clipP = line.getPoints();
        double x = clipP[0].x - clipP[1].x;
        double y = clipP[0].y - clipP[1].y;
        Line clippingLine = new Line(new Point(clipP[0].x + 100 * x, clipP[0].y + 100 * y), new Point(clipP[0].x - 100 * x, clipP[0].y - 100 * y));
        CollisionSubBody subBody;
        ArrayList<PointNode> intersections = new ArrayList<>();
        for(Line l : body.subBodyLines) {
            Point intersection = l.getIntersection(clippingLine);
            if(intersection != null) {
                PointNode p = new PointNode(intersection);
                p.next = body.getPointNode(l.getPoints()[1]);
                p.previous = body.getPointNode(l.getPoints()[0]);
                intersections.add(p);
            }
        }
        subBody = new CollisionSubBody();
        while(intersections.size() > 1) {
            PointNode intersection = intersections.get(0);
            subBody.add(new RigidBodyPoint(intersection.point.x, intersection.point.y));
            boolean directionNext = (new Vector(intersection.next.point.x - intersection.point.x, intersection.next.point.y - intersection.point.y).dotProduct(desiredSide) > 0)? true : false;
            PointNode currentNode = (directionNext)? intersection.next : intersection.previous;
            boolean bodyClosed = false;
            while(!bodyClosed) {
                for(int i = 1; i < intersections.size(); i++) {
                    Point checkPoint = (directionNext)? intersections.get(i).next.point : intersections.get(i).previous.point;
                    if(checkPoint.equals(currentNode.point)) {
                        subBody.add(new RigidBodyPoint(intersections.get(i).point.x, intersections.get(i).point.y));
                        intersections.remove(i);
                        intersections.remove(0);
                        bodyClosed = true;
                        break;
                    }
                }
                if(bodyClosed)
                    break;
                subBody.add(new RigidBodyPoint(currentNode.point.x, currentNode.point.y));
                currentNode = (directionNext)? currentNode.next : currentNode.previous;               
            }
        }
        subBody.close();
        return subBody;
    }
    
    private static class PointNode {
        Point point;
        PointNode next;
        PointNode previous;
        
        public PointNode(Point p) {
            point = p;
        }
        
        public boolean equals(PointNode p) {
            return point.equals(p.point);
        }
    }
    
}
