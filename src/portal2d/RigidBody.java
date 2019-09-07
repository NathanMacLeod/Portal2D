package portal2d;
/*
 * File added by Nathan MacLeod 2019
 */
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Polygon;
import java.util.ArrayList;
/**
 *
 * @author Nathan
 */
public class RigidBody {
    private RigidBodyPoint[] nodes;
    private RigidBodyLine[] lines;
    private RigidBodyLine[] collisionLines;
    private CollisionSubBody outline;
    private ArrayList<Point[]> nonCollisionLines; 
    private Point previousLocation;
    private double previousOrientation;
    private double maxTimeBack;
    private Point centerOfMass;
    private Vector velocity;
    private double angularVelocity;
    protected double orientation;
    private double mass;
    private double resistution;
    protected double mommentOfInertia;
    private double frictionCoefficent;
    private double redShade = 0;
    private double greenShade = 0;
    private double bluShade = 0;
    private boolean atRest = false;
    private boolean readyToRest = false;
    private ArrayList<Integer> collidedWithList;
    private ArrayList<Integer> nonCollisionList;
    private ArrayList<RigidBody> restBuddies;
    private double restCounter = 0;
    private double restTime = 0.5;
    private Point restReference;
    private double angleReference;
    private boolean fixed;
    private boolean drawCOM;
    private boolean restPointLeft;
    private boolean restPointRight;
    private Color color;
    private int ID;
    private boolean passedThroughPortal = false;
    private boolean nonRotatable = false;
    private boolean resistutionSupremacy = false;
    private boolean portableSurface = false;
    private boolean pushable = true;
    private boolean dontDraw = false;
    private boolean collisionImmune = false;
    
    public RigidBody(RigidBodyPoint[] nodes, Vector velocity, double angularVelocity, double density, double resistution, double frictionCoefficent, boolean fixed, int ID, Color color, boolean drawCOM) {
        this.nodes = nodes;
        this.velocity = velocity;
        restBuddies = new ArrayList<>();
        nonCollisionLines = new ArrayList<>();
        nonCollisionList = new ArrayList<>();
        this.angularVelocity = angularVelocity;
        orientation = 0;
        outline = new CollisionSubBody(nodes);
        //findSubBodies();
        setPointsBody();
        createLines(false);
        calculateInertia(density);
        this.resistution = resistution;
        this.fixed = fixed;
        this.frictionCoefficent = frictionCoefficent;
        collidedWithList = new ArrayList<>();
        this.ID = ID;
        this.color = color;
        this.drawCOM = drawCOM;
        nonRotatable = false;
    }
    
    public RigidBody(RigidBodyPoint[] nodes, Point centerOfMass, Vector velocity, double angularVelocity, double density, double resistution, double frictionCoefficent, boolean fixed, int ID, Color color, boolean drawCOM) {
        this(nodes, velocity, angularVelocity, density, resistution, frictionCoefficent, fixed, ID, color, drawCOM);
        this.centerOfMass = centerOfMass;
    }
    
    public RigidBody(RigidBodyPoint[] nodes, Point centerOfMass, Vector velocity, double angularVelocity, double mass, double rotationalInertia, double friction, double resistution, boolean fixed, int ID, Color color, boolean drawCenterOfMass) {
        orientation = 0;
        nonCollisionLines = new ArrayList<>();
        nonCollisionList = new ArrayList<>();
        this.angularVelocity = angularVelocity;
        this.velocity = velocity;
        restBuddies = new ArrayList<>();
        collidedWithList = new ArrayList<>();
        this.nodes = nodes;
        this.centerOfMass = centerOfMass;
        this.mass = mass;
        this.mommentOfInertia = rotationalInertia;
        this.frictionCoefficent = friction;
        this.resistution = resistution;
        this.fixed = fixed;
        this.ID = ID;
        this.color = color;
        drawCOM = drawCenterOfMass;
        outline = new CollisionSubBody(nodes);
        setPointsBody();
        createLines(false);
        nonRotatable = false;
    }
    
    public RigidBody(RigidBodyPoint[] nodes, Point centerOfMass, double mass, double rotationalInertia, double friction, double resistution, boolean fixed, int ID, Color color, boolean drawCenterOfMass, boolean portable) {
        orientation = 0;
        angularVelocity = 0;
        velocity = new Vector(0, 0);
        nonCollisionLines = new ArrayList<>();
        nonCollisionList = new ArrayList<>();
        restBuddies = new ArrayList<>();
        collidedWithList = new ArrayList<>();
        this.nodes = nodes;
        this.centerOfMass = centerOfMass;
        this.mass = mass;
        this.mommentOfInertia = rotationalInertia;
        this.frictionCoefficent = friction;
        this.resistution = resistution;
        this.fixed = fixed;
        this.ID = ID;
        this.color = color;
        drawCOM = drawCenterOfMass;
        outline = new CollisionSubBody(nodes);
        setPointsBody();
        createLines(false);
        nonRotatable = false;
        portableSurface = portable;
    }
    
    public static void setUpNonCollisionNetwork(RigidBody[] bodies) {
        for(int i = 0; i < bodies.length; i ++) {
            for(int j = 0; j < bodies.length; j++) {
                if(i != j)
                    bodies[i].addToNonCollisionList(bodies[j].getID());
            }
        }
    }
    
    public void addToNonCollisionList(int i) {
        nonCollisionList.add(i);
    }
    
    public boolean onNonCollisionList(int i) {
        return nonCollisionList.contains(i);
    }
    
