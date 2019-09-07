/*
 * File added by Nathan MacLeod 2019
 */
package portal2d;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Graphics2D;
import java.awt.Color;
/**
 *
 * @author Nathan
 */
public class Background {
    private BufferedImage backgroundImage;
    private BufferedImage foregroundImage;
    private double borderSize;
    private int tileSize;
    private int[] topLeftCorner;
    
    public Background(BlockLevel level) {
        try {
            this.borderSize = borderSize;
            RigidBody interiorPolygon = level.getInteriorPolygon();
            int baseTileSize = level.getTileSize();
            tileSize = 2 * baseTileSize;
            int[] tileMapDimensions = level.getDimensions();
            int borderSize = 2000;
            int[] dimensions = new int[] {baseTileSize * (tileMapDimensions[2] - tileMapDimensions[0]) + borderSize * 2, baseTileSize * (tileMapDimensions[3] - tileMapDimensions[1]) + borderSize * 2};
            topLeftCorner = new int[] {baseTileSize * tileMapDimensions[0] - borderSize, baseTileSize * tileMapDimensions[1] - borderSize};
            
            BufferedImage exteriorWall = new BufferedImage(dimensions[0], dimensions[1], BufferedImage.TYPE_INT_ARGB);
            BufferedImage exteriorTile = ImageIO.read(Class.class.getResourceAsStream("/sprites/ExteriorTile.png"));
//            BufferedImage darkTile = ImageIO.read(new File("./sprites/exteriorTileDark.png"));
//            BufferedImage veryDarkTile = ImageIO.read(new File("./sprites/exteriorTileVeryDark.png"));
            for(int i = 0; i < (dimensions[0])/tileSize; i++) {
                for(int j = 0; j < (dimensions[1])/(tileSize); j++) {
                    BufferedImage selectedTile = exteriorTile;
//                    if((int)(Math.random() * 30) == 0)
//                        selectedTile = veryDarkTile;
//                    if((int)(Math.random() * 40) == 0)
//                        selectedTile = darkTile;
                    exteriorWall.getGraphics().drawImage(selectedTile, i * tileSize, j * tileSize, tileSize, tileSize * 2, null);
                }
            }
            
            Polygon clip = new Polygon();
            for(Point p : interiorPolygon.getNodes()) {
                clip.addPoint((int)p.x - topLeftCorner[0], (int)p.y - topLeftCorner[1]);
            }
            BufferedImage interiorWall = new BufferedImage(dimensions[0], dimensions[1], BufferedImage.TYPE_INT_ARGB);
            Graphics interiorGraphics = interiorWall.getGraphics();
            interiorGraphics.setClip(clip);
            BufferedImage interiorTile = ImageIO.read(Class.class.getResourceAsStream("/sprites/InteriorTile.png"));
            for(int i = 0; i < (dimensions[0])/tileSize; i++) {
                for(int j = 0; j < (dimensions[1])/(2 * tileSize); j++) {
                    interiorGraphics.drawImage(interiorTile, i * tileSize, j * 2 * tileSize, tileSize, tileSize * 2, null);
                }
            }
            backgroundImage = exteriorWall;
            backgroundImage.getGraphics().drawImage(interiorWall, 0, 0, null);
            
            foregroundImage = new BufferedImage(dimensions[0], dimensions[1], BufferedImage.TYPE_INT_ARGB);
            level.drawTilesOnly(foregroundImage.getGraphics(), topLeftCorner);
//            Polygon shape = new Polygon(new int[] {0, 25, 50, 35, 15}, new int[] {25, 10, 25, 50, 50}, 5);
//            Graphics2D g2d = (Graphics2D) exteriorWall.getGraphics();
//            g2d.setClip(shape);
//            g2d.setColor(Color.RED);
//            g2d.draw(shape);
            //g2d.drawImage(exteriorWall, (int) (mapDimensions[0] * tileSize/2.0), (int) (mapDimensions[1] * tileSize/2.0), 50, 50, null);
        }
        catch(IOException e) {
            
        }
    }
    
    private void draw(Graphics g, Camera c, BufferedImage image, Point cameraPoint) {
        //g.drawImage(image, 0, 0, (int) c.getWidth() * 2, (int) c.getHeight() * 2, (int) (cameraPoint.x - c.getWidth() - topLeftCorner[0]), (int) (cameraPoint.y - c.getHeight() - topLeftCorner[1]), (int) (cameraPoint.x + c.getWidth() - topLeftCorner[0]), (int) (cameraPoint.y + c.getHeight() - topLeftCorner[1]), null);
        g.drawImage(image, (int) -(cameraPoint.x - c.getWidth() - topLeftCorner[0]), (int) -(cameraPoint.y - c.getHeight() - topLeftCorner[1]), null);
    }
    
    public void drawBackground(Graphics g, Camera c) {
        draw(g, c, backgroundImage,c.getPosition());
//        int width = (int) c.getWidth() * 2;
//        int height = (int) c.getHeight() * 2;
//        g.setColor(Color.gray);
//        g.fillRect(0, 0, width, height);
//        g.setColor(Color.BLACK);
//        for(int i = 0; i < width; i+= tileSize) {
//            g.drawLine(i, 0, i, height);

//        }
//        for(int i = 0; i < height; i+= tileSize) {
//            g.drawLine(0, i, width, i);
//        }
    }
    
    public void drawForeground(Graphics g, Camera c) {
        draw(g, c, foregroundImage, c.getPosition());
    }
}
