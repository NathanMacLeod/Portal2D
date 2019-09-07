/*
 * File added by Nathan MacLeod 2019
 */
package portal2d;

import java.util.ArrayList;
import java.awt.Graphics;
import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.StringJoiner;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Polygon;
import java.util.ConcurrentModificationException;
import java.io.InputStream;
/**
 *
 * @author Nathan
 */
public class BlockLevel {
    private Block[][] tiles; //height, width
    private int tileSize;
    private Point topLeftCorner;
    private ArrayList<Block> wallTiles;
    private ArrayList<DiagonalWall> diagonalWalls;
    private ArrayList<GameAsset> gameAssets;
    private ArrayList<ActivePair> activationPairs;
    private RigidBody interiorPolygon;
    
    public BlockLevel(Point topLeftCorner, int height, int width, int tileSize) {
        wallTiles = new ArrayList();
        diagonalWalls = new ArrayList();
        gameAssets = new ArrayList();
        activationPairs = new ArrayList();
        this.tileSize = tileSize;
        this.topLeftCorner = topLeftCorner;
        initiateTiles(height, width);
        interiorPolygon = null;
    }
    
    public BlockLevel(String file) throws FileNotFoundException {
        wallTiles = new ArrayList<>();
        diagonalWalls = new ArrayList();
        gameAssets = new ArrayList();
        activationPairs = new ArrayList();
        try {
            loadTileInfo(file);
        }
        catch(FileNotFoundException e) {
            throw e;
        } 
        //determineTilesInsideOutsideStatus();
    }
    
    public BlockLevel(InputStream file) throws FileNotFoundException {
        wallTiles = new ArrayList<>();
        diagonalWalls = new ArrayList();
        gameAssets = new ArrayList();
        activationPairs = new ArrayList();
        try {
            loadTileInfo(file);
        }
        catch(FileNotFoundException e) {
            throw e;
        } 
    }
    
    public BlockLevel(File f) throws FileNotFoundException {
        wallTiles = new ArrayList<>();
        diagonalWalls = new ArrayList();
        gameAssets = new ArrayList();
        activationPairs = new ArrayList();
        try {
            loadTileInfo(f);
        }
        catch(FileNotFoundException e) {
            throw e;
        } 
    }
    
    public RigidBody getInteriorPolygon() {
        return interiorPolygon;
    }
    
    public int[] getDimensions() {
        int leftmostEntity = -1;
        int rightmostEntity = -1;
        int topmostEntity = -1;
        int bottommostEntity = -1;
        
        for(int i = 0; i < tiles.length; i++) {
            for(int j = 0; j < tiles[0].length; j++) {
                if(tiles[i][j].containsWall()) {
                    if(leftmostEntity == -1) {
                        leftmostEntity = j;
                        rightmostEntity = j;
                        topmostEntity = i;
                        bottommostEntity = i;
                        continue;
                    }
                    if(j < leftmostEntity)
                        leftmostEntity = j;
                    if(j > rightmostEntity)
                        rightmostEntity = j;
                    if(i < topmostEntity)
                        topmostEntity = i;
                    if(i > bottommostEntity)
                        bottommostEntity = i;
                }
            }
        }
        
        for(DiagonalWall d : diagonalWalls) {
            for(Block b : d.rootContacts) {
                if(b.x < leftmostEntity)
                    leftmostEntity = b.x;
                if(b.x > rightmostEntity)
                    rightmostEntity = b.x;
                if(b.y > bottommostEntity)
                    bottommostEntity = b.y;
                if(b.y < topmostEntity)
                    topmostEntity = b.y;
            }
        }
        return new int[] {leftmostEntity, topmostEntity, rightmostEntity, bottommostEntity};
    }
    
    public int getTileSize() {
        return tileSize;
    }
    
    private void initiateTiles(int height, int width) {
        tiles = new Block[height][width];
        for(int i = 0; i < height; i++) {
            for(int j = 0; j < width; j++) {
                Point pos = new Point(topLeftCorner.x + j * tileSize, topLeftCorner.y + i * tileSize);
                tiles[i][j] = new Block(pos, j, i);
            }
        }
    }
    
    public void placeWallTile(Point p, boolean portableSurface) {
        Block b = getTile(p);
        if(!b.containsWall())
            wallTiles.add(b);
        b.content = new WallTile(portableSurface);
    }
    
    public void removeTileContent(Point p) {
        Block b = getTile(p);
        if(wallTiles.contains(b))
            wallTiles.remove(b);
        b.content = null;
    }
    
    private Block getTile(Point p) {
        if((int)p.y/tileSize >= tiles.length || (int)p.x/tileSize >= tiles[0].length)
            return null;
        return tiles[(int)p.y/tileSize][(int)p.x/tileSize];
    }
    