    public void setDontDraw(Boolean b) {
        dontDraw = b;
    }
    
    public void applyImpulseOnPoint(Point p, Vector impulse, Object source) {
        applyImpulseOnPoint(p, impulse);
    }
    
    public void setCollisionImmune(Boolean b) {
        collisionImmune = b;
    }
    
    public boolean getCollisionImmune() {
        return collisionImmune;
    }
    
    public boolean collisionReactiveOnly() {
        return false;
    }
    
    public boolean getNonPushable() {
        return !pushable || fixed;
    }
    
    public void setPushable(Boolean b) {
        pushable = b;
    }
    
    public boolean collidedWithSomeone() {
        return collidedWithList.size() > 0;
    }
    
    public boolean hasRestBuddies() {
        return restBuddies.size() > 0;
    }
    
    public boolean getResistutionSupremacy() {
        return resistutionSupremacy;
    }
    
    public void setResistutionSupremacy(boolean b) {
        resistutionSupremacy = b;
    }
    
    public boolean getNonRotatable() {
        return nonRotatable;
    }
    
    public void setNonRotatable(boolean b) {
        nonRotatable = b;
    }
    
    public void setMass(Double d) {
        mass = d;
    }
    
    public void setPassedThroughPortal(Boolean b) {
        passedThroughPortal = b;
    }
    
    public boolean passedThroughPortal() {
        return passedThroughPortal;
    }
    
    public boolean getPortable() {
        return portableSurface;
    }
    
    public void setPortable(boolean b) {
        portableSurface = b;
    }
    
    public void setColor(Color c) {
        color = c;
    }
    
    public Color getColor() {
        return color;
    }
    
    public boolean getDrawCOM() {
        return drawCOM;
    }
    
    public void breakRest(boolean delete) {
        if((fixed && !delete) || (!atRest && restCounter == 0) || restCounter == 0)
            return;
        //System.out.println("smh");
        restCounter = 0;
        atRest = false;
        readyToRest = false;
        for(RigidBody b : restBuddies)
            b.breakRest(false);
        restBuddies.clear();
    }
    
    public boolean tryingToRest() {
        return restCounter != 0;
    }
    
    public void addRestBuddy(RigidBody b, Point pointOfRest) {
        if(pointOfRest.x < centerOfMass.x)
            restPointLeft = true;
        if(pointOfRest.x > centerOfMass.x)
            restPointRight = true;
        if(restCounter == 0 || restBuddies.contains(b))
            return;
        restBuddies.add(b);
    }
    
    public void determineIfAtRest(double dt) {
        //start trying to rest
        if(atRest || this instanceof PlayerHitbox)
            return;
        
        if(readyToRest) {
            boolean allReady = true;
            for(RigidBody b : restBuddies)
                if(!b.readyToRest && !b.atRest)
                    allReady = false;
            if(allReady) {
                atRest = true;
                for(RigidBody b : restBuddies)
                    b.atRest = true;
                return;
            }
        }
        
        //System.out.println(restCounter);
        
        if(restCounter == 0) {
            restReference = new Point(centerOfMass.x, centerOfMass.y);
            angleReference = orientation;
            restPointLeft = false;
            restPointRight = false;
            restCounter += dt;
        }
        else if(Math.abs(angularVelocity) <= 1.5 && restCounter != 0 && (getCenterDriftSquared() < 0.5 || (getCenterDriftSquared() < 3 && restBuddies.size() > 0)) && Math.abs(orientation - angleReference) < 0.05) {
            restCounter += dt;
            if(restCounter >= restTime && ((restPointLeft && restPointRight) || fixed))
                readyToRest = true;
        }
        else if(restCounter != 0) {
            breakRest(false);
        }
    }
    
    private double getCenterDriftSquared() {
        return Math.pow(centerOfMass.x - restReference.x, 2) + Math.pow(centerOfMass.y - restReference.y, 2);
    }
    
    public int getID() {
        return ID;
    }
    
    public void setOrientation(double d) {
        orientation = d;
        while(orientation >= 2 * Math.PI) 
            orientation -= 2 * Math.PI;
        while(orientation < 0)
            orientation += 2 * Math.PI;
    }
    
    public double getOrientation() {
        return orientation;
    }
    
    public void setAngularVelocity(double d) {
        angularVelocity = d;
    }
    
    public boolean getAtRest() {
        return atRest;
    }
    
    public void setAtRest(Boolean b) {
        atRest = b;
    }
    
    public void setVelocity(Vector v) {
        velocity = v;
    }
    
    public double getAngularVelocity() {
        return angularVelocity;
    }
    
    public double getFrictionCoefficent() {
        return frictionCoefficent;
    }
    
    public void addToCollidedWithList(int id) {
        collidedWithList.add(id);
    }
    
    public boolean collidedWith(int id) {
        return collidedWithList.contains(id);
    }
    
    public void clearCollidedWithList() {
        collidedWithList.clear();
    }
    
    public void setMaxTimeBack(double t) {
        maxTimeBack = t;
    }
    
    public double getMaxTimeBack() {
        return maxTimeBack;
    }
    
    private void setPointsBody() {
        for(RigidBodyPoint p : nodes)
            p.setBody(this);
    }
    
    public void push(Vector v) {
        translate(v);
    }
    
    public void applyImpulseOnPoint(Point p, Vector impulse) {
        if(fixed) {
            return;
        }
        velocity = velocity.add(impulse.multiplyByScalar(getInverseMass()));
        if(!nonRotatable)
            angularVelocity += getPerpendicularizedVectorToPoint(p).dotProduct(impulse)/mommentOfInertia;
    }
    
