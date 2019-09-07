/*
 * File added by Nathan MacLeod 2019
 */
package portal2d;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.awt.event.MouseAdapter;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import javax.swing.JFileChooser;
/**
 *
 * @author Nathan
 */
public class CreateLevelPanel extends JPanel implements Runnable {
    private boolean running;
    private Thread runner;
    private BlockLevel level;
    private boolean w, a, s, d;
    private Camera c;
    private MouseTool tool = null;
    private String assetType;
    private boolean moveLock;
    
    
    public void start() {
        running = true;
        runner = new Thread(this);
        runner.start();
    }       
    
    public void run() {
        moveLock = false;
        long currentTime;
        long pastTime = System.nanoTime();
        double timePassed = 0;
        c = new Camera(getWidth()/2.0, getHeight()/2.0);
        int levelSize = 255;
        int tileSize = 25;
        //level = new BlockLevel(new Point(0, 0), levelSize, levelSize, tileSize);
        
        c.setPosition(new Point(levelSize * tileSize/2.0, levelSize * tileSize/2.0));
        setMouseTool(new PlaceBlockTool());
        w = false;
        a = false;
        s = false;
        d = false;
        assetType = "Turret";
        while(running) {
            currentTime = System.nanoTime();
            timePassed = (currentTime - pastTime)/Math.pow(10, 9);
            pastTime = currentTime;
            
            moveCamera(timePassed);
            render();
            try {
                Thread.sleep(15);
            }
            catch(Exception e) {}
        }
    }
    
    private void moveCamera(double dt) {
        if(moveLock)
            return;
        double cameraSpeed = 400;
        Vector moveVector = new Vector(0, 0);
        if(w)
            moveVector = moveVector.add(new Vector(0, -1));
        if(a)
            moveVector = moveVector.add(new Vector(-1, 0));
        if(s)
            moveVector = moveVector.add(new Vector(0, 1));
        if(d)
            moveVector = moveVector.add(new Vector(1, 0));
        if(moveVector.getSquaredMagnitude() != 0)
            c.translate(moveVector.getUnitVector().multiplyByScalar(cameraSpeed * dt));
    }
    
    private void render() {
        BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = img.getGraphics();
        g.setColor(Color.black);
        g.fillRect(0, 0, getWidth(), getHeight());
        level.draw(g, c);
        tool.draw(g, c);
        g.setColor(Color.white);
        Graphics panelGraphics = getGraphics();
        panelGraphics.drawImage(img, 0, 0, null);
    }
    
    public class PlaceBlockTool extends MouseAdapter implements MouseTool {
        
        public void mousePressed(MouseEvent e) {
            level.placeWallTile(getMousePos(e.getX(), e.getY()), !e.isMetaDown());
        }
        
        public void mouseDragged(MouseEvent e) {
            level.placeWallTile(getMousePos(e.getX(), e.getY()), !e.isMetaDown());
        }
        
        public void draw(Graphics g, Camera c) {
            
        }
    }
    
    public class PlaceDiagonalWallTool extends MouseAdapter implements MouseTool {
        private BlockLevel.CreateDiagonalWallTool wallTool;
        private Point mousePosition;
        
        public PlaceDiagonalWallTool() {
            mousePosition = new Point(0, 0);
            wallTool = level.new CreateDiagonalWallTool();
        }
        
        public void mouseMoved(MouseEvent e) {
            mousePosition = getMousePos(e.getX(), e.getY());
        }
        
        public void mousePressed(MouseEvent e) {
            wallTool.placeDownPoints(getMousePos(e.getX(), e.getY()), !e.isMetaDown());
        }
        
        public void draw(Graphics g, Camera c) {
            wallTool.draw(g, c, mousePosition);
        }
    }
    
    public class PlaceAssetTool extends MouseAdapter implements MouseTool {
        private BlockLevel.PlaceDownAssetTool tool;
        private Point mousePosition;
        
        public PlaceAssetTool() {
            mousePosition = new Point(0, 0);
            tool = level.new PlaceDownAssetTool();
            tool.setAssetType(assetType);
        }
        
        public void mouseMoved(MouseEvent e) {
            mousePosition = getMousePos(e.getX(), e.getY());
            tool.findAssetPositions(mousePosition);
        }
        
        public void mousePressed(MouseEvent e) {
            if(e.isMetaDown()) {
                tool.flip();
            }
            else {
                tool.placeAsset(mousePosition);
            }
        }
        
        public void setAssetType(String s) {
            tool.setAssetType(s);
        }
        
        public void draw(Graphics g, Camera c) {
            tool.draw(g, c);
        }
    }
    
    public class CreateActivationPairTool extends MouseAdapter implements MouseTool {
        private BlockLevel.CreateActivatablePairTool tool;
        