    private RigidBody createInteriorPolygon() {
        GameAsset entryElavator = null;
        for(GameAsset a : gameAssets) {
            if(a.getType().equals("Enter Elavator")) {
                entryElavator = a;
                break;
            }
        }
        if(entryElavator == null)
            throw new NullPointerException();
        Point elavatorPosition = new Point(entryElavator.getPosition().x, entryElavator.getPosition().y - 1);
        Block elavatorTile = getTile(elavatorPosition);
        
        Block roofTile = null;
        for(int i = elavatorTile.y; i >= 0; i--) {
            if(tiles[i][elavatorTile.x].containsWall()) {
                roofTile = tiles[i][elavatorTile.x];
                break;
            }
        }
        
        DiagonalWall roofDiagonal = null;
        Line lowestLine = null;
        double lowestDiagonalWall = -1;
        Line intersectingLine = new Line(new Point(elavatorPosition.x, 0), new Point(elavatorPosition.x, elavatorPosition.y));
        for(DiagonalWall d : diagonalWalls) {
            if(d.structure.lineIntersectsBody(intersectingLine)) {
                for(Line l : d.structure.getLines()) {
                    Point intersection = l.getIntersection(intersectingLine);
                    if(intersection != null && (intersection.y > lowestDiagonalWall)) {
                        lowestLine = l;
                        lowestDiagonalWall = intersection.y;
                        roofDiagonal = d;
                    }
                }
            }
        }
        
        boolean onDiagonal = false;
        int[] vectorOfTravel = null;
        int[] interiorDirectionVector = new int[] {0, 1}; // 0 up, 1 right, 2 down, 3 left
        DiagonalWall currentDiagonal = null;
        Point diagonalPoint = null;
        Block currentBlock = null;
        
        if(roofDiagonal == null && roofTile == null)
            throw new NullPointerException();
        else if(roofDiagonal == null || (roofTile != null && roofTile.y * tileSize > lowestDiagonalWall)) {
            vectorOfTravel = new int[] {1, 0};
            currentBlock = roofTile;
        }
        else {
            currentDiagonal = roofDiagonal;
            diagonalPoint = lowestLine.p1;
            onDiagonal = true;
        }
        
        ArrayList<Point> interiorPolygon = new ArrayList();
        while(true) {
            if(!onDiagonal) {
                while(tiles[currentBlock.y + vectorOfTravel[1]][currentBlock.x + vectorOfTravel[0]].containsWall() && 
                !tiles[currentBlock.y + vectorOfTravel[1] + interiorDirectionVector[1]][currentBlock.x + vectorOfTravel[0] + interiorDirectionVector[0]].containsWall()) {
                    currentBlock = tiles[currentBlock.y + vectorOfTravel[1]][currentBlock.x + vectorOfTravel[0]];
                }
                Point newCorner = new Point(tileSize * currentBlock.x, tileSize * currentBlock.y);
                if(interiorDirectionVector[1] == 1 || vectorOfTravel[1] == 1) {
                    newCorner.y += tileSize;
                }
                if(interiorDirectionVector[0] == 1 || vectorOfTravel[0] == 1) {
                    newCorner.x += tileSize;
                }
                
                interiorPolygon.add(newCorner);
                boolean found = false;
                for(DiagonalWall d : diagonalWalls) {
                    for(Block b : d.rootContacts) {
                        if(b.equals(currentBlock) && ((vectorOfTravel[0] != 0 && vectorOfTravel[0] == -d.getRootVector(b)[0]) || (vectorOfTravel[1] != 0 && vectorOfTravel[1] == -d.getRootVector(b)[1]))) {
                            currentDiagonal = d;
                            diagonalPoint = newCorner;
                            onDiagonal = true;
                            found = true;
                            break;
                        }
                    }
                    if(found)
                        break;
                }
                if(!found) {
                    boolean ranIntoWall = tiles[currentBlock.y + vectorOfTravel[1] + interiorDirectionVector[1]][currentBlock.x + vectorOfTravel[0] + interiorDirectionVector[0]].containsWall();
                    int[] tempVect = new int[] {((ranIntoWall)? 1 : -1) * interiorDirectionVector[0], ((ranIntoWall)? 1 : -1) * interiorDirectionVector[1]};
                    int[] tempInterior = new int[] {((ranIntoWall)? 1 : -1) * -vectorOfTravel[0], ((ranIntoWall)? 1 : -1) * -vectorOfTravel[1]};
                    currentBlock = tiles[currentBlock.y + ((ranIntoWall)? vectorOfTravel[1] + interiorDirectionVector[1] : 0)][currentBlock.x + ((ranIntoWall)? vectorOfTravel[0] + interiorDirectionVector[0] : 0)];
                    vectorOfTravel = tempVect;
                    interiorDirectionVector = tempInterior;
                }
            }
            else {
                Point nextCorner = currentDiagonal.getOppositeWallLinePoint(currentDiagonal.getClosestPoint(diagonalPoint));
                interiorPolygon.add(nextCorner);
                currentBlock = currentDiagonal.getRootContact(nextCorner);
                vectorOfTravel = currentDiagonal.getRootVector(currentBlock);
                Point oppositeRootPoint = currentDiagonal.getOppositeRootPoint(nextCorner);
                Vector interiorVector = new Vector(nextCorner.x - oppositeRootPoint.x, nextCorner.y - oppositeRootPoint.y).getUnitVector();
                interiorDirectionVector = new int[] {(int) interiorVector.getXComp(), (int) interiorVector.getYComp()};
                if(currentBlock.containsWall()) {
                    onDiagonal = false;
                    continue;
                }
                else {
                    for(DiagonalWall d : diagonalWalls) {
                        Point closestPoint = d.getClosestPoint(nextCorner);
                        if(Math.abs(nextCorner.x - closestPoint.x) < 0.1 && Math.abs(nextCorner.y - closestPoint.y) < 0.1) {
                            currentDiagonal = d;
                            diagonalPoint = nextCorner;
                            break;
                        }
                    }
                }
            }
            
            if(interiorPolygon.size() > 3 && interiorPolygon.get(interiorPolygon.size() - 1).equals(interiorPolygon.get(0))) {
                interiorPolygon.remove(0);
                RigidBodyPoint[] rigidBodyPoints = new RigidBodyPoint[interiorPolygon.size()];
                for(int i = 0; i < interiorPolygon.size(); i++) {
                    rigidBodyPoints[i] = new RigidBodyPoint(interiorPolygon.get(i).x, interiorPolygon.get(i).y);
                }
                return new RigidBody(rigidBodyPoints, new Vector(0, 0), 0, 1, 0, 0, true, 0, null, false);
            }
        }
    }
    
    private void determineTilesInsideOutsideStatus(RigidBody interiorPolygon) {
        for(int i = 0; i < tiles.length; i++) {
            for(int k = 0; k < tiles[0].length; k++) {
                if(!tiles[i][k].containsWall() && interiorPolygon.pointInsideBody(new Point(k * tileSize + tileSize/2.0, i * tileSize + tileSize/2.0))) {
                    tiles[i][k].inside = true;
                }
                else
                    tiles[i][k].inside = false;
            }
        }
    }
    
    //Old method
//    private void determineTilesInsideOutsideStatus() {
//        for(int y = 0; y < tiles.length; y++) {
//            ArrayList<Integer> wallPosX = new ArrayList<>();
//            for(int x = 0; x < tiles[0].length - 1; x++) {
//                if(tiles[y][x].containsWall() && !tiles[y][x + 1].containsWall())
//                    wallPosX.add(x);
//            }
//            for(int x = 0; x < tiles[0].length - 1; x++) {
//                ArrayList<Integer> wallPosY = new ArrayList<>();
//                for(int y1 = 0; y1 < tiles.length; y1++) {
//                    if(tiles[y1][x].containsWall() && !tiles[y1 + 1][x].containsWall())
//                    wallPosY.add(y1);
//                }
//                if(tiles[y][x].containsWall()) {
//                    tiles[y][x].inside = false;
//                    continue;
//                }
//                int wallsLeft = 0;
//                int wallsRight = 0;
//                int wallsUp = 0;
//                int wallsDown = 0;
//                for(int i : wallPosX) {
//                    if(i > x)
//                        wallsRight++;
//                    else
//                        wallsLeft++;
//                }
//                for(int i : wallPosY) {
//                    if(i > y)
//                        wallsDown++;
//                    else
//                        wallsUp++;
//                }
//                Line horizontelLine = new Line(new Point(topLeftCorner.x - 10, y * tileSize + tileSize/2.0), new Point(topLeftCorner.x + 10 + tiles[0].length * tileSize, y * tileSize));
//                Line verticleLine = new Line(new Point(x * tileSize + tileSize/2.0, topLeftCorner.y - 10), new Point(x * tileSize + tileSize/2.0, topLeftCorner.y + 10 + tiles.length * tileSize));
//                Point tileCenter = new Point(topLeftCorner.x + tileSize * x + tileSize/2.0, topLeftCorner.y + tileSize * y + tileSize/2.0);
//                for(DiagonalWall w : diagonalWalls) {
//                    Line[] wallLines = w.getWallLines();
//                    Point l1Intersection = wallLines[0].getIntersection(horizontelLine);
//                    Point l2Intersection = wallLines[1].getIntersection(horizontelLine);
//                    if((l1Intersection != null && l1Intersection.x < tileCenter.x)  || (l2Intersection != null && l2Intersection.x < tileCenter.x))
//                        wallsLeft++;
//                    if((l1Intersection != null && l1Intersection.x > tileCenter.x) || (l2Intersection != null && l2Intersection.x > tileCenter.x))
//                        wallsRight++;
//                        
//                    l1Intersection = wallLines[0].getIntersection(verticleLine);
//                    l2Intersection = wallLines[1].getIntersection(verticleLine);
//                    if((l1Intersection != null && l1Intersection.y < tileCenter.y)  || (l2Intersection != null && l2Intersection.y < tileCenter.y))
//                        wallsUp++;
//                    if((l1Intersection != null && l1Intersection.y > tileCenter.y) || (l2Intersection != null && l2Intersection.y > tileCenter.y))
//                        wallsDown++;
//                }
//                wallsRight *= 2;
//                wallsLeft *= 2;
//                wallsUp *= 2;
//                wallsDown *= 2;
//                
//                if(wallsRight == 0 || wallsLeft == 0 || wallsUp == 0 || wallsDown == 0) {
//                    tiles[y][x].inside = false;
//                    continue;
//                }
//                
//                tiles[y][x].inside = ((wallsLeft - 1) % 2 != 0 || (wallsRight - 1) % 2 != 0)
//                        && ((wallsUp - 1) % 2 != 0 || (wallsDown - 1) % 2 != 0);
//            }
//        }
//    }
    
