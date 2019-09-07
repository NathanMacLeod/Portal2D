/*
 * File added by Nathan MacLeod 2019
 */
package portal2d;

import java.awt.Color;
import java.util.ArrayList;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.BasicStroke;
/**
 *
 * @author Nathan
 */
public class PlayerHitbox extends RigidBody implements Asset, SpritedEntity {
    private double width;
    private double height;
    private double jumpVelocity;
    private double runAcceleration;
    private double midAirAdjustVelocity;
    private boolean hasFootContact;
    private double jumpCooldownTime = 0.25;
    private double jumpCooldownCounter;
    private PortalPair portalSystem;
    private ArrayList<PortalProjectile> portalProjectiles;
    private ArrayList<PortalProjectile> projectileQueue;
    private double orientationCorrectionSpeed;
    private RigidBody footContactBody;
    private RigidBodyLine footContactSurface;
    private Vector jumpVector;
    private double topRunSpeed;
    private double timeDelay = 0.25;
    private double timeWaited;
    private double flippedControlsTimer = -1;
    private double maxVelocity = 5000;
    private RigidBodyHolder grabTool;
    private boolean holdingObjectThroughPortal;
    private Portal portalHeldThrough;
    private SegmentableRay daray = null;
    private Point daPoint = null;
    private double grabHoverDist = 180;
    private double loseFootingCounter = 0;
    private double agileFloatTime = 0;
    private double health = 100;
    private double heightMultiplier;
    private SpriteImage head;
    private SpriteImage forwardArm;
    private SpriteImage forwardThigh;
    private SpriteImage forwardShin;
    private SpriteImage forwardFoot;
    private SpriteImage rearThigh;
    private SpriteImage rearShin;
    private SpriteImage rearFoot;
    private SpriteImage rearArm;
    private SpriteImage armNoHand;
    private Vector headToArm;
    private Vector headToForwardThigh;
    private Vector thighToForwardShin;
    private Vector shinToForwardFoot;
    private Vector headToRearThigh;
    private Vector thighToRearShin;
    private Vector shinToRearFoot;
    private Vector armToGun;
    private boolean leftOrientation = false;
    private double forwardThighAngle;
    private double forwardShinAngle;
    private double forwardFootAngle;
    private double armAngle;
    private double rearThighAngle;
    private double rearShinAngle;
    private double rearFootAngle;
    private double t = 0;
    private double speed = 2;
    private int walkDirection = 0;
    private boolean aimingTooLow = false;
    private ArrayList<GibPart> gibParts = null;
    private ArrayList<SmokePuff> smokePuffs;
    double maxPuffRate = 0.10;
    double minPuffRate = 0.75;
    double puffCounter;
    double originalSpriteSize;
    
    
    public PlayerHitbox(PortalPair portalSystem, Point center, int ID) {
        this(portalSystem, center, 35, 110, 2, 0.3, 1, ID, Color.BLUE, true);
    }
    
    public PlayerHitbox(PortalPair portalSystem, Point center, double width, double height, double density, double resistution, double frictionCoefficent, int ID, Color color, boolean drawCOM) {
        super(new RigidBodyPoint[] {new RigidBodyPoint(center.x - width/2.0, center.y - height/2.0), new RigidBodyPoint(center.x + width/2.0, center.y - height/2.0),  new RigidBodyPoint(center.x + width/2.0, center.y + height/2.0), new RigidBodyPoint(center.x - width/2.0, center.y + height/2.0)},
        new Vector(0, 0), 0, density, resistution, frictionCoefficent, false, ID, color, drawCOM);
        this.width = width;
        this.height = height;
        jumpVelocity = 550;
        runAcceleration = 2500;
        midAirAdjustVelocity = 150;
        topRunSpeed = 300;
        this.portalSystem = portalSystem;
        portalProjectiles = new ArrayList<>();
        projectileQueue = new ArrayList<>();
        smokePuffs = new ArrayList<>();
        orientationCorrectionSpeed = 8;
        heightMultiplier = 1.15;
        grabTool = new RigidBodyHolder(getAimPoint(), grabHoverDist * 1.5, portalSystem);
        setUpSpriteInfo();
    }
    
    private void setUpSpriteInfo() {
        double size = height * heightMultiplier;
        originalSpriteSize = 200;
        head = new SpriteImage("playerhead.png", size, new Vector(0, -height * ((1-heightMultiplier))/2.0));
        forwardArm = new SpriteImage("forwardArm.png", size, new Vector(0, 0));
        forwardThigh = new SpriteImage("forwardThigh.png", size, new Vector(0, 0));
        forwardShin = new SpriteImage("forwardshin.png", size, new Vector(0, 0));
        forwardFoot = new SpriteImage("forwardFoot.png", size, new Vector(0, 0));
        rearThigh = new SpriteImage("rearThigh.png", size, new Vector(0, 0));
        rearShin = new SpriteImage("rearShin.png", size, new Vector(0, 0));
        rearFoot = new SpriteImage("rearFoot.png", size, new Vector(0, 0));
        rearArm = new SpriteImage("rearArm.png", size, new Vector(0, 0));
        armNoHand = new SpriteImage("forwardArmNoHand.png", size, new Vector(0, 0));
        
        headToArm = new Vector(-11, -71).multiplyByScalar(size/originalSpriteSize);
        headToForwardThigh = new Vector(-12, -20).multiplyByScalar(size/originalSpriteSize);
        thighToForwardShin = new Vector(-4, 41).multiplyByScalar(size/originalSpriteSize);
        shinToForwardFoot = new Vector(5, 63).multiplyByScalar(size/originalSpriteSize);
        headToRearThigh = new Vector(3, -20).multiplyByScalar(size/originalSpriteSize);
        thighToRearShin = new Vector(-3, 41).multiplyByScalar(size/originalSpriteSize);
        shinToRearFoot = new Vector(4, 65).multiplyByScalar(size/originalSpriteSize);
        armToGun = new Vector(52, 34).multiplyByScalar(size/originalSpriteSize);
    }
    
