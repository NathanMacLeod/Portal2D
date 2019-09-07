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
public class Door extends RigidBody implements ActivatableEntity, Asset {
    private double height;
    private double width;
    private double speed;
    private Point position;
    private boolean opening = false;
    private boolean closing = false;
    
    public Door(Point position, double tileSize, int ID) {
        this(position, tileSize * 6, tileSize * 3/4.0, 10000, 450, 0.3, 1, ID);
    }
    
    public Door(Point position, double height, double width, double mass, double speed, double resistution, double frictionCoefficent, int ID) {
        super(new RigidBodyPoint[] {new RigidBodyPoint(position.x - width/2.0, position.y), new RigidBodyPoint(position.x + width/2.0, position.y),  new RigidBodyPoint(position.x + width/2.0, position.y + height), new RigidBodyPoint(position.x - width/2.0, position.y + height)},
            new Point(position.x, position.y + height/2.0), new Vector(0, 0), 0, mass, 0, frictionCoefficent, resistution, false, ID, Color.RED, false);
        this.height = height;
        this.width = width;
        this.speed = speed;
        this.position = position;
        setPushable(false);
        setNonRotatable(true);
    }
    
    public RigidBody getBoundingBox() {
        RigidBodyPoint[] nodesClone = new RigidBodyPoint[getNodes().length];
        for(int i = 0; i < nodesClone.length; i++) {
            nodesClone[i] = new RigidBodyPoint(getNodes()[i].x, getNodes()[i].y);
        }
        return new RigidBody(nodesClone, new Point(position.x, position.y), new Vector(0, 0), 0, 0, 0, 0, false, 0, null, false);
    }
    
    public void moveForwardOrBackInTime(double dt, Vector accel) {
        super.moveForwardOrBackInTime(dt, new Vector(0, 0));
        getNodes()[0].y = position.y;
        getNodes()[1].y = position.y;
    }
    
    public void applyImpulseOnPoint(Point p, Vector impulse) {
        if(!closing)
            return;
        super.applyImpulseOnPoint(p, impulse);
        setVelocity(new Vector(0, getVelocity().getYComp()));
        if(!opening && !closing) {
            setVelocity(new Vector(0, 0));
        }
    } 
    
    public Vector getVectorToBase() {
        return new Vector(0, 0);
    }
   
    public void draw(Graphics g, Camera c) {
        g.setColor(Color.GRAY);
        int[] xPoints = new int[getNodes().length];
        int[] yPoints = new int[getNodes().length];
        Point cp = c.getPosition();
        for(int i = 0; i < getNodes().length; i++) {
            Point p = getNodes()[i];
            xPoints[i] = (int)(p.x - cp.x + c.getWidth());
            yPoints[i] = (int)(p.y - cp.y + c.getHeight());
        }
        g.fillPolygon(xPoints, yPoints, getNodes().length);
        g.setColor(Color.black);
        for(int i = 0; i < getLines().length; i++) {
            Line l = getLines()[i];
            Color cac = Color.BLACK;
            l.draw(g, cac, c);
        }
    }
    
    public void draw(Graphics g, Camera c, Point position, double orientation) {
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect((int) (position.x - c.getPosition().x + c.getWidth() - width/2.0), (int) (position.y - c.getPosition().y + c.getHeight()), (int) (width), (int) (height));
    }
    
    public void turnOn() {
        openDoor();
    }
    
    public void turnOff() {
        closeDoor();
    }
    
    public void openDoor() {
        opening = true;
        closing = false;
    }
    
    public void closeDoor() {
        closing = true;
        opening = false;
    }
    
    public boolean atRest() {
        return !opening && !closing;
    }
    
    public void update(double dt, PhysicsPanel panel) {
        updateDoor();
    }
    
    public boolean getClosing() {
        return closing;
    }
    
    public void applyImpulseOnPoint(Point p, Vector impulse, Object o) {
        if(o instanceof RigidBody && ((RigidBody)o).getFixed())
            return;
        super.applyImpulseOnPoint(p, impulse, o);
    }
    
    private void updateDoor() {
        double slack = 0.1;
        if(opening) {
            breakRest(true);
            if(getNodes()[2].y <= position.y + slack) {
                opening = false;
                getNodes()[3].y = position.y + slack;
                getNodes()[2].y = position.y + slack;
                setVelocity(new Vector(0, 0));
                return;
            }
            setVelocity(new Vector(0, -speed));
        }
        else if(closing) {
            breakRest(true);
            if(getNodes()[2].y >= position.y + height) {
                closing = false;
                getNodes()[2].y = position.y + height;
                getNodes()[3].y = position.y + height;
                setVelocity(new Vector(0, 0));
                return;
            }
            setVelocity(new Vector(0, speed)); 
        }
        else {
            setVelocity(new Vector(0, 0));
        }
    }
}
