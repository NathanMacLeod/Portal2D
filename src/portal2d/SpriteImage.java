/*
 * File added by Nathan MacLeod 2019
 */
package portal2d;

import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.Graphics2D;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
/**
 *
 * @author Nathan
 */
public class SpriteImage {
    private BufferedImage image;
    private Vector centerOffset;
    private double radialDist;
    
    public SpriteImage(BufferedImage image, Vector centerOffset) {
        this.image = image;
        this.centerOffset = centerOffset;
        calculateRadialDist();
    }
    
    public SpriteImage(String fileName, double size, Vector centerOffset) {
        this.centerOffset = centerOffset;
        try {
            BufferedImage spriteImage = ImageIO.read(Class.class.getResourceAsStream("/sprites/" + fileName));
            image = new BufferedImage((int)(size), (int)(size), BufferedImage.TYPE_INT_ARGB);
            Graphics imageGraphics = image.getGraphics();
            imageGraphics.drawImage(spriteImage, 0, 0, (int)(size), (int)(size), 0, 0, spriteImage.getWidth(), spriteImage.getHeight(), null);
            calculateRadialDist();
        }
        catch(IOException e) {
            System.out.println("cant find file: " + fileName);
            System.exit(0);
        }
    }
    
    private void calculateRadialDist() {
        radialDist = Math.sqrt(image.getHeight() * image.getHeight() + image.getWidth() * image.getWidth()) + centerOffset.getMagnitude();
    }
    
    public BufferedImage getRotatedBaseImage(double rotation) {
        BufferedImage newImage = new BufferedImage((int)(image.getWidth()), (int)(image.getHeight()), BufferedImage.TYPE_INT_ARGB);
        AffineTransform transformation = new AffineTransform();
        transformation.translate(image.getWidth()/2.0, image.getHeight()/2.0);
        transformation.rotate(rotation);
        transformation.translate(-image.getWidth()/2.0, -image.getHeight()/2.0);
        ((Graphics2D)newImage.getGraphics()).drawImage(image, transformation, null);
        return newImage;
    }
    
    public BufferedImage getImage() {
        return image;
    }
    
    public void flip() {
        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        newImage.getGraphics().drawImage(image, 0, 0, image.getWidth(), image.getHeight(), image.getWidth(), 0, 0, image.getHeight(), null);
        image = newImage;
    }
    
    public boolean spriteInFrame(Camera c, Point position) {
        return Math.abs(c.getPosition().x - position.x) < c.getWidth() + radialDist && Math.abs(c.getPosition().y - position.y) < c.getHeight() + radialDist;
    }
    
    public void drawNormallyCustomImage(Graphics g, Camera c, Point position, double orientation, BufferedImage image) {
        if(spriteInFrame(c, position))
            drawNormally(g, c, position, orientation, image);
    }
    
    public void drawNormally(Graphics g, Camera c, Point position, double orientation) {
        if(spriteInFrame(c, position))
            drawNormally(g, c, position, orientation, image);
    }
    
    public void drawNormally(Graphics g, Point position, double orientation) {
        drawNormally(g, new Camera(0, 0), position, orientation, image);
    }
    
    private void drawNormally(Graphics g, Camera c, Point position, double orientation, BufferedImage image) {
        if(!spriteInFrame(c, position))
            return;
        Graphics2D g2D = (Graphics2D) g;
        AffineTransform transformation = new AffineTransform();
        Point cp = c.getPosition();
        transformation.translate(position.x -cp.x + c.getWidth(), position.y -cp.y + c.getHeight());
        transformation.rotate(orientation);
        transformation.translate(- image.getWidth()/2.0 - centerOffset.getXComp(), - image.getHeight()/2.0 - centerOffset.getYComp());
        g2D.drawImage(image, transformation, null);
    }
    
    public void drawCustumSpriteSplitInPortal(Graphics g, Camera c, Point position, double orientation, Portal intersectingPortal, Portal otherPortal, PortalPair portals, BufferedImage otherImage) {
        if(spriteInFrame(c, position))
            drawSpriteSplitInPortal(g, c, position, orientation, intersectingPortal, otherPortal, portals, otherImage);
    }
    
    public void drawSpriteSplitInPortal(Graphics g, Camera c, Point position, double orientation, Portal intersectingPortal, Portal otherPortal, PortalPair portals) {
        if(spriteInFrame(c, position))
            drawSpriteSplitInPortal(g, c, position, orientation, intersectingPortal, otherPortal, portals, image);
    }
    
