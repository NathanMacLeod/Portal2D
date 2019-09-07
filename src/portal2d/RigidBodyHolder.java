/*
 * File added by Nathan MacLeod 2019
 */
package portal2d;

import java.awt.Graphics;
import java.awt.Color;

/**
 *
 * @author Nathan
 */
public class RigidBodyHolder {
    private Point origin;
    private double maxHoldDist;
    private SegmentableRay holdingRay;
    private PortalPair portalSystem;
    private RigidBody heldBody;
    private Point grabPoint;
    private boolean heldThroughPortal;
    private Portal enterPortal;
    private Portal exitPortal;
    private Point previousHeldCOM;
    private double previousHeldOrientation;
    private double rotateSpeed = 2 * Math.PI/3;
    
    public RigidBodyHolder(Point origin, double maxHoldDist, PortalPair portalSystem) {
        this.origin = origin;
        this.portalSystem = portalSystem;
        this.maxHoldDist = maxHoldDist;
    }
    
    public boolean getHeldThroughPortal() {
        return heldThroughPortal;
    }
    
    public Portal getEnterPortal() {
        return enterPortal;
    }
    
    public RigidBody getHeldBody() {
        return heldBody;
    }
    
    public Point getHoldPoint() {
        return grabPoint;
    }
    
    public void releaseObject() {
        if(heldBody == null)
            return;
        heldBody.setVelocity(heldBody.getVelocity().multiplyByScalar(0.4));
        heldBody.setMass(heldBody.getMass() * 1000);
        heldBody = null;
    }
    
    public boolean isHoldingObject() {
        return heldBody != null;
    }
    
    public void pickUpBody(RigidBody b, Point p) {
        if(b instanceof EnergyPellet)
            return;
        releaseObject();
        if(!(b instanceof ShadowRigidBody)) {
            heldBody = b;
            grabPoint = p;
        }
        else if(((ShadowRigidBody)b).getMainBody()) {
            heldBody = ((ShadowRigidBody)b).getBase();
            grabPoint = p;
        }
        else {
            ShadowRigidBody sb = (ShadowRigidBody)b;
            heldBody = sb.getBase();
            grabPoint = portalSystem.teleportPointToOtherPortal(p, sb.getAssocaitedPotal(), sb.getAssociatedPortalPartner());
        }
        heldBody.setMass(heldBody.getMass() / 1000.0);
        previousHeldCOM = new Point(heldBody.getCenterOfMass().x, heldBody.getCenterOfMass().y);
        previousHeldOrientation = heldBody.getOrientation();
    }
    
    public void updateOrigin(Point p) {
        origin = p;
    }
    
    private void updateHoldingRay() {
        Vector originToGrabPoint = new Vector(grabPoint.x - origin.x, grabPoint.y - origin.y);
        SegmentableRay simpleGrab = new SegmentableRay(new Ray(originToGrabPoint, new Point(origin.x, origin.y)));
        
        if(portalSystem.portalSystemActive()) {
            Point portal1Center = portalSystem.getPortals()[0].getCenter();
            Point portal2Center = portalSystem.getPortals()[1].getCenter();

            double grabPointDistPortal1 = Math.pow(grabPoint.x - portal1Center.x, 2) + Math.pow(grabPoint.y - portal1Center.y, 2);
            double grabPointDistPortal2 = Math.pow(grabPoint.x - portal2Center.x, 2) + Math.pow(grabPoint.y - portal2Center.y, 2);

            Portal grabEnterPortal = (grabPointDistPortal1 < grabPointDistPortal2)? portalSystem.getPortals()[0] : portalSystem.getPortals()[1];
            Portal grabExitPortal = (portalSystem.getPortals()[0] == grabEnterPortal)? portalSystem.getPortals()[1] : portalSystem.getPortals()[0];

            Point grabPointPortal = portalSystem.teleportPointToOtherPortal(grabPoint, grabEnterPortal, grabExitPortal);
            Vector originToGrabPortal = new Vector(grabPointPortal.x - origin.x, grabPointPortal.y - origin.y);
            SegmentableRay portalGrab = new SegmentableRay(new Ray(originToGrabPortal, new Point(origin.x, origin.y)));

            if(originToGrabPortal.getSquaredMagnitude() < originToGrabPoint.getSquaredMagnitude()) {
                portalGrab.segmentRay(portalSystem);
                if(portalGrab.getSegments().size() > 0) {
                    heldThroughPortal = true;
                    enterPortal = grabExitPortal;
                    exitPortal = grabEnterPortal;
                    holdingRay = portalGrab;
                    return;
                }
            }
        }
        heldThroughPortal = false;
        holdingRay = simpleGrab;
    }
    
