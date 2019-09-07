/*
 * File added by Nathan MacLeod 2019
 */
package portal2d;
import java.awt.Color;
import java.util.ArrayList;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.awt.BasicStroke;
/**
 *
 * @author Nathan
 */
public class Turret extends RigidBody implements Asset, SpritedEntity, Flippable {
    private double viewArcSize;
    private boolean leftOrientation;
    private Point position;
    private Point boopPoint;
    private double size;
    private SegmentableRay shootRay;
    private Point eyePoint;
    private double laserAngleVariance = 0; //angle off of the center
    private double laserAngularVelocity = 7;
    private double laserTargetAngle;
    private double moveLaserAlertTime = 0.75;
    private double moveLaserCounter = 0;
    private double fireRateGap = 0.1;
    private double holdFireCounter = 0;
    private double fireCounter = 0;
    private double fireSpread = Math.PI/16;
    private double becomeAlertTime = 1;
    private double becomeAlertCounter = 0;
    private double alertCounter = 0;
    private double alertTime = 3.5;
    private double alertChargeUpTime = 0.33;
    private ArrayList<Bullet> activeProjectiles;
    private double safeRadius;
    private double extraBoopCounter = 0;
    private double extraBoopCooldownTime = 1.25;
    private double deathCounter = 0;
    private double deathTime = 4;
    private boolean dead = false;
    private SpriteImage sprite;
    private SpriteImage deadImage;
    private SpriteImage panel;
    private SpriteImage panelFire;
    private SpriteImage turretBase;
    
    //private ArrayList<SegmentableRay> allTheRays;
    
    public Turret(Point center, double size, boolean leftOrientation, double viewArcSize, double density, double resistution, double frictionCoefficent, int ID, Color color) {
        super(producePoints(center, size, leftOrientation), new Point(center.x, center.y), new Vector(0, 0), 0, density, resistution, frictionCoefficent, false, ID, color, true);
        this.leftOrientation = leftOrientation;
        this.viewArcSize = viewArcSize;
        activeProjectiles = new ArrayList<>();
        safeRadius = size * 1.41;
        this.size = size;
        laserAngleVariance = 0;
        this.position = center;
        eyePoint = getNodes()[2];
        produceTurretImage(size);
    }
    
    public boolean getFlipped() {
        return leftOrientation;
    }
    
    public boolean getDead() {
        return dead;
    }
    
    public RigidBody getBoundingBox() {
        RigidBodyPoint[] nodesClone = new RigidBodyPoint[getNodes().length];
        for(int i = 0; i < nodesClone.length; i++) {
            nodesClone[i] = new RigidBodyPoint(getNodes()[i].x, getNodes()[i].y);
        }
        return new RigidBody(nodesClone, new Point(position.x, position.y), new Vector(0, 0), 0, 0, 0, 0, false, 0, null, false);
    }
    
    public Vector getVectorToBase() {
        return new Vector(0, size/2.0);
    }
    
    public void draw(Graphics g, Camera c, Point position, double orientation) {
        sprite.drawNormally(g, c, position, orientation);
    }
    
    public void flipOrientation() {
        leftOrientation = !leftOrientation;
    }
    
    public SpriteImage getSprite() {
        return sprite;
    }
    
    public Point getDrawPosition() {
        return position;
    }
    
    public Point[] getAllPointData() {
        Point[] starterData = super.getAllPointData();
        Point[] allData = new Point[starterData.length + 1];
        for(int i = 0; i < starterData.length; i++) {
            allData[i] = starterData[i];
        }
        allData[allData.length - 1] = position;
        return allData;
    }
    