    public Vector getPerpendicularizedVectorToPoint(Point p) {
        Vector v = new Vector(p.x - centerOfMass.x, p.y - centerOfMass.y);
        //System.out.println(v);
        return v.perpendicularize();
    }
    
    public double getInverseMass() {
        if(fixed)
            return 0;
        return 1.0/mass;
    }
    
    public double getResistution() {
        return resistution;
    }
    
    public Vector getVelocity() {
        return velocity;
    }
    
    public Vector getLinearVelocityOfPointDueToAngularVelocity(Point p) {
        if(angularVelocity == 0)
            return new Vector(0, 0);
       
        return getPerpendicularizedVectorToPoint(p).multiplyByScalar(angularVelocity);
        
    }
    
    public void moveForwardOrBackInTime(double dt, Vector netConstantAccel) {
        
        previousLocation = centerOfMass;
        previousOrientation = orientation;
        rotateInTime(dt);
        if(fixed)
            return;
        double xDist;
        double yDist;
        if(dt < 0) {
            setVelocity(velocity.add(netConstantAccel.multiplyByScalar(dt)));
            xDist = velocity.getXComp() * dt - 0.5 * netConstantAccel.getXComp() * dt * dt;
            yDist = velocity.getYComp() * dt - 0.5 * netConstantAccel.getYComp() * dt * dt;
        }
        else {
            xDist = velocity.getXComp() * dt + 0.5 * netConstantAccel.getXComp() * dt * dt;
            yDist = velocity.getYComp() * dt + 0.5 * netConstantAccel.getYComp() * dt * dt;
            setVelocity(velocity.add(netConstantAccel.multiplyByScalar(dt)));
        }
        translate(xDist, yDist);
    }
    
    private void rotateInTime(double dt) {
        rotate(dt * angularVelocity);
    }
    
    protected void rotate(double theta) {
        orientation += theta;
        while(orientation >= 2 * Math.PI) 
            orientation -= 2 * Math.PI;
        while(orientation < 0)
            orientation += 2 * Math.PI;
        for(Point p : getAllPointData()) { 
            if(p == centerOfMass)
                continue;
            double newX = centerOfMass.x + Math.cos(theta) * (p.x - centerOfMass.x) - Math.sin(theta) * (p.y - centerOfMass.y);
            double newY = centerOfMass.y + Math.sin(theta) * (p.x - centerOfMass.x) + Math.cos(theta) * (p.y - centerOfMass.y);
            p.x = newX;
            p.y = newY;
        }
    } 
    
    public boolean getFixed() {
        return fixed;
    }
    
    public void translate(Vector v) {
        //System.out.println(v.getYComp());
        translate(v.getXComp(), v.getYComp());
    }
    
    private void translate(double distX, double distY) {
        
        for(Point p : getAllPointData()) {
//            if(distY == -0.15866312517084757)
//                System.out.println(p);
            p.x += distX;
            p.y += distY;
//            if(distY == -0.15866312517084757)
//                System.out.println(p);
        }
    }
    
//    private void findSubBodies() {
//        ArrayList<RigidBodyPoint> points = new ArrayList<>();
//        for(RigidBodyPoint p : nodes)
//            points.add(p);
//        subBodies = CollisionSubBody.breakIntoSubBodies(points);
//    }
    
    public void resetCollisionStructure(RigidBodyPoint[] r) {
        nodes = r;
        for(RigidBodyPoint p : nodes)
            p.clearLines();
        setPointsBody();
        createLines(true);
    }
    
    public void addNonCollisionLine(Point[] p) {
        nonCollisionLines.add(p);
    }
    
//    private void createLines(boolean collisionLinesOnly) {
//        if(!collisionLinesOnly)
//            lines = new RigidBodyLine[nodes.length];
//        collisionLines = new RigidBodyLine[nodes.length - nonCollisionLines.size()];
//        int collisionIndex = 0;
//        for(int i = 0; i < nodes.length; i++) {
//            RigidBodyPoint n1 = nodes[i];
//            RigidBodyPoint n2 = (i == nodes.length -1)? nodes[0] : nodes[i + 1];
//            RigidBodyLine l = new RigidBodyLine(n1, n2, this);
//            if(!collisionLinesOnly)
//                lines[i] = l;
//            boolean nonCollisionLine = false;
//            for(Point[] ps : nonCollisionLines) {
//                if((ps[0].equals(n1) || ps[0].equals(n2)) && ((ps[1].equals(n1) || ps[1].equals(n2))))
//                        nonCollisionLine = true;
//            }
//            if(!nonCollisionLine) {
//                collisionLines[collisionIndex] = l;
//            }
//            n1.addLine(collisionLines[i]);
//            n2.addLine(collisionLines[i]);
//        }
//        for(RigidBodyLine l : collisionLines) {
//            l.findNormalDirection(this);
//            //System.out.println(l.getNormalUnitVector());
//        }
//    }
    
    public void reEvaluateLineNormalDirection() {
        for(RigidBodyLine l : collisionLines) {
            l.findNormalDirection(this);
        }
    }
    
    public RigidBodyLine[] getCollisionLines() {
        return collisionLines;
    }
    