    public Point getAimPoint() {
        Vector vect = new Vector(0, -height/4.0);
        vect = vect.rotate(orientation);
        return new Point(getCenterOfMass().x + vect.getXComp(), getCenterOfMass().y + vect.getYComp());
    }
    
    public SpriteImage getSprite() {
        return head;
    }
    
    public Point getDrawPosition() {
        return getCenterOfMass();
    }
    
    public RigidBody getBoundingBox() {
        RigidBodyPoint[] nodesClone = new RigidBodyPoint[getNodes().length];
        for(int i = 0; i < nodesClone.length; i++) {
            nodesClone[i] = new RigidBodyPoint(getNodes()[i].x, getNodes()[i].y);
        }
        return new RigidBody(nodesClone, new Vector(0, 0), 0, 0, 0, 0, false, 0, null, false);
    }
    
    public void flipSprite() {
        if(getDead())
            return;
        leftOrientation = !leftOrientation;
        head.flip();
        forwardArm.flip();
        forwardThigh.flip();
        forwardShin.flip();
        forwardFoot.flip();
        rearThigh.flip();
        rearShin.flip();
        rearFoot.flip();
        rearArm.flip();
        armNoHand.flip();
        
        headToArm = new Vector(-headToArm.getXComp(), headToArm.getYComp());
        headToForwardThigh = new Vector(-headToForwardThigh.getXComp(), headToForwardThigh.getYComp());
        thighToForwardShin = new Vector(-thighToForwardShin.getXComp(), thighToForwardShin.getYComp());
        shinToForwardFoot = new Vector(-shinToForwardFoot.getXComp(), shinToForwardFoot.getYComp());
        headToRearThigh = new Vector(-headToRearThigh.getXComp(), headToRearThigh.getYComp());
        thighToRearShin = new Vector(-thighToRearShin.getXComp(), thighToRearShin.getYComp());
        shinToRearFoot = new Vector(-shinToRearFoot.getXComp(), shinToRearFoot.getYComp());
        armToGun = new Vector(-armToGun.getXComp(), armToGun.getYComp());
    }
    
    public boolean getDead() {
        return health < 0;
    }
    
    public boolean getAtRest() {
        return false;
    }
    
    public boolean isHoldingObject() {
        return grabTool.isHoldingObject();
    }
    
    public boolean getObjectIsHeld(Object o) {
        return grabTool.getHeldBody() != null && grabTool.getHeldBody().equals(o);
    }
    
    public Vector getVectorToBase() {
        return new Vector(0, height/2.0);
    }
    
    public void draw(Graphics g, Camera c, Point position, double orientation) {
        //sprite.drawNormally(g, c, position, orientation);
    }
    
    public void setVelocity(Vector v) {
        if(v.getMagnitude() > maxVelocity) 
            v = v.multiplyByScalar(maxVelocity/v.getMagnitude());
        super.setVelocity(v);
    }
    
    public void indicateFlippedControls() {
        flippedControlsTimer = 0.5;
    }
    
    public void updateGrabTool(double dt, Point mouse, int direction) {
        Point aimPoint = getAimPoint();
        Vector mouseDirection = new Vector(mouse.x - aimPoint.x, mouse.y - aimPoint.y);
        if(mouseDirection.getMagnitude() > grabHoverDist)
            mouseDirection = mouseDirection.getUnitVector().multiplyByScalar(grabHoverDist);
        SegmentableRay holdRay = new SegmentableRay(new Ray(mouseDirection, new Point(aimPoint.x, aimPoint.y)));
        holdRay.segmentRay(portalSystem);
        if(holdRay.getSegments().size() > 1) {
            holdingObjectThroughPortal = true;
            portalHeldThrough = holdRay.getFirstPortal();
        }
        else {
            holdingObjectThroughPortal = false;
        }
        
        daray = holdRay;
        Ray terminalRay = holdRay.getSegments().get(0);
        Point newGrabPoint = new Point(terminalRay.origin.x + terminalRay.vector.getXComp(), terminalRay.origin.y + terminalRay.vector.getYComp());
        daPoint = newGrabPoint;
        grabTool.updateOrigin(newGrabPoint);
        grabTool.update(dt, direction);
    }
    
    public void releaseObject() {
        grabTool.releaseObject();
    }
    
    public double getHeight() {
        return height;
    }
    
    public double getWidth() {
        return width;
    }
    