    private static RigidBodyPoint[] producePoints(Point center, double size, boolean lo) {
        return new RigidBodyPoint[] {findCoordRelative(0, -30, size, center, lo), //50,22; 
        findCoordRelative(10, -19, size, center, lo), findCoordRelative(12, -1, size, center, lo), //60,31; 62,49; 
        findCoordRelative(10, 12, size, center, lo), //60,62;
        findCoordRelative(19, 24, size, center, lo), findCoordRelative(27, 49, size, center, lo), findCoordRelative(25, 49, size, center, lo),//69,74; 76,100; 
        findCoordRelative(11, 26, size, center, lo), findCoordRelative(-2, 27, size, center, lo), //61,76; 48,77; 
        findCoordRelative(-26, 19, size, center, lo), findCoordRelative(-36, 50, size, center, lo), findCoordRelative(-40, 50, size, center, lo), //24,69; 12,100; 
        findCoordRelative(-35, 10, size, center, lo), findCoordRelative(-23, 3, size, center, lo), //15,60; 27,53; 
        findCoordRelative(-15, 2, size, center, lo), findCoordRelative(-15, -10, size, center, lo), //35,52; 35,40; 
        findCoordRelative(-12, -19, size, center, lo)}; //38,31; 
    }
    
    private static RigidBodyPoint findCoordRelative(double relativeX, double relativeY, double size, Point center, boolean leftOrientation) {
        double xCord = (leftOrientation)? -relativeX * size/100.0 + center.x : relativeX * size/100.0 + center.x;
        return new RigidBodyPoint(xCord, center.y + relativeY * size/100.0);
    }
    
    public Turret(Point position, int ID, boolean leftOrientation) {
        this(position, 100, leftOrientation, Math.PI/2.0, 20, 0.3, 1, ID, Color.WHITE);
    }
    
    private void produceTurretImage(double size) {
        sprite = new SpriteImage("turretClosed.png", size, new Vector(0, 0));
        turretBase = new SpriteImage("turretBase.png", size, new Vector(0, 0));
        panel = new SpriteImage("TurretPanel.png", size, new Vector(0, 0));
        panelFire = new SpriteImage("TurretPanelFiring.png", size, new Vector(0, 0));
        deadImage = new SpriteImage("turretDead.png", size, new Vector(0, 0));
        if(leftOrientation) {
            sprite.flip();
            turretBase.flip();
            panel.flip();
            panelFire.flip();
            deadImage.flip();
        }
    }
    
    public void flip() {
        for(Point p : getNodes()) {
            p.x = -(p.x - position.x) + position.x;
        }
        flipSprite();
        leftOrientation = !leftOrientation;
    }
    
    public void flipSprite() {
        sprite.flip();
        turretBase.flip();
        panel.flip();
        panelFire.flip();
        deadImage.flip();
    }
    
    public void update(double dt, PhysicsPanel panel) {
        updateTurret(dt, panel.getPlayer(), panel.getBodies(), panel.getPortals());
    }
    
    private void updateTurret(double dt, PlayerHitbox player, ArrayList<RigidBody> possibleObstacles, PortalPair portals) {
        if(extraBoopCounter > 0)
                extraBoopCounter -= dt;
        
        if(deathCounter <= 0 && !dead) {
            boolean beingHeld = player.getObjectIsHeld(this);
            trackPlayer(player, possibleObstacles, portals);
            shootAtPlayer(shootRay, dt, sawPlayer(), beingHeld);  
            checkIfDead(beingHeld);
            adjustLaserAngle(dt, beingHeld);
        }
        else if(deathCounter > 0) {
            deathCounter -= dt;
            if(deathCounter > 1)
                shootWildly(dt);
            else
                laserTargetAngle = 0;
            if(deathCounter <= 0)
                dead = true;
        }
        moveLaserToTargetAngle(dt);
        updateBullets(dt, portals, possibleObstacles);
    }
    
    private void checkIfDead(boolean beingHeld) {
        if(collidedWithSomeone() && Math.cos(getOrientation()) < 1/2.0 && !beingHeld)
            deathCounter = deathTime;
    }
    
    public double safeRadius() {
        return safeRadius;
    }
    
    public boolean sawPlayer() {
        return shootRay != null;
    }
    