    private void createLines(boolean collisionLinesOnly) {
        if(!collisionLinesOnly)
            lines = new RigidBodyLine[nodes.length];
        collisionLines = new RigidBodyLine[nodes.length];
        for(int i = 0; i < nodes.length; i++) {
            RigidBodyLine l = (i == nodes.length - 1)? new RigidBodyLine(nodes[i], nodes[0], this) : new RigidBodyLine(nodes[i], nodes[i + 1], this);
            if(i == nodes.length - 1) {
                if(!collisionLinesOnly)
                    lines[i] = l;
                collisionLines[i] = l;
                nodes[i].addLine(collisionLines[i]);
                nodes[0].addLine(collisionLines[i]);
            }
            else {
                if(!collisionLinesOnly)
                    lines[i] = l;
                collisionLines[i] = l;
                nodes[i].addLine(collisionLines[i]);
                nodes[i + 1].addLine(collisionLines[i]);
            }
        }
        if(!collisionLinesOnly) {
            for(RigidBodyLine l : lines) {
                l.findNormalDirection(this);
                //System.out.println(l.getNormalUnitVector());
            }
        }
        for(RigidBodyLine l : collisionLines) {
            l.findNormalDirection(this);
            //System.out.println(l.getNormalUnitVector());
        }
    }
    
    private class PointMass extends Point{
        public double mass;
        
        public PointMass(double x, double y, double mass) {
            super(x, y);
            this.mass = mass;
        }
    }
    
    protected void calculateInertia(double density) {
        double[] dimensions = getDimensions();
        double[] corner = getTopLeftCorner();
        final double FIDEL_VAL = 0.5;
        double currentRect = corner[1];
        ArrayList<PointMass> pointMasses = new ArrayList<PointMass>();
        this.mass = 0;
        while(currentRect < corner[1] + dimensions[1]) {
            ArrayList<Point> intersects = new ArrayList<>();
            Line l = new Line(new Point(-Integer.MAX_VALUE, currentRect + FIDEL_VAL/2.0), new Point(Integer.MAX_VALUE, currentRect + FIDEL_VAL/2.0));
            for(int i = 0; i < lines.length; i++) {
                Point intersect = lines[i].getIntersection(l);
                if(intersect == null)
                    continue;
                for(int j = 0; j <= intersects.size(); j++) {
                    if(j == intersects.size()) {
                        intersects.add(intersect);
                        break;
                    }
                    else {
                        if(intersect.x < intersects.get(j).x) {
                            intersects.add(j, intersect);
                            break;
                        }
                    }
                }
            }
            if(intersects.size() <= 1) {
                currentRect += FIDEL_VAL;
                continue;
            }
            while(!intersects.isEmpty()) {
                double mass = (intersects.get(1).x - intersects.get(0).x) * FIDEL_VAL;
                this.mass += mass * density;
                double x = (intersects.get(1).x + intersects.get(0).x)/2.0;
                pointMasses.add(new PointMass(x, currentRect, mass));
                intersects.remove(0);
                intersects.remove(0);
            }
            currentRect += FIDEL_VAL;
        }
        double totalMass = 0;
        centerOfMass = new Point(0, 0);
        for(PointMass p : pointMasses) {
            centerOfMass.x += p.x * p.mass;
            centerOfMass.y += p.y * p.mass;
            totalMass += p.mass;
        }
        centerOfMass.x /= (totalMass);
        centerOfMass.y /= (totalMass);
        
        pointMasses.clear();
        //System.out.println(dimensions[0] + " " + dimensions[1]);
        for(double i = 0; i <= dimensions[0]; i+= FIDEL_VAL) {
            for(double j = 0; j <= dimensions[1]; j+= FIDEL_VAL) {
                if(pointInsideBody(new Point(corner[0] + i, corner[1] + j))) 
                    pointMasses.add(new PointMass(corner[0] + i, corner[1] + j, 0));
            }
        }
        mommentOfInertia = 0;
        for(PointMass p : pointMasses) 
            mommentOfInertia += (Math.pow(p.x - centerOfMass.x, 2) + Math.pow(p.y - centerOfMass.y, 2)) * mass/pointMasses.size();
    }
    
    private double[] getTopLeftCorner() {
        double left = 0;
        double top = 0;
        
        for(int i = 0; i < nodes.length; i++) {
            Point p = nodes[i];
            if(i == 0) {
                left = p.x;
                top = p.y;
                continue;
            }
            if(p.x < left)
                left =  p.x;
            if(p.y < top)
                top = p.y;
        }
        return new double[] {left, top};
    }
    
    private double[] getDimensions() {
        double left = 0;
        double right = 0;
        double top = 0;
        double bottum = 0;
        for(int i = 0; i < nodes.length; i++) {
            Point p = nodes[i];
            if(i == 0) {
                left = p.x;
                right = p.x;
                top = p.y;
                bottum = p.y;
                continue;
            }
            if(p.x > right)
                right = p.x;
            if(p.x < left)
                left =  p.x;
            if(p.y > top)
                top = p.y;
            if(p.y < bottum)
                bottum = p.y;
        }
        return new double[] {right - left, top - bottum};
    }
    
    public RigidBodyPoint[] getNodes() {
        return nodes;
    }
    
    public Point getCenterOfMass() {
        return centerOfMass;
    }
    
    public void setCenterOfMass(Point p) {
        centerOfMass = p;
    }
    
    public double getMommentOfInertia() {
        if(fixed || nonRotatable)
            return Double.MAX_VALUE;
        return mommentOfInertia;
    }
    
    public double getInverseMommentOfInertia() {
        if(fixed || nonRotatable)
            return 0;
        return 1.0/mommentOfInertia;
    }
    
    public void setMommentOfInertia(double d) {
        mommentOfInertia = d;
    }
    
    public double getMass() {
        return mass;
    }
    