    public void loadTileInfo(String fileName) throws FileNotFoundException {
        try {
            loadTileInfo(new File("./levels/" + fileName));
        }
        catch(FileNotFoundException e) {
            throw e;
        }
    }
    
    public void loadTileInfo(File f) throws FileNotFoundException{
        try {
            loadTileInfo(new Scanner(f));
        }
        catch(FileNotFoundException e) {
            throw e;
        }
    }
    
    public void loadTileInfo(InputStream f) throws FileNotFoundException{
        try {
            loadTileInfo(new Scanner(f));
        }
        catch(FileNotFoundException e) {
            throw e;
        }
    }
    
    public static String getFullFileName(String fileName) {
        return "./levels/" + fileName;
    }
    
    public void loadTileInfo(Scanner scanner) throws FileNotFoundException {
        //Each line of the info
        readSetupData(scanner.nextLine());
        int readState = 0; //0 reads blocks, 1 reads diagonal walls, 2 reads assets
        ArrayList<RigidBodyPoint> interiorPolygonPoints = new ArrayList();
        while(scanner.hasNext()) {
            String lineData = scanner.nextLine();
            if(lineData.equals("::")) {
                readState++;
                continue;
            }
            switch(readState) {
                case 0:
                    loadTileLine(lineData);
                    break;
                case 1:
                    loadDiagonalWall(lineData);
                    break;
                case 2:
                    loadAsset(lineData);
                    break;
                case 3:
                    loadActivePair(lineData);
                    break;
                case 4:
                    interiorPolygonPoints.add(loadPolygonPoint(lineData));
                    break;
            }
        }
        RigidBodyPoint[] rigidBodyPoints = new RigidBodyPoint[interiorPolygonPoints.size()];
        for(int i = 0; i < interiorPolygonPoints.size(); i++) {
            rigidBodyPoints[i] = interiorPolygonPoints.get(i);
        }
        interiorPolygon = new RigidBody(rigidBodyPoints, new Vector(0, 0), 0, 1, 0, 0, true, 0, null, false);
    }
    
    private RigidBodyPoint loadPolygonPoint(String lineData) {
        String[] pointsInfo = lineData.split(",");
        double x = Double.parseDouble(pointsInfo[0]);
        double y = Double.parseDouble(pointsInfo[1]);
        return new RigidBodyPoint(x, y);
    }
    
    private void readSetupData(String data) {
        //Format: topLeftX,topLeftY,height,width,tilesize
        wallTiles.clear();
        diagonalWalls.clear();
        gameAssets.clear();
        activationPairs.clear();
        String[] info  = data.split(",");
        topLeftCorner = new Point(Double.parseDouble(info[0]), Double.parseDouble(info[1]));
        initiateTiles(Integer.parseInt(info[2]), Integer.parseInt(info[3]));
        tileSize = Integer.parseInt(info[4]);
    }
    
    private void loadDiagonalWall(String info) {
        //portable p1x,p1y;p2x,p2y;
        boolean portalable = info.charAt(0) == '1';
        String[] pointsInfo = info.substring(1).split(";");
        Point[] points = new Point[4];
        for(int i = 0; i < pointsInfo.length; i++) {
            String pointInfo = pointsInfo[i];
            String[] xyInfo = pointInfo.split(",");
            points[i] = new Point(Double.parseDouble(xyInfo[0]), Double.parseDouble(xyInfo[1]));
        }
        diagonalWalls.add(new DiagonalWall(points, portalable));
    }
    
    private void loadTileLine(String line) {
        //format:  rowNum:i,blocked;i,blocked;i,blocked....
        String[] ray = line.split(":");
        int row = Integer.parseInt(ray[0]);
        String[] dataSets = ray[1].split(";");
        for(String set : dataSets) {
            String[] data = set.split(",");
            int col = Integer.parseInt(data[0]);
            boolean portableTile = data[1].equals("1");
            tiles[row][col].content = new WallTile(portableTile);
            wallTiles.add(tiles[row][col]);
        }
    }
    
    private void loadAsset(String line) {
        //type,px:py,orientation,flipped
        String[] info = line.split(",");
        String type = info[0];
        String[] position = info[1].split(":");
        String orientation = info[2];
        boolean flipped = info[3].equals("1");
        String message = info[4];
        gameAssets.add(new GameAsset(type, new Point(Double.parseDouble(position[0]), Double.parseDouble(position[1])), Double.parseDouble(orientation), getAssetFromType(type, tileSize, new Point(0, 0), 0, flipped, null, null, message)));
    }
    
    private void loadActivePair(String line) {
       //activatorIndex,activatableIndex
       String[] info = line.split(",");
       int activatorIndex = Integer.parseInt(info[0]);
       int activateeIndex = Integer.parseInt(info[1]);
       activationPairs.add(new ActivePair(gameAssets.get(activatorIndex), gameAssets.get(activateeIndex)));
    }
    
    private void saveBlockLevel(String filePath) {
        try {
            PrintWriter writer = new PrintWriter(filePath);
            writer.println(topLeftCorner.x + "," + topLeftCorner.y + "," + tiles.length + "," + tiles[0].length + "," + tileSize);
            for(int i = 0; i < tiles.length; i++) {
                StringJoiner rowData = new StringJoiner(";", "", "");
                for(int k = 0; k < tiles[i].length; k++) {
                    if(tiles[i][k].occupied()) {
                        if(tiles[i][k].content instanceof WallTile) {
                            int portableFlag = (((WallTile)tiles[i][k].content).portableSurface)? 1 : 0;
                            rowData.add(k + "," + portableFlag);
                        }
                    }
                }
                String rowDataString = rowData.toString();
                if(!rowDataString.equals(""))
                    writer.println(i + ":" + rowDataString);
            }
            writer.println("::");
            for(DiagonalWall w : diagonalWalls) {
                StringJoiner pointsData = new StringJoiner(";");
                for(Point p : w.getPoints()) {
                    pointsData.add("" + p.x + "," + p.y);
                    
                }
                writer.println(((w.getPortalable())? 1 : 0) + pointsData.toString());
            }
            writer.println("::");
            for(GameAsset a : gameAssets) {
                StringJoiner info = new StringJoiner(",");
                info.add(a.getType() + "");
                info.add(""+ a.getPosition().x + ":" + a.getPosition().y);
                info.add(a.getOrientation() + "");
                info.add((a.getFlipped())? "1" : "0");
                info.add((a.getType().equals("Floating Text"))? ((FloatingText)a.exampleAsset).getMessage() : " ");
                writer.println(info.toString());
            }
            writer.println("::");
            for(ActivePair a : activationPairs) {
                writer.println(gameAssets.indexOf(a.activator) + "," + gameAssets.indexOf(a.activatee));
            }
            writer.println("::");
            for(Point p : interiorPolygon.getNodes()) {
                writer.println(p.x + "," + p.y);
            }
            writer.close();
        } 
        catch(FileNotFoundException e) {
            try {
                new File(filePath).createNewFile();
                saveBlockLevel(filePath);
            }
            catch(IOException er) {
                System.out.println("Failed to create file\n" + er.toString());
            }
        }
        catch(Exception e) {
            System.out.println("Failure to save level");
        }
        System.out.println("level succesfully saved");
    }
    
    public void saveFile(String filePath) {
        interiorPolygon = createInteriorPolygon();
        determineTilesInsideOutsideStatus(interiorPolygon);
        saveBlockLevel(filePath);
    }
    