    private void drawSpriteSplitInPortal(Graphics g, Camera c, Point position, double orientation, Portal intersectingPortal, Portal otherPortal, PortalPair portals, BufferedImage image) {
        if(!spriteInFrame(c, position))
            return;
        BufferedImage rotatedImage = new BufferedImage(image.getWidth() * 2, image.getHeight() * 2, BufferedImage.TYPE_INT_ARGB);
        double angleOff = Math.acos(intersectingPortal.getDownVector().dotProduct(new Vector(0, 1)));
        if(intersectingPortal.getDownVector().getXComp() > 0)
            angleOff *= -1;
        AffineTransform transform = new AffineTransform();
        transform.translate(rotatedImage.getWidth()/2.0, rotatedImage.getHeight()/2.0);
        transform.rotate(orientation - angleOff);
        transform.translate(-image.getWidth()/2.0 - centerOffset.getXComp(), -image.getHeight()/2.0 - centerOffset.getYComp());
        ((Graphics2D)rotatedImage.getGraphics()).drawImage(image, transform, null);
        double portalPlaneDist = new Vector(position.x - intersectingPortal.getCenter().x, position.y - intersectingPortal.getCenter().y).dotProduct(intersectingPortal.getDirection());
        BufferedImage mainSide = new BufferedImage(image.getWidth() * 2, image.getHeight() * 2, BufferedImage.TYPE_INT_ARGB);
        BufferedImage shadowSide = new BufferedImage(image.getWidth() * 2, image.getHeight() * 2, BufferedImage.TYPE_INT_ARGB);
        boolean portalIsLeftBound = Math.cos(intersectingPortal.getDirection().getDirection() - angleOff) > 0;
        
        
        
        if(portalIsLeftBound) {
            portalPlaneDist = mainSide.getWidth()/2.0 - portalPlaneDist;
            mainSide.getGraphics().drawImage(rotatedImage, (int)portalPlaneDist, 0, mainSide.getWidth(), mainSide.getHeight(), (int)portalPlaneDist, 0, mainSide.getWidth(), mainSide.getHeight(), null);
            shadowSide.getGraphics().drawImage(rotatedImage, 0, 0, (int)portalPlaneDist, mainSide.getHeight(), 0, 0, (int)portalPlaneDist, mainSide.getHeight(), null);
        }
        else {
            portalPlaneDist += mainSide.getWidth()/2.0;
            mainSide.getGraphics().drawImage(rotatedImage, 0, 0, (int)portalPlaneDist, mainSide.getHeight(), 0, 0, (int)portalPlaneDist, mainSide.getHeight(), null);
            shadowSide.getGraphics().drawImage(rotatedImage, (int)portalPlaneDist, 0, mainSide.getWidth(), mainSide.getHeight(), (int)portalPlaneDist, 0, mainSide.getWidth(), mainSide.getHeight(), null);
        }
        
        AffineTransform imageToWorldTransform = new AffineTransform();
        imageToWorldTransform.translate(position.x - c.getPosition().x + c.getWidth(), position.y - c.getPosition().y + c.getHeight());
        imageToWorldTransform.rotate(angleOff);
        imageToWorldTransform.translate(-mainSide.getWidth()/2.0, -mainSide.getHeight()/2.0);
        
        Graphics2D g2D = (Graphics2D) g;
        
        g2D.drawImage(mainSide, imageToWorldTransform, null);
        
        if(portals.needsXRotation(intersectingPortal, otherPortal)) {
            BufferedImage newShadow = new BufferedImage(shadowSide.getHeight(), shadowSide.getWidth(), BufferedImage.TYPE_INT_ARGB);
            newShadow.getGraphics().drawImage(shadowSide, 0, 0, newShadow.getWidth(), mainSide.getHeight(), newShadow.getWidth(), 0, 0, mainSide.getHeight(), null);
            shadowSide = newShadow;
        }
        
        imageToWorldTransform = new AffineTransform();
        Point shadowPoint = portals.teleportPointToOtherPortal(position, intersectingPortal, otherPortal);
        imageToWorldTransform.translate(shadowPoint.x - c.getPosition().x + c.getWidth(), shadowPoint.y - c.getPosition().y + c.getHeight());
        imageToWorldTransform.rotate(angleOff + otherPortal.getDownVector().getDirection() - intersectingPortal.getDownVector().getDirection());
        imageToWorldTransform.translate(-mainSide.getWidth()/2.0, -mainSide.getHeight()/2.0);
        
        g2D.drawImage(shadowSide, imageToWorldTransform, null);
    }
}