    public ArrayList<Point> getPointsInside(RigidBody b) {
        //Returns the points of body b that are inside this body
        ArrayList<Point> points = new ArrayList<>();
        Point[] bNodes = b.getNodes();
        for(Point p : bNodes) {
            if(pointInsideBody(p)) 
                points.add(p);
        }
        return points;
    }
    
    public boolean hasPointsInsideEachOther(RigidBody b) {
        for(Point p : b.getNodes()) {
            if(pointInsideBody(p))
                return true;
        }
        for(Point p : nodes) {
            if(b.pointInsideBody(p))
                return true;
        }
        return false;
    }
    
    public double[] getMaxAndMinVals() {
        //returns the lowest x, y and max x, y coordinates
        double minX = 0;
        double minY = 0;
        double maxX = 0;
        double maxY = 0;
        for(int i = 0; i < nodes.length; i++) {
            Point p = nodes[i];
            if(i == 0) {
                minX = p.x;
                maxX = p.x;
                minY = p.y;
                maxY = p.y;
                continue;
            }
            if(p.x < minX)
                minX = p.x;
            if(p.x > maxX)
                maxX = p.x;
            if(p.y < minY)
                minY = p.y;
            if(p.y > maxY)
                maxY = p.y;
        }
        return new double[] {minX, minY, maxX, maxY};
    }
    
//    public Object[] getSquaredPointPenetrationDepth(Point p) {
//        double smallestDist = -1;
//        Line smallestLine = null;
//        for(Line l : lines) {
//            double dist = l.getSquaredDistToLine(p);
//            if(smallestDist == -1 || dist < smallestDist) {
//                smallestDist = dist;
//                smallestLine = l;
//            }
//        }
//        return new Object[] {smallestLine, smallestDist};
//    }
//    
//    public Object[] getDeepestPenetratingPoint(ArrayList<Point> penetratingPoints) {
//        Point deepestPoint = null;
//        Line deepestLine = null;
//        double deepestDist = -1;
//        for(Point p : penetratingPoints) {
//            Object[] penInfo = getSquaredPointPenetrationDepth(p);
//            if(deepestDist == -1 || (Double) penInfo[1] > deepestDist) {
//                deepestPoint = p;
//                deepestDist = (Double) penInfo[1];
//                deepestLine = (Line) penInfo[0];
//            }
//        }
//        return new Object[] {deepestPoint, deepestLine, deepestDist};
//    }
    
//    private Object[] getGreatestPenDepth(Line l, ArrayList<Point> points) {
//        Point deepestPoint = null;
//        double deepestDist = -1;
//        for(Point p : points) {
//            double penDepth = l.getSquaredDistToLine(p);
//            if(deepestDist == -1 || penDepth > deepestDist) {
//                deepestPoint = p;
//                deepestDist = penDepth;
//            }
//        }
//        return new Object[] {deepestPoint, deepestDist};
//    }
    
//    public Object[] getDeepestPenetratingPoint(ArrayList<Point> penetratingPoints) {
//        Point deepestPoint = null;
//        Line deepestLine = null;
//        double deepestDist = -1;
//        for(Line l : lines) {
//            Object[] penInfo = getGreatestPenDepth(l, penetratingPoints);
//            if(deepestDist == -1 || (Double) penInfo[1] < deepestDist) {
//                deepestPoint = (Point) penInfo[0];
//                deepestDist = (Double) penInfo[1];
//                deepestLine = l;
//            }
//        }
//        return new Object[] {deepestPoint, deepestLine, deepestDist};
//    }
    
    
    public RigidBodyLine getLineClosestToPoint(Point p) {
        RigidBodyLine closestLine = null;
        double closestDist = -1;

        for(RigidBodyLine l : collisionLines) {
            double dist = l.getSquaredDistToLine(p);
            if(dist < closestDist || closestDist == -1) {
                closestLine = l;
                closestDist = dist;
            }   
        }

        return closestLine;
    }
    
    public RigidBodyLine getLineClosestToPoint(Point p, boolean otherRestriction) {
        RigidBodyLine closestLine = null;
        double closestDist = -1;

        for(RigidBodyLine l : collisionLines) {
            double dist = l.getSquaredDistToLine(p);
            if(l.closestIntersectInDomain(p) && (dist < closestDist || closestDist == -1)) {
                closestLine = l;
                closestDist = dist;
            }   
        }

        return closestLine;
    }
    
    public ArrayList<RigidBodyLine> getLinesCloseToPoint(Point p, double minSquaredDist) {
        ArrayList<RigidBodyLine> closeLines = new ArrayList<>();
        for(RigidBodyLine l : collisionLines) {
            double dist = l.getSquaredDistToLine(p);
            if(dist < minSquaredDist)
                closeLines.add(l);
        }
        return closeLines;
    }
    
    public ArrayList<Point> getProblemPoints(RigidBody b, double minSquaredDist) {
        ArrayList<Point> problemPoints = new ArrayList<>();
        for(Point p : b.getNodes()) {
            double squaredDist = getLineClosestToPoint(p).getSquaredDistToLine(p);
            if(squaredDist < minSquaredDist)
                problemPoints.add(p);
        }
        return problemPoints;
    }
    
    public CollisionSubBody getOutLine() {
        return outline;
    }
    
