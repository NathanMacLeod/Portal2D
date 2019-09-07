/*
 * File added by Nathan MacLeod 2019
 */
package portal2d;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.event.*;

/**
 *
 * @author Nathan
 */
public class CreateLevelUIFrame extends JFrame {
    private JComboBox<String> mouseTools;
    private JComboBox<String> assetTypeSelector;
    private JButton helpButton;
    private JButton saveButton;
    private UIListener listener;
    private JTextArea text;
    
    
    private class UIListener implements ActionListener, ChangeListener, WindowFocusListener{
        private CreateLevelPanel createLevelPanel;
        private JFrame frame;
        
        
        public UIListener(CreateLevelPanel p, JFrame f) {
            createLevelPanel = p;
            frame = f;
        }
        
        public void stateChanged(ChangeEvent e) {
            
        }
        
        public void windowLostFocus(WindowEvent e) {
            //frame.toFront();
        }
        
        public void windowGainedFocus(WindowEvent e) {
            
        }
        
        public void actionPerformed(ActionEvent e) {
            switch(((JComponent)e.getSource()).getName()) {
                case "mouseTools":
                    String selection = (String)mouseTools.getSelectedItem();
                    MouseTool t = null;
                    switch(selection) {
                        case "Place Walls":
                            t = createLevelPanel.new PlaceBlockTool();
                            break;
                        case "Place Diagonal Walls":
                            t = createLevelPanel.new PlaceDiagonalWallTool();
                            break;
                        case "Place Asset":
                            t = createLevelPanel.new PlaceAssetTool();
                            break;
                        case "Delete":
                            t = createLevelPanel.new DeleteTool();
                            break;
                        case "Connect Activatables":
                            t = createLevelPanel.new CreateActivationPairTool();
                            break;
                        case "Edit Text":
                            t = createLevelPanel.new EditTextTool();
                            break;
                    }
                    createLevelPanel.setMouseTool(t);
                    break;
                case "assetTypeSelector":
                    createLevelPanel.setAssetType((String)assetTypeSelector.getSelectedItem());
                    break;
                case "save":
                    createLevelPanel.saveFile(text.getText());
            }
            repaint();
        }
        
        
    }
    
    public CreateLevelUIFrame(CreateLevelPanel p) {
        //Declarations
        listener = new UIListener(p, this);
        this.addWindowFocusListener(listener);
        
        mouseTools = new JComboBox(new String[] {"Place Walls", "Place Diagonal Walls", "Place Asset", "Connect Activatables", "Delete", "Edit Text"});
        mouseTools.setName("mouseTools");
        mouseTools.addActionListener(listener);
        
        assetTypeSelector = new JComboBox(new String[] {"Enter Elavator", "Exit Elavator", "Button", "Companion Cube", "Companion Cube Dispenser", "Energy Pellet Launcher", "Energy Pellet Reciever"
        , "Turret", "Door", "Floating Text"});
        assetTypeSelector.setName("assetTypeSelector");
        assetTypeSelector.addActionListener(listener);
        
        helpButton = new JButton("Help");
        helpButton.setName("help");
        helpButton.addActionListener(listener);
        
        saveButton = new JButton("Save");
        saveButton.setName("save");
        saveButton.addActionListener(listener);
        
        text = new JTextArea();
        
        //JFrame settings
        this.setSize(300, 400);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);//lol
        
        //Base && header
        JPanel base = new JPanel();
        setContentPane(base);
        base.setLayout(new BorderLayout());
        JPanel header = new JPanel();
        header.add(new JLabel("User Interface"));
        base.add(header, BorderLayout.NORTH);
        
        //Body, holds left and right panels
        JPanel body = new JPanel();
        base.add(body, BorderLayout.CENTER);
        body.setLayout(new GridLayout(5, 1));
        
        body.add(mouseTools);
        body.add(assetTypeSelector);
        body.add(helpButton);
        body.add(text);
        body.add(saveButton);
        
        setVisible(true);
        
    }
    
}
