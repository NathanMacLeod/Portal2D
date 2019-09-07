/*
 * File added by Nathan MacLeod 2019
 */
package portal2d;

import java.util.ArrayList;
import java.awt.Color;
import java.awt.Graphics;

/**
 *
 * @author Nathan
 */
public class EnergyPelletLauncher extends RigidBody implements Asset, SpritedEntity {
    private EnergyPellet pellet;
    private Point position;
    private int pelletID;
    private double timeSinceLaunchCounter;
    private double waitToLaunchTime = 2;
    private double waitToLaunchCounter = 0;
    private double height;
    private double pelletSpeed;
    private double pelletSize;
    private double pelletMass;
    private boolean pelletRecieved;
    private SpriteImage sprite;
    private SpriteImage openSprite;
    private SpriteImage thirdOpenSprite;
    private SpriteImage twothirdOpenSprite;
    
    public EnergyPelletLauncher(Point position, Vector surfaceNormalVector, double height, double width, double frictionCoefficent, double resistution, int ID, int pelletID) {
        super(new RigidBodyPoint[] {new RigidBodyPoint(position.x + height, position.y - width/2.0), new RigidBodyPoint(position.x + height, position.y + width/2.0),
            new RigidBodyPoint(position.x, position.y + width/2.0), new RigidBodyPoint(position.x, position.y - width/2.0)},
            new Point(position.x, position.y), new Vector(0, 0), 0, 0, 0, frictionCoefficent, resistution, true, ID, Color.RED, false);
        this.rotate(surfaceNormalVector.getDirection());
        this.position = position;
        pelletSpeed = 200;
        pelletSize = 8;
        pelletRecieved = false;
        pelletMass = 100;
        this.height = height;
        this.pelletID = pelletID;
        double scaleFactor = 2;
        sprite = new SpriteImage(new SpriteImage("energyPelletDispenser.png", height * scaleFactor, new Vector(0, 0)).getRotatedBaseImage(Math.PI/2.0), new Vector(-height * scaleFactor/2.0, 0));
        openSprite = new SpriteImage(new SpriteImage("energyPelletDispenserOpenFull.png", height * scaleFactor, new Vector(0, 0)).getRotatedBaseImage(Math.PI/2.0), new Vector(-height * scaleFactor/2.0, 0));
        thirdOpenSprite = new SpriteImage(new SpriteImage("energyPelletDispenserOpenThird.png", height * scaleFactor, new Vector(0, 0)).getRotatedBaseImage(Math.PI/2.0), new Vector(-height * scaleFactor/2.0, 0));
        twothirdOpenSprite = new SpriteImage(new SpriteImage("energyPelletDispenserOpenTwoThird.png", height * scaleFactor, new Vector(0, 0)).getRotatedBaseImage(Math.PI/2.0), new Vector(-height * scaleFactor/2.0, 0));
    }
    
    public RigidBody getBoundingBox() {
        RigidBodyPoint[] nodesClone = new RigidBodyPoint[getNodes().length];
        for(int i = 0; i < nodesClone.length; i++) {
            nodesClone[i] = new RigidBodyPoint(getNodes()[i].x, getNodes()[i].y);
        }
        return new RigidBody(nodesClone, new Point(position.x, position.y), new Vector(0, 0), 0, 0, 0, 0, false, 0, null, false);
    }
    
    public EnergyPelletLauncher(Point position, Vector surfaceNormalVector, int ID, int pelletID) {
        this(position, surfaceNormalVector, 40, 50, 1, 0.3, ID, pelletID);
    }
    
    private boolean pelletDead() {
        return pellet == null || pellet.getDead();
    }
    
    private boolean pelletRemoved(ArrayList<RigidBody> bodies) {
        return pellet == null || !bodies.contains(pellet);
    }
    
    private void removePellet(ArrayList<RigidBody> bodies, ArrayList<Asset> deleteQueue) {
        if(pellet == null)
            return;
        else {
            bodies.remove(pellet);
            deleteQueue.add(pellet);
        }
    }
    
    public boolean pelletRecieved() {
        return pelletRecieved;
    }
    
    public void indicatePelletRecieved() {
        pelletRecieved = true;
    }
    
    public Vector getVectorToBase() {
        return new Vector(0, 0);
    }
    
    public void draw(Graphics g, Camera c, Point position, double orientation) {
        sprite.drawNormally(g, c, position, orientation);
    }
    
    private void launchPellet(ArrayList<RigidBody> bodies, ArrayList<Asset> addAssetQueue) {
        pellet = new EnergyPellet(new Point(position.x, position.y), pelletSize, 
            new Vector(Math.cos(getOrientation()), Math.sin(getOrientation())).multiplyByScalar(pelletSpeed), 
            pelletMass, pelletID, 7, height + pelletSize + 1, this);
        bodies.add(pellet);
        addAssetQueue.add(pellet);
        timeSinceLaunchCounter = 0;
    }
    
    public boolean getLastDrawPriority() {
        return true;
    }
    
    public void flipSprite() {
        // not needded
    }
    
    public SpriteImage getSprite() {
        return sprite;
    }
    
    public Point getDrawPosition() {
        return position;
    }
    
    public void draw(Graphics g, Camera c, PortalPair portals, ArrayList<RigidBody> obstacles) {
        if(timeSinceLaunchCounter < 0.5 || pelletDead() && waitToLaunchCounter < 0.3)
            openSprite.drawNormally(g, c, position, orientation);
        else if(timeSinceLaunchCounter < 0.6 || pelletDead() && waitToLaunchCounter < 0.4)
            twothirdOpenSprite.drawNormally(g, c, position, orientation);
        else if(timeSinceLaunchCounter < 0.7 || pelletDead() &&  waitToLaunchCounter < 0.5)
            thirdOpenSprite.drawNormally(g, c, position, orientation);
        else
            sprite.drawNormally(g, c, position, orientation);
    }
    
    
    public void update(double dt, PhysicsPanel panel) {
        update(dt, panel.getBodies(), panel.getAddAssetQueue(),panel.getAssetDeleteQueue());
    }
            
    private void update(double dt, ArrayList<RigidBody> bodies, ArrayList<Asset> addAssetQueue, ArrayList<Asset> removeAssetQueue) {
        timeSinceLaunchCounter += dt;
        if(pelletRemoved(bodies) && pelletRecieved())
            return;
        
        if(waitToLaunchCounter > 0) {
            waitToLaunchCounter -= dt;
            if(waitToLaunchCounter <= 0)
                launchPellet(bodies, addAssetQueue);
        }
        else if(pelletDead()) {
            removePellet(bodies, removeAssetQueue);
            waitToLaunchCounter = waitToLaunchTime;
        }
        else if(pelletRecieved()) {
            removePellet(bodies, removeAssetQueue);
        }
    }
}
