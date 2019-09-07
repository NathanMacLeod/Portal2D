/*
 * File added by Nathan MacLeod 2019
 */
package portal2d;

/**
 *
 * @author Nathan
 */
public class Ray {
    Vector vector;
    Point origin;
    
    public Ray(Vector v, Point p) {
        vector = v;
        origin = p;
    }
}