    public ArrayList<RigidBody> produceWalls() {
        //tile inside outside status must first be determined
        determineTilesInsideOutsideStatus(interiorPolygon);
        ArrayList<BlockFaces> allWallTiles = new ArrayList<>();
        ArrayList<RigidBody> walls = new ArrayList<>();
        double resitution = 0.3;
        double friction = 1;
        
        for(int i = 0; i < wallTiles.size(); i++) {
            BlockFaces tile = new BlockFaces(wallTiles.get(i));
            boolean hasInsideContact = false;
            for(int k = 0; k < 4; k++) {
                boolean insideFace = false;
                switch(k) {
                    case 0://left
                        insideFace = tiles[tile.block.y][tile.block.x - 1].inside;
                        tile.left = insideFace;
                        break;
                    case 1://up
                        insideFace = tiles[tile.block.y - 1][tile.block.x].inside;
                        tile.up = insideFace;
                        break;
                    case 2://right
                        insideFace = tiles[tile.block.y][tile.block.x + 1].inside;
                        tile.right = insideFace;
                        break;
                    case 3://down
                        insideFace = tiles[tile.block.y + 1][tile.block.x].inside;
                        tile.down = insideFace;
                        break;
                }
                if(insideFace)
                    hasInsideContact = true;
                
            }
            if(hasInsideContact) {
                allWallTiles.add(tile);
            }
        }
        
        for(int i = 0; i < 4; i++) {
            boolean travHorizontel = false;
            Vector insideDirection = null;
            ArrayList<Block> tilesWithFace = new ArrayList<>();
            switch(i) {
                case 0:
                    //up
                    for(BlockFaces b : allWallTiles)
                        if(b.up)
                            tilesWithFace.add(b.block);
                    insideDirection = new Vector(0, -1);
                    travHorizontel = true;
                    break;
                case 1:
                    //down
                    for(BlockFaces b : allWallTiles)
                        if(b.down)
                            tilesWithFace.add(b.block);
                    insideDirection = new Vector(0, 1);
                    travHorizontel = true;
                    break;
                case 2:
                    //left
                    for(BlockFaces b : allWallTiles)
                        if(b.left)
                            tilesWithFace.add(b.block);
                    insideDirection = new Vector(-1, 0);
                    travHorizontel = false;
                    break;
                case 3:
                    //right
                    for(BlockFaces b : allWallTiles)
                        if(b.right)
                            tilesWithFace.add(b.block);
                    insideDirection = new Vector(1, 0);
                    travHorizontel = false;
                    break;
            }
            
            RigidBodyPoint[] wallPoints = null;
            
            while(tilesWithFace.size() > 0) {
                Block positiveDirectTile = tilesWithFace.get(0);
                Block negativeDirectTile = tilesWithFace.get(0);
                boolean portableSurface = ((WallTile)tilesWithFace.get(0).content).portableSurface;
                tilesWithFace.remove(0);
                Block nextPositiveTile = (travHorizontel)? tiles[positiveDirectTile.y][positiveDirectTile.x + 1] : tiles[positiveDirectTile.y + 1][positiveDirectTile.x];
                Block nextNegativeTile = (travHorizontel)? tiles[negativeDirectTile.y][negativeDirectTile.x - 1] : tiles[negativeDirectTile.y - 1][negativeDirectTile.x];
                
                while(tilesWithFace.contains(nextPositiveTile) && ((WallTile)nextPositiveTile.content).portableSurface == portableSurface) {
                    positiveDirectTile = nextPositiveTile;
                    tilesWithFace.remove(positiveDirectTile);
                    nextPositiveTile = (travHorizontel)? tiles[positiveDirectTile.y][positiveDirectTile.x + 1] : tiles[positiveDirectTile.y + 1][positiveDirectTile.x];
                }
                while(tilesWithFace.contains(nextNegativeTile) && ((WallTile)nextNegativeTile.content).portableSurface == portableSurface) {
                    negativeDirectTile = nextNegativeTile;
                    tilesWithFace.remove(negativeDirectTile);
                    nextNegativeTile = (travHorizontel)? tiles[negativeDirectTile.y][negativeDirectTile.x - 1] : tiles[negativeDirectTile.y - 1][negativeDirectTile.x];
                }
                
//                if(positiveDirectTile == negativeDirectTile) {
//                    wallPoints = new RigidBodyPoint[4];
//                    wallPoints[0] = new RigidBodyPoint(tileSize * positiveDirectTile.x, tileSize * positiveDirectTile.y);
//                    wallPoints[1] = new RigidBodyPoint(wallPoints[0].x + tileSize, wallPoints[0].y);
//                    wallPoints[2] = new RigidBodyPoint(wallPoints[0].x + tileSize, wallPoints[0].y + tileSize);
//                    wallPoints[3] = new RigidBodyPoint(wallPoints[0].x, wallPoints[0].y + tileSize);
//                    continue;
//                }
                wallPoints = new RigidBodyPoint[4];
                wallPoints[0] = new RigidBodyPoint(negativeDirectTile.x * tileSize, negativeDirectTile.y * tileSize);
                wallPoints[1] = new RigidBodyPoint(positiveDirectTile.x * tileSize, positiveDirectTile.y * tileSize);
                switch(i) {
                    case 0:
                    //up
                    wallPoints[1].x += tileSize;
                    break;
                case 1:
                    //down
                    wallPoints[1].x += tileSize;
                    wallPoints[0].y += tileSize;
                    wallPoints[1].y += tileSize;
                    break;
                case 2:
                    //left
                    wallPoints[1].y += tileSize;
                    break;
                case 3:
                    //right
                    wallPoints[0].x += tileSize;
                    wallPoints[1].x += tileSize;
                    wallPoints[1].y += tileSize;
                    break;
                }
                wallPoints[3] = new RigidBodyPoint((wallPoints[0].x - insideDirection.getXComp() * tileSize/2), (wallPoints[0].y - insideDirection.getYComp() * tileSize/2));
                wallPoints[2] = new RigidBodyPoint((wallPoints[1].x - insideDirection.getXComp() * tileSize/2), (wallPoints[1].y - insideDirection.getYComp() * tileSize/2));
                double wallDepth = tileSize/2;
                switch(i) {
                    case 0:
                        if(!tiles[positiveDirectTile.y][positiveDirectTile.x + 1].inside && !tiles[positiveDirectTile.y][negativeDirectTile.x - 1].inside) {
                            wallPoints[1].x += 5;
                            wallPoints[0].x -= 5;
                        }
                    case 1:
                        wallPoints[2].x -= wallDepth;
                        wallPoints[3].x += wallDepth;
                    break;
                    case 2:
                    case 3:
                        if(!tiles[positiveDirectTile.y + 1][positiveDirectTile.x].inside)
                            wallPoints[1].y += 5;
                        //wallPoints[0].y -= 0.5;
                        wallPoints[2].y -= wallDepth;
                        wallPoints[3].y += wallDepth;
                    break;
                }
                //RigidBodyPoint[] nodes, Vector velocity, double angularVelocity, double density,
                //double resistution, double frictionCoefficent, boolean fixed, int ID, Color color, boolean drawCOM
                RigidBody body = new RigidBody(wallPoints, new Vector(0, 0), 0, 1, resitution, friction, true, walls.size(), (portableSurface)? Color.WHITE: Color.DARK_GRAY, false);
                body.setPortable(portableSurface);
                walls.add(body);
            }
        }
        
        for(DiagonalWall w : diagonalWalls) {
            RigidBodyPoint[] rigidBodyPoints = new RigidBodyPoint[w.getPoints().length];
            for(int i = 0; i < rigidBodyPoints.length; i++) {
                rigidBodyPoints[i] = new RigidBodyPoint(w.getPoints()[i].x, w.getPoints()[i].y);
            }
            RigidBody body = new RigidBody(rigidBodyPoints, new Vector(0, 0), 0, 1, resitution, friction, true, walls.size(), (w.getPortalable())? Color.WHITE: Color.DARK_GRAY, false);
            body.setPortable(w.getPortalable());
            walls.add(body);
        }
        return walls;
    }
    