    public void setAgileFloatTime(double d) {
        agileFloatTime = d;
    }
    
    public void update(double dt, PhysicsPanel panel) {
        if(agileFloatTime > 0)
            agileFloatTime -= dt;
        if(loseFootingCounter > 0)
            loseFootingCounter -= dt;
        if(health > 0) {
            health += dt * 10;
            if(health > 100) {
                health = 100;
                puffCounter = 0;
            }
        }
        else {
            if(gibParts == null)
                setBodyToDead(panel);
        }
        updateSmokePuffs(dt);
        doAnimations(dt, panel);
    }
    
    private void doAnimations(double dt, PhysicsPanel panel) {
        Point aimPoint = getAimPoint();
        Point mouse = panel.getMouse();
        Vector centerToMouse = new Vector(mouse.x - aimPoint.x, mouse.y - aimPoint.y);
        aimingTooLow = centerToMouse.getUnitVector().getYComp() > 0.51;
        boolean mouseLeft = mouse.x < aimPoint.x;
        if(mouseLeft != leftOrientation)
            flipSprite();
        armAngle = centerToMouse.getDirection();
        if(leftOrientation) {
            armAngle += Math.PI;
        }
        
        if(walkDirection == 0) {
            forwardThighAngle = 0;
            forwardShinAngle = 0;
            rearThighAngle = 0;
            rearShinAngle = 0;
        }
        else {
            t += dt;
            t %= Math.PI * 2;
            
            boolean walkingBackwards = (leftOrientation && walkDirection == 1) || (!leftOrientation && walkDirection == -1);
            double time = t * ((walkingBackwards)? -1 : 1);
            
            forwardThighAngle = -Math.PI/6 +Math.PI * Math.cos(time * Math.PI * 2 * speed)/4.0;
            forwardShinAngle = Math.PI/3.0 - Math.PI * Math.cos(time * Math.PI * 2 * speed + Math.PI/4.0)/4.0;
            rearThighAngle = -Math.PI/6 +Math.PI * Math.cos(time * Math.PI * 2 * speed + Math.PI)/4.0;
            rearShinAngle = Math.PI/3.0 - Math.PI * Math.cos(time * Math.PI * 2 * speed + Math.PI/4.0 + Math.PI)/4.0;

            if(leftOrientation) {
                forwardThighAngle *= -1;
                forwardShinAngle *= -1;
                rearThighAngle *= -1;
                rearShinAngle *= -1;
            }
        }
    }
    
    private Point getGunPoint() {
        Point armPoint = new Point(getCenterOfMass().x + headToArm.getXComp(), getCenterOfMass().y + (height * ((1-heightMultiplier))/2.0) +headToArm.getYComp());
        Vector v = armToGun.rotate(armAngle);
        return new Point(armPoint.x + v.getXComp(), armPoint.y + v.getYComp());
    }
    
    public void drawAimingLaser(Graphics g, Camera c, Point mousePoint, PortalPair p, ArrayList<RigidBody> obstacles) {
        Point gunPoint = getGunPoint();
        Point aimPoint = getAimPoint();
        Ray aimRay = new Ray(new Vector(mousePoint.x - aimPoint.x, mousePoint.y - aimPoint.y).getUnitVector().multiplyByScalar(10000), aimPoint);
        Point endPoint = new Point(aimRay.origin.x + aimRay.vector.getXComp(), aimRay.origin.y + aimRay.vector.getYComp());
        for(RigidBody b : obstacles) {
            if(!b.getFixed() && !(b instanceof Door))
                continue;
            for(Line l : b.getLines()) {
                Line currentLine = new Line(endPoint, aimRay.origin);
                Point intersection = currentLine.getIntersection(l);
                if(intersection != null) {
                    endPoint = intersection;
                }

            }
        }
//        Graphics2D g2d = (Graphics2D) g;
//        g2d.setStroke(new BasicStroke(2));
        new Line(gunPoint, endPoint).draw(g, Color.BLUE, c);
        //g2d.setStroke(new BasicStroke(1));
    }
    
