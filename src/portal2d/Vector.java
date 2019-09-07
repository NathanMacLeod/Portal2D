package portal2d;
/*
 * File added by Nathan MacLeod 2019
 */

/**
 *
 * @author Nathan
 */
public class Vector {
    private double xComp;
    private double yComp;
    private double magnitude = -1;
    
    public Vector(double xComp, double yComp) {
        this.xComp = xComp;
        this.yComp = yComp;
    }
    
    public double getMagnitude() {
        if(magnitude == -1)
            magnitude = Math.sqrt((xComp * xComp) + (yComp * yComp));
        return magnitude;
    }
    
    public Vector getUnitVector() {
        double mag = getMagnitude();
        return new Vector(xComp/mag, yComp/mag);
    }
    
    public Vector rotate(double theta) {
        //rotates about origin
        double newX = Math.cos(theta) * xComp - Math.sin(theta) * yComp;
        double newY = Math.sin(theta) * xComp + Math.cos(theta) * yComp;
        return new Vector(newX, newY);
    }
    
    public double getSquaredMagnitude() {
        return (xComp * xComp) + (yComp * yComp);
    }
    
    public double getDirection() {
        if(xComp == 0) {
            if(yComp > 0)
                return Math.PI/2.0;
            return 3 * Math.PI/2.0;
        }
        double angle = Math.atan(yComp/xComp);
        if(yComp < 0)
            angle += Math.PI * 2;
        if(xComp < 0 && yComp < 0)
            angle += Math.PI;
        else if(xComp < 0 && yComp >= 0)
            angle += Math.PI;
        while(angle > Math.PI * 2)
            angle -= Math.PI * 2;
        while(angle < 0)
            angle += Math.PI * 2;
        return angle;
    }
    
    public double getXComp() {
        return xComp;
    }
    
    public double getYComp() {
        return yComp;
    }
    
    public Vector add(Vector v) {
        return new Vector(xComp + v.getXComp(), yComp + v.getYComp());
    }
    
    public Vector subtract(Vector v) {
        return add(v.multiplyByScalar(-1));
    }
    
    public Vector perpendicularize() {
        double xPart = Math.abs(yComp);
        double yPart = Math.abs(xComp);
        if(yComp > 0) 
            xPart *= -1;
        if(xComp < 0)
            yPart *= -1;
        return new Vector(xPart, yPart);
    }
    
    public double dotProduct(Vector v) {
        return (v.xComp * xComp) +  (v.yComp * yComp);
    }    
    
    public Vector multiplyByScalar(double scal) {
        return new Vector(xComp * scal, yComp * scal);
    }
    
    public boolean equals(Object o) {
        if(!(o instanceof Vector))
            return false;
        Vector v = (Vector) o;
        return v.getXComp() == xComp && v.getYComp() == yComp;
    }
    
    public boolean basicallyIs(Vector v) {
        return Math.abs(Math.round(v.getDirection()) - Math.round(getDirection())) < 0.03;
    }
    
    public static void main(String[] args) {
        Vector v = new Vector(-1, 1);
        System.out.println(v.toString());
    }
    
    public String toString() {
        return "X component: " + xComp + ", yComponenet: " + yComp + ", Magnitude: " + getMagnitude() + ", Direction: " + getDirection() * 180.0/Math.PI + " degrees";
    }
}
