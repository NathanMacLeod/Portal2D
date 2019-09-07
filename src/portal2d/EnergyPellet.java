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
public class EnergyPellet extends RigidBody implements Asset, SpritedEntity {
    private double speed;
    private int bounceLife;
    private int bouncesLeft;
    private double distTraveled;
    private double inactiveDist;
    private double bounceInactiveTime = 0.2;
    private EnergyPelletLauncher launcher;
    private double bounceInactiveCounter = 0;
    private SpriteImage sprite;
    private SpriteImage fade0;
    private SpriteImage fade1;
    private SpriteImage fade2;
    //RigidBodyPoint[] nodes, Point centerOfMass, Vector velocity, double angularVelocity, double mass, double rotationalInertia, double friction, double resistution, boolean fixed, int ID, Color color, boolean drawCenterOfMass
    public EnergyPellet(Point coords, double radius, Vector velocity, double mass, int ID, int bounceLife, double inactiveDist, EnergyPelletLauncher launcher) {
        super(generatePelletNodes(coords, radius, 15), coords, velocity, 0, mass, 0, 0, 1, false, ID, Color.ORANGE, false);
        setNonRotatable(true);
        setResistutionSupremacy(true);
        speed = velocity.getMagnitude();
        this.bounceLife = bounceLife;
        bouncesLeft = bounceLife;
        this.inactiveDist = inactiveDist;
        this.launcher = launcher;
        distTraveled = 0;
        sprite = new SpriteImage("EnergyPellet.png", radius * 2 * 1.2, new Vector(0, 0));
        fade0 = new SpriteImage("EnergyPelletFade0.png", radius * 2 * 1.2, new Vector(0, 0));
        fade1 = new SpriteImage("EnergyPelletFade1.png", radius * 2 * 1.2, new Vector(0, 0));
        fade2 = new SpriteImage("EnergyPelletFade2.png", radius * 2 * 1.2, new Vector(0, 0));
    }
    
    public RigidBody getBoundingBox() {
        RigidBodyPoint[] nodesClone = new RigidBodyPoint[getNodes().length];
        for(int i = 0; i < nodesClone.length; i++) {
            nodesClone[i] = new RigidBodyPoint(getNodes()[i].x, getNodes()[i].y);
        }
        return new RigidBody(nodesClone, new Vector(0, 0), 0, 0, 0, 0, false, 0, null, false);
    }
    
    public EnergyPellet(Point position, Vector velocity, double inactiveDist, EnergyPelletLauncher launcher, int ID) {
        this(position, 10, velocity, 50, ID, 5, inactiveDist, launcher);
    }
    
    public boolean collisionReactiveOnly() {
        return distTraveled < inactiveDist;
    }
    
    private static RigidBodyPoint[] generatePelletNodes(Point center, double radius, int nSides) {
        double angInterval = 2 * Math.PI / nSides;

        RigidBodyPoint[] points = new RigidBodyPoint[nSides];
        points[0] = new RigidBodyPoint(center.x, center.y - radius);

        for(int i = 1; i < nSides; i++) {
            Point p = points[i - 1];
            double x = center.x + Math.cos(angInterval) * (p.x - center.x) - Math.sin(angInterval) * (p.y - center.y);
            double y = center.y + Math.sin(angInterval) * (p.x - center.x) + Math.cos(angInterval) * (p.y - center.y);
            points[i] = new RigidBodyPoint(x, y);
        }
        return points;
    }
    
    public void update(double dt, PhysicsPanel panel) {
        update(dt);
    }
    
    private void update(double dt) {
        tickCounter(dt);
    }
    
    public void applyImpulseOnPoint(Point p, Vector impulse, Object o) {
        super.applyImpulseOnPoint(p, impulse, o);
        if(o instanceof EnergyPelletReciever && !((EnergyPelletReciever)o).pelletRecieved()) {
            ((EnergyPelletReciever)o).setPelletRecieved(true);
            launcher.indicatePelletRecieved();
        }
        else if(o instanceof PlayerHitbox) {
            ((PlayerHitbox)o).dealDamage(130);
            bouncesLeft = -1;
        }
        else if(o instanceof Turret && !((Turret)o).getDead()) {
            bouncesLeft = -1;
        }
    }
    
    public void flipSprite() {
        sprite.flip();
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
    
    public Vector getVectorToBase() {
        return null;
    }
    
    public void draw(Graphics g, Camera c, Point position, double orientation) {}
    
    public void draw(Graphics g, Camera c, PortalPair portals, ArrayList<RigidBody> obstacles) {
        Portal[] intersectingPortals = portals.getDominantAndOtherPortalOfBody(this);
        SpriteImage drawnSprite = sprite;
        if(bouncesLeft < 2)
            drawnSprite = fade2;
        else if(bouncesLeft < 4)
            drawnSprite = fade1;
        else if(bouncesLeft < 6)
            drawnSprite = fade0;
        
        if(intersectingPortals == null) {
            drawnSprite.drawNormally(g, c, getCenterOfMass(), orientation);
        }
        else {
            drawnSprite.drawSpriteSplitInPortal(g, c, getCenterOfMass(), orientation, intersectingPortals[0], intersectingPortals[1], portals);
        }
    }
    
    private void tickCounter(double dt) {
        if(bounceInactiveCounter > 0)
            bounceInactiveCounter -= dt;
    }
    
    public void applyImpulseOnPoint(Point p, Vector impulse) {
//        super.applyImpulseOnPoint(p, impulse);
//        setVelocity(getVelocity().multiplyByScalar(speed/getVelocity().getMagnitude()));
        if(getVelocity().dotProduct(impulse) < 0) {
            setVelocity(getVelocity().add(impulse.getUnitVector().multiplyByScalar(impulse.getUnitVector().dotProduct(getVelocity()) * -2)));
        }
        if(bounceInactiveCounter <= 0) {
            bouncesLeft--;
            bounceInactiveCounter = bounceInactiveTime;
        }
    }
    
    public boolean getDead() {
        return bouncesLeft <= 0;
    }

    public void moveForwardOrBackInTime(double dt, Vector accel) {
        super.moveForwardOrBackInTime(dt, new Vector(0, 0));
        distTraveled += dt * getVelocity().getMagnitude();
    }
    
}