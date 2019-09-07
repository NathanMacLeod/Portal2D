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
public class CompanionCubeDispenser implements ActivatableEntity, Asset, SpritedEntity {
    private Point position;
    private RigidBody edge1;
    private RigidBody edge2;
    private RigidBody door;
    private double width;
    private double height;
    private double waitToDropNextCubeTime = 2;
    private double waitToDropNextCubeCounter = 0;
    private double doorOpenTime = 1;
    private double doorOpenCounter = 0;
    private CompanionCube exampleCube;
    private CompanionCube readyCube;
    private CompanionCube outCube;
    private SpriteImage sprite;
    private SpriteImage openSprite;
    private SpriteImage thirdOpenSprite;
    private SpriteImage twothirdOpenSprite;
    
    public CompanionCubeDispenser(CompanionCube exampleCube, Point position, int[] IDs, ArrayList<RigidBody> bodies) {
        this(exampleCube, position, 125, 83, 10, IDs, bodies);
    }
    
    public CompanionCubeDispenser(CompanionCube exampleCube, Point position, double height, double width, double thickness, int[] IDs, ArrayList<RigidBody> bodies) {
        createWalls(position, height, width, thickness, IDs, bodies);
        this.position = position;
        this.exampleCube = exampleCube;
        this.width = width;
        this.height = height;
        waitToDropNextCubeCounter = waitToDropNextCubeTime;
        double scaleFactor = 2.66;
        sprite = new SpriteImage("dispenser.png", height * scaleFactor, new Vector(0, -height * scaleFactor/2.0));
        openSprite = new SpriteImage("dispenserOpenFull.png", height * scaleFactor, new Vector(0, -height * scaleFactor/2.0));
        thirdOpenSprite = new SpriteImage("dispenserOpenThird.png", height * scaleFactor, new Vector(0, -height * scaleFactor/2.0));
        twothirdOpenSprite = new SpriteImage("dispenserOpenTwoThird.png", height * scaleFactor, new Vector(0, -height * scaleFactor/2.0));
    }
    
    private void createWalls(Point position, double height, double width, double thickness, int[] IDs, ArrayList<RigidBody> bodies) {
        RigidBodyPoint[] edge1Points = new RigidBodyPoint[] {new RigidBodyPoint(position.x - width/2.0 - thickness, position.y), new RigidBodyPoint(position.x - width/2.0, position.y),
            new RigidBodyPoint(position.x - width/2.0, position.y + height), new RigidBodyPoint(position.x - width/2.0 - thickness, position.y + height)};
        
        RigidBodyPoint[] edge2Points = new RigidBodyPoint[] {new RigidBodyPoint(position.x + width/2.0, position.y), new RigidBodyPoint(position.x + width/2.0 + thickness, position.y),
            new RigidBodyPoint(position.x + width/2.0 + thickness, position.y + height), new RigidBodyPoint(position.x + width/2.0, position.y + height)};
        
        RigidBodyPoint[] doorPoints = new RigidBodyPoint[] {new RigidBodyPoint(position.x - width/2.0, position.y + height - thickness), new RigidBodyPoint(position.x + width/2.0, position.y + height - thickness),
            new RigidBodyPoint(position.x + width/2.0, position.y + height), new RigidBodyPoint(position.x - width/2.0, position.y + height)};
        
        edge1 = new RigidBody(edge1Points, new Vector(0, 0), 0, 10, 0.3, 1, true, IDs[0], Color.RED, false);
        edge2 = new RigidBody(edge2Points, new Vector(0, 0), 0, 10, 0.3, 1, true, IDs[1], Color.RED, false);
        door = new RigidBody(doorPoints, new Vector(0, 0), 0, 10, 0.3, 1, true, IDs[2], Color.RED, false);
        
        edge1.setDontDraw(true);
        edge2.setDontDraw(true);
        door.setDontDraw(true);
        
        bodies.add(edge1);
        bodies.add(edge2);
        bodies.add(door);
    }

    public RigidBody getBoundingBox() {
        RigidBodyPoint[] boxNodes = new RigidBodyPoint[] {
            new RigidBodyPoint(position.x - width/2.0, position.y),
            new RigidBodyPoint(position.x + width/2.0, position.y),
            new RigidBodyPoint(position.x + width/2.0, position.y + height),
            new RigidBodyPoint(position.x - width/2.0, position.y + height)
        };
        return new RigidBody(boxNodes, new Point(position.x, position.y), new Vector(0, 0), 0, 0, 0, 0, false, 0, null, false);
    }
    
    public void turnOn() {
        openDoor();
    }
    
    public boolean getLastDrawPriority() {
        return true;
    }
    
    public void turnOff() {
        //do nothing lol
    }
    
    private void dropNewCube(ArrayList<RigidBody> bodies, ArrayList<Asset> gameAssets, PhysicsPanel panel) {
        int ID = panel.giveID();
        outCube = readyCube;
        readyCube = new CompanionCube(new Point(position.x, position.y - exampleCube.getSize()/2.0), exampleCube.getSize(), exampleCube.getDensity(), exampleCube.getResistution(), exampleCube.getFrictionCoefficent(), ID, Color.RED, false);
        readyCube.giveInactiveDist(exampleCube.getSize() + 3);
        bodies.add(readyCube);
        gameAssets.add(readyCube);
    }
    
    private void openDoor() {
        door.setCollisionImmune(true);
        door.breakRest(true);
        doorOpenCounter = doorOpenTime;
        if(outCube != null) {
            outCube.fizzle();
        }
    }
    
    private void closeDoor() {
        door.setCollisionImmune(false);
        if(waitToDropNextCubeCounter <= 0)
            waitToDropNextCubeCounter = waitToDropNextCubeTime;
    }
    
    public void update(double dt, PhysicsPanel panel) {
        update(dt, panel.getBodies(), panel.getAddAssetQueue(), panel);
    }
    
    private void update(double dt, ArrayList<RigidBody> bodies, ArrayList<Asset> gameAssets, PhysicsPanel panel) {
        if(doorOpenCounter > 0) {
            doorOpenCounter -= dt;
            if(doorOpenCounter <= 0)
                closeDoor();
        }
        else if(waitToDropNextCubeCounter > 0) {
            waitToDropNextCubeCounter -= dt;
            if(waitToDropNextCubeCounter <= 0)
                dropNewCube(bodies, gameAssets, panel);
        }
    }
    
    public void flipSprite() {
        //pointless
    }
    
    public SpriteImage getSprite() {
        return sprite;
    }
    
    public Point getDrawPosition() {
        return position;
    }
    
    public Vector getVectorToBase() {
        return new Vector(0, 0);
    }
    
    public void draw(Graphics g, Camera c, Point position, double orientation) {
        sprite.drawNormally(g, c, position, orientation);
    }
    
    public void draw(Graphics g, Camera c, PortalPair portals, ArrayList<RigidBody> obstacles) {
        if(doorOpenCounter > 0) {
            if(doorOpenCounter < 0.1 || doorOpenTime - doorOpenCounter < 0.1)
                thirdOpenSprite.drawNormally(g, c, position, 0);
            else if(doorOpenCounter < 0.2 || doorOpenTime - doorOpenCounter < 0.2)
                twothirdOpenSprite.drawNormally(g, c, position, 0);
            else
                openSprite.drawNormally(g, c, position, 0);
        }
        else
            sprite.drawNormally(g, c, position, 0);
    }
    
}
