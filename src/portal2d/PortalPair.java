package portal2d;

/*
 * File added by Nathan MacLeod 2019
 */
import java.util.ArrayList;
import java.awt.Color;
import java.awt.Graphics;
/**
 *
 * @author Nathan
 */
public class PortalPair {
    private Portal[] portalPair;
    private Color[] correspondingPortalColors;
    private ArrayList<BrokenBody> bodiesInPortal;
    private PhysicsPanel physicsEngine;
    private ArrayList<RigidBody> bodiesLastInPortal;
    
    public PortalPair(PhysicsPanel p) {
        portalPair = new Portal[2];
        correspondingPortalColors = new Color[] {new Color(0, 145, 255), new Color(255, 106, 0)};
        bodiesInPortal = new ArrayList<>();
        physicsEngine = p;
        bodiesLastInPortal = new ArrayList<>();
    }
    
    public void resolveBodies() {
        while(bodiesInPortal.size() > 0) {
            BrokenBody b = bodiesInPortal.get(0);
            for(Impulse i : b.baseShadowBody.getImpulses())
                b.body.applyImpulseOnPoint(i.point, i.impulse);
            for(Vector v : b.baseShadowBody.getTranslations()) {
                b.body.translate(v);
            }
            for(Impulse i : b.clippedShadowBody.getImpulses()) {
                Vector impulse = redirectVector(i.impulse, b.secondaryPortal, b.primaryPortal);
                Point p = new Point(i.point.x, i.point.y);
                teleportPointsToOtherPortal(new Point[] {p}, b.secondaryPortal, b.primaryPortal);
                b.body.applyImpulseOnPoint(p, impulse);
            }
            for(Vector v : b.clippedShadowBody.getTranslations()) {
                b.body.translate(redirectVector(v, b.secondaryPortal, b.primaryPortal));
            }
            
            physicsEngine.removeBody(b.clippedShadowBody);
            physicsEngine.removeBody(b.baseShadowBody);
            physicsEngine.addBody(b.body);
            bodiesInPortal.remove(0);
            bodiesLastInPortal.add(b.body);
        }
    }
    
    public void splitBodiesInPortal(ArrayList<RigidBody> allBodies) {
        for(int i = 0; i < allBodies.size(); i++) {
            RigidBody b = allBodies.get(i);
            b.setPassedThroughPortal(false);
        }
        if(portalPair[0] == null || portalPair[1] == null)
            return;
        for(int i = 0; i < allBodies.size(); i++) {          
            RigidBody b = allBodies.get(i);
            if(b.getFixed())
                continue;
            
            Portal intersectingPortal = null;
            Portal otherPortal = null;
            boolean intersect1 = false;
            boolean intersect2 = false;
            
            
            if(b.lineIntersectsBodyTwice(portalPair[0].getPortalSurface()) || (bodiesLastInPortal.contains(b) && b.lineIntersectsBody(portalPair[0].getPortalSurface()))) {
                intersect1 = true;
            }
            if(b.lineIntersectsBodyTwice(portalPair[1].getPortalSurface()) || (bodiesLastInPortal.contains(b) && b.lineIntersectsBody(portalPair[1].getPortalSurface()))) {
                intersect2 = true; 
            }
            
            if(intersect1 && intersect2) {
                double distTo1 = new Vector(b.getCenterOfMass().x - portalPair[0].getCenter().x, b.getCenterOfMass().y - portalPair[0].getCenter().y).getSquaredMagnitude();
                double distTo2 = new Vector(b.getCenterOfMass().x - portalPair[1].getCenter().x, b.getCenterOfMass().y - portalPair[1].getCenter().y).getSquaredMagnitude();
                if(distTo1 < distTo2)
                    intersect2 = false;
                else
                    intersect1 = false;
            }
            
            if(intersect1) {
                intersectingPortal = portalPair[0];
                otherPortal = portalPair[1];
            }
            else if(intersect2) {
                intersectingPortal = portalPair[1];
                otherPortal = portalPair[0];
            }
            else {
                continue;
            }
            
            if(new Vector(b.getCenterOfMass().x - intersectingPortal.getCenter().x, b.getCenterOfMass().y - intersectingPortal.getCenter().y).dotProduct(intersectingPortal.getDirection()) < 0) {
                if(b instanceof PlayerHitbox && Math.abs(intersectingPortal.getDirection().getXComp()) > 0.99 && Math.abs(otherPortal.getDirection().getYComp()) > 0.99) {
                    b.translate(new Vector(0, -1));
                }
                b.setPassedThroughPortal(true);
                teleportPointsToOtherPortal(b.getAllPointData(), intersectingPortal, otherPortal);
                b.setVelocity(redirectVector(b.getVelocity(), intersectingPortal, otherPortal));
                b.setOrientation(redirectVector(new Vector(Math.cos(b.getOrientation() - Math.PI/2.0), Math.sin(b.getOrientation() - Math.PI/2.0)), intersectingPortal, otherPortal).getDirection() + Math.PI/2.0);
                if(needsXRotation(intersectingPortal, otherPortal)) {
                    b.setAngularVelocity(-b.getAngularVelocity());
                }
                Portal temp = intersectingPortal;
                intersectingPortal = otherPortal;
                otherPortal = temp;
                
                double minExitVelocity = (b instanceof EnergyPellet)? 0 : 450;
                if(b instanceof PlayerHitbox || b instanceof SpritedEntity) {
                    if(b instanceof PlayerHitbox) {
                        ((PlayerHitbox)b).setAgileFloatTime(1);
                        minExitVelocity = 650;
                    }
                    if(needsXRotation(intersectingPortal, otherPortal)) {
                        if(b instanceof PlayerHitbox && Math.abs(otherPortal.getDirection().getYComp()) < 0.01 && Math.abs(intersectingPortal.getDirection().getYComp()) < 0.01)
                            ((PlayerHitbox)b).indicateFlippedControls();
                        if(b instanceof Turret) {
                            ((Turret)b).flipOrientation();
                        }
                        if(b instanceof SpritedEntity)
                            ((SpritedEntity)b).flipSprite();
                    }
                }
                if(physicsEngine.getPlayer() != null && physicsEngine.getPlayer().getObjectIsHeld(b)) {
                    minExitVelocity = 0;
                }
                minExitVelocity *= Math.abs(Math.sin(intersectingPortal.getDirection().getDirection()));
                double exitVelocity = b.getVelocity().dotProduct(intersectingPortal.getDirection());
                if(Math.abs(intersectingPortal.getDirection().getXComp()) != 1 && exitVelocity > 0 && exitVelocity < minExitVelocity) {
                    b.setVelocity(b.getVelocity().add(intersectingPortal.getDirection().multiplyByScalar(minExitVelocity - exitVelocity)));
                }
                b.reEvaluateLineNormalDirection();
            }
            
            bodiesInPortal.add(new BrokenBody(b, intersectingPortal, otherPortal, this));
        }
        for(BrokenBody b : bodiesInPortal) { 
            allBodies.remove(b.body);
            allBodies.add(b.clippedShadowBody);
            allBodies.add(b.baseShadowBody);
        }
        bodiesLastInPortal.clear();
    }
    