    public AssetListWrapper getAssets(IDGiver IDs, ArrayList<RigidBody> bodies) {
        ArrayList<Asset> list = new ArrayList<>();
        for(GameAsset a : gameAssets) {
            Asset asset = getAssetFromType(a.getType(), tileSize, a.position, a.orientation, a.getFlipped(), IDs, bodies, (a.getType().equals("Floating Text"))? ((FloatingText)a.exampleAsset).getMessage() : "");
            list.add(asset);
        }
        ArrayList<ActivationPair> activatables = new ArrayList<>();
        for(int i = 0; i < gameAssets.size(); i++) {
            GameAsset a = gameAssets.get(i);
            for(int j = 0; j < activationPairs.size(); j++) {
                ActivePair p = activationPairs.get(j);
                if(a.equals(p.activator)) {
                    for(int k = 0; k < gameAssets.size(); k++) {
                        if(gameAssets.get(k).equals(p.activatee)) {
                            activatables.add(new ActivationPair((ActivatableInput)list.get(i), (ActivatableEntity)list.get(k)));
                            break;
                        }
                    }
                }
            }
        }
        
        for(int i = 0; i < activatables.size(); i++) {
            ActivationPair pair = activatables.get(i);
            for(int j = i + 1; j < activatables.size(); j++) {
                ActivationPair secondPair = activatables.get(j);
                if(secondPair.getOutput().equals(pair.getOutput())) {
                    pair.addInput(secondPair.getInput());
                    activatables.remove(j);
                    j--;
                }
            }
        }
        
        return new AssetListWrapper(list, activatables);
    }
    
    private enum Direction {
        up, down, left, right;
    }
    
    private class BlockFaces {
        Block block;
        boolean up, down, left, right;
                
        public BlockFaces(Block b) {
            block = b;
            up = true;
            down = true;
            left = true;
            right = true;
        }
        
        public boolean equals(Object o) {
            return o instanceof BlockFaces && ((BlockFaces)o).block == block;
        }
    }
    
    public void deleteEntityAtPoint(Point mousePosition) {
        for(int i = 0; i < diagonalWalls.size(); i++) {
            DiagonalWall d = diagonalWalls.get(i);
            if(d.pointInsideBody(mousePosition)) {
                diagonalWalls.remove(i);
                return;
            }
        }
        for(int i = 0; i < gameAssets.size(); i++) {
            GameAsset a = gameAssets.get(i);
            if(a.pointInsideAssetHitbox(mousePosition)) {
                GameAsset removedAsset = gameAssets.remove(i);
                for(int j = 0; j < activationPairs.size(); j++) {
                    ActivePair pair = activationPairs.get(j);
                    if(pair.activatee.equals(removedAsset) || pair.activator.equals(removedAsset)) {
                        activationPairs.remove(j);
                        j--;
                    }
                }
                return;
            }
        }
        for(int i = 0; i < activationPairs.size(); i++) {
            ActivePair pair = activationPairs.get(i);
            Line connection = new Line(pair.activatee.position, pair.activator.position);
            if(connection.withinDimensions(mousePosition.x, mousePosition.y) && connection.getSquaredDistToLine(mousePosition) < 5) {
                activationPairs.remove(i);
                return;
            }
        }
        try {
            if(getTile(mousePosition).containsWall()) {
                this.removeTileContent(mousePosition);
            }
        }
        catch(NullPointerException e) {
            System.out.println("null, mouse not in grid");
        }
    }
    
    private static Asset getAssetFromType(String assetType, int tileSize, Point position, double orientation, boolean flipped, IDGiver IDs, ArrayList<RigidBody> bodyList, String string) {
        if(bodyList == null)
            bodyList = new ArrayList();
        Asset asset = null;
        if(IDs == null)
            IDs = new DefaultIDGiver();
        switch(assetType) {
           case "Turret":
               asset = new Turret(position, IDs.giveID(), flipped);
               break;
           case "Companion Cube":
               asset = new CompanionCube(position, IDs.giveID());
               break;
           case "Companion Cube Dispenser":
               asset = new CompanionCubeDispenser(new CompanionCube(new Point(0, 0), 0), position, new int[] {IDs.giveID(), IDs.giveID(), IDs.giveID(), IDs.giveID()}, bodyList);
               break;
           case "Door":
               asset = new Door(position, tileSize, IDs.giveID());
               break;
           case "Energy Pellet Launcher":
               asset = new EnergyPelletLauncher(position, new Vector(Math.cos(orientation), Math.sin(orientation)), IDs.giveID(), IDs.giveID());
               break;
           case "Energy Pellet Reciever":
               asset = new EnergyPelletReciever(position, new Vector(Math.cos(orientation), Math.sin(orientation)), IDs.giveID());
               break;
           case "Button":
               asset = new Button(position, new int[] {IDs.giveID(), IDs.giveID(), IDs.giveID()}, bodyList);
               break;
           case "Enter Elavator":
               asset = new Elavator(position, true, new int[] {IDs.giveID(), IDs.giveID(), IDs.giveID()}, bodyList, flipped);
               break;
           case "Exit Elavator":
               asset = new Elavator(position, false, new int[] {IDs.giveID(), IDs.giveID(), IDs.giveID()}, bodyList, flipped);
               break;
           case "Floating Text":
               asset = new FloatingText(position, string);
               break;
        }
        return asset;
    }
    
    public class PlaceDownAssetTool {
        private Point placePosition;
        private double placeOrientation;
        private boolean assetNonRotatable;
        private String assetType;
        private Asset asset;
            
        public void setAssetType(String assetType) {
            
            this.assetType = assetType;
            assetNonRotatable = false;
            
            asset = getAssetFromType(assetType, tileSize, new Point(0, 0), 0, false, null, null, "Default");
            
            if(assetType.equals("Door") || assetType.equals("Button") || assetType.equals("Enter Elavator") || assetType.equals("Exit Elavator") || assetType.equals("Companion Cube Dispenser"))
                assetNonRotatable = true;
                
        }
        
        public void flip() {
            if(asset instanceof Flippable)
                ((Flippable)asset).flip();
        }
        
