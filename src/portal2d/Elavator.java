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
public class Elavator implements Asset, SpritedEntity, Flippable{
    private Point position;
    private RigidBody edge;
    private RigidBody floor;
    private RigidBody roof;
    private double rimPorportion;
    private double width;
    private double height;
    private double thickness;
    private double doorOpenCounter;
    private double doorOpenTime;
    private boolean entranceElavator;
    private SpriteImage sprite;
    private SpriteImage openSprite;
    private SpriteImage thirdOpenSprite;
    private SpriteImage twothirdOpenSprite;
    private boolean flipped = false;
    
    public Elavator(Point position, boolean entranceElavator, int[] IDs, ArrayList<RigidBody> bodies, boolean flipped) {
        this(position, 155, 140, 10, IDs, bodies, entranceElavator, flipped);
    }
    
    public Elavator(Point position, double height, double width, double thickness, int[] IDs, ArrayList<RigidBody> bodies, boolean entranceElavator, boolean flipped) {
        rimPorportion = 1.2;
        createWalls(position, height, width, thickness, IDs, bodies);
        this.position = position;
        this.width = width;
        this.height = height;
        this.thickness = thickness;
        this.entranceElavator = entranceElavator;
        double scaleFactor = 1.15;
        sprite = new SpriteImage("elavatorFullOpen.png", height * scaleFactor, new Vector(0, height * scaleFactor/2.0));
        openSprite = new SpriteImage("dispenserOpenFull.png", height * scaleFactor, new Vector(0, height * scaleFactor/2.0));
        thirdOpenSprite = new SpriteImage("dispenserOpenThird.png", height * scaleFactor, new Vector(0, height * scaleFactor/2.0));
        twothirdOpenSprite = new SpriteImage("dispenserOpenTwoThird.png", height * scaleFactor, new Vector(0, height * scaleFactor/2.0));
        if(flipped)
            flip();
    }
    
    public boolean isEntrance() {
        return entranceElavator;
    }
    
    public PlayerHitbox spawnPlayer(PortalPair p, int ID) {
        PlayerHitbox player = new PlayerHitbox(p, new Point(position.x, position.y - height/2.0), ID);
        return player;
    }
    
    private void flipBody(RigidBody b) {
        for(Point p : b.getNodes()) {
            p.x = -(p.x - position.x) + position.x;
        }
        b.resetCollisionStructure(b.getNodes());
    }
    
    public void flip() {
        flipBody(edge);
        flipBody(roof);
        flipBody(floor);
        flipSprite();
        flipped = true;
    }
    
    public boolean getFlipped() {
        return flipped;
    }
    
    private void createWalls(Point position, double height, double width, double thickness, int[] IDs, ArrayList<RigidBody> bodies) {
        RigidBodyPoint[] roofPoints = new RigidBodyPoint[] {new RigidBodyPoint(position.x - width * rimPorportion/2.0, position.y - height), new RigidBodyPoint(position.x + width * rimPorportion/2.0, position.y - height),
            new RigidBodyPoint(position.x + width * rimPorportion/2.0, position.y - height + thickness), new RigidBodyPoint(position.x - width * rimPorportion/2.0, position.y - height + thickness)};
        
        RigidBodyPoint[] edgePoints = new RigidBodyPoint[] {new RigidBodyPoint(position.x - width/2.0, position.y - height + thickness), new RigidBodyPoint(position.x - width/2.0 + thickness, position.y - height + thickness),
            new RigidBodyPoint(position.x - width/2.0 + thickness, position.y - thickness), new RigidBodyPoint(position.x - width/2.0, position.y - thickness)};
        
        RigidBodyPoint[] floorPoints = new RigidBodyPoint[] {new RigidBodyPoint(position.x - width * rimPorportion/2.0, position.y - thickness), new RigidBodyPoint(position.x + width/2.0, position.y - thickness),
            new RigidBodyPoint(position.x + width * (rimPorportion + 0.2)/2.0, position.y + 2), new RigidBodyPoint(position.x - width * rimPorportion/2.0, position.y + 2)};
        
        edge = new RigidBody(edgePoints, new Vector(0, 0), 0, 10, 0.3, 1, true, IDs[0], Color.RED, false);
        floor = new RigidBody(floorPoints, new Vector(0, 0), 0, 10, 0.3, 1, true, IDs[1], Color.RED, false);
        roof = new RigidBody(roofPoints, new Vector(0, 0), 0, 10, 0.3, 1, true, IDs[2], Color.RED, false);
        
        edge.setDontDraw(true);
        floor.setDontDraw(true);
        roof.setDontDraw(true);
        
        bodies.add(edge);
        bodies.add(roof);
        bodies.add(floor);
    }

    public RigidBody getBoundingBox() {
        RigidBodyPoint[] boxNodes = new RigidBodyPoint[] {
            new RigidBodyPoint(position.x - width/2.0, position.y),
            new RigidBodyPoint(position.x + width/2.0, position.y),
            new RigidBodyPoint(position.x + width/2.0, position.y - height),
            new RigidBodyPoint(position.x - width/2.0, position.y - height)
        };
        return new RigidBody(boxNodes, new Point(position.x, position.y), new Vector(0, 0), 0, 0, 0, 0, false, 0, null, false);
    }
    
    public void flipSprite() {
        sprite.flip();
        openSprite.flip();
        thirdOpenSprite.flip();
        twothirdOpenSprite.flip();
    }
    
    public boolean playerFullyInside(PlayerHitbox p) {
        if(p.getCenterOfMass().y > position.y - height && p.getCenterOfMass().y < position.y) {
            if(p.getCenterOfMass().x < position.x + (width - thickness - p.getWidth())/2.0 && p.getCenterOfMass().x > position.x - (width - thickness - p.getWidth())/2.0) {
                return true;
            }
        }
        return false;
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
    
    public boolean getLastDrawPriority() {
        return true;
    }
    
    public void update(double dt, PhysicsPanel panel) {
        
    }
    
    public void draw(Graphics g, Camera c, Point position, double orientation) {
        sprite.drawNormally(g, c, position, orientation);
    }
    
    public void draw(Graphics g, Camera c, PortalPair portals, ArrayList<RigidBody> obstacles) {
        sprite.drawNormally(g, c, position, 0);
//        if(doorOpenCounter > 0) {
//            if(doorOpenCounter < 0.1 || doorOpenTime - doorOpenCounter < 0.1)
//                thirdOpenSprite.drawNormally(g, c, position, 0);
//            else if(doorOpenCounter < 0.2 || doorOpenTime - doorOpenCounter < 0.2)
//                twothirdOpenSprite.drawNormally(g, c, position, 0);
//            else
//                openSprite.drawNormally(g, c, position, 0);
//        }
//        else
//            sprite.drawNormally(g, c, position, 0);
    }
    
}
