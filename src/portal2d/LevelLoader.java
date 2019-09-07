/*
 * File added by Nathan MacLeod 2019
 */
package portal2d;
import java.awt.Color;
import java.util.StringJoiner;
import java.util.Scanner;
import java.util.ArrayList;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
/**
 *
 * @author Nathan
 */
public class LevelLoader {
    
    public static ArrayList<RigidBody> readFile(String fileName) {
        try {
            ArrayList<RigidBody> bodies = new ArrayList<>();
            Scanner scanner = new Scanner(new File("./levels/RigidBodyLevels/" + fileName));
            while(scanner.hasNext())
                bodies.add(loadBody(scanner.nextLine(), -1));
            return bodies;
        }
        catch(FileNotFoundException e) {
            System.out.println("File Not Found");
            return null;
        }
    }
    
    public static void writeSceneToFile(ArrayList<RigidBody> bodies, String fileName) {
        try {
            PrintWriter writer = new PrintWriter("./levels/RigidBodyLevels/" + fileName);
            for(RigidBody b : bodies) {
                writer.println(encodeBody(b));
            }
            writer.close();
        } 
        catch(FileNotFoundException e) {
            try {
                
                new File("./levels/RigidBodyLevels" + fileName).createNewFile();
                writeSceneToFile(bodies, fileName);
            }
            catch(IOException er) {
                System.out.println("Failed to create file\n" + er.toString());
            }
        }
    }
    
    public static RigidBody loadBody(String info, int givenID) {
        //RigidBodyPoint[] nodes, Point centerOfMass, double mass, double rotationalInertia, double friction, double resistution, boolean fixed, int ID, Color color, boolean drawCenterOfMass
        String[] elements = info.split(",", 0);
        String[] pointInfo = elements[0].split(";", 0);
        RigidBodyPoint[] nodes = new RigidBodyPoint[pointInfo.length/2];//
        for(int i = 0; i < pointInfo.length; i += 2) {
            RigidBodyPoint p = new RigidBodyPoint(Double.parseDouble(pointInfo[i]), Double.parseDouble(pointInfo[i + 1]));
            nodes[i/2] = p;
        }
        Point centerOfMass = new Point(Double.parseDouble(elements[1]), Double.parseDouble(elements[2]));//
        double mass = Double.parseDouble(elements[3]);//
        double rotationalInertia = Double.parseDouble(elements[4]);//
        double friction = Double.parseDouble(elements[5]);//
        double resistution = Double.parseDouble(elements[6]);
        boolean fixed = elements[7].equals("1");
        int ID = (givenID != -1)? givenID : Integer.parseInt(elements[8]);
        Color color = null;
        switch(elements[9]) {
            case "r":
                color = Color.red;
                break;
            case "o":
                color = Color.orange;
                break;
            case "y":
                color = Color.yellow;
                break;
            case "g":
                color = Color.green;
                break;
            case "b":
                color = Color.blue;
                break;
            case "m":
                color = Color.magenta;
                break;
        }
        boolean drawCenterOfMass = elements[10].equals("1");
        return new RigidBody(nodes, new Vector(0, 0), mass, rotationalInertia, friction, resistution, fixed, ID, color, drawCenterOfMass);
    }
    
    public static String encodeBody(RigidBody body) {
        //RigidBodyPoint[] nodes, Point centerOfMass, double mass, double rotationalInertia, double friction, double resistution, boolean fixed, int ID, Color color, boolean drawCenterOfMass
        StringJoiner elements = new StringJoiner(",", "", "");
        StringJoiner nodeData = new StringJoiner(";", "", "");
        for(Point p : body.getNodes()) {
            nodeData.add("" + p.x);
            nodeData.add("" + p.y);
        }
        elements.add(nodeData.toString());
        elements.add("" + body.getCenterOfMass().x);
        elements.add("" + body.getCenterOfMass().y);
        elements.add("" + body.getMass());
        elements.add("" + body.getMommentOfInertia());
        elements.add("" + body.getFrictionCoefficent());
        elements.add("" + body.getResistution());
        String fixedIndicator = (body.getFixed())? "1" : "0";
        elements.add(fixedIndicator);
        elements.add("" + body.getID());
        String color = " ";
        switch(body.getColor().toString()) {
            case "java.awt.Color[r=255,g=0,b=0]"://red
                color = "r";
                break;
            case "java.awt.Color[r=255,g=200,b=0]"://orange
                color = "o";
                break;
            case "java.awt.Color[r=255,g=255,b=0]"://yellow
                color = "y";
                break;
            case "java.awt.Color[r=0,g=255,b=0]"://green
                color = "g";
                break;
            case "java.awt.Color[r=0,g=0,b=255]"://blue
                color = "b";
                break;
            case "java.awt.Color[r=255,g=0,b=255]"://magenta
                color = "m";
                break;
        }
        elements.add(color);
        String drawCOMIndicator = (body.getDrawCOM())? "1" : "0";
        elements.add(drawCOMIndicator);
        return elements.toString();
    }
}
