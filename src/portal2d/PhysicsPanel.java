
/*
 * File added by Nathan MacLeod 2019
 */
package portal2d;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.KeyAdapter;
import java.util.ArrayList;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
/**
 *
 * @author Nathan
 */
public class PhysicsPanel extends JPanel implements Runnable, IDGiver {
    private JFrame frame;
    private ArrayList<RigidBody> bodies;
    private ArrayList<RigidBody> addBodyQueue;
    private ArrayList<RigidBody> deleteQueue;
    private ArrayList<Asset> gameAssets;
    private ArrayList<Asset> addAssetQueue;
    private ArrayList<Asset> removeAssetQueue;
    private ArrayList<ActivationPair> activatables;
    private int playerRunDirection;
    private boolean jump;
    private boolean a;
    private boolean d;
    private boolean z;
    private boolean E;
    private boolean r;
    private boolean q;
    private boolean t;
    private boolean space;
    private JFrame fuckingPeiceOfShitCuntFuck;
    private PlayerHitbox player;
    private Elavator exitElavator;
    private boolean running;
    private Thread runner;
    private Vector gravity;
    private int currentIDIndex;
    private double timeStep;
    private double timeQueue;
    private Point cursor;
    private Point mouse;
    private double maxTimeQueue;
    private MouseTool mouseTool = new FirePortalTool();
    private double timeScale = 1;
    private boolean randomColor = true;
    private Color bodyColor = null;
    private boolean drawCOM = false;
    private boolean createFixedBody = false;
    private int nSides = 3;
    private double friction = 0.5;
    private double resistution = 0.3;
    private double density = 50;
    private PortalPair portals;
    private Camera camera;
    private Background background;
    private boolean drawHitboxes = false;
    private int currentLevel;
    private File file;
  
    
    public void start() {
        running = true;
        runner = new Thread(this);
        runner.start();
    }       
    
    public void run() {
        
        mouse = new Point(0, 0);
        camera = new Camera(getWidth()/2.0, getHeight()/2.0);
        currentIDIndex = 0;
        maxTimeQueue = 0.033;
        timeQueue = 0;
        playerRunDirection = 0;
        timeStep = 0.001;
        bodies = new ArrayList<>();
        addBodyQueue = new ArrayList<>();
        deleteQueue = new ArrayList<>();
        gameAssets = new ArrayList<>();
        addAssetQueue = new ArrayList<>();
        removeAssetQueue = new ArrayList<>();
        activatables = new ArrayList<>();
        gravity = new Vector(0, 1200);
        try {
            if(currentLevel == -1)
                loadLevel(new FileInputStream(file), true);
            else
                loadLevelNumber(currentLevel, true);
            }
        catch(IOException e) {
            System.out.println(e);
        }
        long currentTime;
        long pastTime = System.nanoTime();
        double timePassed = 0;
        fuckingPeiceOfShitCuntFuck.requestFocus();
        
        double timeInElavator = 0;
        double rCounter = 0;
        long t1;
        long t2;
        while(running) {
            currentTime = System.nanoTime();
            timePassed = (currentTime - pastTime)/Math.pow(10, 9);
            pastTime = currentTime;
            
            doPhysicsShit(timePassed);
            t1 = System.nanoTime();
            if(r || player.getDead()) {
                if(r)
                    rCounter += timePassed;
                else
                    rCounter += timePassed/3.0;
                if(rCounter > 1.25)
                    loadLevelNumber(currentLevel, false);
            }
            else {
                rCounter = 0;
            }
            
            if(playerInExitElavator()) {
                timeInElavator += timePassed;
                if(timeInElavator > 1) {
                    currentLevel++;
                    loadLevelNumber(currentLevel, false);
                }
            }
            else {
                timeInElavator = 0;
            }
            
            
            
            double lookDist = 0;
            if(z)
                lookDist = 750;
            Point trackPoint = findTrackPoint(player.getAimPoint(), mouse, lookDist);
            camera.trackPoint(timePassed, 3, 10, trackPoint);
            render();
            t2 = System.nanoTime();
            //System.out.println(1/timePassed);
            //System.out.println("Physics: " + (t1 - currentTime) + " Render: " + (t2 - t1));
        }
    }
    
    private void doPhysicsShit(double timePassed) {
        while(addBodyQueue.size() > 0)
            bodies.add(addBodyQueue.remove(0));
        if(timeScale <= 1)
            timeQueue += timePassed;
        else
            timeQueue += timePassed * timeScale;
        if(timeQueue >= maxTimeQueue)
            timeQueue = maxTimeQueue;

        while(timeQueue >= timeStep) {
            if(timeScale <= 1)
                update(timeStep * timeScale);
            else
                update(timeStep);
            timeQueue -= timeStep;
        }
    }
    
    public Point getMouse() {
        return mouse;
    }
    
    private void loadLevelNumber(int n, boolean initialLoad) {
        String fileName = "/levels/level" + n +"/level";
        loadLevel(fileName, initialLoad);
    }
    
    private void loadLevel(String level, boolean initialLoad) {
        try {
            loadLevel(Class.class.getResourceAsStream(level), initialLoad);
        }
        catch(Exception e) {
            System.out.println(e);
            new UserInterface();
            frame.dispose();
        }
    }
    