        public CreateActivationPairTool() {
            tool = level.new CreateActivatablePairTool();
        }
        
        public void mouseMoved(MouseEvent e) {
            tool.setMousePosition(getMousePos(e.getX(), e.getY()));
        }
        
        public void mousePressed(MouseEvent e) {
            tool.selectNewAsset(getMousePos(e.getX(), e.getY()));
        }
        
        public void draw(Graphics g, Camera c) {
            tool.draw(g, c);
        }
    }
    
    public class DeleteTool extends MouseAdapter implements MouseTool {
        
        public void mousePressed(MouseEvent e) {
            level.deleteEntityAtPoint(getMousePos(e.getX(), e.getY()));
        }
        
        public void mouseDragged(MouseEvent e) {
            level.deleteEntityAtPoint(getMousePos(e.getX(), e.getY()));
        }
        
        public void draw(Graphics g, Camera c) {
            level.drawActivationPairs(g, c);
        }
    }
    
    public class EditTextTool extends MouseAdapter implements MouseTool, KeyListener{
        private BlockLevel.EditFloatingTextTool tool;
        
        public EditTextTool() {
            tool = level.new EditFloatingTextTool();
        }
        
        public void mousePressed(MouseEvent e) {
            tool.selectText(getMousePos(e.getX(), e.getY()));
            if(tool.isWriting())
                moveLock = true;
            else
                moveLock = false;
        }
        
        public void keyPressed(KeyEvent e) {
            tool.editText(e.getKeyChar(), e.getKeyCode() == KeyEvent.VK_BACK_SPACE);
        }
        
        public void keyReleased(KeyEvent e) {
            
        }
        
        public void keyTyped(KeyEvent e) {
            
        }
        
        public void draw(Graphics g, Camera c) {
            
        }
    }
    
    public void setAssetType(String type) {
        assetType = type;
        if(tool instanceof PlaceAssetTool) {
            ((PlaceAssetTool)tool).setAssetType(type);
        }
    }
    
    public void setMouseTool(MouseTool t) {
        moveLock = false;
        if(tool != null) {
            removeMouseListener(tool);
            removeMouseMotionListener(tool);
            if(tool instanceof KeyListener)
                removeKeyListener((KeyListener) tool);
        }
        tool = t;
        if(tool instanceof KeyListener)
            addKeyListener((KeyListener) tool);
        this.addMouseListener(t);
        this.addMouseMotionListener(t);
    }
    
    public void saveFile(String fileName) {
        FloatingText.drawTextOnScreen(this.getGraphics(), "SAVING...", new int[] {WIDTH/2, HEIGHT/2}, 12, Color.WHITE);
        JFileChooser fileSelector = new JFileChooser();
        fileSelector.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = fileSelector.showOpenDialog(new JFrame());
        File directory;
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            directory = fileSelector.getSelectedFile();
            String location = directory.getPath() + "/" + fileName;
            level.saveFile(location);
        }
        
    }
    
    public Point getMousePos(double x, double y) {
        return new Point(x - c.getWidth() + c.getPosition().x, y - c.getHeight() + c.getPosition().y);
    }
    
    public CreateLevelPanel() {
        this(null);
    }
    
    public CreateLevelPanel(File file) {
        try {
        if(file == null)
            level = new BlockLevel(new Point(0, 0), 400, 400, 25);
        else
            level = new BlockLevel(file);
        }
        catch(FileNotFoundException e) {
            
        } 
        JFrame frame = new JFrame();
        frame.setSize(2000, 1050);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(this);
        this.addKeyListener(new KeyAdapter() {
           public void keyPressed(KeyEvent e) {
                switch(e.getKeyCode()) {
                    case KeyEvent.VK_W:
                        w = true;
                        break;
                    case KeyEvent.VK_A:
                        a = true;
                        break;
                    case KeyEvent.VK_S:
                        s = true;
                        break;
                    case KeyEvent.VK_D:
                        d = true;
                        break;
                }
           }
           
           public void keyReleased(KeyEvent e) {
               switch(e.getKeyCode()) {
                    case KeyEvent.VK_W:
                        w = false;
                        break;
                    case KeyEvent.VK_A:
                        a = false;
                        break;
                    case KeyEvent.VK_S:
                        s = false;
                        break;
                    case KeyEvent.VK_D:
                        d = false;
                        break;
                }
           }
        }); 
        frame.setVisible(true);
        this.setVisible(true);
        requestFocusInWindow();
        start();
    }
    
    public static void main(String[] args) {
        CreateLevelPanel p = new CreateLevelPanel();
        CreateLevelUIFrame f = new CreateLevelUIFrame(p);
    }
    
}
