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
public class Button implements Asset, ActivatableInput, SpritedEntity {
    private RigidBody edge1;
    private RigidBody edge2;
    private boolean buttonActive;
    private ButtonSurface surface;
    private Point position;
    private double height, width, buttonHeight, buttonWidth, buttonLipDist;
    private double buttonSpeed;
    private double pressTime;
    private double requiredPressTime = 0.15;
    private double stickTime = 0.1;
    private double stickCounter = 0;
    private Vector surfaceVelocity = new Vector(0, 0);
    private SpriteImage body;
    private SpriteImage plate;
    
    public Button(Point position, int[] IDs, ArrayList<RigidBody> bodyList) {
        this.position = position;
        height = 15;
        width = 140;
        buttonWidth = 110;
        buttonHeight = 15.1;
        buttonLipDist = 6;
        buttonSpeed = 45;
        surface = new ButtonSurface(position, buttonWidth, buttonHeight, 1, 0.3, 1, IDs[0]);
        createEdges(1, 0.3, 1, IDs);
        bodyList.add(surface);
        bodyList.add(edge1);
        bodyList.add(edge2);
        double scaleFactor = 11;
        body = new SpriteImage("buttonBody.png", height * scaleFactor, new Vector(0, height * scaleFactor/2.0));
        plate = new SpriteImage("buttonPlate.png", height * scaleFactor, new Vector(0, buttonHeight/2.0 + 3));
    }
    
    public RigidBody getBoundingBox() {
        RigidBodyPoint[] boxNodes = new RigidBodyPoint[] {
            new RigidBodyPoint(position.x - width/2.0, position.y - height),
            new RigidBodyPoint(position.x + width/2.0, position.y - height),
            new RigidBodyPoint(position.x + width/2.0, position.y),
            new RigidBodyPoint(position.x - width/2.0, position.y)
        };
        return new RigidBody(boxNodes, new Point(position.x, position.y), new Vector(0, 0), 0, 0, 0, 0, false, 0, null, false);
    }
    
    private void createEdges(double density, double resistution, double frictionCoefficent, int[] IDs) {
        edge1 = new RigidBody(new RigidBodyPoint[] {new RigidBodyPoint(position.x - buttonWidth/2.0, position.y + 0.1), new RigidBodyPoint(position.x - buttonWidth/2.0, position.y - height),
            new RigidBodyPoint(position.x - width/2.0, position.y + 0.1)},
            new Vector(0, 0), 0, density, resistution, frictionCoefficent, true, IDs[1], Color.GRAY, false);
        edge2 = new RigidBody(new RigidBodyPoint[] {new RigidBodyPoint(position.x + buttonWidth/2.0, position.y + 0.1), new RigidBodyPoint(position.x + buttonWidth/2.0, position.y - height),
            new RigidBodyPoint(position.x + width/2.0, position.y + 0.1)},
            new Vector(0, 0), 0, density, resistution, frictionCoefficent, true, IDs[2], Color.GRAY, false);
    }
    
    public boolean readInput() {
        return buttonActive;
    }
    
    public void update(double dt, PhysicsPanel panel) {
        updateButton(dt);
    }
    
    private void updateButton(double dt) {
        boolean depressed = surface.impulsed;
        if(depressed)
            stickCounter = stickTime;
        else if(stickCounter > 0) {
            stickCounter -= dt;
            depressed = true;
        }
        if(depressed)
            pressTime += dt;
        else
            pressTime = 0;
        moveSurface(depressed);
        buttonActive = pressTime >= requiredPressTime;
    }
    
    private void moveSurface(boolean surfaceDepressed) {
        if(surfaceDepressed) {
            if(surface.getCenterOfMass().y > position.y) {
                surface.translate(new Vector( position.x -surface.getCenterOfMass().x, position.y - surface.getCenterOfMass().y));
            }
            else {
                surfaceVelocity = new Vector(0, buttonSpeed);
            }
        }
        else {
            double topHeight = height - buttonHeight + buttonLipDist;
            if(surface.getCenterOfMass().y < position.y - topHeight) {
                surface.translate(new Vector( position.x -surface.getCenterOfMass().x, position.y - surface.getCenterOfMass().y - topHeight));
            }
            else {
                surfaceVelocity = new Vector(0, -buttonSpeed);
            }
        }
        surface.breakRest(true);
        surface.impulsed = false;
    }
    
    public boolean getLastDrawPriority() {
        return true;
    }
    
    public void flipSprite() {
        // not needded
    }
    
    public SpriteImage getSprite() {
        return body;
    }
    
    public Point getDrawPosition() {
        return position;
    }
    
    public void draw(Graphics g, Camera c, Point position, double orientation) {
        plate.drawNormally(g, c, position, orientation);
        body.drawNormally(g, c, position, orientation);
    }
    
    public void draw(Graphics g, Camera c, PortalPair portals, ArrayList<RigidBody> obstacles) {
        plate.drawNormally(g, c, surface.getCenterOfMass(), 0);
        body.drawNormally(g, c, position, 0);
    }
    
    public Vector getVectorToBase() {
        return new Vector(0, 0);
    }
    
    public class ButtonSurface extends RigidBody {
        private boolean impulsed;
        
        public ButtonSurface(Point position, double width, double height, double density, double resistution, double frictionCoefficent, int ID) {
            super(new RigidBodyPoint[] {new RigidBodyPoint(position.x - width * 7/16.0, position.y - height), new RigidBodyPoint(position.x + width * 7/16.0, position.y - height),
            new RigidBodyPoint(position.x + width/2.0, position.y), new RigidBodyPoint(position.x - width/2.0, position.y)},
            new Point(position.x, position.y), new Vector(0, 0), 0, 0, 0, frictionCoefficent, resistution, true, ID, Color.RED, false);
        }
        
        public void applyImpulseOnPoint(Point p, Vector impulse) {
            super.applyImpulseOnPoint(p, impulse);
            impulsed = true;
        }
        
        public void moveForwardOrBackInTime(double dt, Vector accel) {
            super.moveForwardOrBackInTime(dt, accel);
            Vector movement = surfaceVelocity.multiplyByScalar(dt);
            translate(movement);
        }
        
    }
}