        public void findAssetPositions(Point mousePosition) {
            try {
                if(assetType.equals("Door")) {
                    placeOrientation = 0;
                    placePosition = new Point(mousePosition.x - (mousePosition.x % tileSize) + tileSize/2.0, mousePosition.y - mousePosition.y % tileSize);
                    return;
                }
                placePosition = mousePosition;
                placeOrientation = 0;
                for(DiagonalWall d : diagonalWalls) {
                    if(d.pointInsideBody(mousePosition)) {
                        Line[] wallLines = d.getWallLines();
                        Line closerLine = (wallLines[0].getSquaredDistToLine(mousePosition) < wallLines[1].getSquaredDistToLine(mousePosition))? wallLines[0] : wallLines[1];
                        placePosition = closerLine.getPointOnLineClosestToPoint(mousePosition);
                        if(!assetNonRotatable) {
                            placeOrientation = d.getStructure().getLines()[(closerLine == wallLines[0])? 3 : 1].getNormalUnitVector().getDirection() - Math.PI/2.0;
                        }
                    }
                }
                if(getTile(mousePosition) != null && getTile(mousePosition).containsWall()) {
                    Point relativePosition = new Point(mousePosition.x - topLeftCorner.x, mousePosition.y - topLeftCorner.y);
                    Point relativeTilePos = new Point((relativePosition.x % tileSize), (relativePosition.y % tileSize));
                    Block block = getTile(mousePosition);
                    boolean wallLeft = tiles[block.y][block.x - 1].containsWall();
                    boolean wallRight = tiles[block.y][block.x + 1].containsWall();
                    boolean wallTop = tiles[block.y - 1][block.x].containsWall();
                    boolean wallBottum = tiles[block.y + 1][block.x].containsWall();

                    if(relativeTilePos.y > relativeTilePos.x && (!wallBottum || !wallLeft)) {
                        //bottumLeft
                        if(wallBottum || (relativeTilePos.y < -relativeTilePos.x + tileSize && !wallLeft)) {
                            //left
                            placePosition = new Point(topLeftCorner.x + relativePosition.x - relativeTilePos.x, mousePosition.y);
                            if(!assetNonRotatable)
                                placeOrientation = -Math.PI/2;
                        }
                        else {
                            //bottum
                            placePosition = new Point(mousePosition.x, topLeftCorner.y + relativePosition.y - relativeTilePos.y + tileSize);
                            if(!assetNonRotatable)
                                placeOrientation = Math.PI;
                        }
                    }
                    else {
                        //topRight
                        if(wallRight || (relativeTilePos.y < -relativeTilePos.x + tileSize && !wallTop)) {
                            //top
                            placePosition = new Point(mousePosition.x, topLeftCorner.y + relativePosition.y - relativeTilePos.y);
                        }
                        else {
                            //right
                            if(!assetNonRotatable)
                                placeOrientation = -3/2.0 * Math.PI;
                            placePosition = new Point(topLeftCorner.x + relativePosition.x - relativeTilePos.x + tileSize, mousePosition.y);
                        }
                    }   
                }
                if(assetType.equals("Energy Pellet Reciever") || assetType.equals("Energy Pellet Launcher")) {
                    placeOrientation -= Math.PI/2.0;
                }
                Vector offsetVector = asset.getVectorToBase().multiplyByScalar(-1);
                Point assetPosition = new Point(placePosition.x + offsetVector.getXComp(), placePosition.y + offsetVector.getYComp());
                double newX = placePosition.x + Math.cos(placeOrientation) * (assetPosition.x - placePosition.x) - Math.sin(placeOrientation) * (assetPosition.y - placePosition.y);
                double newY = placePosition.y + Math.sin(placeOrientation) * (assetPosition.x - placePosition.x) + Math.cos(placeOrientation) * (assetPosition.y - placePosition.y);
                placePosition = new Point(newX, newY);
            }
            catch(Exception e) {
                System.out.println("Error finding position");
            }
        }
        
        public void placeAsset(Point mousePosition) {
            findAssetPositions(mousePosition);
            gameAssets.add(new GameAsset(assetType, placePosition, placeOrientation, asset));
            setAssetType(assetType);
        }
        
        public void draw(Graphics g, Camera c) {
            if(placePosition == null || asset == null)
                return;
            asset.draw(g, c, placePosition, placeOrientation);
        }
        
    }
    
    public class EditFloatingTextTool {
        GameAsset selectedAsset;
        FloatingText selectedText;
        
        public void selectText(Point mousePosition) {
            for(int i = 0; i < gameAssets.size(); i++) {
                GameAsset a = gameAssets.get(i);
                if(a.getType().equals("Floating Text") && a.pointInsideAssetHitbox(mousePosition)) {
                    selectedText = (FloatingText) a.exampleAsset;
                    selectedAsset = a;
                    return;
                }
            }
            selectedText = null;
                    
        }
        
        public boolean isWriting() {
            return selectedText != null;
        }
        
        public void editText(char character, boolean delete) {
            if(selectedText != null) {
                selectedText.editText(character, delete);
                selectedAsset.updateBoundingBox();
            }
        }
    }
    
    public class CreateDiagonalWallTool {
        Point[] firstSet;
        Point[] secondSet;
        
        private Point[] getPointSet(Point mousePosition) {
            Point relativePosition = new Point(mousePosition.x - topLeftCorner.x, mousePosition.y - topLeftCorner.y);
            Point relativeTilePos = new Point((relativePosition.x % tileSize), (relativePosition.y % tileSize));
            
            Point[] pointSet = new Point[2];
            
            if(relativeTilePos.y > relativeTilePos.x) {
                //bottumLeft
                if(relativeTilePos.y < -relativeTilePos.x + tileSize) {
                    //left
                    pointSet[0] = new Point(relativePosition.x - relativeTilePos.x, relativePosition.y - relativeTilePos.y + tileSize);
                    pointSet[1] = new Point(pointSet[0].x, pointSet[0].y - tileSize);
                }
                else {
                    //bottum
                    pointSet[0] = new Point(relativePosition.x - relativeTilePos.x, relativePosition.y - relativeTilePos.y + tileSize);
                    pointSet[1] = new Point(pointSet[0].x + tileSize, pointSet[0].y);
                }
            }
            else {
                //topRight
                if(relativeTilePos.y < -relativeTilePos.x + tileSize) {
                    //top
                    pointSet[0] = new Point(relativePosition.x - relativeTilePos.x + tileSize, relativePosition.y - relativeTilePos.y);
                    pointSet[1] = new Point(pointSet[0].x - tileSize, pointSet[0].y);
                }
                else {
                    //right
                    pointSet[0] = new Point(relativePosition.x - relativeTilePos.x + tileSize, relativePosition.y - relativeTilePos.y + tileSize);
                    pointSet[1] = new Point(pointSet[0].x, pointSet[0].y - tileSize);
                }
            }
            return pointSet;
        }
        
        public void placeDownPoints(Point mousePosition, boolean portalable) {
            Point[] pointSet = getPointSet(mousePosition);
            if(firstSet == null)
                firstSet = pointSet;
            else {
                secondSet = pointSet;
                diagonalWalls.add(createNewDiagonalWall(portalable, firstSet, secondSet));
                firstSet = null;
                secondSet = null;
            }
        }
        
        private DiagonalWall createNewDiagonalWall(boolean portleable, Point[] firstSet, Point[] secondSet) {
            if(firstSet == null || secondSet == null)
                return null;
            if(new Line(firstSet[0], secondSet[0]).getIntersection(new Line(firstSet[1], secondSet[1])) != null) {
                //Needs to go in order: f[0], f[1], s[0], s[1] for proper shape
                return (new DiagonalWall(new Point[] {firstSet[0], firstSet[1], secondSet[0], secondSet[1]}, portleable));
            }
            else {
                //needs to go in order: f[0], f[1]. s[1], s[0]
                return (new DiagonalWall(new Point[] {firstSet[0], firstSet[1], secondSet[1], secondSet[0]}, portleable));
            }
        }
        
        
        public void draw(Graphics g, Camera c, Point mousePos) {
            Point[] currentSet = getPointSet(mousePos);
            if(firstSet != null) {
                Line[] outline = createNewDiagonalWall(false, firstSet, currentSet).getLines();
                for(Line l : outline)
                    l.draw(g, Color.white, c);
            }
            new Line(currentSet[0], currentSet[1]).draw(g, Color.red, c); 
        }
    }
    
    public class CreateActivatablePairTool {
        private GameAsset activator;
        private Point mousePosition;
        
        public void selectNewAsset(Point mousePosition) {
            for(GameAsset a : gameAssets) {
                if(a.boundingBox.pointInsideBody(mousePosition)) {
                    if(activator == null) {
                        if(a.activator) {
                            activator = a;
                            return;
                        }
                    }
                    else {
                        if(a.activatable) {
                            ActivePair newPair = new ActivePair(activator, a);
                            if(!activationPairs.contains(newPair))
                            activationPairs.add(newPair);
                                activator = null;
                            return;
                        }
                    }
                }
            }
            activator = null;
        }
        
        public void setMousePosition(Point mousePosition) {
            this.mousePosition = mousePosition;
        }
        