    private void adjustLaserAngle(double dt, boolean beingHeld) {
        
        if(sawPlayer() && !beingHeld && alertCounter > 0) {
            Vector shootVector = shootRay.getBaseRay().vector.getUnitVector();
            double shootAngle = Math.atan(shootVector.getYComp()/shootVector.getXComp());
            double centerViewAngle = ((leftOrientation)? Math.PI : 0) + getOrientation();
            
            if(centerViewAngle >= Math.PI * 2) {
                centerViewAngle -= Math.PI * 2;
            }
            else if(centerViewAngle < 0) {
                centerViewAngle += Math.PI * 2;
            }
            
            if(shootVector.getXComp() < 0)
                shootAngle += Math.PI;
            if(shootAngle >= Math.PI * 2) {
                shootAngle -= Math.PI * 2;
            }
            else if(shootAngle < 0) {
                shootAngle += Math.PI * 2;
            }
            
            if(!leftOrientation && shootAngle > Math.PI)
                shootAngle -= 2 * Math.PI;
            if(!leftOrientation && centerViewAngle > Math.PI)
                shootAngle -= 2 * Math.PI;
            laserTargetAngle = shootAngle - centerViewAngle;
            moveLaserCounter = 1;
        }
        else if(alertCounter > 0) {
            if(moveLaserCounter <= 0) {
                laserTargetAngle = viewArcSize * (Math.random()  * 1 - 0.5);
                moveLaserCounter = moveLaserAlertTime;
            }
            else {
                moveLaserCounter -= dt;
            }
        }
        else {
            laserTargetAngle = 0;
        }
    }
    
    private void moveLaserToTargetAngle(double dt) {
        if(Math.abs(laserTargetAngle - laserAngleVariance) < laserAngularVelocity * dt) {
            laserAngleVariance = laserTargetAngle;
        }
        else if(laserTargetAngle > laserAngleVariance) {
            laserAngleVariance += laserAngularVelocity * dt;
        }
        else
            laserAngleVariance -= laserAngularVelocity * dt;
    }
    
    private void shootWildly(double dt) {
        if(fireCounter <= 0) {
            double randomAngle = Math.random() * viewArcSize - viewArcSize/2.0;
            laserTargetAngle = randomAngle;
            randomAngle += getOrientation()+ ((leftOrientation)? Math.PI : 0);
            shootBullet(new Vector(Math.cos(randomAngle), Math.sin(randomAngle)));
            fireCounter = fireRateGap;
        }
        else {
            fireCounter -= dt;
        }
    }
    
    private void shootAtPlayer(SegmentableRay shootRay, double dt, boolean canSeePlayer, boolean beingHeld) {
        if(beingHeld) {
            fireCounter = 0;
            alertCounter = alertTime;
            return;
        }
        
        if(alertCounter > 0 && canSeePlayer) {
            if(fireCounter <= 0 && holdFireCounter <= 0) {
                shootBullet(shootRay.getBaseRay().vector);
                fireCounter = fireRateGap;
            }
            else {
                holdFireCounter -= dt;
                fireCounter -= dt;
            }
            alertCounter = alertTime;
        }
        else if(becomeAlertCounter > 0) {
            becomeAlertCounter -= dt;
            if(becomeAlertCounter <= 0)
                alertCounter = alertTime;
        }
        else if(alertCounter > 0) {
            holdFireCounter = alertChargeUpTime;
            fireCounter = 0;
            alertCounter -= dt;
        }
        else if(canSeePlayer) {
            becomeAlertCounter = becomeAlertTime;
        }
    }
    
    private void shootBullet(Vector v) {
        double bulletSpeed = 5500;
        Vector shootDirection = v.getUnitVector();
        double deviationAngle = Math.random() * fireSpread - fireSpread/2.0;
        Vector bulletVelocity = new Vector(shootDirection.getXComp() * Math.cos(deviationAngle) - shootDirection.getYComp() * Math.sin(deviationAngle),
        shootDirection.getYComp() * Math.cos(deviationAngle) + shootDirection.getXComp() * Math.sin(deviationAngle)).multiplyByScalar(bulletSpeed);
        activeProjectiles.add(new Bullet(new Point(position.x, position.y), bulletVelocity, this));
    }
    