    private ArrayList<Vector> getPotentialConcaveVectorSolutions(RigidBody b) {
        //tries combining the normal vectors of the points closest lines to find a potential solution
        ArrayList<Vector> possibleSolutions = new ArrayList<>();
//        if(true)
//            return possibleSolutions;
        ArrayList<Point> pointsForThisBody = getProblemPoints(b, 9);
        ArrayList<Point> pointsForOtherBody = b.getProblemPoints(this, 9);
        
        ArrayList<RigidBodyLine> lines = new ArrayList<>();
        for(Point p : pointsForThisBody)
            lines.addAll(getLinesCloseToPoint(p, 9));
        for(Point p : pointsForOtherBody)
            lines.addAll(b.getLinesCloseToPoint(p, 9));
        
        for(int i = 0; i < lines.size() - 1; i++) {
            RigidBodyLine l1 = lines.get(i);
            RigidBodyLine l2 = lines.get(i +  1);
            
            possibleSolutions.add(l1.getNormalUnitVector().add(l2.getNormalUnitVector()).getUnitVector());
        }
        return possibleSolutions;
    }
    
    private Object[] getPenDepth(Point p, RigidBody b, boolean forPush, boolean fuckIt) {
        double smallestDist = -1;
        Line smallestLine = null;
        
        Vector relativeVelocity = b.getLinearVelocityOfPointDueToAngularVelocity(p).add(b.getVelocity()).subtract(getLinearVelocityOfPointDueToAngularVelocity(p).add(getVelocity()));

        for(RigidBodyLine l : collisionLines) {
            double distToLine = Math.sqrt(l.getSquaredDistToLine(p));
            if(smallestDist != -1 && distToLine >= smallestDist)
                continue;
            if(!forPush && relativeVelocity.dotProduct(l.getNormalUnitVector()) > 0)
                continue;
            
            if(fuckIt) {
                smallestLine = l;
                smallestDist = distToLine;
                continue;
            }
            
            double slack = 0.1;
            Vector pointToLine = l.getNormalUnitVector().multiplyByScalar(distToLine + slack);
            b.translate(pointToLine);
            if(!b.bodiesColliding(this)) {
                smallestLine = l;
                smallestDist = distToLine;
            }
            b.translate(pointToLine.multiplyByScalar(-1));
        }
        if(smallestDist == -1)
            return null;
        return new Object[] {smallestLine, smallestDist, ((RigidBodyLine)smallestLine).getNormalUnitVector()};
    }
     
    public Point[] getAllPointData() {
        Point[] allData = new Point[nodes.length + 1];
        for(int i = 0; i < nodes.length; i++)
            allData[i] = nodes[i];
        allData[allData.length - 1] = centerOfMass;
        return allData;
    }
    
    private Object[] getPenetratingPoint(ArrayList<Point> penetratingPoints, RigidBody b, boolean forPush, Vector gravity) {
        Object[] penInfo = null;
        
        ArrayList<Object[]> infoCantidates = new ArrayList<>(); 
        
        for(int i = 0; i < penetratingPoints.size(); i++) {
            Object[] o = getPenDepth(penetratingPoints.get(i), b, forPush, false);
            if(o != null)
                infoCantidates.add(new Object[] {penetratingPoints.get(i), o[0], o[1], o[2]});
        }
        
        boolean fuckIt = false;
        boolean fuck = false;
        
        while(true) {
            
            for(Object[] o : infoCantidates) {
                if(penInfo == null || (double) o[2] < (double) penInfo[2]) {
                    penInfo = o;
                }
            }
            if((penInfo != null && (double)penInfo[2] < 10) || fuck) {
                if(penInfo == null || (!forPush && (double)penInfo[2] > 100)) {
                    return null;
                }
                return penInfo;
            }
            
            
            if(fuckIt) {
                fuck = true;
                Object[] possibleInfo = null;
                for(int i = 0; i < penetratingPoints.size(); i++) {
                    Object[] o = getPenDepth(penetratingPoints.get(i), b, forPush, true);
                    if(o == null)
                        continue;
                    if(possibleInfo == null || (double)o[1] > (double)possibleInfo[2])
                        possibleInfo = new Object[] {penetratingPoints.get(i), o[0], o[1], o[2]};
                }
                if(possibleInfo != null)
                    infoCantidates.add(possibleInfo);
                continue;
            }
            else {      
                fuckIt = true;
                
                ArrayList<Vector> possiblePenVectors = getPotentialConcaveVectorSolutions(b);
                //possiblePenVectors.add(gravity.multiplyByScalar(-1).getUnitVector());
                possiblePenVectors.addAll(getCustomVectorSolutions(b, penetratingPoints));
                
                for(Vector v : possiblePenVectors) {
                    Object[] o = getPenDepthCustomVector(penetratingPoints, v, b, forPush);
                    if(o != null)
                        infoCantidates.add(o);
                }       
            }
        }
    }
    
