/*
 * File added by Nathan MacLeod 2019
 */
package portal2d;

/**
 *
 * @author Nathan
 */
import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
/**
 *
 * @author macle
 */
public class UserInterface extends javax.swing.JFrame {
    private BackgroundPanel backgroundPanel;
    private BufferedImage background;
    private ArrayList<Button> buttonSet;
    private Point mouse;
    
    public UserInterface() {
        super();
        this.setSize(800, 500);
        this.addMouseListener(new MouseAdapter() {
            
            public void mousePressed(MouseEvent e) {
                for(Button b : buttonSet) {
                    if(b.pointInButton(new Point(e.getX(), e.getY() - 25)))
                        b.performAction();
                }
            }
            
        });
        buttonSet = createMainButtonSet();
        backgroundPanel = new BackgroundPanel();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        try {
            background = ImageIO.read(Class.class.getResourceAsStream("/sprites/menuImage.png"));
        }
        catch(IOException e) {
            System.out.println("cant find menu image");
            System.exit(0);
        }
        setContentPane(backgroundPanel);
        setVisible(true);
    }

    private ArrayList<Button> createMainButtonSet() {
        ArrayList<Button> buttonSet = new ArrayList();
        buttonSet.add(new Button(60, 200, 50, 230, "Play"));
        buttonSet.get(0).assignAction(new ButtonAction() {
            public void performAction() {
                PhysicsPanel p = new PhysicsPanel(1);
                dispose();
            }
        });
        buttonSet.add(new Button(60, 270, 50, 230, "Create Level"));
        buttonSet.get(1).assignAction(new ButtonAction() {
            public void performAction() {
                setButtonSet(createCreateLevelButtonSet());
                repaint();
            }
        });
        buttonSet.add(new Button(60, 340, 50, 230, "Play Custum Level"));
        buttonSet.get(2).assignAction(new ButtonAction() {
            public void performAction() {
                JFileChooser fileSelector = new JFileChooser();
                int returnVal = fileSelector.showOpenDialog(new JFrame());
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fileSelector.getSelectedFile();
                    PhysicsPanel p = new PhysicsPanel(file);
                    dispose();
                }
            }
        });
        return buttonSet;
    }
    
    public void setButtonSet(ArrayList<Button> buttons) {
        buttonSet = buttons;
    }
    
//    public void actionPerformed(ActionEvent e) {
//    //Handle open button action.
//    if (e.getSource() == openButton) {
//        int returnVal = fc.showOpenDialog(FileChooserDemo.this);
//
//        if (returnVal == JFileChooser.APPROVE_OPTION) {
//            File file = fc.getSelectedFile();
//            //This is where a real application would open the file.
//            log.append("Opening: " + file.getName() + "." + newline);
//        } else {
//            log.append("Open command cancelled by user." + newline);
//        }
//   } ...
//}
    
    private ArrayList<Button> createCreateLevelButtonSet() {
        ArrayList<Button> buttonSet = new ArrayList();
        buttonSet.add(new Button(60, 200, 50, 230, "Start From Scratch"));
        buttonSet.get(0).assignAction(new ButtonAction() {
            public void performAction() {
                CreateLevelPanel p = new CreateLevelPanel();
                CreateLevelUIFrame f = new CreateLevelUIFrame(p);
                dispose();
            }
        });
        buttonSet.add(new Button(60, 270, 50, 230, "Open from file"));
        buttonSet.get(1).assignAction(new ButtonAction() {
            public void performAction() {
                JFileChooser fileSelector = new JFileChooser();
                int returnVal = fileSelector.showOpenDialog(new JFrame());
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fileSelector.getSelectedFile();
                    CreateLevelPanel p = new CreateLevelPanel(file);
                    CreateLevelUIFrame f = new CreateLevelUIFrame(p);
                    dispose();
                }
            }
        });
        buttonSet.add(new Button(60, 340, 50, 230, "Back"));
        buttonSet.get(2).assignAction(new ButtonAction() {
            public void performAction() {
                setButtonSet(createMainButtonSet());
                repaint();
            }
        });
        return buttonSet;
    }
    
    public void paint(Graphics g) {
        super.paint(g);
        ((Graphics2D)backgroundPanel.getGraphics()).drawImage(background, 0, 0, null);
        drawButtons(backgroundPanel.getGraphics(), buttonSet);
        
    }
    
    private class BackgroundPanel extends JPanel {
        public void paintComponent(Graphics g) {
            ((Graphics2D)getGraphics()).drawImage(background, 0, 0, null);
            super.paintComponent(g);
            
        }
    }
    
    private void drawButtons(Graphics g, ArrayList<Button> buttons) {
        for(Button b : buttons) {
            b.draw(g);
        }
    }
    
    private interface ButtonAction {
        public void performAction();
    }
    
    private class Button {
        Point pos;
        int height;
        int width;
        String text;
        ButtonAction action;
        
        public Button(int x, int y, int height, int width, String text) {
            pos = new Point(x, y);
            this.height = height;
            this.width = width;
            this.text = text;
        }
        
        public void assignAction(ButtonAction action) {
            this.action = action; 
        }
        
        public void performAction() {
            action.performAction();
        }
        
        public boolean pointInButton(Point p) {
            return p.x > pos.x && p.y > pos.y && p.x < pos.x + width && p.y < pos.y + height;
        }
        
        public void draw(Graphics g) {
            g.setColor(new Color(230, 230, 230));
            g.fillRect((int) pos.x, (int) pos.y, width, height);
            g.setColor(Color.DARK_GRAY);
            g.drawRect((int) pos.x, (int) pos.y, width, height);
            if(mouse != null)
                g.drawLine(0, 0, (int) mouse.x, (int) mouse.y);
            //Graphics g, String message, int[] position, int size, Color color
            FloatingText.drawTextOnScreen(g, text, new int[] {(int) pos.x + width/2, (int) pos.y + (int) (height * 2/3.0)}, height/2, Color.DARK_GRAY);
        }
    }                                     
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        UserInterface menu = new UserInterface();
    }
                  
}