    public boolean getBodyInPortal(RigidBody b) {
        for(BrokenBody body : bodiesInPortal) {
            if(body.body.equals(b)) {
                return true;
            }
        }
        return false;
    }
    
    public Portal[] getDominantAndOtherPortalOfBody(RigidBody b) {
        //Returns array with primary and secondary portal in that order
        for(BrokenBody body : bodiesInPortal) {
            if(body.body.equals(b)) {
                return new Portal[] {body.primaryPortal, body.secondaryPortal};
            }
        }
        return null;
    }
    
    public Color getPortalColor(int n) {
        return correspondingPortalColors[n];
    }
    
    public boolean portalSystemActive() {
        return portalPair[0] != null  && portalPair[1] != null;
    }
    
    public void openPortal(int n, Portal p) {
        if(portalPair[0] != null && portalPair[1] != null) {
            closePortal(n);
        }
        portalPair[n] = p;
        if(portalPair[0] != null && portalPair[1] != null) {
            portalPair[0].indentBodySurface();
            portalPair[1].indentBodySurface();
        }
        
    }
    
    public Portal[] getPortals() {
        return portalPair;
    }
    
    private void pushBodiesOutOfPortals(int closingPortal) {
        for(BrokenBody b : bodiesInPortal) {
            if(!b.primaryPortal.equals(portalPair[closingPortal]))
                continue;
            Portal port = b.primaryPortal;
            double depth = 0;
            for(Point p : b.body.getNodes()) {
                if(new Vector(p.x - port.getCenter().x, p.y - port.getCenter().y).dotProduct(port.getDirection()) < 0) {
                    double pDepth = port.getPortalSurface().getSquaredDistToLine(p);
                    if(pDepth > depth) {
                        depth = pDepth;
                    }
                }
            }
            b.body.translate(port.getDirection().multiplyByScalar(Math.sqrt(depth) + 1));
        }
    }
    
    public void closePortal(int n) {        
        if(portalPair[0] != null && portalPair[1] != null)
            pushBodiesOutOfPortals(n);
        
        if(portalPair[1] != null)
            portalPair[1].removePortalFromSurface();
        if(portalPair[0] != null)
            portalPair[0].removePortalFromSurface();
        
        portalPair[n] = null;
    }
    
    public class BrokenBody {
        RigidBody body;
        ShadowRigidBody baseShadowBody;
        ShadowRigidBody clippedShadowBody;
        Portal primaryPortal;
        Portal secondaryPortal;
        PortalPair portalPair;
        
        public BrokenBody(RigidBody body, Portal primaryPortal, Portal secondaryPortal, PortalPair portalPair) {
            this.body = body;
            this.primaryPortal = primaryPortal;
            this.secondaryPortal = secondaryPortal;
            this.portalPair = portalPair;
            clipBody();
        }
        