    private void updateBullets(double dt, PortalPair portals, ArrayList<RigidBody> obstacles) {
        for(int i = 0; i < activeProjectiles.size(); i++) {
            if(activeProjectiles.get(i).move(dt, portals, obstacles)) {
                activeProjectiles.remove(i);
                i--;
            }
        }
    }
    
    private void drawBullets(Graphics g, Camera c) {
        for(Bullet b : activeProjectiles)
            b.draw(g, c);
    } 
    
    private void trackPlayer(PlayerHitbox player, ArrayList<RigidBody> possibleObstacles, PortalPair portals) {
        shootRay = getLineOfSightRayIfPossible(player, possibleObstacles, portals);
    }
    
    private SegmentableRay getLineOfSightRayIfPossible(PlayerHitbox player, ArrayList<RigidBody> possibleObstacles, PortalPair portals) {
        //Calculate points that turret will check for LOS with
        Point[] possibleShootPoints = new Point[3];
        possibleShootPoints[0] = new Point(player.getCenterOfMass().x, player.getCenterOfMass().y);
        Vector playerFootVector = new Vector(Math.cos(player.getOrientation() + Math.PI/2.0), Math.sin(player.getOrientation() + Math.PI/2.0)).multiplyByScalar(player.getHeight()/4.0);
        possibleShootPoints[1] = new Point(possibleShootPoints[0].x + playerFootVector.getXComp(), possibleShootPoints[0].y + playerFootVector.getYComp());
        playerFootVector = playerFootVector.multiplyByScalar(-1);
        possibleShootPoints[2] = new Point(possibleShootPoints[0].x + playerFootVector.getXComp(), possibleShootPoints[0].y + playerFootVector.getYComp());
        
        ArrayList<SegmentableRay> possibleRays = produceRaysToShootPoints(possibleShootPoints, position, portals, possibleObstacles, player);
        //allTheRays = possibleRays;
        for(int i = 0; i < possibleRays.size(); i++) {
            if(checkLOSRayValid(possibleRays.get(i), possibleObstacles, player))
                return possibleRays.get(i);
        }
        return null;
    }
    
    private ArrayList<SegmentableRay> produceRaysToShootPoints(Point[] shootPoints, Point viewPoint, PortalPair portals, ArrayList<RigidBody> obstacles, PlayerHitbox player) {
        //Produces rays to look through portals.
        ArrayList<SegmentableRay> possibleRays = new ArrayList<>();
        for(Point p : shootPoints) {
            SegmentableRay ray = createRayToPoint(viewPoint, p);
            ray.segmentRay(portals);
            if(ray.getSegments().size() <= 1)
                possibleRays.add(ray);
        }
        
        if(!portals.portalSystemActive())
            return possibleRays;
        
        //Merges rays that shoot at target by looking through the portal
        possibleRays.addAll(produceRaysThroughPortal(shootPoints, viewPoint, portals, portals.getPortals()[0], portals.getPortals()[1], obstacles, player));
        possibleRays.addAll(produceRaysThroughPortal(shootPoints, viewPoint, portals, portals.getPortals()[1], portals.getPortals()[0], obstacles, player));
        return possibleRays;
    }
    
    private ArrayList<SegmentableRay> produceRaysThroughPortal(Point[] shootPoints, Point viewPoint, PortalPair portals, Portal inPortal, Portal outPortal, ArrayList<RigidBody> obstacles, PlayerHitbox player) {
        //Checks for line of sight to portal
        Vector vect = new Vector(inPortal.getCenter().x - viewPoint.x, inPortal.getCenter().y - viewPoint.y);
        SegmentableRay turretToPortal = new SegmentableRay(new Ray(vect, new Point(viewPoint.x, viewPoint.y)));
        if(!checkLOSRayValid(turretToPortal, obstacles, player))
            return new ArrayList<SegmentableRay>();
        //Creates ray to shoot through portal
        Point[] shootPointsClone = new Point[shootPoints.length];
        for(int i = 0; i < shootPoints.length; i++) {
            shootPointsClone[i] = new Point(shootPoints[i].x, shootPoints[i].y);
        }
        portals.teleportPointsToOtherPortal(shootPointsClone, outPortal, inPortal);
        ArrayList<SegmentableRay> rays = new ArrayList<>();
        for(Point p : shootPointsClone) {
            SegmentableRay ray = createRayToPoint(viewPoint, p);
            ray.segmentRay(portals);
            if(ray.getSegments().size() > 1)
                rays.add(ray);
        }
        return rays;
    }
    
