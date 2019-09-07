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
public class EnergyPelletReciever extends RigidBody implements ActivatableInput, SpritedEntity, Asset {
    private boolean pelletRecieved;
    private Point position;
    private SpriteImage sprite;
    private SpriteImage openSprite;
    private SpriteImage openFifthSprite;
    private SpriteImage openTwoFifthSprite;
    private SpriteImage openThreeFifthSprite;
    private SpriteImage openFourFifthSprite;
    private double timeSinceCatchCounter;
    
    public EnergyPelletReciever(Point position, Vector surfaceNormalVector, double height, double width, double frictionCoefficent, double resistution, int ID) {
        super(new RigidBodyPoint[] {new RigidBodyPoint(position.x + height, position.y - width * 1/4.0), new RigidBodyPoint(position.x + height, position.y + width * 1/4.0),
            new RigidBodyPoint(position.x - 2, position.y + width/2.0 + 5), new RigidBodyPoint(position.x - 2, position.y - width/2.0 - 5)},
            new Point(position.x , position.y), new Vector(0, 0), 0, 0, 0, frictionCoefficent, resistution, true, ID, Color.RED, false);
        this.rotate(surfaceNormalVector.getDirection());
        this.position = position;
        double scaleFactor = 2 * 1.35 * 5;
        sprite = new SpriteImage(new SpriteImage("energyPelletReciever.png", height * scaleFactor, new Vector(0, 0)).getRotatedBaseImage(Math.PI/2.0), new Vector(-height * scaleFactor/2.0, 0));
        openSprite = new SpriteImage(new SpriteImage("energyPelletRecieverOpenFull.png", height * scaleFactor, new Vector(0, 0)).getRotatedBaseImage(Math.PI/2.0), new Vector(-height * scaleFactor/2.0, 0));
        openFifthSprite = new SpriteImage(new SpriteImage("energyPelletRecieverOpenFifth.png", height * scaleFactor, new Vector(0, 0)).getRotatedBaseImage(Math.PI/2.0), new Vector(-height * scaleFactor/2.0, 0));
        openTwoFifthSprite = new SpriteImage(new SpriteImage("energyPelletRecieverOpenTwoFifth.png", height * scaleFactor, new Vector(0, 0)).getRotatedBaseImage(Math.PI/2.0), new Vector(-height * scaleFactor/2.0, 0));
        openThreeFifthSprite = new SpriteImage(new SpriteImage("energyPelletRecieverOpenThreeFifth.png", height * scaleFactor, new Vector(0, 0)).getRotatedBaseImage(Math.PI/2.0), new Vector(-height * scaleFactor/2.0, 0));
        openFourFifthSprite = new SpriteImage(new SpriteImage("energyPelletRecieverOpenFourFifth.png", height * scaleFactor, new Vector(0, 0)).getRotatedBaseImage(Math.PI/2.0), new Vector(-height * scaleFactor/2.0, 0));
        timeSinceCatchCounter = 0;
    }
    
    public RigidBody getBoundingBox() {
        RigidBodyPoint[] nodesClone = new RigidBodyPoint[getNodes().length];
        for(int i = 0; i < nodesClone.length; i++) {
            nodesClone[i] = new RigidBodyPoint(getNodes()[i].x, getNodes()[i].y);
        }
        return new RigidBody(nodesClone, new Point(position.x, position.y), new Vector(0, 0), 0, 0, 0, 0, false, 0, null, false);
    }
    
    public EnergyPelletReciever(Point position, Vector surfaceNormalVector, int ID) {
        this(position, surfaceNormalVector, 8, 80, 1, 0.3, ID);
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
    
    public void update(double dt, PhysicsPanel panel) {
        update(dt);
    }
    
    private void update(double dt) {
        if(pelletRecieved)
            timeSinceCatchCounter += dt;
    }
    
    public Vector getVectorToBase() {
        return new Vector(0, 0);
    }
    
    public void draw(Graphics g, Camera c, Point position, double orientation) {
        sprite.drawNormally(g, c, position, orientation);
    }
    
    public void draw(Graphics g, Camera c, PortalPair portals, ArrayList<RigidBody> obstacles) {
        if(timeSinceCatchCounter <= 0) {
            sprite.drawNormally(g, c, position, orientation);
        }
        else if(timeSinceCatchCounter < 0.1) {
            openFifthSprite.drawNormally(g, c, position, orientation);
        }
        else if(timeSinceCatchCounter < 0.2) {
            openTwoFifthSprite.drawNormally(g, c, position, orientation);
        }
        else if(timeSinceCatchCounter < 0.3) {
            openThreeFifthSprite.drawNormally(g, c, position, orientation);
        }
        else if(timeSinceCatchCounter < 0.4) {
            openFourFifthSprite.drawNormally(g, c, position, orientation);
        }
        else {
            openSprite.drawNormally(g, c, position, orientation);
        }
            
    }
    
    public boolean pelletRecieved() {
        return pelletRecieved;
    }
    
    public void setPelletRecieved(boolean b) {
        pelletRecieved = true;
    }
    
    public boolean readInput() {
        return pelletRecieved();
    }
    
}