        public void draw(Graphics g, Camera c) {
            try {
            drawActivationPairs(g, c);
            if(activator != null && mousePosition != null)
                new Line(activator.position, mousePosition).draw(g, Color.blue, c);
            }
            catch(ConcurrentModificationException e) {
                
            }
        }
    }
    
    public void drawActivationPairs(Graphics g, Camera c) {
        for(ActivePair p : activationPairs)
            p.draw(g, c);
    }
    
    private class DiagonalWall {
        private Line[] lines;
        private Point[] points;
        private boolean portalableSurface;
        private RigidBody structure;
        private Block[] rootContacts;
        private int[][] rootContactVectors;
        
        public DiagonalWall(Point[] points, boolean portalableSurface) {
            this.points = points;
            this.portalableSurface = portalableSurface;
            lines = new Line[points.length];
            for(int i = 0; i < points.length; i++) {
                Point p1 = points[i];
                Point p2 = points[(i + 1 == points.length)? 0 : i + 1];
                lines[i] = new Line(p1, p2);
            }
            RigidBodyPoint[] rigidBodyPoints = new RigidBodyPoint[points.length];
            for(int i = 0; i < rigidBodyPoints.length; i++) {
                rigidBodyPoints[i] = new RigidBodyPoint(points[i].x, points[i].y);
            }
            structure = new RigidBody(rigidBodyPoints, new Vector(0, 0), 0, 1, 0, 0, true, 0, null, false);
            findRootContacts();
        }
        
        private void findRootContacts() {
            RigidBodyLine baseLine1 = structure.getLines()[0];
            RigidBodyLine baseLine2 = structure.getLines()[2];
            rootContacts = new Block[2];
            rootContactVectors = new int[2][2];
            
            Vector normalVector = baseLine1.getNormalUnitVector();
            Point pointInBlock = new Point((baseLine1.p1.x + baseLine1.p2.x)/2.0 + normalVector.getXComp(), (baseLine1.p1.y + baseLine1.p2.y)/2.0 + normalVector.getYComp());
            rootContacts[0] = getTile(pointInBlock);
            rootContactVectors[0] = new int[] {(int) normalVector.getXComp(), (int) normalVector.getYComp()};
            
            normalVector = baseLine2.getNormalUnitVector();
            pointInBlock = new Point((baseLine2.p1.x + baseLine2.p2.x)/2.0 + normalVector.getXComp(), (baseLine2.p1.y + baseLine2.p2.y)/2.0 + normalVector.getYComp());
            rootContacts[1] = getTile(pointInBlock);
            rootContactVectors[1] = new int[] {(int) normalVector.getXComp(), (int) normalVector.getYComp()};
        }
        
        public Block getRootContact(Point p) {
            if(lines[0].p1.equals(p) || lines[0].p2.equals(p))
                return rootContacts[0];
            if(lines[2].p1.equals(p) || lines[2].p2.equals(p))
                return rootContacts[1];
            return null;
                
        }
        
        public Block getOppositeRootContact(Block b) {
            if(b.equals(rootContacts[0]))
                return rootContacts[1];
            else if(b.equals(rootContacts[1]))
                return rootContacts[0];
            return null;
        }
        
        public int[] getRootVector(Block b) {
            if(b.equals(rootContacts[0]))
                return rootContactVectors[0];
            else if(b.equals(rootContacts[1]))
                return rootContactVectors[1];
            return null;
        }
        
        public Point getClosestPoint(Point p) {
            double closestDist = -1;
            Point closestPoint = null;
            for(Point point : points) {
                double dist = new Vector(p.x - point.x, p.y - point.y).getSquaredMagnitude();
                if(closestDist == -1 || dist < closestDist) {
                    closestPoint = point;
                    closestDist = dist;
                }
            }
            return closestPoint;
        }
        
        public Point getOppositeWallLinePoint(Point p) {
            for(Line l : getWallLines()) {
                if(l.p1.equals(p)) {
                    return l.p2;
                }
                else if(l.p2.equals(p)) {
                    return l.p1;
                }
            }
            return null;
        }
        
        public Point getOppositeRootPoint(Point p) {
            if(lines[0].p1.equals(p))
                return lines[0].p2;
            else if(lines[0].p2.equals(p))
                return lines[0].p1;
            else if(lines[2].p1.equals(p))
                return lines[2].p2;
            else if(lines[2].p2.equals(p))
                return lines[2].p1;
            return null;
        }
        
        public RigidBody getStructure() {
            return structure;
        }
        
        public boolean pointInsideBody(Point p) {
            //Creates a horizontel line centered around P and checks how it intersects with 
            //the lines in the body, if the number of intersects to the left and right of p are odd numbers p is inside the body
            Line l = new Line(new Point(-Integer.MAX_VALUE, p.y), new Point(Integer.MAX_VALUE, p.y));
            int intersectsLeft = 0;
            int intersectsRight = 0;
            for(int i = 0; i < lines.length; i++) {
                Point intersect = lines[i].getIntersection(l);
                if(intersect == null)
                    continue;
                int acc = 10000;
                if((int)(intersect.x * acc) == (int)(p.x * acc))
                    return true;
                else if(intersect.x < p.x)
                    intersectsLeft++;
                else if(intersect.x > p.x)
                    intersectsRight++;
            }
            return (intersectsLeft % 2) == 1 && (intersectsRight % 2) == 1;
        }
        
        public Line[] getLines() {
            return lines;
        }
        
        public Point[] getPoints() {
            return points;
        }
        
        public boolean getPortalable() {
            return portalableSurface;
        }
        
        public Line[] getWallLines() {
            return new Line[] {lines[1], lines[3]};
        }
        
        public void draw(Graphics g, Camera c) {
            int[] xCords = new int[4];
            int[] yCords = new int[4];
            for(int i = 0; i < points.length; i++) {
                xCords[i] = (int) (points[i].x - c.getPosition().x + c.getWidth());
                yCords[i] = (int) (points[i].y - c.getPosition().y + c.getHeight());
            }
            g.setColor((portalableSurface)? Color.WHITE : Color.DARK_GRAY);
            g.fillPolygon(xCords, yCords, points.length);
        }
        
        public void draw(Graphics g, int[] topLeftCorner) {
            int[] xCords = new int[4];
            int[] yCords = new int[4];
            for(int i = 0; i < points.length; i++) {
                xCords[i] = (int) (points[i].x - topLeftCorner[0]);
                yCords[i] = (int) (points[i].y - topLeftCorner[1]);
            }
            g.setColor((portalableSurface)? Color.WHITE : Color.DARK_GRAY);
            g.fillPolygon(xCords, yCords, points.length);
            g.setColor(Color.BLACK);
            g.drawPolygon(xCords, yCords, points.length);
            
            Line sampleWallLine = getWallLines()[0];
            Line sampleFaceLine = getLines()[0];
            Vector wallDirection = new Vector(sampleWallLine.p1.x - sampleWallLine.p2.x, sampleWallLine.p1.y - sampleWallLine.p2.y).getUnitVector();
            Vector root = new Vector(sampleFaceLine.p1.x - sampleFaceLine.p2.x, sampleFaceLine.p1.y - sampleFaceLine.p2.y);
            
            double widthAcross = root.subtract(wallDirection.multiplyByScalar(wallDirection.dotProduct(root))).getMagnitude();
            
            //double widthAcross = tileSize * (sampleWallLine.getLargestY() - sampleWallLine.getSmallestY()) / sampleWallLine.getLength();
            
            double diagonal1 = Math.sqrt(Math.pow(points[0].x - points[2].x, 2) + Math.pow(points[0].y - points[2].y, 2));
            double diagonal2 = Math.sqrt(Math.pow(points[1].x - points[3].x, 2) + Math.pow(points[1].y - points[3].y, 2));
            Line shorterDiagonal = (diagonal1 < diagonal2)? new Line(points[0], points[2]) : new Line(points[1], points[3]);
            Point p1 = shorterDiagonal.getPoints()[0];
            Point p2 = shorterDiagonal.getPoints()[1];
            Vector p1WallLineNormalVector = structure.getLines()[((getWallLines()[0].containsPoint(p1))? 1 : 3)].getNormalUnitVector().multiplyByScalar(widthAcross);
            Vector p1ToP2 = new Vector(p2.x - p1.x, p2.y - p1.y);
            Vector wallVector = p1ToP2.add(p1WallLineNormalVector.multiplyByScalar(1));
            Vector wallVectorUnitVector = wallVector.getUnitVector();
            for(int i = 0; i < wallVector.getMagnitude()/tileSize; i++) {
                Vector v = wallVectorUnitVector.multiplyByScalar(tileSize * i);
                Point linePoint1 = new Point(p1.x + v.getXComp() - topLeftCorner[0], p1.y + v.getYComp() - topLeftCorner[1]);
                Point linePoint2 = new Point(linePoint1.x - p1WallLineNormalVector.getXComp(), linePoint1.y - p1WallLineNormalVector.getYComp());
                Line l = new Line(linePoint1, linePoint2);
                l.draw(g);
            }
            
            Point linePoint1 = new Point(p2.x - topLeftCorner[0], p2.y - topLeftCorner[1]);
            Point linePoint2 = new Point(linePoint1.x + p1WallLineNormalVector.getXComp(), linePoint1.y + p1WallLineNormalVector.getYComp());
            
            Line l = new Line(linePoint1, linePoint2);
            //l.draw(g);
        }
    }
    