    private SegmentableRay createRayToPoint(Point origin, Point target) {
        return new SegmentableRay(new Ray(new Vector(target.x - origin.x, target.y - origin.y), new Point(origin.x, origin.y)));
    }
    
    private boolean checkLOSRayValid(SegmentableRay ray, ArrayList<RigidBody> possibleObstacles, PlayerHitbox p) {
        //Takes a segmentable ray that connects to one of the shoot points and check that it falls within field of view and that it is not obstructed by obstacles
        //Check base of ray is within FOV
        double centerViewAngle = ((leftOrientation)? Math.PI : 0) + getOrientation();
        Vector centerViewVector = new Vector(Math.cos(centerViewAngle), Math.sin(centerViewAngle));
        Vector baseRayDirection = ray.getBaseRay().vector.getUnitVector();
        
        //By taking dot product with magnitudes 1 the number produced is the cos of angle between direction of view and the ray
        double theta = Math.acos(centerViewVector.dotProduct(baseRayDirection));
        
        if(theta > viewArcSize/2.0)
            return false;
        //Ray thefore is within FOV, need to check for obstructions of fixed objects blocking view. Need to check for all sub says and all objects
        for(Ray r : ray.getSegments()) {
            for(RigidBody b : possibleObstacles) {
                if(b instanceof Turret || b instanceof PlayerHitbox || p.getObjectIsHeld(b))
                    continue;
                Line rLine = new Line(r.origin, new Point(r.origin.x + r.vector.getXComp(), r.origin.y + r.vector.getYComp()));
                for(Line l : b.getCollisionLines())
                    if(l.getIntersection(rLine) != null)
                        return false;
            }
        }
        
        return true;
    }
    
    public boolean getLastDrawPriority() {
        return false;
    }
    
    public class Bullet {
        private Point pos;
        private Vector velocity;
        private Vector velocityUnitVector;
        private SegmentableRay streakRay;
        private Turret sourceTurret;
        private double distTraveled;
        private double impulse;
        private double playerScaleFactor;
        
        public Bullet(Point pos, Vector velocity, Turret sourceTurret) {
            this.pos = pos;
            this.velocity = velocity;
            velocityUnitVector = velocity.getUnitVector();
            distTraveled = 0;
            this.sourceTurret = sourceTurret;
            impulse = 7500;
            playerScaleFactor = 100;
        }
        