    private void updateGrabPointPosition() {
        if(heldBody == null)
        return;
        
        Point centerOfMass = heldBody.getCenterOfMass();
        
        if(heldBody.passedThroughPortal()) {
            Point portal1Center = portalSystem.getPortals()[0].getCenter();
            Point portal2Center = portalSystem.getPortals()[1].getCenter();

            double grabPointDistPortal1 = Math.pow(grabPoint.x - portal1Center.x, 2) + Math.pow(grabPoint.y - portal1Center.y, 2);
            double grabPointDistPortal2 = Math.pow(grabPoint.x - portal2Center.x, 2) + Math.pow(grabPoint.y - portal2Center.y, 2);

            Portal grabEnterPortal = (grabPointDistPortal1 < grabPointDistPortal2)? portalSystem.getPortals()[0] : portalSystem.getPortals()[1];
            Portal grabExitPortal = (portalSystem.getPortals()[0] == grabEnterPortal)? portalSystem.getPortals()[1] : portalSystem.getPortals()[0];
            
            portalSystem.teleportPointsToOtherPortal(heldBody.getAllPointData(), grabExitPortal, grabEnterPortal);
            
            grabPoint.x += centerOfMass.x - previousHeldCOM.x;
            grabPoint.y += centerOfMass.y - previousHeldCOM.y;

            double theta = heldBody.getOrientation() - previousHeldOrientation;


            double rotX = Math.cos(theta) * (grabPoint.x - centerOfMass.x) - Math.sin(theta) * (grabPoint.y - centerOfMass.y);
            double rotY = Math.sin(theta) * (grabPoint.x - centerOfMass.x) + Math.cos(theta) * (grabPoint.y - centerOfMass.y);

            grabPoint.x = centerOfMass.x + rotX;
            grabPoint.y = centerOfMass.y + rotY;
            
            portalSystem.teleportPointsToOtherPortal(heldBody.getAllPointData(), grabEnterPortal, grabExitPortal);
            grabPoint = portalSystem.teleportPointToOtherPortal(grabPoint, grabEnterPortal, grabExitPortal);
        }
        else {
            grabPoint.x += centerOfMass.x - previousHeldCOM.x;
            grabPoint.y += centerOfMass.y - previousHeldCOM.y;

            double theta = heldBody.getOrientation() - previousHeldOrientation;


            double rotX = Math.cos(theta) * (grabPoint.x - centerOfMass.x) - Math.sin(theta) * (grabPoint.y - centerOfMass.y);
            double rotY = Math.sin(theta) * (grabPoint.x - centerOfMass.x) + Math.cos(theta) * (grabPoint.y - centerOfMass.y);

            grabPoint.x = centerOfMass.x + rotX;
            grabPoint.y = centerOfMass.y + rotY;
        }
        previousHeldCOM = new Point(centerOfMass.x, centerOfMass.y);
        previousHeldOrientation = heldBody.getOrientation();
    }
    
    private void rotateObject(int direction) {
        if(heldBody == null)
            return;
        heldBody.setAngularVelocity(0);
        heldBody.setAngularVelocity(direction * rotateSpeed);
    }
    
    private void pullOnObject(double dt) {
        if(heldBody == null)
            return;
            
            heldBody.breakRest(false);
        
            double accelerationScale = 400;
            Vector v = holdingRay.getTerminalRay().vector.multiplyByScalar(-1);
            double distMagnitudeFactor = holdingRay.getBaseRay().vector.getMagnitude();
            if(distMagnitudeFactor > 75)
                distMagnitudeFactor = 75;
            Vector impulse = v.getUnitVector().multiplyByScalar(accelerationScale * dt * heldBody.getMass() * distMagnitudeFactor);
            if(heldThroughPortal && holdingRay.getSegments().size() == 1) {
                impulse = portalSystem.redirectVector(impulse, enterPortal, exitPortal);
            }
            heldBody.applyImpulseOnPoint(grabPoint, impulse);
            double dampen = 1 - 0.25 * dt/0.01;
            heldBody.setVelocity(heldBody.getVelocity().multiplyByScalar(dampen));
            //dampen = 1 - 0.25 * dt * 10;
            //heldBody.setAngularVelocity(heldBody.getAngularVelocity() * dampen);
            heldBody.setAngularVelocity(0);
    }

    public void update(double dt, int direction) {
        if(heldBody == null)
            return;
        heldBody.breakRest(false);
        updateGrabPointPosition();
        updateHoldingRay();
        if(holdingRay.getBaseRay().vector.getMagnitude() > maxHoldDist)
            releaseObject();
        else {
            pullOnObject(dt);
            rotateObject(direction);
        }
    }
    
    public void draw(Graphics g, Camera c) {
        if(holdingRay == null || heldBody == null)
            return;
        holdingRay.draw(g, Color.GREEN, c);
        g.setColor(Color.WHITE);
        g.fillOval((int) (grabPoint.x - 6 - c.getPosition().x + c.getWidth()), (int) (grabPoint.y - 6 - c.getPosition().y + c.getHeight()), 12, 12);
        g.setColor(Color.MAGENTA);
        g.drawOval((int) (grabPoint.x - 4 - c.getPosition().x + c.getWidth()), (int) (grabPoint.y - 4 - c.getPosition().y + c.getHeight()), 8, 8);
        g.setColor(Color.white);
    }
    
}