    public ArrayList<Vector> getCustomVectorSolutions(RigidBody b, ArrayList<Point> penetratingPoints) {
        ArrayList<Vector> possiblePenVectors = new ArrayList<>();           
        ArrayList<RigidBodyLine> lineCantidates = new ArrayList<>();

        //Determine the lines closest to the penetrating points
        for(Point p : penetratingPoints) {
            RigidBodyLine closestLine = null;
            double closestDist = -1;

            for(RigidBodyLine l : collisionLines) {
                double dist = l.getSquaredDistToLine(p);
                if(dist < closestDist || closestDist == -1) {
                    closestLine = l;
                    closestDist = dist;
                }   
            }

            for(RigidBodyLine l : lineCantidates)
                if(l.equals(closestLine))
                    continue;

            lineCantidates.add(closestLine);
        }

        //Get the vector that bisects the line and its adjacent lines. If the two adjacent lines are concave the vector is added to possible vectors
        for(RigidBodyLine l : lineCantidates) {

            for(Point point : l.getPoints()) {

                RigidBodyPoint p = (RigidBodyPoint) point;

                for(RigidBodyLine pLine : p.getLines()) {      
                    if(pLine.equals(l))
                        continue;                       
                    Vector lVector;
                    Vector pLineVector;

                    Point otherLPoint = null;
                    Point otherPLinePoint = null;

                    for(Point lPoint: l.getPoints()) 
                        if(!lPoint.equals(p))
                            otherLPoint = lPoint;

                    for(Point pLinePoint: pLine.getPoints()) 
                        if(!pLinePoint.equals(p))
                            otherPLinePoint = pLinePoint;

                    lVector = new Vector(otherLPoint.x - p.x, otherLPoint.y - p.y);
                    pLineVector = new Vector(otherPLinePoint.x - p.x, otherPLinePoint.y - p.y);

                    Vector bisectingUnitVector = lVector.getUnitVector().add(pLineVector.getUnitVector()).getUnitVector();

                    if(bisectingUnitVector.dotProduct(l.getNormalUnitVector()) > 0)
                        possiblePenVectors.add(bisectingUnitVector);
                }
            }               
        }
        return possiblePenVectors;
    }
    
    
    private Object[] getPenDepthCustomVector(ArrayList<Point> points, Vector penDirection, RigidBody b, boolean forPush) {
        if(!forPush) {
            for(Point p : points) {
                Vector relativeVelocity = b.getLinearVelocityOfPointDueToAngularVelocity(p).add(b.getVelocity()).subtract(getLinearVelocityOfPointDueToAngularVelocity(p).add(getVelocity()));
                if(penDirection.dotProduct(relativeVelocity) > 0)
                    return null;
            }
        }
        for(Point p : points) {
            Line ray = new Line(p, new Point(p.x + penDirection.getXComp() * 10000, p.y + penDirection.getYComp() * 10000));
            Line line = null;
            double dist = -1;
            Point DAintersection = null;
            for(Line l : collisionLines) {
                Point intersection = l.getIntersection(ray);
                if(intersection == null)
                    continue;
                double currentDist = Math.sqrt(Math.pow(intersection.x - p.x, 2) + Math.pow(intersection.y - p.y, 2));
                if(currentDist > dist || dist == -1) {
                    dist = currentDist;
                    line = l;
                    DAintersection = intersection;
                }
            }
            if(dist == -1)
                continue;
            double slack = 0.001;
            Vector pointToLine = penDirection.multiplyByScalar(dist + slack);
            b.translate(pointToLine);
            if(!b.bodiesColliding(this)) {
                b.translate(pointToLine.multiplyByScalar(-1));
                return new Object[] {p, line, dist, penDirection, DAintersection};
            }
            b.translate(pointToLine.multiplyByScalar(-1));
        }
        return null;
    }
//    
//    public Object[] getPenetratingPointEntryVector(ArrayList<Point> penetratingPoints, RigidBody b) {
//        Vector[] possiblePenetrationDirections = new Vector[penetratingPoints.size()];
//        for(int i = 0; i < penetratingPoints.size(); i++) {
//            Point p = penetratingPoints.get(i);
//            
//            Vector bPointVelocity = b.getLinearVelocityOfPointDueToAngularVelocity(p).add(b.getVelocity()).multiplyByScalar(-1);
//            Vector thisPointVelocity = getLinearVelocityOfPointDueToAngularVelocity(p).add(getVelocity()).multiplyByScalar(-1);           
//            
//            possiblePenetrationDirections[i] = (bPointVelocity.subtract(thisPointVelocity)).getUnitVector();
//        }
//        Object[] shortestPenDirection = null;
//        for(Vector v: possiblePenetrationDirections) {
//            Object[] info = getPenDepthEntryVector(penetratingPoints, v, b);
//            if(info == null || (shortestPenDirection != null && (double)info[2] < (double)shortestPenDirection[2]))
//                continue;
//            shortestPenDirection = info;
//        }
//        return shortestPenDirection;
//    }
//    
//    public RigidBodyLine[] getLines() {
//        return collisionLines;
//    }
    
    public RigidBodyLine[] getLines() {
        return lines;
    }
    
    public boolean bodiesColliding(RigidBody b) {
        double[] bounds = getMaxAndMinVals();
        double[] bBounds = b.getMaxAndMinVals();
        
        if(bounds[0] > bBounds[2] || bounds[2] < bBounds[0] || bounds[1] > bBounds[3] || bounds[3] < bBounds[1])
            return false;
        
        for(Point p : b.getNodes()) {
            if(pointInsideBody(p))
                return true;
        }
        for(Point p : nodes) {
            if(b.pointInsideBody(p))
                return true;
        }
        for(Line l1 : collisionLines) {
            for(Line l2 : b.getCollisionLines()) {
                if(l1.getIntersection(l2) != null)
                    return true;
            }
        }
        return false;
    } 
    
    public Object[] getPenetratingPoint(RigidBody b, boolean forPush, Vector gravity) {
        ArrayList<Point> pointsInside = getPointsInside(b);
        if(pointsInside.size() == 0)
            return null;
        Object[] attempt = null;
        if(forPush)
            attempt = getPenetratingPoint(pointsInside, b, true, gravity);
        else
            attempt = getPenetratingPoint(pointsInside, b, false, gravity);
        return attempt;
    }
    
