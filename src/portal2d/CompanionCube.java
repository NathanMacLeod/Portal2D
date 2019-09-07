/*
 * File added by Nathan MacLeod 2019
 */
package portal2d;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

/**
 *
 * @author Nathan
 */
public class CompanionCube extends RigidBody implements SpritedEntity, Asset {
    private double size;
    private double fizzleTime;
    private double fizzleCounter;
    private boolean fizzled;
    private double fizzleVelocity;
    private double fizzleSpinMax;
    private SpriteImage sprite;
    private SpriteImage fizzledSprite;
    private double inactiveDist;
    
    public CompanionCube(Point center, double size, double density, double resistution, double frictionCoefficent, int ID, Color color, boolean drawCOM) {
        super(new RigidBodyPoint[] {new RigidBodyPoint(center.x - size/2.0, center.y - size/2.0), new RigidBodyPoint(center.x + size/2.0, center.y - size/2.0),  new RigidBodyPoint(center.x + size/2.0, center.y + size/2.0), new RigidBodyPoint(center.x - size/2.0, center.y + size/2.0)},
        new Vector(0, 0), 0, density, resistution, frictionCoefficent, false, ID, color, drawCOM);
        this.size = size;
        sprite = new SpriteImage("companionCube.png", size * 1.1, new Vector(0, 0));
        fizzledSprite = new SpriteImage("CompanionCubeFizzled.png", size * 1.1, new Vector(0, 0));
        fizzleTime = 1.5;
        fizzleSpinMax = 3;
        fizzleVelocity = 100;
    }
    
    public CompanionCube(Point position, int ID) {
        this(position, 55, 10, 0.3, 1, ID, Color.RED, false);
    }
    
    public RigidBody getBoundingBox() {
        RigidBodyPoint[] nodesClone = new RigidBodyPoint[getNodes().length];
        for(int i = 0; i < nodesClone.length; i++) {
            nodesClone[i] = new RigidBodyPoint(getNodes()[i].x, getNodes()[i].y);
        }
        return new RigidBody(nodesClone, new Point(getCenterOfMass().x, getCenterOfMass().y), new Vector(0, 0), 0, 0, 0, 0, false, 0, null, false);
    }
    
    public void giveInactiveDist(double dist) {
        inactiveDist = dist;
    }
    
    public boolean collisionReactiveOnly() {
        return inactiveDist > 0;
    }
    
    public void fizzle() {
        if(fizzled)
            return;
        breakRest(false);
        fizzled = true;
        fizzleCounter = fizzleTime;
        double randomDirect = -Math.random() * Math.PI/2.0 - Math.PI/4.0;
        this.applyImpulseOnPoint(getCenterOfMass(), new Vector(Math.cos(randomDirect), Math.sin(randomDirect)).multiplyByScalar(fizzleVelocity * getMass()));
        this.setAngularVelocity(fizzleSpinMax * (Math.random() * 1 - 0.5));
        this.setMass(5.0);
    }
    
    public void update(double dt, PhysicsPanel panel) {
        update(dt, panel.getBodyDeleteQueue(), panel.getAssetDeleteQueue());
    }
    
    private void update(double dt, ArrayList<RigidBody> bodyDeleteQueue, ArrayList<Asset> assetDeleteQueue) {
        if(fizzled) {
            fizzleCounter -= dt;
            if(fizzleCounter <= 0) {
                bodyDeleteQueue.add(this);
                assetDeleteQueue.add(this);
            }
        }
    }
    
    public void moveForwardOrBackInTime(double dt, Vector accel) {
        if(fizzled) {
            if(getVelocity().getMagnitude() > fizzleVelocity)
               setVelocity(getVelocity().multiplyByScalar(fizzleVelocity/getVelocity().getMagnitude()));
            super.moveForwardOrBackInTime(dt, new Vector(0, 0));
        }
        else {
            super.moveForwardOrBackInTime(dt, accel);
        }
        if(inactiveDist > 0) {
            inactiveDist -= dt * getVelocity().getMagnitude();
        }
    }
    
    public double getDensity() {
        return getMass()/Math.pow(size, 2);
    }
    
    public double getSize() {
        return size;
    }
    
    public void flipSprite() {
        sprite.flip();
        fizzledSprite.flip();
    }
    
    public SpriteImage getSprite() {
        return sprite;
    }
    
    public Point getDrawPosition() {
        return getCenterOfMass();
    }
    
    public boolean getLastDrawPriority() {
        return false;
    }
    
    public void draw(Graphics g, Camera c, Point position, double orientation) {
        sprite.drawNormally(g, c, position, orientation);
    }
    
    public Vector getVectorToBase() {
        return new Vector(0, size/2.0);
    }
    
    public void draw(Graphics g, Camera c, PortalPair portals, ArrayList<RigidBody> obstacles) {
        Portal[] intersectingPortals = portals.getDominantAndOtherPortalOfBody(this);
        if(intersectingPortals == null) {
            if(fizzled) 
                fizzledSprite.drawNormally(g, c, getCenterOfMass(), orientation);
            else
                sprite.drawNormally(g, c, getCenterOfMass(), orientation);
        }
        else {
            if(fizzled) 
                fizzledSprite.drawSpriteSplitInPortal(g, c, getCenterOfMass(), orientation, intersectingPortals[0], intersectingPortals[1], portals);
            else
                sprite.drawSpriteSplitInPortal(g, c, getCenterOfMass(), orientation, intersectingPortals[0], intersectingPortals[1], portals);
        }
    }
    
}
