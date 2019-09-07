/*
 * File added by Nathan MacLeod 2019
 */
package portal2d;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
/**
 *
 * @author Nathan
 */
public class FloatingText implements Asset {
    private Point position;
    private double size, width;
    private String message;
    private Font font;
    
    public FloatingText(Point position, String message) {
        this.position = position;
        this.message = message;
        size = 30;
        font = new Font("Futura", Font.PLAIN, (int) size);
        width = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB).getGraphics().getFontMetrics(font).stringWidth(message);
    }
    
    public static void drawTextOnScreen(Graphics g, String message, int[] position, int size, Color color) {
        Font font = new Font("Futura", Font.PLAIN, size);
        double width = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB).getGraphics().getFontMetrics(font).stringWidth(message);
        g.setFont(font);
        g.setColor(color);
        g.drawString(message, position[0] - (int)width/2, position[1]);
    }
    
    public void editText(char newChar, boolean delete) {
        if(delete && message.length() > 0)
            message = message.substring(0, message.length() - 1);
        else if((int)newChar != 65535) {
            message = message + newChar;
        }
        width = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB).getGraphics().getFontMetrics(font).stringWidth(message);
    }
    
    public String getMessage() {
        return message;
    }
    
    public RigidBody getBoundingBox() {
        RigidBodyPoint[] boxNodes = new RigidBodyPoint[] {
            new RigidBodyPoint(position.x - width/2.0, position.y - size),
            new RigidBodyPoint(position.x + width/2.0, position.y - size),
            new RigidBodyPoint(position.x + width/2.0, position.y),
            new RigidBodyPoint(position.x - width/2.0, position.y)
        };
        return new RigidBody(boxNodes, new Point(position.x, position.y), new Vector(0, 0), 0, 0, 0, 0, false, 0, null, false);
    }
    
    public void update(double dt, PhysicsPanel panel) {
        
    }
    
    public void draw(Graphics g, Camera c) {
        Font font = new Font("Futura", Font.PLAIN, (int) size);
        g.setFont(font);
        g.setColor(Color.BLACK);
        g.drawString(message, (int) (position.x - width/2.0 - c.getPosition().x + c.getWidth()), (int) (position.y - c.getPosition().y + c.getHeight()));
    }
    
    public Vector getVectorToBase() {
        return new Vector(0, 0);
    }
    
    public void draw(Graphics g, Camera c, Point position, double orientation) {
        Font font = new Font("Futura", Font.PLAIN, (int) size);
        g.setFont(font);
        g.setColor(Color.LIGHT_GRAY);
        g.drawString(message, (int) (position.x - width/2.0 - c.getPosition().x + c.getWidth()), (int) (position.y - c.getPosition().y + c.getHeight()));
    }
}
