package portal2d;
/*
 * File added by Nathan MacLeod 2019
 */
import java.awt.Color;
/**
 *
 * @author Nathan
 */
public class RigidRectangle extends RigidBody {
    
    public RigidRectangle(double x, double y, double width, double height, Vector velocity, double angularVelocity, double mass, double resistution, double frictionCoefficent, boolean fixed, int ID, double initOrientation) {
        super(new RigidBodyPoint[]{new RigidBodyPoint(x, y), new RigidBodyPoint(x + width, y), new RigidBodyPoint(x + width, y + height), new RigidBodyPoint(x, y + height)}, velocity, angularVelocity, mass, resistution, frictionCoefficent, fixed, ID, Color.RED, false);
        rotate(initOrientation);
    }
    
    protected void calculateInertia() {
        Point[] nodes = getNodes();
        Point centerOfMass = new Point((nodes[0].x + nodes[1].x)/2.0, (nodes[1].y + nodes[3].y)/2.0);
        setCenterOfMass(centerOfMass);
        double width = Math.abs(nodes[0].x - nodes[1].x);
        double height = Math.abs(nodes[0].y - nodes[3].y);
        setMommentOfInertia(getMass() * (Math.pow(width, 2) + Math.pow(height, 2))/12.0);
    }
    
}