    public void drawHoldBeam(Graphics g, Camera c, PortalPair pair) {
        if(!isHoldingObject() || getDead())
            return;
        Point gunPoint = getGunPoint();
        Point holdPoint = grabTool.getHoldPoint();
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(2));
        if(!holdingObjectThroughPortal) {
            new Line(gunPoint, holdPoint).draw(g, Color.white, c);
        }
        else {
            Portal enterPortal = portalHeldThrough;
            Portal exitPortal = (pair.getPortals()[0] == enterPortal)? pair.getPortals()[1] : pair.getPortals()[0];
            Point apparentPoint = pair.teleportPointToOtherPortal(holdPoint, exitPortal, enterPortal);
            //new Line(gunPoint, apparentPoint).draw(g, Color.red, c);
            Vector vect = new Vector(apparentPoint.x - gunPoint.x, apparentPoint.y - gunPoint.y);
            SegmentableRay ray = new SegmentableRay(new Ray(vect, gunPoint));
            ray.segmentRay(pair);
            ray.draw(g, Color.white, c);
        }
        g2d.setStroke(new BasicStroke(1));
    }
    
    
    public void dealDamage(double damage) {
        if(getDead())
            return;
        health -= damage;
    }
    
    public void pickUpObject(ArrayList<RigidBody> allBodies, Point mouse) {
        Point aimPoint = getAimPoint();
        Vector mouseDirection = new Vector(mouse.x - aimPoint.x, mouse.y - aimPoint.y).getUnitVector().multiplyByScalar(1.5);
        mouseDirection = mouseDirection.multiplyByScalar(grabHoverDist);
        Line grabRay = new Line(aimPoint, new Point(aimPoint.x + mouseDirection.getXComp(), aimPoint.y + mouseDirection.getYComp()));
        RigidBody grabBody = null;
        Point closestIntersection = null;
        double intersectionDist = -1;
        for(RigidBody b : allBodies) {
            if(b instanceof PlayerHitbox || b.getFixed() || b instanceof Door || (b instanceof ShadowRigidBody && ((ShadowRigidBody)b).getBase() instanceof PlayerHitbox))
                continue;
            if(b.lineIntersectsBody(grabRay)) {
                for(Line l : b.getLines()) {
                    Point intersection = grabRay.getIntersection(l);
                    if(intersection == null)
                        continue;
                    double currentDist = new Vector(aimPoint.x - intersection.x, aimPoint.y - intersection.y).getSquaredMagnitude();
                    if(intersectionDist == -1 || currentDist < intersectionDist) {
                        closestIntersection = intersection;
                        intersectionDist = currentDist;
                        grabBody = b;
                    }
                }
                
            }
        }
        if(grabBody != null && closestIntersection != null)
            grabTool.pickUpBody(grabBody, closestIntersection);
        return;
    }
    
    private void setBodyToDead(PhysicsPanel p) {
        Point spriteCenter = new Point(getCenterOfMass().x, getCenterOfMass().y + height * (1-heightMultiplier)/2.0);
        //create arm
        Vector armVector = headToArm.rotate(getOrientation());
        Point armWorldPosition = new Point(spriteCenter.x + armVector.getXComp(), spriteCenter.y + armVector.getYComp());
        GibPart armGib = new GibPart(armNoHand, getVelocity().add(getRandomExplosionVector()), getGibHitbox(0), armWorldPosition, armAngle + getOrientation(), p.giveID(), getRandomExplosionRotation());
        //create head
        Point headWorldPosition = getCenterOfMass();
        GibPart headGib = new GibPart(head, getVelocity().add(getRandomExplosionVector()), getGibHitbox(1), headWorldPosition, getOrientation(), p.giveID(), getRandomExplosionRotation());
        //create front thigh
        Vector frontThighVector = headToForwardThigh.rotate(getOrientation());
        Point frontThighPosition = new Point(spriteCenter.x + frontThighVector.getXComp(), spriteCenter.y + frontThighVector.getYComp());
        GibPart frontThighGib = new GibPart(forwardThigh, getVelocity().add(getRandomExplosionVector()), getGibHitbox(2), frontThighPosition, getOrientation() + forwardThighAngle, p.giveID(), getRandomExplosionRotation());
        //create front shin
        Vector frontShinVector = thighToForwardShin.rotate(getOrientation() + forwardThighAngle);
        Point frontShinPosition = new Point(frontThighPosition.x + frontShinVector.getXComp(), frontThighPosition.y + frontShinVector.getYComp());
        GibPart frontShinGib = new GibPart(forwardShin, getVelocity().add(getRandomExplosionVector()), getGibHitbox(3), frontShinPosition, getOrientation() + forwardThighAngle + forwardShinAngle, p.giveID(), getRandomExplosionRotation());
        //create front foot
        Vector frontFootVector = shinToForwardFoot.rotate(getOrientation() + forwardThighAngle + forwardShinAngle);
        Point frontFootPosition = new Point(frontShinPosition.x + frontFootVector.getXComp(), frontShinPosition.y + frontFootVector.getYComp());
        GibPart frontFootGib = new GibPart(forwardFoot, getVelocity().add(getRandomExplosionVector()), getGibHitbox(4), frontFootPosition, getOrientation() + forwardThighAngle + forwardShinAngle + forwardFootAngle, p.giveID(), getRandomExplosionRotation());
        //create rear thigh
        Vector rearThighVector = headToRearThigh.rotate(getOrientation());
        Point rearThighPosition = new Point(spriteCenter.x + rearThighVector.getXComp(), spriteCenter.y + rearThighVector.getYComp());
        GibPart rearThighGib = new GibPart(rearThigh, getVelocity().add(getRandomExplosionVector()), getGibHitbox(2), rearThighPosition, getOrientation() + rearThighAngle, p.giveID(), getRandomExplosionRotation());
        //create rear shin
        Vector rearShinVector = thighToRearShin.rotate(getOrientation() + rearThighAngle);
        Point rearShinPosition = new Point(rearThighPosition.x + rearShinVector.getXComp(), rearThighPosition.y + rearShinVector.getYComp());
        GibPart rearShinGib = new GibPart(rearShin, getVelocity().add(getRandomExplosionVector()), getGibHitbox(3), rearShinPosition, getOrientation() + rearThighAngle + rearShinAngle, p.giveID(), getRandomExplosionRotation());
        //create rear foot
        Vector rearFootVector = shinToRearFoot.rotate(getOrientation() + rearThighAngle + rearShinAngle);
        Point rearFootPosition = new Point(rearShinPosition.x + rearFootVector.getXComp(), rearShinPosition.y + rearFootVector.getYComp());
        GibPart rearFootGib = new GibPart(rearFoot, getVelocity().add(getRandomExplosionVector()), getGibHitbox(4), rearFootPosition, getOrientation() + rearThighAngle + rearShinAngle + rearFootAngle, p.giveID(), getRandomExplosionRotation());
        
        ArrayList<RigidBody> bodies = p.getBodies();
        bodies.add(rearThighGib);
        bodies.add(rearShinGib);
        bodies.add(rearFootGib);
        bodies.add(frontFootGib);
        bodies.add(headGib);
        bodies.add(frontThighGib);
        bodies.add(frontShinGib);
        bodies.add(armGib);
        bodies.remove(this);
        
        RigidBody.setUpNonCollisionNetwork(new RigidBody[] {armGib, frontShinGib, frontThighGib, headGib, frontFootGib, rearFootGib, rearShinGib, rearThighGib});
        
        gibParts = new ArrayList();
        
        gibParts.add(rearThighGib);
        gibParts.add(rearShinGib);
        gibParts.add(rearFootGib);
        gibParts.add(frontFootGib);
        gibParts.add(headGib);
        gibParts.add(frontThighGib);
        gibParts.add(frontShinGib);
        gibParts.add(armGib);
        
    }
    
    private RigidBodyPoint[] flipXIfFlipped(RigidBodyPoint[] points) {
        if(!leftOrientation)
            return points;
        for(Point p : points) {
            p.x *= -1;
        }
        return points;
    }
    
    private Vector getRandomExplosionVector() {
        double veloc = 600;
        return new Vector((Math.random() - 0.5) * 2 * veloc, (Math.random() - 0.5) * 2 *  veloc);
    }
    
    private double getRandomExplosionRotation() {
        double maxVeloc = 5;
        return 2 * (Math.random() - 0.5) * maxVeloc;
    }
    
    private RigidBodyPoint[] getGibHitbox(int gibPart) {
        RigidBodyPoint[] points = null;
        switch(gibPart) {
            case 0:
                //arm
                points = new RigidBodyPoint[] {
                    new RigidBodyPoint(-2, -15), new RigidBodyPoint(6, -3), new RigidBodyPoint(-22, 32),
                    new RigidBodyPoint(25, 26), new RigidBodyPoint(51, 31), new RigidBodyPoint(50, 43),
                    new RigidBodyPoint(10, 48), new RigidBodyPoint(-34, 36)  
                };
                break;
            case 1:
                //head
                points = new RigidBodyPoint[] {
                    new RigidBodyPoint(1, -87), new RigidBodyPoint(14, -52), new RigidBodyPoint(12, -17),
                    new RigidBodyPoint(-4, -5), new RigidBodyPoint(-19, -18), new RigidBodyPoint(-18, -52),
                    new RigidBodyPoint(-5, -87)
                };
                break;
            case 2:
                //Thigh
                points = new RigidBodyPoint[] {
                    new RigidBodyPoint(-1, -12), new RigidBodyPoint(10, 2), new RigidBodyPoint(11, 10),
                    new RigidBodyPoint(6, 18), new RigidBodyPoint(2, 45), new RigidBodyPoint(-6, 45),
                    new RigidBodyPoint(-7, 34), new RigidBodyPoint(-12, 18), new RigidBodyPoint(-11, 3)
                };
                break;
            case 3:
                //shin
                points = new RigidBodyPoint[] {
                    new RigidBodyPoint(-4, -5), new RigidBodyPoint(5, -5), new RigidBodyPoint(8, 55),
                    new RigidBodyPoint(12, 65), new RigidBodyPoint(7, 71), new RigidBodyPoint(0, 68),
                    new RigidBodyPoint(-4, 56)
                };
                break;
            case 4:
                //foot
                points = new RigidBodyPoint[] {
                    new RigidBodyPoint(-1, -4), new RigidBodyPoint(20, 13), new RigidBodyPoint(-7, 13)
                };
                break;
        }
        for(RigidBodyPoint point : points) {
            point.x *= height * heightMultiplier/originalSpriteSize;
            point.y *= height * heightMultiplier/originalSpriteSize;
            if(gibPart == 1)
                point.y += height * (1-heightMultiplier)/2.0;
        }
        return flipXIfFlipped(points);
    }
    
    private class GibPart extends RigidBody {
        private Vector centerOfMassToPosition;
        SpriteImage sprite;
        
        public GibPart(SpriteImage sprite, Vector initialVelocity, RigidBodyPoint[] points, Point worldPosition, double orientation, int ID, double angVeloc) {
            super(points, new Vector(initialVelocity.getXComp(), initialVelocity.getYComp()), angVeloc, 2, 0.3, 1, false, ID, Color.BLACK, true);
            centerOfMassToPosition = new Vector(-getCenterOfMass().x, -getCenterOfMass().y);
            this.rotate(orientation);
            Point pos = getPosition();
            Vector translate = new Vector(worldPosition.x - pos.x, worldPosition.y - pos.y);
            translate(translate);
            this.sprite = sprite;
        }
        
        private Point getPosition() {
            Vector v = centerOfMassToPosition.rotate(getOrientation());
            return new Point(getCenterOfMass().x + v.getXComp(), getCenterOfMass().y + v.getYComp());
        }
        
        public void draw(Graphics g, Camera c, PortalPair portals) {
            Portal[] intersectingPortals = portals.getDominantAndOtherPortalOfBody(this);
            if(intersectingPortals == null) {
                sprite.drawNormally(g, c, getPosition(), orientation);
            }
            else {
                sprite.drawSpriteSplitInPortal(g, c, getPosition(), orientation, intersectingPortals[0], intersectingPortals[1], portals);
            }   
        }
        
    }
    
    private void addSmokePuff() {
        Point aimPoint = getAimPoint();
        smokePuffs.add(new SmokePuff(new Point(aimPoint.x, aimPoint.y), (int) (2.55 * health), new Vector(getVelocity().getXComp(), getVelocity().getYComp())));
    }
    
    private void updateSmokePuffs(double dt) {
        puffCounter += dt;
        if(health != 100 && health > 0) {
            double requiredPuffTime =  maxPuffRate + (health/100) * (minPuffRate  - maxPuffRate);
            if(puffCounter >= requiredPuffTime) {
                puffCounter = 0;
                addSmokePuff();
            }
        }
        for(int i = 0; i < smokePuffs.size(); i++) {
            smokePuffs.get(i).update(dt);
            if(smokePuffs.get(i).expired()) {
                smokePuffs.remove(i);
                i--;
            }
        }
    }
    
    private class SmokePuff {
        private Point position;
        private double initialSize;
        private Vector velocity;
        private double finalSize;
        private double finalRiseHeight;
        private double initialAlpha = 170;
        private double age;
        private double lifeTime;
        private int smokeColor;
        
        public SmokePuff(Point initialPosition, int smokeColor, Vector velocity) {
            position = initialPosition;
            initialSize = 10;
            finalRiseHeight = height * 3;
            this.velocity = velocity;
            finalSize = 200;
            lifeTime = 6;
            age = 0;
            this.smokeColor = smokeColor;
        }
        
        public void update(double dt) {
            age += dt;
            position.x += velocity.getXComp() * dt;
            position.y += velocity.getYComp() * dt;
            velocity = velocity.subtract(velocity.multiplyByScalar(2 * dt));
        }
        
        public boolean expired() {
            return age > lifeTime;
        }
        
        public void draw(Graphics g, Camera c) {
            double heightRisen = (1 - (1/(age * 4/lifeTime + 1))) * finalRiseHeight/2.0;
            double size = initialSize + (1 - (1/(age * 4/lifeTime + 1))) * (finalSize - initialSize)/2.0;
            int alphaValue = (int) (initialAlpha * ((lifeTime - age) / lifeTime));
            g.setColor(new Color(smokeColor, smokeColor, smokeColor, alphaValue));
            g.fillOval((int) (position.x - size/2.0 - c.getPosition().x + c.getWidth()), (int) (position.y - heightRisen - size/2.0 - c.getPosition().y + c.getHeight()), (int) size, (int) size);
        }
    }
    
    public void applyImpulseOnPoint(Point point, Vector impulse, Object o) {
        if(o instanceof Turret.Bullet) {
            this.setLoseFootingTime(0.07);
        }
        super.applyImpulseOnPoint(point, impulse, o);
    }
    
    private boolean pointNotFootPoint(Point p) {
        return !p.equals(getFootLine().getPoints()[0]) && !p.equals(getFootLine().getPoints()[1]);
    }
    
    public void applyImpulseOnPoint(Point collidingPoint, Vector impulse) {
        if(getDead()) {
            super.applyImpulseOnPoint(collidingPoint, impulse);
            return;
        }
        if(getFootLine().getSquaredDistToLine(collidingPoint) < 0.5 && pointNotFootPoint(collidingPoint)) {
            double oldXVelocity = getVelocity().getXComp();
            applyImpulse(collidingPoint, impulse);
            setVelocity(new Vector(oldXVelocity, getVelocity().getYComp()));
        }
        else
            applyImpulse(collidingPoint, impulse);
    }
    
    public void move(int direction, double dt) {
        walkDirection = direction;
        if(getDead()) 
            return;
        if(jumpCooldownCounter > 0)
            jumpCooldownCounter -= dt;
        if(flippedControlsTimer > 0) { 
            direction *= -1;
            flippedControlsTimer -= dt;
        }
        if(hasFootContact) {
            double relativeV = getVelocity().getXComp() - footContactBody.getVelocity().getXComp();
            if(direction != 0) {
                if(relativeV * direction < topRunSpeed) {
                    setVelocity(getVelocity().add(new Vector(direction * runAcceleration * dt, 0)));
                }
            }
            else {
                double slowMagnitude = 2 * runAcceleration * dt;
                if(Math.abs(relativeV) < slowMagnitude)
                    setVelocity(new Vector(footContactBody.getVelocity().getXComp(), getVelocity().getYComp()));
                else if(relativeV > 0)
                    setVelocity(getVelocity().add(new Vector(-slowMagnitude, 0)));
                else 
                    setVelocity(getVelocity().add(new Vector(slowMagnitude, 0)));
            }
        }
        else {
            double floatControl = (agileFloatTime > 0)? 2 : 1;
            double slowDownModifier = (direction * getVelocity().getXComp() < 0)? 2.5 : 1;
            setVelocity(getVelocity().add(new Vector(direction * floatControl * midAirAdjustVelocity * slowDownModifier * dt, 0)));
        }
    }
    
    public void jump() {
        if(!getDead() && jumpCooldownCounter <= 0 && hasFootContact /*&& Math.abs(jumpVector.getYComp()) > Math.abs(jumpVector.getXComp())*/) {
            setVelocity(getVelocity().add(jumpVector.multiplyByScalar(jumpVelocity)));
            hasFootContact = false;
            jumpCooldownCounter = jumpCooldownTime;
        }
    }
    
    private void setLoseFootingTime(double t) {
        loseFootingCounter = t;
    }
    
    public void resetFootContact() {
        hasFootContact = false;
    }
    
    public void checkForFootContact(RigidBody collidingBody, Point collidingPoint, RigidBodyLine surface) {
        checkForFootContact(getLines()[2], this, collidingBody, collidingPoint, surface, true);
    }
    
    public void checkForFootContact(Line footLine, RigidBody baseBody, RigidBody collidingBody, Point collidingPoint, RigidBodyLine surface, boolean mainBody) {
        if(loseFootingCounter > 0)
            return;
        if(grabTool.getHeldBody() != null && grabTool.getHeldBody() == collidingBody)
            return;
        if((!hasFootContact || mainBody) && (collidingPoint.equals(footLine.getPoints()[0]) || collidingPoint.equals(footLine.getPoints()[1]) 
        || getLineClosestToPoint(collidingPoint).equals(footLine)) && Math.abs(surface.getSlope()) < 1.73) {
            hasFootContact = true;
            footContactBody = collidingBody;
            footContactSurface = surface;
            jumpVector = new Vector(0, footContactSurface.getNormalUnitVector().getYComp());
            if(!(collidingPoint.equals(footLine.getPoints()[0]) || collidingPoint.equals(footLine.getPoints()[1])))
                jumpVector = jumpVector.multiplyByScalar(-1);
            if(!mainBody) {
                ShadowRigidBody b = (ShadowRigidBody) baseBody;
                jumpVector = b.getPortalPair().redirectVector(jumpVector, b.getAssocaitedPotal(), b.getAssociatedPortalPartner());
            }
        }
    }
    
    public void firePortalProjectile(Point mousePoint, int portalNumber) {
        Point aimPoint = getAimPoint();
        if(isHoldingObject())
            return;
        Vector centerToPoint = new Vector(mousePoint.x - aimPoint.x, mousePoint.y - aimPoint.y);
        double portalSpeed = 2500;
        Vector portalVelocity = centerToPoint.multiplyByScalar(portalSpeed/centerToPoint.getMagnitude());
        Point gunPoint = getGunPoint();
        PortalProjectile projectile = new PortalProjectile(portalSystem, new Point(aimPoint.x, aimPoint.y), portalVelocity, 130, portalNumber);
        projectileQueue.add(projectile);
    }
    
    public void updateProjectiles(double dt, ArrayList<RigidBody> bodies) {
        while(projectileQueue.size() > 0)
            portalProjectiles.add(projectileQueue.remove(0));
        for(int i = 0; i < portalProjectiles.size(); i++) {
            if(portalProjectiles.get(i).moveForwardInTime(dt, bodies)) {
                portalProjectiles.remove(i);
                i--;
            }
        }
    }
    
    public void drawPortals(Graphics g, Camera c) {
        for(PortalProjectile p : portalProjectiles)
            p.draw(g, c);
    }
    
    public void draw(Graphics graphics, Camera c, PortalPair portals, ArrayList<RigidBody> obstacles) {
        if(!getDead())
            drawAliveBody(graphics, c, portals);
        else {
            for(GibPart p : gibParts) {
                p.draw(graphics, c, portals);
            }
        }
        for(SmokePuff puff : smokePuffs) {
            puff.draw(graphics, c);
        }
    }
    
    
    private void drawAliveBody(Graphics graphics, Camera c, PortalPair portals) {
        BufferedImage playerCanvas = new BufferedImage((int) (head.getImage().getWidth() * 1.5), (int) (head.getImage().getHeight() * 1.5), BufferedImage.TYPE_INT_ARGB);
        Graphics g = playerCanvas.getGraphics();
        
        Point centerOfSprite = new Point(playerCanvas.getWidth()/2.0, playerCanvas.getHeight()/2.0 + (height * ((1-heightMultiplier))/2.0));
        Point armPoint = new Point(centerOfSprite.x + headToArm.getXComp(), centerOfSprite.y + headToArm.getYComp());
        Point forwardThighPoint = new Point(centerOfSprite.x + headToForwardThigh.getXComp(), centerOfSprite.y + headToForwardThigh.getYComp());
        Vector forwardThighVector = thighToForwardShin.rotate(forwardThighAngle);
        Point forwardShinPoint = new Point(forwardThighPoint.x + forwardThighVector.getXComp(), forwardThighPoint.y + forwardThighVector.getYComp());
        Vector forwardShinVector = shinToForwardFoot.rotate(forwardThighAngle + forwardShinAngle);
        Point forwardFootPoint = new Point(forwardShinPoint.x + forwardShinVector.getXComp(), forwardShinPoint.y + forwardShinVector.getYComp());
        
        Point rearThighPoint = new Point(centerOfSprite.x + headToRearThigh.getXComp(), centerOfSprite.y + headToRearThigh.getYComp());
        Vector rearThighVector = thighToRearShin.rotate(rearThighAngle);
        Point rearShinPoint = new Point(rearThighPoint.x + rearThighVector.getXComp(), rearThighPoint.y + rearThighVector.getYComp());
        Vector rearShinVector = shinToRearFoot.rotate(rearThighAngle + rearShinAngle);
        Point rearFootPoint = new Point(rearShinPoint.x + rearShinVector.getXComp(), rearShinPoint.y + rearShinVector.getYComp());
        
        if(!aimingTooLow)
            rearArm.drawNormally(g, armPoint, armAngle);
        rearThigh.drawNormally(g, rearThighPoint, rearThighAngle);
        rearShin.drawNormally(g, rearShinPoint, rearThighAngle + rearShinAngle);
        rearFoot.drawNormally(g, rearFootPoint, rearThighAngle + rearShinAngle + rearFootAngle);
        head.drawNormally(g, new Point(playerCanvas.getWidth()/2.0, playerCanvas.getHeight()/2.0), 0);
        forwardThigh.drawNormally(g, forwardThighPoint, forwardThighAngle);
        forwardShin.drawNormally(g, forwardShinPoint, forwardShinAngle + forwardThighAngle);
        forwardFoot.drawNormally(g, forwardFootPoint, forwardThighAngle + forwardShinAngle + forwardFootAngle);
        if(!aimingTooLow)
            forwardArm.drawNormally(g, armPoint, armAngle);
        else
            armNoHand.drawNormally(g, armPoint, armAngle);
        Portal[] intersectingPortals = portals.getDominantAndOtherPortalOfBody(this);
        if(intersectingPortals == null) {
            forwardArm.drawNormallyCustomImage(graphics, c, getCenterOfMass(), orientation, playerCanvas);
        }
        else {
            forwardArm.drawCustumSpriteSplitInPortal(graphics, c, getCenterOfMass(), orientation, intersectingPortals[0], intersectingPortals[1], portals, playerCanvas);
        }
        //drawHoldBeam(graphics, c);
    }
    
    public void draw(Graphics g, Camera c) {
        //super.draw(g, c);
        drawPortals(g, c);
        
//        if(daray != null) {
//            
//            daray.draw(g, Color.white, c);
//            g.setColor(Color.BLACK);
//            g.fillOval((int) (daPoint.x - 6 - c.getPosition().x + c.getWidth()), (int) (daPoint.y - 6 - c.getPosition().y + c.getHeight()), 12, 12);
//            g.setColor(Color.WHITE);
//            g.drawOval((int) (daPoint.x - 4 - c.getPosition().x + c.getWidth()), (int) (daPoint.y - 4 - c.getPosition().y + c.getHeight()), 8, 8);
//            g.setColor(Color.white);
//            grabTool.draw(g, c);
//        }
    }
    
    public RigidBodyLine getFootLine() {
        return getCollisionLines()[2];
    }
    
    public void setOrientation(double d) {
        super.setOrientation(d);
        timeWaited = 0;
    }
    
    public boolean getLastDrawPriority() {
        return false;
    }
    
    public void correctOrientation(double dt) {
        if(getDead())
            return;
        if(timeWaited < timeDelay) {
            timeWaited += dt;
            return;
        }
        if(getAngularVelocity() == 0 && (getOrientation() != 0)) {
            if(getOrientation() < Math.PI)
                setAngularVelocity(-orientationCorrectionSpeed);
            else
                setAngularVelocity(orientationCorrectionSpeed);
        }
        if(getAngularVelocity() != 0 && (getOrientation() < dt * orientationCorrectionSpeed || getOrientation() > 2 * Math.PI - dt * orientationCorrectionSpeed)) {
            rotateAccuratly(-getOrientation());
            setOrientation(0);
            setAngularVelocity(0);
        }
    }
    
    private void rotateAccuratly(double angle) {
        double newAngle = getOrientation() + angle;
        orientation = 0;
        RigidBodyPoint[] points = getNodes();
        points[0].x = getCenterOfMass().x - width/2.0;
        points[0].y = getCenterOfMass().y - height/2.0;
        points[1].x = points[0].x + width;
        points[1].y = points[0].y;
        points[2].x = points[1].x;
        points[2].y = points[1].y + height;
        points[3].x = points[0].x;
        points[3].y = points[0].y + height;
        
        rotate(newAngle);
        resetCollisionStructure(points);
    }
    
    private void applyImpulse(Point p, Vector impulse) {
        setVelocity(getVelocity().add(impulse.multiplyByScalar(getInverseMass())));
    }
    
    public double getInverseMommentOfInertia() {
        return (getDead())? 1.0/this.getMommentOfInertia() : 0;
    }
}