        public boolean move(double dt, PortalPair portals, ArrayList<RigidBody> bodies) {
            pos.x += velocity.getXComp() * dt;
            pos.y += velocity.getYComp() * dt;
            
            Line lineOfTravel = new Line(pos, new Point(pos.x - velocity.getXComp() * dt, pos.y - velocity.getYComp() * dt));
            
            RigidBody collidedBody = null;
            Point collidedBodyPoint = null;
            double greatestObstacleDistVal = -1;
            Portal mostImmediatePortal = null;
            double greatestPortalDistVal = -1;
            for(RigidBody b: bodies) {
                if(!(b instanceof EnergyPellet || (b.equals(sourceTurret) || (b instanceof ShadowRigidBody && ((ShadowRigidBody)b).getBase().equals(sourceTurret))) && distTraveled < sourceTurret.safeRadius()) && b.lineIntersectsBodyFrame(lineOfTravel)) {
                    for(RigidBodyLine l: b.getCollisionLines()) {
                        Point p = l.getIntersection(lineOfTravel);
                        if(p != null) {
                            double dist = Math.sqrt(Math.pow(pos.x - p.x, 2) + Math.pow(pos.y - p.y, 2));
                            if(dist > greatestObstacleDistVal) {
                                collidedBody = b;
                                collidedBodyPoint = p;
                                greatestObstacleDistVal = dist;
                            }
                        }
                    }
                }
            }
            for(Portal port : portals.getPortals()) {
                if(!portals.portalSystemActive())
                    break;
                Point p = port.getPortalSurface().getIntersection(lineOfTravel);
                if(p != null) {
                    double dist = Math.sqrt(Math.pow(pos.x - p.x, 2) + Math.pow(pos.y - p.y, 2));
                    if(dist > greatestPortalDistVal) {
                        greatestPortalDistVal = dist;
                        mostImmediatePortal = port;
                    }
                }
            }
            if(greatestObstacleDistVal == -1 && greatestPortalDistVal == -1) {
                //do nothing
            }
            else if(greatestObstacleDistVal > greatestPortalDistVal) {
                collidedBody.applyImpulseOnPoint(collidedBodyPoint, velocityUnitVector.multiplyByScalar(impulse * ((collidedBody instanceof PlayerHitbox)? playerScaleFactor : 1)), this);
                if(collidedBody instanceof PlayerHitbox)
                    ((PlayerHitbox)collidedBody).dealDamage(5.5);
                return true;
            }
            else {
                Portal outPortal = portals.getPortals()[0].equals(mostImmediatePortal)? portals.getPortals()[1] : portals.getPortals()[0];
                pos = portals.teleportPointToOtherPortal(pos, mostImmediatePortal, outPortal);
                velocity = portals.redirectVector(velocity, mostImmediatePortal, outPortal);
                velocityUnitVector = velocity.getUnitVector();
            }
            
            double streakLength = 50;
            if(streakLength > distTraveled)
                streakLength = distTraveled;
            Vector streakVector = velocityUnitVector.multiplyByScalar(-streakLength);
            streakRay = new SegmentableRay(new Ray(streakVector, pos));
            streakRay.segmentRay(portals);
            distTraveled += velocity.getMagnitude() * dt;
            return false;
        }
        
