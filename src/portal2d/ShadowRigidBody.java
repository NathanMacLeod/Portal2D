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
public class ShadowRigidBody extends RigidBody {
    private ArrayList<Impulse> impulses;
    private ArrayList<Vector> translations;
    private RigidBody baseBody;
    private Line footLine = null;
    private boolean mainBody;
    private PortalPair portalPair;
    private Portal associatedPortal;
    private Portal portalPartner;
    
    public ShadowRigidBody(RigidBody base, RigidBodyPoint[] nodes, Point centerOfMass, Vector velocity, double angularVelocity, double mass, double rotationalInertia, double friction, double resistution, boolean fixed, int ID, Color color, boolean drawCenterOfMass, boolean mainBody, PortalPair portalPair, Portal p1, Portal p2) {
        super(nodes, centerOfMass, velocity, angularVelocity, mass, rotationalInertia, friction, resistution, fixed, ID, color, drawCenterOfMass);
        impulses = new ArrayList<>();
        translations = new ArrayList<>();
        baseBody = base;
        this.portalPair = portalPair;
        associatedPortal = p1;
        portalPartner = p2;
        this.mainBody = mainBody;
    }
    
    public Portal getAssocaitedPotal() {
        return associatedPortal;
    }
    
    public Portal getAssociatedPortalPartner() {
        return portalPartner;
    }
    
    public PortalPair getPortalPair() {
        return portalPair;
    }
    
    public boolean hasFootLine() {
        return footLine != null;
    }
    
    public Line getFootLine() {
        return footLine;
    }
    
    public void giveFootLine(Line l) {
        footLine = l;
    }
    
    public RigidBody getBase() {
        return baseBody;
    }
    
    public boolean getMainBody() {
        return mainBody;
    }
    
    public void push(Vector v) {
        super.push(v);
        translations.add(v);
    }
    
    public void applyImpulseOnPoint(Point p, Vector impulse) {
        super.applyImpulseOnPoint(p, impulse);
        impulses.add(new Impulse(p, impulse));
    }
    
    public ArrayList<Vector> getTranslations() {
        return translations;
    }
    
    public ArrayList<Impulse> getImpulses() {
        return impulses;
    }
    
    public void draw(Graphics g, Camera c) {
        if(getBase() instanceof SpritedEntity)
            return;
        super.draw(g, c);
            //((SpritedEntity)getBase()).getSprite().drawSpriteSplitInPortal(g, c, ((SpritedEntity)getBase()).getDrawPosition(), getBase().getOrientation(), associatedPortal, getAssociatedPortalPartner(), portalPair);
        if(getBase() instanceof PlayerHitbox && getMainBody()) {
            ((PlayerHitbox)getBase()).drawPortals(g, c);
        }
    }
}