    public boolean lineIntersectsBody(Line l ) {
        double[] bounds = getMaxAndMinVals();
        double[] bBounds = new double[] {l.getSmallestX(), l.getSmallestY(), l.getLargestX(), l.getLargestY()};
        if(bounds[0] > bBounds[2] || bounds[2] < bBounds[0] || bounds[1] > bBounds[3] || bounds[3] < bBounds[1])
            return false;
        for(Line l2 : getCollisionLines()) {
            if(l.getIntersection(l2) != null)
                return true;
        }
        return false;
    }
    
    public boolean lineIntersectsBodyFrame(Line l ) {
        double[] bounds = getMaxAndMinVals();
        double[] bBounds = new double[] {l.getSmallestX(), l.getSmallestY(), l.getLargestX(), l.getLargestY()};
        if(bounds[0] > bBounds[2] || bounds[2] < bBounds[0] || bounds[1] > bBounds[3] || bounds[3] < bBounds[1])
            return false;
        for(Line l2 : getLines()) {
            if(l.getIntersection(l2) != null)
                return true;
        }
        return false;
    }
    
    public boolean lineIntersectsBodyTwice(Line l) {
        double[] bounds = getMaxAndMinVals();
        double[] bBounds = new double[] {l.getSmallestX(), l.getSmallestY(), l.getLargestX(), l.getLargestY()};
        if(bounds[0] > bBounds[2] || bounds[2] < bBounds[0] || bounds[1] > bBounds[3] || bounds[3] < bBounds[1])
            return false;
        int count = 0;
        for(Line l2 : getCollisionLines()) {
            if(l.getIntersection(l2) != null) {
                count++;
            }
        }
        return count > 1;
    }
    
    public boolean pointInsideBody(Point p) {
        double[] bounds = getMaxAndMinVals();
        if(p.x > bounds[2] || p.x < bounds[0] || p.y > bounds[3] || p.y < bounds[1])
            return false;
        //Creates a horizontel line centered around P and checks how it intersects with 
        //the lines in the body, if the number of intersects to the left and right of p are odd numbers p is inside the body
        Line l = new Line(new Point(-Integer.MAX_VALUE, p.y), new Point(Integer.MAX_VALUE, p.y));
        int intersectsLeft = 0;
        int intersectsRight = 0;
        for(int i = 0; i < collisionLines.length; i++) {
            Point intersect = collisionLines[i].getIntersection(l);
            if(intersect == null)
                continue;
            
            int acc = 10000;
            if((int)(intersect.x * acc) == (int)(p.x * acc))
                return true;
            else if(intersect.x < p.x)
                intersectsLeft++;
            else if(intersect.x > p.x)
                intersectsRight++;
        }
        return (intersectsLeft % 2) == 1 && (intersectsRight % 2) == 1;
    }
    
    public String toString() {
        String message = "RigidBody, ID: " + getID() + " Angular Velocity:" + angularVelocity + " Velocity:" + velocity + " Points:";
        for(Point p : nodes)
            message += p.toString();
        return message;
    }
    
    public boolean equals(Object o) {
        return o instanceof RigidBody && ((RigidBody)o).getID() == getID();
    }
    
    public void draw(Graphics g, Camera c) {
        g.setColor(color);
        int[] xPoints = new int[nodes.length];
        int[] yPoints = new int[nodes.length];
        Point cp = c.getPosition();
        for(int i = 0; i < nodes.length; i++) {
            Point p = nodes[i];
            xPoints[i] = (int)(p.x - cp.x + c.getWidth());
            yPoints[i] = (int)(p.y - cp.y + c.getHeight());
        }
       // g.fillPolygon(xPoints, yPoints, nodes.length);
        g.setColor(Color.GRAY);
        for(int i = 0; i < collisionLines.length; i++) {
            Line l = collisionLines[i];
            Color cac = Color.MAGENTA;
            l.draw(g, cac, c);
        }
//        for(int i = 0; i < nodes.length; i++) {
//            Point p = nodes[i];
//            int x = (int)(p.x - cp.x + c.getWidth()) - 3;
//            int y = (int)(p.y - cp.y + c.getHeight() - 3);
//            Color col = null;
//            switch(i % 6) {
//                case 0:
//                    col = Color.red;
//                    break;
//                case 1:
//                    col = Color.white;
//                    break;
//                case 2:
//                    col = Color.gray;
//                    break;
//                case 3:
//                    col = Color.green;
//                    break;
//                case 4:
//                    col = Color.blue;
//                    break;
//                case 5:
//                    col = Color.magenta;
//                    break;
//            }
//            g.setColor(col);
//            g.fillOval(x, y, 6, 6);
//        }
//        if(velocity.getMagnitude() > 650)
//            g.setColor(Color.red);
        //g.drawLine((int) centerOfMass.x, (int) centerOfMass.y, (int) (centerOfMass.x + velocity.getXComp()/3.0), (int) (centerOfMass.y + velocity.getYComp()/3.0));
        g.setColor(Color.orange);
        //g.drawLine((int) centerOfMass.x, (int) centerOfMass.y, (int) (centerOfMass.x + Math.cos(orientation) * 50), (int) (centerOfMass.y + Math.sin(orientation) * 50));
        
        if(drawCOM) {
            g.setColor(Color.magenta);
            g.fillOval((int) (centerOfMass.x - 4 -cp.x + c.getWidth()), (int) (centerOfMass.y - 4 - cp.y + c.getHeight()), 8, 8);
        }
//        for(Point p: nodes) {
//            g.setColor(Color.green);
//            g.fillOval((int) p.x - 3, (int) p.y - 3, 5, 5);
//        }
    }
}