        public void draw(Graphics g, Camera c) {
            if(streakRay != null) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setStroke(new BasicStroke(2));
                streakRay.draw(g2d, Color.yellow, c);
                g2d.setStroke(new BasicStroke(1));
            }
        }
    }
    
    public void applyImpulseOnPoint(Point p, Vector impulse, Object o) {
        super.applyImpulseOnPoint(p, impulse, o);
        if((o instanceof EnergyPellet || o instanceof CompanionCube) && deathCounter <= 0 && !dead && extraBoopCounter <= 0) {
            breakRest(false);
            Vector vectorDirect1 = new Vector(-1, 0);
            Vector vectorDirect2 = new Vector(1, 0);
            extraBoopCounter = extraBoopCooldownTime;
            deathCounter = deathTime;
            if(o instanceof EnergyPellet) {
                double bonusImpulse = 450 * getMass();
                applyImpulseOnPoint(new Point(position.x, position.y - 50), ((vectorDirect1.dotProduct(impulse) > 0)? vectorDirect1 : vectorDirect2).multiplyByScalar(bonusImpulse));
            }
            if(o instanceof CompanionCube) {
                if(getAngularVelocity() < 0) {
                    setAngularVelocity(-30);
                }
                else
                    setAngularVelocity(30);
            }
        }
    }
    
    public void drawLaserAtAngle(Graphics g, Camera c, double angleVariance, ArrayList<RigidBody> obstacles, PortalPair portals, Point eyePoint) {
        double laserAngle = ((leftOrientation)? Math.PI : 0) + getOrientation() + angleVariance;
        Vector laserVector = new Vector(Math.cos(laserAngle), Math.sin(laserAngle)).multiplyByScalar(5000);
        SegmentableRay laser = new SegmentableRay(new Ray(laserVector, eyePoint));
        laser.segmentRay(portals);
        boolean firstRay = true;
        for(int i = laser.getSegments().size() - 1; i >= 0; i--) {
            Ray segment = laser.getSegments().get(i);
            boolean intersected = false;
            Point endPoint = new Point(segment.origin.x + segment.vector.getXComp(), segment.origin.y + segment.vector.getYComp());
            for(RigidBody b : obstacles) {
                if(firstRay && b.equals(this))
                    continue;
                for(Line l : b.getCollisionLines()) {
                    Line currentLine = new Line(endPoint, segment.origin);
                    Point intersection = currentLine.getIntersection(l);
                    if(intersection != null) {
                        intersected = true;
                        endPoint = intersection;
                    }
                    
                }
            }
            firstRay = false;
            new Line(segment.origin, endPoint).draw(g, Color.red, c);
            if(intersected)
                break;
        }
    }
    
    public void draw(Graphics g, Camera c, PortalPair portals, ArrayList<RigidBody> obstacles) {
        BufferedImage baseImage = (!dead)? sprite.getImage() : deadImage.getImage();
        if(!dead && (alertCounter > 0 || deathCounter > 1)) {
            baseImage = new BufferedImage(baseImage.getWidth(), baseImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            baseImage.getGraphics().drawImage(turretBase.getImage(), 0, 0, null);
            AffineTransform panelTransformation = new AffineTransform();
            panelTransformation.translate(baseImage.getWidth()/2.0, baseImage.getHeight()/2.0);
            panelTransformation.rotate(laserAngleVariance);
            panelTransformation.translate(-baseImage.getWidth()/2.0, -baseImage.getHeight()/2.0);
            ((Graphics2D)baseImage.getGraphics()).drawImage((fireRateGap - fireCounter < 0.05)? panelFire.getImage() : panel.getImage(), panelTransformation, null);
        }
        Portal[] intersectingPortals = portals.getDominantAndOtherPortalOfBody(this);
        if(intersectingPortals == null) {
            sprite.drawNormallyCustomImage(g, c, position, orientation, baseImage);
        }
        else {
            sprite.drawCustumSpriteSplitInPortal(g, c, position, orientation, intersectingPortals[0], intersectingPortals[1], portals, baseImage);
        }
        if(!dead)
            drawLaserAtAngle(g, c, laserAngleVariance, obstacles, portals, eyePoint);
        drawBullets(g, c);
    }
    
//    public void draw(Graphics g, Camera c, PortalPair portals, ArrayList<RigidBody> obstacles) {
//        return;
//        
////        if(alertCounter > 0)
////            setColor(Color.RED);
////        else
////            setColor(Color.BLUE);
////        
////        //super.draw(g, c);
////        
////        sprite.drawNormally(g, c, position, getOrientation());
//////        if(allTheRays != null)
//////            for(SegmentableRay shootRay : allTheRays)
//////                shootRay.draw(g, Color.red, c);
//////        if(shootRay != null)
//////            shootRay.draw(g, Color.red, c);
////        if(!dead) {
////            drawLaserAtAngle(g, c, laserAngleVariance, obstacles, portals, eyePoint);
////            //drawFOV(g, c);
////        }
////        drawBullets(g, c);
//    }
    
//    private void drawFOV(Graphics g, Camera c) {
//        double centerViewAngle = ((leftOrientation)? Math.PI : 0) + getOrientation();
//        double length = 300;
//        Point p1 = new Point(getCenterOfMass().x + length * Math.cos(centerViewAngle + viewArcSize/2.0), getCenterOfMass().y + length * Math.sin(centerViewAngle + viewArcSize/2.0));
//        Point p2 = new Point(getCenterOfMass().x + length * Math.cos(centerViewAngle - viewArcSize/2.0), getCenterOfMass().y + length * Math.sin(centerViewAngle - viewArcSize/2.0));
//        Line l1 = new Line(getCenterOfMass(), p1);
//        Line l2 = new Line(getCenterOfMass(), p2);
//        l1.draw(g, Color.BLUE, c);
//        l2.draw(g, Color.BLUE, c);
//    }
}