    private class Block {
        BlockContent content;
        boolean inside;
        Point pos;
        int x;
        int y;
        
        public Block(Point pos, int x, int y) {
            this.pos = pos;
            this.x = x;
            this.y = y;
        }
        
        public boolean containsWall() {
            return content != null && content instanceof WallTile;
        }
        
        public boolean occupied() {
            return content != null;
        }
    }
    
    private abstract class BlockContent {
        public abstract void draw(Graphics g, Point pos, Camera c);
        public abstract void draw(Graphics g, Point pos);
    }
    
    private class ActivePair {
        private GameAsset activator;
        private GameAsset activatee;
        
        public ActivePair(GameAsset activator, GameAsset activatee) {
            this.activator = activator;
            this.activatee = activatee;
        }
        
        public void draw(Graphics g, Camera c) {
            new Line(activator.position, activatee.position).draw(g, Color.green, c);
        }
        
        public boolean equals(Object o) {
            return o instanceof ActivePair && ((ActivePair)o).activator.equals(activator) && ((ActivePair)o).activatee.equals(activatee);
        }
    }
    
    private class GameAsset {
        private String assetType;
        private boolean activatable;
        private boolean activator;
        private double orientation;
        private Point position;
        private Asset exampleAsset;
        private RigidBody boundingBox;
        boolean flipped = false;
        
        
        public boolean pointInsideAssetHitbox(Point p) {
            return boundingBox.pointInsideBody(p);
        }
        
        public boolean getFlipped() {
            return flipped;
        }
        
        public String getType() {
            return assetType;
        }
        
        public Point getPosition() {
            return position;
        }
        
        public double getOrientation() {
            return orientation;
        }
        
        public void updateBoundingBox() {
            if(exampleAsset == null)
                return;
                
            boundingBox = exampleAsset.getBoundingBox();
            boundingBox.rotate(orientation);
            boundingBox.translate(new Vector(position.x, position.y));
        }
        
        public GameAsset(String type, Point position, double oreintation, Asset example) {
            this.position = position;
            this.orientation = oreintation;
            assetType = type;
            activatable = example instanceof ActivatableEntity;
            activator = example instanceof ActivatableInput;
            exampleAsset = example;
            
            boundingBox = example.getBoundingBox();
            boundingBox.rotate(oreintation);
            boundingBox.translate(new Vector(position.x, position.y));
            if(example instanceof Flippable)
                flipped = ((Flippable)example).getFlipped();
        }
        
        public void draw(Graphics g, Camera c) {
            exampleAsset.draw(g, c, position, orientation);
        }
    }
    
    
    
    private class WallTile extends BlockContent {
        boolean portableSurface;
        
        public WallTile(boolean portable) {
            portableSurface = portable;
        }
        
        public void draw(Graphics g, Point pos, Camera c) {
            Point corner = new Point(pos.x - c.getPosition().x + c.getWidth(), pos.y - c.getPosition().y + c.getHeight());
            g.setColor((portableSurface)? Color.white : Color.darkGray); //temporary, replace with rgb colors
            g.fillRect((int) corner.x, (int) corner.y, tileSize, tileSize);
        }
        
        public void draw(Graphics g, Point pos) {
            Point corner = new Point(pos.x, pos.y);
            g.setColor((portableSurface)? Color.white : Color.darkGray); //temporary, replace with rgb colors
            g.fillRect((int) corner.x, (int) corner.y, tileSize, tileSize);
        }
    }
    
    public void drawBorder(Graphics g, Camera c) {
        for(Block[] bl : tiles) {
            for(Block b : bl) {
                if(b.occupied())
                    b.content.draw(g, new Point(topLeftCorner.x + tileSize * b.x, topLeftCorner.y + tileSize * b.y), c);
            }
        }
        for(DiagonalWall w : diagonalWalls) {
            w.draw(g, c);
        }
    }
    
    public void drawTilesOnly(Graphics g, int[] topLeftCorner) {
        for(Block[] bl : tiles) {
            for(Block b : bl) {
                if(b.occupied()) {
                    b.content.draw(g, new Point(-topLeftCorner[0] + tileSize * b.x, -topLeftCorner[1] + tileSize * b.y));
                    g.setColor(Color.BLACK);
                    g.drawRect((int) (-topLeftCorner[0] + tileSize * b.x), (int) (-topLeftCorner[1] + tileSize * b.y), tileSize, tileSize);
                }
            }
        }
        for(DiagonalWall w : diagonalWalls) {
            w.draw(g, topLeftCorner);
        }
    }
    
    public void draw(Graphics g, Camera c) {
        if(interiorPolygon != null) {
            interiorPolygon.draw(g, c);
        }
        for(Block[] bl : tiles) {
            for(Block b : bl) {
                if(b.occupied())
                    b.content.draw(g, new Point(topLeftCorner.x + tileSize * b.x, topLeftCorner.y + tileSize * b.y), c);
//                if(b.inside) {
//                    g.setColor(Color.PINK);
//                    g.fillRect((int) (topLeftCorner.x + tileSize * b.x - c.getPosition().x + c.getWidth()), (int) (topLeftCorner.y + tileSize * b.y - c.getPosition().y + c.getHeight()), tileSize, tileSize);
//                }
            }
        }
        for(DiagonalWall w : diagonalWalls) {
            w.draw(g, c);
        }
        try {
            for(GameAsset a : gameAssets) {
                a.draw(g, c);
            }
        }
        catch(ConcurrentModificationException e) {
            
        }
        for(int i = 0; i < tiles.length + tiles[0].length + 2; i++) {
            double xShift = (i >= tiles.length + 1)? (i - tiles.length - 1) * tileSize : 0;
            double yShift = (i >= tiles.length + 1)? 0 : i * tileSize;
            Point p1 = new Point(topLeftCorner.x + xShift, topLeftCorner.y + yShift);
            Point p2 = (i >= tiles.length + 1)? new Point(p1.x, topLeftCorner.y + tiles.length * tileSize)
                    : new Point(topLeftCorner.x + tiles[0].length * tileSize, p1.y);
            Line l = new Line(p1, p2);
            l.draw(g, Color.gray, c);
        }
    }
    
}
