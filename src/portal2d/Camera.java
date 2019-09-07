/*
 * File added by Nathan MacLeod 2019
 */
package portal2d;

/**
 *
 * @author Nathan
 */
public class Camera {
    private Point position;
    private double rWidth, rHeight;
    
    public Camera(double width, double height) {
        rWidth = width;
        rHeight = height;
        position = new Point(0, 0);
    }
    
    public void translate(Vector v) {
        position.x += v.getXComp();
        position.y += v.getYComp();
    }
    
    public void setPosition(Point p) {
        position.x = p.x;
        position.y = p.y;
    }
    
    public void trackPoint(double dt, double speedFactor, double curveVal, Point p) {
        Vector cameraToPlayer = new Vector(p.x - position.x, p.y - position.y);
        Vector travelVector = cameraToPlayer.multiplyByScalar(dt * speedFactor * Math.pow(cameraToPlayer.getMagnitude(), 1/curveVal));
        if(travelVector.getSquaredMagnitude() > cameraToPlayer.getSquaredMagnitude()) {
            setPosition(p);
        }
        else {
            translate(travelVector);
        }
    }
    
    public Point getPosition() {
        return position;
    }
    
    public double getWidth() {
        return rWidth;
    }
    
    public double getHeight() {
        return rHeight;
    }
    
}