        private void clipBody() {
            //RigidBodyPoint[] nodes, Point centerOfMass, double mass, double rotationalInertia, double friction, double resistution, boolean fixed, int ID, Color color, boolean drawCenterOfMass
            RigidBodyPoint[] baseNodes = CollisionSubBody.clipBody(body.getOutLine(), primaryPortal.getPortalSurface(), primaryPortal.getDirection()).getNodes();
            RigidBodyPoint[] shadowNodes = CollisionSubBody.clipBody(body.getOutLine(), primaryPortal.getPortalSurface(), primaryPortal.getDirection().multiplyByScalar(-1)).getNodes();
            baseShadowBody = new ShadowRigidBody(body, baseNodes, new Point(body.getCenterOfMass().x, body.getCenterOfMass().y), body.getVelocity(), body.getAngularVelocity(),   body.getMass(), body.getMommentOfInertia(), body.getFrictionCoefficent(), body.getResistution(), false, physicsEngine.giveID(), body.getColor(), false, true, portalPair, primaryPortal, secondaryPortal);
            clippedShadowBody = new ShadowRigidBody(body, shadowNodes, new Point(body.getCenterOfMass().x, body.getCenterOfMass().y), body.getVelocity(), body.getAngularVelocity(), body.getMass(), body.getMommentOfInertia(), body.getFrictionCoefficent(), body.getResistution(), false, physicsEngine.giveID(), body.getColor(), false, false, portalPair, secondaryPortal, primaryPortal);
            
            if(body instanceof PlayerHitbox) {
                PlayerHitbox b = (PlayerHitbox)body;
                for(RigidBodyLine l : baseShadowBody.getCollisionLines()) 
                    if(l.getNormalUnitVector().basicallyIs(b.getFootLine().getNormalUnitVector()))
                        baseShadowBody.giveFootLine(l);
                for(RigidBodyLine l : clippedShadowBody.getCollisionLines())
                    if(l.getNormalUnitVector().basicallyIs(b.getFootLine().getNormalUnitVector()))
                        clippedShadowBody.giveFootLine(l);
            }
            teleportPointsToOtherPortal(clippedShadowBody.getAllPointData(), primaryPortal, secondaryPortal);
            clippedShadowBody.setVelocity(redirectVector(body.getVelocity(), primaryPortal, secondaryPortal));
            clippedShadowBody.setAngularVelocity(-body.getAngularVelocity());
            clippedShadowBody.addToCollidedWithList(secondaryPortal.getBody().getID());
            clippedShadowBody.reEvaluateLineNormalDirection();
        }
    }
    
    public Point teleportPointToOtherPortal(Point p, Portal enterPortal, Portal exitPortal) {
        Point enterDownP = enterPortal.getDownPoint();
        double downDist = new Vector(p.x - enterDownP.x, p.y - enterDownP.y).dotProduct(enterPortal.getDownVector());
        Point enterCenterP = enterPortal.getCenter();
        double directDist = new Vector(p.x - enterCenterP.x, p.y - enterCenterP.y).dotProduct(enterPortal.getDirection());

        Vector relativePos = exitPortal.getDownVector().multiplyByScalar(downDist).add(exitPortal.getDirection().multiplyByScalar(-directDist));

        return new Point(exitPortal.getDownPoint().x + relativePos.getXComp(), exitPortal.getDownPoint().y + relativePos.getYComp());
    }
    
    public void teleportPointsToOtherPortal(Point[] points, Portal enterPortal, Portal exitPortal) {
        for(Point p : points) {
            Point newP = teleportPointToOtherPortal(p, enterPortal, exitPortal);
            p.x = newP.x;
            p.y = newP.y;
        }
    }
    
    private double getPortalRotation(Portal enterPortal, Portal exitPortal) {
        double exitDirection = (exitPortal.getDownVector().getDirection());
        double enterDirection = enterPortal.getDownVector().getDirection();
        return exitDirection - enterDirection;
    }
    
    public boolean needsXRotation(Portal enterPortal, Portal exitPortal) {
        double theta = getPortalRotation(enterPortal, exitPortal);
        Vector v = enterPortal.getDirection();
        double newX = Math.cos(theta) * v.getXComp() - Math.sin(theta) * v.getYComp();
        double newY = Math.sin(theta) * v.getXComp() + Math.cos(theta) * v.getYComp();
        Vector newDirect = new Vector(newX, newY);
        return newDirect.dotProduct(exitPortal.getDirection()) > 0;
    }
    
    public Vector redirectVector(Vector v, Portal enterPortal, Portal exitPortal) {
        double theta = getPortalRotation(enterPortal, exitPortal);
        if(needsXRotation(enterPortal, exitPortal))
            v = v.add(enterPortal.getDirection().multiplyByScalar(-2 * enterPortal.getDirection().dotProduct(v)));
        double newX = Math.cos(theta) * v.getXComp() - Math.sin(theta) * v.getYComp();
        double newY = Math.sin(theta) * v.getXComp() + Math.cos(theta) * v.getYComp();
        return new Vector(newX, newY);
    }
    
    public void draw(Graphics g, Camera c) {
        for(Portal p : portalPair) {
            if(p != null)
                p.draw(g, c);
        }
    }
}