    private void loadLevel(InputStream level, boolean initialLoad) {
        try {
            BufferedImage lastRender = (initialLoad)? null : renderFrame();
            Graphics panelGraphics = getGraphics();

            long currentTime;
            long pastTime = System.nanoTime();
            double timePassed = 0;
            double opacity = 0;
            double opacityRate = 255;

            BufferedImage render;

            if(!initialLoad) {
                while(opacity < 255) {
                    render = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                    Graphics g = render.getGraphics();
                    currentTime = System.nanoTime();
                    timePassed = (currentTime - pastTime)/Math.pow(10, 9);
                    pastTime = currentTime;

                    opacity += timePassed * opacityRate;
                    g.drawImage(lastRender, 0, 0, null);
                    g.setColor(new Color(0, 0, 0, (opacity > 255)? 255 : (int) opacity));
                    g.fillRect(0, 0, getWidth(), getHeight());

                    panelGraphics.drawImage(render, 0, 0, null);
                }
            }
            else {
                render = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics g = render.getGraphics();
                opacity = 255;
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, WIDTH, HEIGHT);
                panelGraphics.drawImage(render, 0, 0, null);
            }
            BlockLevel blockLevel = new BlockLevel(level);
            ArrayList<RigidBody> fileBodies = blockLevel.produceWalls();
            bodies.clear();
            addBodyQueue.clear();
            deleteQueue.clear();
            gameAssets.clear();
            addAssetQueue.clear();
            removeAssetQueue.clear();
            activatables.clear();

            for(RigidBody b : fileBodies) {
                bodies.add(b);
                giveID();
            }
            AssetListWrapper wrapper = blockLevel.getAssets(this, bodies);
            for(Asset a : wrapper.assets) {
                gameAssets.add(a);
                if(a instanceof RigidBody)
                    bodies.add((RigidBody)a);
            }
            for(ActivationPair p : wrapper.activatables) {
                activatables.add(p);
            }
            portals = new PortalPair(this);
            player = wrapper.getEntryElavator().spawnPlayer(portals, giveID());
            exitElavator = wrapper.getExitElavator();
            bodies.add(player);
            gameAssets.add(player);
            background = new Background(blockLevel);
            camera.setPosition(player.getCenterOfMass());

            lastRender = renderFrame();

            opacity = 255;
            pastTime = System.nanoTime();
            while(opacity > 0) {
                render = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics g = render.getGraphics();
                currentTime = System.nanoTime();
                timePassed = (currentTime - pastTime)/Math.pow(10, 9);
                pastTime = currentTime;

                opacity -= timePassed * opacityRate;
                g.drawImage(lastRender, 0, 0, null);
                g.setColor(new Color(0, 0, 0, (opacity < 0)? 0 : (int) opacity));
                g.fillRect(0, 0, getWidth(), getHeight());

                panelGraphics.drawImage(render, 0, 0, null);
            }
        }
        catch(FileNotFoundException e) {
            new UserInterface();
            frame.dispose();
        }
    }
    
    private boolean playerInExitElavator() {
        if(player == null || exitElavator == null)
            return false;
        else {
            return exitElavator.playerFullyInside(player);
        }
    }
    
    private Point findTrackPoint(Point start, Point target, double maxDist) {
        Vector startToTarget = new Vector(target.x - start.x, target.y - start.y);
        double magnitude = startToTarget.getMagnitude();
        if(magnitude > maxDist)
            startToTarget = startToTarget.multiplyByScalar(maxDist/magnitude);
        return new Point(start.x + startToTarget.getXComp(), start.y + startToTarget.getYComp());
    }
    
    public void addBody(RigidBody b) {
        bodies.add(b);
    }
    
    public void removeBody(RigidBody b) {
        bodies.remove(b);
    }
    
    public int giveID() {
        int id = currentIDIndex;
        currentIDIndex++;
        return id;
    }
    
    private BufferedImage renderFrame() {
        BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        long t = System.nanoTime();
        Graphics g = img.getGraphics();
        background.drawBackground(g, camera);
        long t1 = System.nanoTime();
//        g.setColor(Color.gray);
//        g.fillRect(0, 0, 1920, 1080);
//        for(RigidBody b : bodies) {
//            if(b instanceof ShadowRigidBody)
//                b.draw(g, camera);
//        }
        if(player != null)
            player.drawHoldBeam(g, camera, portals);
        for(Asset a : gameAssets) {
            if(a instanceof SpritedEntity) {
                if(!((SpritedEntity) a).getLastDrawPriority())
                    ((SpritedEntity) a).draw(g, camera, portals, bodies);
            }
            
            if(a instanceof PlayerHitbox) {
                ((PlayerHitbox) a).draw(g, camera);
                if(space)
                    ((PlayerHitbox)a).drawAimingLaser(g, camera, mouse, portals, bodies);
            }
//            if(a instanceof EnergyPellet)
//                ((EnergyPellet) a).draw(g, camera);
            if(a instanceof Door)
                ((Door) a).draw(g, camera);
            if(a instanceof FloatingText)
                ((FloatingText) a).draw(g, camera);
            
        }
        for(Asset a : gameAssets) {
            if(a instanceof SpritedEntity) {
                if(((SpritedEntity) a).getLastDrawPriority())
                    ((SpritedEntity) a).draw(g, camera, portals, bodies);
            }
        }
        long t2 = System.nanoTime();
        background.drawForeground(g, camera);
        if(drawHitboxes) {
            for(RigidBody b : bodies) {
                b.draw(g, camera);
            }
        }
        long t3 = System.nanoTime();
        portals.draw(g, camera);
        long t4 = System.nanoTime();
        //System.out.println("Portals: " + (t4 - t3) + " Foreground: " + (t3 - t2) + " Assets: " + (t2 - t1) + " Background: " + (t1 - t));
        return img;
    }
    
    private void render() {
        BufferedImage img = renderFrame();
        Graphics panelGraphics = getGraphics();
        panelGraphics.drawImage(img, 0, 0, null);
    }
    
    private void physicsUpdate(double dt) {
        if(mouse != null && cursor != null)
            mouse = getMousePos(cursor.x, cursor.y);
        if(mouseTool instanceof BodyManipulationTool)
            ((BodyManipulationTool)mouseTool).interactWithBodyBeforeUpdate(dt);
        player.updateProjectiles(dt, bodies);
        portals.resolveBodies();
        player.resetFootContact();
        while(deleteQueue.size() > 0)
                bodies.remove(deleteQueue.remove(0));
        for(RigidBody b : bodies) {
            if(!b.getAtRest())
                b.moveForwardOrBackInTime(dt, gravity);
            b.clearCollidedWithList();
        } 
        portals.splitBodiesInPortal(bodies);
        for(int i = 0; i < bodies.size(); i++) {
            RigidBody b = bodies.get(i);
            //System.out.println(b.getID() + " " + b.getAngularVelocity() + " " + b.getVelocity());
            if(b.getFixed() || b.getAtRest() || b.collisionReactiveOnly()) 
                continue;
            
            for(int j = 0; j < bodies.size(); j++) {
                if(j == i)
                    continue;
                RigidBody b2 = bodies.get(j);
                if(b.collidedWith(b2.getID()) || b2.getCollisionImmune() || onNonCollisionList(b, b2))
                    continue;
                if(b.bodiesColliding(b2)) {
                    Object[] collision = collideBodies(b, b2, dt);
                    if(collision != null) {
                        Point pointOfCollision = (Point)collision[0];
                        RigidBodyLine surface = (RigidBodyLine)collision[1];
                        if(b.tryingToRest() || b2.tryingToRest()) {
                            b.addRestBuddy(b2, pointOfCollision);
                            b2.addRestBuddy(b, pointOfCollision);
                        }
                        if(b2.getAtRest()) {
                            b2.breakRest(false);
                        }
                        if(b instanceof PlayerHitbox) {
                            ((PlayerHitbox)b).checkForFootContact(b2, pointOfCollision, surface);
                        }
                        if(b2 instanceof PlayerHitbox) {
                            ((PlayerHitbox)b2).checkForFootContact(b, pointOfCollision, surface);
                        }
                        if(b instanceof ShadowRigidBody && ((ShadowRigidBody)b).hasFootLine()) {
                            player.checkForFootContact(((ShadowRigidBody)b).getFootLine(), b, b2, pointOfCollision, surface, ((ShadowRigidBody)b).getMainBody());
                        }
                        if(b2 instanceof ShadowRigidBody && ((ShadowRigidBody)b2).hasFootLine()) {
                            player.checkForFootContact(((ShadowRigidBody)b2).getFootLine(), b2, b, pointOfCollision, surface, ((ShadowRigidBody)b2).getMainBody());
                        }
                        
                        
                        b.addToCollidedWithList(b2.getID());
                        b2.addToCollidedWithList(b.getID());
                    }
                }
            }
        }
        player.move(playerRunDirection, dt);
        
        int rotateDirection = 0;
        if(t)
            rotateDirection = 1;
        else if(q)
            rotateDirection = -1;
        
        player.updateGrabTool(dt, mouse, rotateDirection);
        if(jump)
            player.jump();
        player.correctOrientation(dt);
        for(RigidBody b : bodies) {
            b.determineIfAtRest(dt);
        }
        if(mouseTool instanceof BodyManipulationTool)
            ((BodyManipulationTool)mouseTool).interactWithBodyAfterUpdate();
    }
    
    private boolean onNonCollisionList(RigidBody b, RigidBody b2) {
        int b2ID = b2.getID();
        return b.onNonCollisionList(b2ID) || (b instanceof ShadowRigidBody && ((ShadowRigidBody)b).getBase().onNonCollisionList(b2ID))
            || (b2 instanceof ShadowRigidBody && b.onNonCollisionList(((ShadowRigidBody)b2).getBase().getID()) || 
            (b instanceof ShadowRigidBody && b2 instanceof ShadowRigidBody && ((ShadowRigidBody)b).getBase().onNonCollisionList(((ShadowRigidBody)b2).getBase().getID())));
    }
    
    private void update(double dt) {
        physicsUpdate(dt);
        checkActivationPairs();
        updateAssets(dt);
    }
    
    private void checkActivationPairs() {
        for(ActivationPair activatable : activatables) {
            activatable.updatePair();
        }
    }
    
    private void updateAssets(double dt) {
        for(Asset a : addAssetQueue) {
            gameAssets.add(a);
        }
        addAssetQueue.clear();
        
        for(Asset a : removeAssetQueue) {
            gameAssets.remove(a);
        }
        removeAssetQueue.clear();
        
        for(Asset a : gameAssets) {
            a.update(dt, this);
        }
    }
    
    public ArrayList<RigidBody> getBodies() {
        return bodies;
    }
    
    public PortalPair getPortals() {
        return portals;
    }
    
    public ArrayList<RigidBody> getBodyDeleteQueue() {
        return deleteQueue;
    }
    
    public ArrayList<Asset> getAssetDeleteQueue() {
        return removeAssetQueue;
    }
    
    public ArrayList<Asset> getAddAssetQueue() {
        return addAssetQueue;
    }
    
    public PlayerHitbox getPlayer() {
        return player;
    }
    
    public ArrayList<Asset> getAssets() {
        return gameAssets;
    }
    
    private void pushBodiesApart(Object[] penInfo) {
        if(penInfo == null)
            return;

        RigidBodyPoint collidingPoint = (RigidBodyPoint) penInfo[0];
        RigidBodyLine collidingLine = (RigidBodyLine) penInfo[1];
        
        RigidBody lineBody = collidingLine.getBody();
        RigidBody pointBody = collidingPoint.getBody();
        Vector direct = (Vector)penInfo[3];
        double distScaleFactor = 1;
        double dist = (double)penInfo[2];
//        if(actualDist > 10)
//            distScaleFactor = 1 + Math.sqrt((actualDist - 1)/10);
//        if(distScaleFactor > 1)
//            distScaleFactor = 1;
        double pushDist = dist * distScaleFactor;
        double lineBodyPushDist = pushDist/2.0;// * Math.pow(pointBody.getMass(),4)/(Math.pow(lineBody.getMass(),4) + Math.pow(pointBody.getMass(),4));
        double pointBodyPushDist = pushDist - lineBodyPushDist;
        if(lineBody.getFixed() || (lineBody instanceof Door && !((Door)lineBody).getClosing())) {
            pointBodyPushDist = pushDist;
            lineBodyPushDist = 0;
        }
        else if(pointBody.getFixed() || (pointBody instanceof Door && !((Door)pointBody).getClosing()))  {
            pointBodyPushDist = 0;
            lineBodyPushDist = pushDist;
        }
//        else {
//            //System.out.println(normalVector);
//            lineBodyPushDist = pushDist/(1 + pointBody.getMass() * lineBody.getInverseMass());
//            pointBodyPushDist = lineBodyPushDist * pointBody.getMass() * lineBody.getInverseMass();
//        }
        //System.out.println(normalVector.multiplyByScalar(pointBodyPushDist) + " " + normalVector.multiplyByScalar(-lineBodyPushDist));
        Vector pointPushVector = direct.multiplyByScalar(pointBodyPushDist);
        Vector linePushVector = direct.multiplyByScalar(lineBodyPushDist);
        pointBody.push(pointPushVector);
        lineBody.push(linePushVector.multiplyByScalar(-1));
        if(pointBody instanceof Door) {
            pointBody.push(new Vector(-pointPushVector.getXComp(), 0));
            lineBody.push(new Vector(-pointPushVector.getXComp(), 0));
        }
        else if(lineBody instanceof Door) {
            lineBody.push(new Vector(linePushVector.getXComp(), 0));
            pointBody.push(new Vector(linePushVector.getXComp(), 0));
        }
    }
    
    private Object[] collideBodies(RigidBody b1, RigidBody b2, double dt) {
        //Resolve a collision between two colliding bodies
        b1.moveForwardOrBackInTime(-dt, gravity);
        b2.moveForwardOrBackInTime(-dt, gravity);

        boolean alreadyInside = (b1.bodiesColliding(b2))? true : false;
        
//        if(b1.bodiesColliding(b2)) {
//            b1.moveForwardOrBackInTime(dt, gravity);
//            b2.moveForwardOrBackInTime(dt, gravity);
//            pushBodiesApart(getPenInfoForPush(b1, b2));
//            alreadyInside = true;
//        }
//        else {
//            b1.moveForwardOrBackInTime(dt, gravity);
//            b2.moveForwardOrBackInTime(dt, gravity);
//        }
        
        double maxSlackDist = 0.01;
        boolean outside = true;
        int count = 0;
        
        if(alreadyInside) {
            b1.moveForwardOrBackInTime(dt, gravity);
            b2.moveForwardOrBackInTime(dt, gravity);
        }
        
        Object[] penInfo = getPenInfoForCollision(b1, b2);
//        if(penInfo == null) {
//            if(alreadyInside) {
//                b1.moveForwardOrBackInTime(-dt, gravity);
//                b2.moveForwardOrBackInTime(-dt, gravity);
//                penInfo = getPenInfoForPush(b1, b2);
//                pushBodiesApart(penInfo);
//            }
//            return false;
//        }
//        
//      
         
        
        while(!alreadyInside && (penInfo == null || ((double) penInfo[2] > maxSlackDist) && !outside)) {
            dt /= 2;
            count++;
            if(count == 80) {   
                //System.out.println(debugInfo);
                break;
            }
            if(outside) {
                //System.out.println("Forward");
                b1.moveForwardOrBackInTime(dt, gravity);
                b2.moveForwardOrBackInTime(dt, gravity);
            }
            else {
                //System.out.println("Back");
                b1.moveForwardOrBackInTime(-dt, gravity);
                b2.moveForwardOrBackInTime(-dt, gravity);
            }        
            //System.out.println(b1.toString());
            //System.out.println(b2.toString());
            Object[] newInfo = getPenInfoForCollision(b1, b2);
            if(newInfo == null) {
                outside = true;               
            }
            else {
                outside = false;
                penInfo = newInfo;
            }
        }
        if(penInfo == null) {
            penInfo = getPenInfoForPush(b1, b2);
            if(penInfo != null) {
                penInfo[2] = (double)penInfo[2] + 0.1;
                pushBodiesApart(penInfo);
                return new Object[] {penInfo[0], penInfo[1]};
            }
            return null;
        }
        //System.out.println("final pos: " + b1.toString() + "\n" + b2.toString());
        RigidBodyPoint collidingPoint = (RigidBodyPoint) penInfo[0];
        RigidBodyLine collidingLine = (RigidBodyLine) penInfo[1];
        
        RigidBody lineBody = collidingLine.getBody();
        RigidBody pointBody = collidingPoint.getBody();
        
        Line pointsCollidingLine = getCollidingLineIfExists(collidingPoint, collidingLine);
        boolean lineCollision = false;
        if(pointsCollidingLine != null) {
            lineCollision = true;
            collidingPoint = getMidpointOfLineOverlap(collidingLine, pointsCollidingLine);
        }
       
        double resistution;
        if(((b1 instanceof PlayerHitbox && !(((PlayerHitbox)b1).getDead())) || (b2 instanceof PlayerHitbox && !(((PlayerHitbox)b2).getDead()))))
            resistution = 0;
        else if((!b1.getResistutionSupremacy() && !b2.getResistutionSupremacy()) || (b1.getResistutionSupremacy() && b2.getResistutionSupremacy()))
            resistution = (b1.getResistution() + b2.getResistution())/2.0;
        else if(b1.getResistutionSupremacy())
            resistution = b1.getResistution();
        else
            resistution = b2.getResistution();
        
        Vector normalVector = collidingLine.getNormalUnitVector();
        Vector Vap = lineBody.getVelocity().add(lineBody.getLinearVelocityOfPointDueToAngularVelocity(collidingPoint));
        Vector Vbp = pointBody.getVelocity().add(pointBody.getLinearVelocityOfPointDueToAngularVelocity(collidingPoint));
        Vector Vab = Vap.subtract(Vbp);
        
        //System.out.println("Vab: " + Vab.dotProduct(normalVector));
        double numerator = (1 + resistution) * Math.abs(Vab.dotProduct(normalVector));
        //System.out.println(numerator);
        
        double denomenator = //Jesus christ
(b1.getInverseMass() + b2.getInverseMass()) + 
(Math.pow(normalVector.dotProduct(b1.getPerpendicularizedVectorToPoint(collidingPoint)), 2) * b1.getInverseMommentOfInertia())
+ (Math.pow(normalVector.dotProduct(b2.getPerpendicularizedVectorToPoint(collidingPoint)), 2) * b2.getInverseMommentOfInertia());
        
        double j = numerator/denomenator;
        
        double frictionJ;
        Vector t = normalVector.perpendicularize();
        double frictionNumerator = resistution * Vab.dotProduct(t);
        double frictionDenomenator = //Jesus christ
((b1.getInverseMass() + b2.getInverseMass()) + 
(Math.pow(t.dotProduct(b1.getPerpendicularizedVectorToPoint(collidingPoint)), 2) * b1.getInverseMommentOfInertia())
+ (Math.pow(t.dotProduct(b2.getPerpendicularizedVectorToPoint(collidingPoint)), 2) * b2.getInverseMommentOfInertia()));
        
        frictionJ = frictionNumerator/frictionDenomenator;
        double frictionCoefficent = (lineCollision)? (lineBody.getFrictionCoefficent() + pointBody.getFrictionCoefficent())/2.0 : lineBody.getFrictionCoefficent();
        if(Math.abs(frictionJ) > j * frictionCoefficent) {
            if(frictionJ < 0)
                frictionJ = -j * frictionCoefficent;
            else
                frictionJ = j * frictionCoefficent;
        }
        
        if(!(b1 instanceof PlayerHitbox && b1.getOrientation() == 0 && !((PlayerHitbox)b1).getDead()) && !(b2 instanceof PlayerHitbox && b2.getOrientation() == 0 && !((PlayerHitbox)b2).getDead()) && ! (b1 instanceof EnergyPellet) && !(b2 instanceof EnergyPellet)) {
            pointBody.applyImpulseOnPoint(collidingPoint, t.multiplyByScalar(frictionJ));
            lineBody.applyImpulseOnPoint(collidingPoint, t.multiplyByScalar(-frictionJ));
        }
        pointBody.applyImpulseOnPoint(collidingPoint, normalVector.multiplyByScalar(j), lineBody);
        lineBody.applyImpulseOnPoint(collidingPoint, normalVector.multiplyByScalar(-j), pointBody);
        
        penInfo = getPenInfoForPush(b1, b2);
        if(penInfo == null) {
            return null;
        }
        penInfo[2] = (double)penInfo[2] + 0;
        pushBodiesApart(penInfo);
        
//        if(Math.abs(b1.getAngularVelocity()) < 0.0001)
//            b1.setAngularVelocity(0);
//        
//        if(Math.abs(b2.getAngularVelocity()) < 0.0001)
//            b2.setAngularVelocity(0);
        //b1.moveForwardOrBackInTime(-timeReversed, gravity);
        //b2.moveForwardOrBackInTime(-timeReversed, gravity);
        
        return new Object[] {penInfo[0], penInfo[1]};
    }
    
    private RigidBodyPoint getMidpointOfLineOverlap(Line l1, Line l2) {
        double smallX = l1.getSmallestX();
        if(l2.getSmallestX() > smallX)
            smallX = l2.getSmallestX();
        double smallY = l1.getSmallestY();
        if(l2.getSmallestY() > smallY)
            smallY = l2.getSmallestY();
        double bigX = l1.getLargestX();
        if(l2.getLargestX() < bigX)
            bigX = l2.getLargestX();
        double bigY = l1.getLargestY();
        if(l2.getLargestY() < bigX)
            bigY = l2.getLargestY();
        
        return new RigidBodyPoint((bigX + smallX)/2.0, (bigY + smallY)/2.0);
    }
    
    private Line getCollidingLineIfExists(RigidBodyPoint p, Line l) {
        double radianDiffTolerance = 0.0005;
        Line[] pointsLines = p.getLines();
        Line possibleLine = null;
        for(Line collLine : pointsLines) {
            if(Math.abs(Math.tan(collLine.getSlope()) - Math.tan(l.getSlope())) < radianDiffTolerance)
                possibleLine = collLine;
        }
        if(possibleLine == null)
            return null;
        return possibleLine;
    }
    
    private Object[] decidePenInfo(Object[] pointInB1, Object[] pointInB2) {
        if(pointInB1 == null && pointInB2 == null)
            return null;
        
        if(pointInB1 == null) {
            return pointInB2;
        }
        if(pointInB2 == null || (double)pointInB1[2] > (double)pointInB2[2]) {
            return pointInB1;
        }
        
        return pointInB2;
    }
    
    private Object[] getPenInfoForCollision(RigidBody b1, RigidBody b2) {
        Object[] pointInB1 = b1.getPenetratingPoint(b2, false, gravity);
        Object[] pointInB2 = b2.getPenetratingPoint(b1, false, gravity);
        return decidePenInfo(pointInB1, pointInB2);
    }
    
    private Object[] getPenInfoForPush(RigidBody b1, RigidBody b2) {
        Object[] pointInB1 = b1.getPenetratingPoint(b2, true, gravity);
        Object[] pointInB2 = b2.getPenetratingPoint(b1, true, gravity);
        return decidePenInfo(pointInB1, pointInB2);
    }
    
    public void updateNSides(int n) {
        nSides = n;
    }
    
    public void updateFixed(boolean b) {
        createFixedBody = b;
    }
    
    public void updateDrawCOM(boolean b) {
        drawCOM = b;
    }
    
    public void updateDensity(Double d) {
        density = d;
    }
    
    public void updateFriction(Double d) {
        friction = d;
    }
    
    public void updateResistution(Double d) {
        resistution = d;
    }
    
    public void updateGravity(Double d) {
        gravity = new Vector(0, d);
    }
    
    public void updateTimeScale(Double d) {
        timeScale = d;
    }
    
    public void setMouseTool(MouseTool tool) {
        removeMouseListener(mouseTool);
        removeMouseMotionListener(mouseTool);
        mouseTool = tool;
        addMouseListener(mouseTool);
        addMouseMotionListener(mouseTool);
    }
    
    public void updateColor(Color c) {
        bodyColor = c;
        randomColor = false;
    }
    
    public void randomColor() {
        randomColor = true;
    }
    
    public interface MouseTool extends MouseMotionListener, MouseListener {
        
        public abstract void drawTool(Graphics g);
        
    }
    
    private interface BodyManipulationTool extends MouseTool {
        
        public abstract boolean bodySelected(RigidBody b);
        
        public abstract void interactWithBodyBeforeUpdate(double dt);
        
        public abstract void interactWithBodyAfterUpdate();
    }
    
    public class LinearDragTool implements MouseTool {
        private RigidBody selectedBody;
        private double[] COMrelativePos;
        
        public void mouseMoved(MouseEvent e) {}
        
        public void mouseDragged(MouseEvent e) {
            if(selectedBody == null)
                return;
            Point mouse = getMousePos(e.getX(), e.getY());
            Point newCOM = new Point(mouse.x + COMrelativePos[0], mouse.y + COMrelativePos[1]);
            Point currentCOM = selectedBody.getCenterOfMass();
            selectedBody.translate(new Vector(newCOM.x - currentCOM.x, newCOM.y - currentCOM.y));
        }
        
        public void mouseExited(MouseEvent e) {}
        
        public void mouseEntered(MouseEvent e) {}
        
        public void mouseReleased(MouseEvent e) {
            if(selectedBody == null)
                return;
            selectedBody.setAngularVelocity(0);
            selectedBody.setVelocity(new Vector(0, 0));
            addBodyQueue.add(selectedBody);
            selectedBody.breakRest(false);
            selectedBody = null;
        }
        
        public void mouseClicked(MouseEvent e) {}
        
        public void mousePressed(MouseEvent e) {
            for(RigidBody b : bodies) {
                if(b.pointInsideBody(new Point(e.getX(), e.getY()))) {
                    selectedBody = b;
                    if(b instanceof ShadowRigidBody)
                        selectedBody = ((ShadowRigidBody)b).getBase();
                    deleteQueue.add(selectedBody);
                    selectedBody.breakRest(drawCOM);
                    Point COM = selectedBody.getCenterOfMass();
                    Point mouse = getMousePos(e.getX(), e.getY());
                    COMrelativePos = new double[] {COM.x - mouse.x, COM.y - mouse.y};
                    break;
                }
            }
        }
        
        public void drawTool(Graphics g) {
            if(selectedBody != null)
                selectedBody.draw(g, camera);
        }
        
    }
    
    public class ForceTool implements BodyManipulationTool {
        protected RigidBody selectedBody;
        protected Point selectedDragPoint;
        protected Point cursor = new Point(0, 0);
        private Point selectedBodyPreviousCOM;
        private double selectedBodyPreviousOrientation;
        
        public boolean bodySelected(RigidBody b) {
            return selectedBody == b;
        }
        
        public void mouseMoved(MouseEvent e) {
            cursor.x = e.getX();
            cursor.y = e.getY();
        }
        
        public void mouseDragged(MouseEvent e) {
            cursor = getMousePos(e.getX(), e.getY());
        }
        
        public void mouseExited(MouseEvent e) {}
        
        public void mouseEntered(MouseEvent e) {}
        
        public void mouseReleased(MouseEvent e) {
            selectedBody = null;
        }
        
        public void mouseClicked(MouseEvent e) {}
        
        public void mousePressed(MouseEvent e) {
            for(RigidBody b : bodies) {
                Point mouse = getMousePos(e.getX(), e.getY());
                if(b.pointInsideBody(mouse)) {
                    selectedBody = b;
                    if(b instanceof ShadowRigidBody)
                        selectedBody = ((ShadowRigidBody)b).getBase();
                    selectedBodyPreviousCOM = selectedBody.getCenterOfMass();
                    selectedBodyPreviousOrientation = selectedBody.getOrientation();
                    selectedDragPoint = mouse;
                    break;
                }
            }
        }
        
        public void interactWithBodyBeforeUpdate(double dt) {
            if(selectedBody == null)
            return;
            
            selectedBody.breakRest(false);
            double accelerationScale = 2;
            Vector v = new Vector(cursor.x - selectedDragPoint.x, cursor.y - selectedDragPoint.y);
            Vector impulse = v.multiplyByScalar(accelerationScale * dt * selectedBody.getMass());
            selectedBody.applyImpulseOnPoint(selectedDragPoint, impulse);
            //Dampining
            //selectedBody.setVelocity(selectedBody.getVelocity().multiplyByScalar(0.9));
            //selectedBody.setAngularVelocity(selectedBody.getAngularVelocity() * 0.9);
        }
        
        public void interactWithBodyAfterUpdate() {
            if(selectedBody == null)
            return;
            
            
            Point centerOfMass = selectedBody.getCenterOfMass();
              
            selectedDragPoint.x += centerOfMass.x - selectedBodyPreviousCOM.x;
            selectedDragPoint.y += centerOfMass.y - selectedBodyPreviousCOM.y;

            double theta = selectedBody.getOrientation() - selectedBodyPreviousOrientation;


            double rotX = Math.cos(theta) * (selectedDragPoint.x - centerOfMass.x) - Math.sin(theta) * (selectedDragPoint.y - centerOfMass.y);
            double rotY = Math.sin(theta) * (selectedDragPoint.x - centerOfMass.x) + Math.cos(theta) * (selectedDragPoint.y - centerOfMass.y);

            selectedDragPoint.x = centerOfMass.x + rotX;
            selectedDragPoint.y = centerOfMass.y + rotY;

            selectedBodyPreviousCOM = new Point(centerOfMass.x, centerOfMass.y);
            selectedBodyPreviousOrientation = selectedBody.getOrientation();
        }
        
        public void drawTool(Graphics g) {
            if(selectedBody == null)
            return;
            g.setColor(Color.BLACK);
            g.fillOval((int) selectedDragPoint.x - 6, (int) selectedDragPoint.y - 6, 12, 12);
            g.setColor(Color.WHITE);
            g.drawOval((int) selectedDragPoint.x - 4, (int) selectedDragPoint.y - 4, 8, 8);
            g.setColor(Color.white);
            g.drawLine((int) cursor.x, (int) cursor.y, (int) selectedDragPoint.x, (int) selectedDragPoint.y);
        }
    }
    
    public class ForceDragTool extends ForceTool {
        public void interactWithBodyBeforeUpdate(double dt) {
            if(selectedBody == null)
            return;
            
            selectedBody.breakRest(false);
        
            double accelerationScale = 200;
            Vector v = new Vector(cursor.x - selectedDragPoint.x, cursor.y - selectedDragPoint.y);
            Vector impulse = v.multiplyByScalar(accelerationScale * dt * selectedBody.getMass());
            selectedBody.applyImpulseOnPoint(selectedDragPoint, impulse);
            double dampen = 1 - 0.25 * dt/0.01;
            selectedBody.setVelocity(selectedBody.getVelocity().multiplyByScalar(dampen));
            selectedBody.setAngularVelocity(selectedBody.getAngularVelocity() * dampen);
        }
    }
    
    public class CustomBodyCreatorTool implements MouseTool {
        private ArrayList<RigidBodyPoint> customBodyPoints = new ArrayList<>();
        
        public void mouseMoved(MouseEvent e) {}
        
        public void mouseExited(MouseEvent e) {}
        
        public void mouseEntered(MouseEvent e) {}
        
        public void mouseReleased(MouseEvent e) {}
        
        public void mouseClicked(MouseEvent e) {}
        
        public void mouseDragged(MouseEvent e) {
            if(e.isMetaDown())
                return;
            Point mouse = getMousePos(e.getX(), e.getY());
            if(customBodyPoints.isEmpty())
                customBodyPoints.add(new RigidBodyPoint(mouse.x, mouse.y));   
            else {
                Point p = customBodyPoints.get(customBodyPoints.size() - 1);
                if(Math.pow(p.x - mouse.x, 2) + Math.pow(p.y - mouse.y, 2) > 250)
                    customBodyPoints.add(new RigidBodyPoint(mouse.x, mouse.y));
            }
        }
        
        public void mousePressed(MouseEvent e) {
            randomizeColorIfNececary();
            if(e.isMetaDown()) { 
                if(customBodyPoints.size() > 2) {
                    RigidBodyPoint[] points = new RigidBodyPoint[customBodyPoints.size()];
                    for(int i = 0; i < points.length; i++) {
                        points[i] = customBodyPoints.get(i);
                    }
                    addBodyQueue.add(new RigidBody(points, new Vector(0, 0), 0, density, resistution, friction, createFixedBody, giveID(), bodyColor, drawCOM));
                }
                customBodyPoints.clear();
            }
            else {
                Point mouse = getMousePos(e.getX(), e.getY());
                customBodyPoints.add(new RigidBodyPoint(mouse.x, mouse.y));
            }
        }
        
        public void drawTool(Graphics g) {
            if(customBodyPoints.size() > 1) {
                for(int i = 0; i < customBodyPoints.size() - 1; i++)
                    g.drawLine((int)customBodyPoints.get(i).x, (int)customBodyPoints.get(i).y, (int)customBodyPoints.get(i + 1).x, (int)customBodyPoints.get(i + 1).y);
            }
        }
    }
    
    public class FirePortalTool implements MouseTool {
        public void mouseMoved(MouseEvent e) {}
        
        public void mouseDragged(MouseEvent e) {}
        
        public void mouseExited(MouseEvent e) {}
        
        public void mouseEntered(MouseEvent e) {}
        
        public void mouseReleased(MouseEvent e) {}
        
        public void mouseClicked(MouseEvent e) {}
        
        public void mousePressed(MouseEvent e) {
            int num = (e.isMetaDown())? 1 : 0;
            player.firePortalProjectile(getMousePos(e.getX(), e.getY()), num);
        }
        
        public void drawTool(Graphics g) {};
    }
    
    public class PickUpTool implements MouseTool {
        public void mouseMoved(MouseEvent e) {}
        
        public void mouseDragged(MouseEvent e) {}
        
        public void mouseExited(MouseEvent e) {}
        
        public void mouseEntered(MouseEvent e) {}
        
        public void mouseReleased(MouseEvent e) {}
        
        public void mouseClicked(MouseEvent e) {}
        
        public void mousePressed(MouseEvent e) {
            if(e.isMetaDown())
                player.releaseObject();
            else
                player.pickUpObject(bodies, mouse);
        }
        
        public void drawTool(Graphics g) {};
    }
    
    public class RectangleCreatorTool implements MouseTool {
        private Point setPoint;
        private Point cursor;
        private RigidBodyPoint[] points;
        
        public void mouseMoved(MouseEvent e) {}
        
        public void mouseExited(MouseEvent e) {}
        
        public void mouseEntered(MouseEvent e) {}
        
        public void mouseReleased(MouseEvent e) {
            randomizeColorIfNececary();
            if(points != null)
                addBodyQueue.add(new RigidBody(points, new Vector(0, 0), 0, density, resistution, friction, createFixedBody, giveID(), bodyColor, drawCOM));
            points = null;
        }
        
        public void mouseClicked(MouseEvent e) {}
        
        public void mouseDragged(MouseEvent e) {
            cursor = getMousePos(e.getX(), e.getY());
            points = new RigidBodyPoint[4];
            points[0] = new RigidBodyPoint(setPoint.x, setPoint.y);
            points[1] = new RigidBodyPoint(cursor.x, setPoint.y);
            points[2] = new RigidBodyPoint(cursor.x, cursor.y);
            points[3] = new RigidBodyPoint(setPoint.x, cursor.y);
        }
        
        public void mousePressed(MouseEvent e) {
            setPoint = getMousePos(e.getX(), e.getY());
            cursor = new Point(setPoint.x, setPoint.y);
        }
        
        public void drawTool(Graphics g) {
            try {
                if(points == null)
                    return;
                g.setColor(Color.white);
                for(int i = 0; i < points.length; i++) {
                    Point p1 = points[i];
                    Point p2 = (i == points.length - 1) ? points[0] : points[i + 1];
                    g.drawLine((int) p1.x, (int) p1.y, (int) p2.x, (int) p2.y);
                }
            }
            catch(NullPointerException e) {
                // 10/10 fix
            }
        }
    }
    
    public class DeleteTool implements MouseTool {
        public void mouseMoved(MouseEvent e) {}
        
        public void mouseDragged(MouseEvent e) {}
        
        public void mouseExited(MouseEvent e) {}
        
        public void mouseEntered(MouseEvent e) {}
        
        public void mouseReleased(MouseEvent e) {}
        
        public void mouseClicked(MouseEvent e) {}
        
        public void mousePressed(MouseEvent e) {
            for(RigidBody b : bodies) {
                if(b.pointInsideBody(getMousePos(e.getX(), e.getY()))) {
                    RigidBody selectedBody = b;
                    if(b instanceof ShadowRigidBody)
                        selectedBody = ((ShadowRigidBody)b).getBase();
                    selectedBody.breakRest(true);
                    deleteQueue.add(selectedBody);
                    break;
                }
            }
        }
        
        public void drawTool(Graphics g) {}
    }
    
    public class NSidedPolygonCreatorTool implements MouseTool {
        private Point center;
        private Point cursor;
        private RigidBodyPoint[] points;
        
        public void mouseMoved(MouseEvent e) {}
        
        public void mouseExited(MouseEvent e) {}
        
        public void mouseEntered(MouseEvent e) {}
        
        public void mouseReleased(MouseEvent e) {
            randomizeColorIfNececary();
            if(points != null) {
                RigidBody bod = new RigidBody(points, new Vector(0, 0), 0, density, resistution, friction, createFixedBody, giveID(), bodyColor, drawCOM);
                addBodyQueue.add(bod);
            }
            points = null;
        }
        
        public void mouseClicked(MouseEvent e) {}
        
        public void mouseDragged(MouseEvent e) {
            cursor = getMousePos(e.getX(), e.getY());
            double angInterval = 2 * Math.PI / nSides;

            points = new RigidBodyPoint[nSides];
            points[0] = new RigidBodyPoint(cursor.x, cursor.y);

            for(int i = 1; i < nSides; i++) {
                Point p = points[i - 1];
                double x = center.x + Math.cos(angInterval) * (p.x - center.x) - Math.sin(angInterval) * (p.y - center.y);
                double y = center.y + Math.sin(angInterval) * (p.x - center.x) + Math.cos(angInterval) * (p.y - center.y);
                points[i] = new RigidBodyPoint(x, y);
            }
        }
        
        public void mousePressed(MouseEvent e) {
            
            center = getMousePos(e.getX(), e.getY());
            cursor = new Point(center.x, center.y);
        }
        
        public void drawTool(Graphics g) {
            try {
                if(points == null)
                    return;
                g.setColor(Color.white);
                g.drawLine((int)center.x, (int)center.y, (int)cursor.x, (int)cursor.y);
                for(int i = 0; i < points.length; i++) {
                    Point p1 = points[i];
                    Point p2 = (i == points.length - 1) ? points[0] : points[i + 1];
                    g.drawLine((int) p1.x, (int) p1.y, (int) p2.x, (int) p2.y);
                }
            }
            catch(NullPointerException e) {
                // 10/10 fix
            }
        }
    }   
    
    private void randomizeColorIfNececary() {
        if(randomColor) {
            switch((int) (Math.random() * 6)) {
                case 0:
                    bodyColor = Color.RED;
                    break;
                case 1:
                    bodyColor = Color.YELLOW;
                    break;
                case 2:
                    bodyColor = Color.GREEN;
                    break;
                case 3:
                    bodyColor = Color.BLUE;
                    break;
                case 4:
                    bodyColor = Color.MAGENTA;
                    break;
                case 5:
                    bodyColor = Color.ORANGE;
                    break;
            }
        }
    }
    
    public Point getMousePos(double x, double y) {
        return new Point(x - camera.getWidth() + camera.getPosition().x, y - camera.getHeight() + camera.getPosition().y);
    }
    
    public PhysicsPanel(File f) {
        this(-1, f);
    }
    
    public PhysicsPanel(int currentLevel) {
        this(currentLevel, null);
    }
    
    public PhysicsPanel(int currentLevel, File f) {
        file = f;
        this.currentLevel = currentLevel;
        frame = new JFrame();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize((int) screenSize.getWidth(), (int) screenSize.getHeight());
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.setContentPane(this);
        this.addMouseListener(mouseTool);
        this.addMouseMotionListener(new MouseAdapter() {
            
            public void mouseMoved(MouseEvent e) {
                cursor = new Point(e.getX(), e.getY());
                mouse = getMousePos(e.getX(), e.getY());
            }
            
            public void mouseDragged(MouseEvent e) {
                mouseMoved(e);
            }
        });
        this.addMouseMotionListener(mouseTool);
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                switch(e.getKeyCode()) {
                    case KeyEvent.VK_W:
                        jump = true;
                        break;
                    case KeyEvent.VK_A:
                        a = true;
                        playerRunDirection = -1;
                        break;
                    case KeyEvent.VK_D:
                        d = true;
                        playerRunDirection = 1;
                        break;
                    case KeyEvent.VK_Z:
                        z = true;
                        break;
                    case KeyEvent.VK_E:
                        if(!E) {
                            if(player.isHoldingObject())
                                player.releaseObject();
                            else
                                player.pickUpObject(bodies, mouse);
                        }
                        E = true;
                        break;
                    case KeyEvent.VK_R:
                        r = true;
                        break;
                    case KeyEvent.VK_Q:
                        q = true;
                        break;
                    case KeyEvent.VK_T:
                        t = true;
                        break;
                    case KeyEvent.VK_SPACE:
                        space = true;
                        break;
                }
            } 
            
            public void keyReleased(KeyEvent e) {
                switch(e.getKeyCode()) {
                    case KeyEvent.VK_W:
                        jump = false;
                        break;
                    case KeyEvent.VK_A:
                        a = false;
                        if(d)
                            playerRunDirection = 1;
                        else
                            playerRunDirection = 0;
                        break;
                    case KeyEvent.VK_D:
                        d = false;
                        if(a)
                            playerRunDirection = -1;
                        else
                            playerRunDirection = 0;
                        break;
                    case KeyEvent.VK_Z:
                        z = false;
                        break;
                    case KeyEvent.VK_E:
                        E = false;
                        break;
                    case KeyEvent.VK_R:
                        r = false;
                        break;
                    case KeyEvent.VK_Q:
                        q = false;
                        break;
                    case KeyEvent.VK_T:
                        t = false;
                        break;
                    case KeyEvent.VK_SPACE:
                        space = false;
                        break;
                }
            } 
        });
        frame.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                requestFocusInWindow();
            } 
        });
        frame.setVisible(true);
        this.setVisible(true);
        fuckingPeiceOfShitCuntFuck = frame;
        start();
    }
    
    public static void main(String[] args) {
        PhysicsPanel panel = new PhysicsPanel(1);
        //UserInterfaceFrame ui = new  UserInterfaceFrame(panel);
        panel.requestFocus();
    }
    
}
