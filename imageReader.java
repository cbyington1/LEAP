import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import java.awt.*;
import java.util.*;


public class imageReader extends JPanel {

    //boolean for declaring if the spot has been found
    volatile boolean found = false;

    //boolean for declaring if a path that has a lower slope has been found
    volatile boolean currLower = false;

    int equalCounter = 0;

    //arraylist for final path
    static ArrayList<land> finalPath;

    //create BufferedImage object
    BufferedImage canvas;

    public imageReader(BufferedImage image, int currRow, int currCol, int desiredRow, int desiredCol, int intervalNum, int landmarks[][], int landmarkCount) {
        //set canvas to current image provided from main
        canvas = image;

        //reset rows due to how images are read for 2D arrays
        currRow = ((canvas.getHeight()-1) - currRow);
        desiredRow = ((canvas.getHeight()-1) - desiredRow);

        if(!inRange(currRow, currCol, desiredRow, desiredCol)){
            throw new IndexOutOfBoundsException("Coordinates out of bounds!");
        }

        //define image
        image = new BufferedImage(image.getWidth(), image.getHeight(),
                BufferedImage.TYPE_INT_ARGB);

        //get elevations into 2d to make data easier to access
        int[][] elevations = getShadeValues();

        //Arraylist for current path currently being tried
        ArrayList<land> currPath = new ArrayList<land>();

        //ArrayList for lowest path in interval
        ArrayList<land> lowestPath = new ArrayList<land>();

        //loop through landmarks
        for (int i = 0; i < landmarkCount; i++) {
            //establish landmark variables
            int desiredLMRow = ((canvas.getHeight()-1) - landmarks[i][0]);
            int desiredLMCol = landmarks[i][1];

            //if first landmark
            if(i == 0) {
                //get best path in intervals
                finalPath = completePath(elevations, currRow, currCol, desiredLMRow, desiredLMCol, intervalNum);
                found = false;
            }
            //if not first landmark
            else{
                //get best path in intervals
                ArrayList<land> path = completePath(elevations, currRow, currCol, desiredRow, desiredCol, intervalNum);

                //add values to path
                for (int j = 0; j < path.size(); j++) {
                    finalPath.add(path.get(j));
                }

                found = false;
            }

            //reset current values
            currRow = desiredLMRow;
            currCol = desiredLMCol;
        }

        //check if there are no landmarks
        if(landmarkCount == 0) {
            //get best path in intervals
            finalPath = completePath(elevations, currRow, currCol, desiredRow, desiredCol, intervalNum);
        }
        else{
            //get best path in intervals
            ArrayList<land> path = completePath(elevations, currRow, currCol, desiredRow, desiredCol, intervalNum);

            //add values to path
            for (int j = 0; j < path.size(); j++) {
                finalPath.add(path.get(j));
            }
        }

        //establish color counter
        int intervalColorCounter = intervalNum;

        //color in path
        for(int i = 0; i < finalPath.size(); i++){
            //if end of path, make black
            if(finalPath.get(i).getCol() == desiredCol && finalPath.get(i).getRow() == desiredRow){
                canvas.setRGB(finalPath.get(i).getCol(), finalPath.get(i).getRow(), Color.BLACK.getRGB());
                break;
            }
            //if spot marks new interval, make green
            else if(intervalColorCounter == intervalNum){
                canvas.setRGB(finalPath.get(i).getCol(), finalPath.get(i).getRow(), Color.GREEN.getRGB());
                intervalColorCounter = 1;
            }
            //else make it red
            else {
                canvas.setRGB(finalPath.get(i).getCol(), finalPath.get(i).getRow(), Color.RED.getRGB());
                intervalColorCounter++;
            }
            //if spot is landmark make it blue
            for (int j = 0; j < landmarkCount; j++) {
                int landmarkRow = ((canvas.getHeight()-1) - landmarks[j][0]);
                int landmarkCol = landmarks[j][1];
                if (finalPath.get(i).getCol() == landmarkCol && finalPath.get(i).getRow() == landmarkRow) {
                    canvas.setRGB(finalPath.get(i).getCol(), finalPath.get(i).getRow(), Color.BLUE.getRGB());
                    intervalColorCounter = 1;
                    continue;
                }
            }
        }
    }

    //Creates image
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.drawImage(canvas, 0, 0, getWidth(), getHeight(), this);
    }

    //Checks if the coordinates provides are in range of the picture
    public boolean inRange(int currRow, int currCol, int desiredRow, int desiredCol){
        /*
        checks if any of the coordinates are greater than or equal to the max height or width
        it's important to check for equal to as well as the array index only goes to the max - 1
         */
        if(currRow >= canvas.getHeight() || desiredRow >= canvas.getHeight() ||
                currCol >= canvas.getWidth() || desiredCol >= canvas.getWidth()){
            return false;
        }
        //check if any values are less than 0
        else if(currRow < 0 || currCol < 0 || desiredRow < 0 || desiredCol < 0){
            return false;
        }

        return true;
    }

    //get shade values of image
    public int[][] getShadeValues() {
        //create 2d array based on height and width of image
        int[][] shadeValues = new int[canvas.getWidth()][canvas.getHeight()];

        //loop through each pixel
        for (int i = 0; i < canvas.getWidth(); i++) {
            for (int j = 0; j < canvas.getHeight(); j++) {
                //get RGB value of pixel
                int color = canvas.getRGB(i, j);

                //Get true individual RGB value as shades have all the same RGB values
                int num = color & 0xff;

                //set value to pixel
                shadeValues[i][j] = num;
            }
        }

        //return shades in 2d array
        return shadeValues;
    }

    //finds total best path
    public ArrayList<land> completePath(int[][] elevations, int currRow, int currCol, int desiredRow, int desiredCol, int intervalNum){
        //establish arraylists for keeping track of the lowest path for that interval, and completedPath which will be all interval paths added together
        ArrayList<land> completedPath = new ArrayList<>();
        ArrayList<land> intervalPath = new ArrayList<>();

        //Arraylist for current path currently being tried
        ArrayList<land> currPath = new ArrayList<land>();

        //ArrayList for lowest path in interval
        ArrayList<land> lowestPath = new ArrayList<land>();

        //variable to control while loop
        boolean notFoundSpot = true;

        //variable for finding total Elevation change
        int totalElevationChange = 0;

        while(notFoundSpot){
            //starting location for each interval
            land root = new land(elevations[currRow][currCol], currRow, currCol);
            currPath.add(root);

            //reset direction to adjust for new direction
            int direction = getDirection(currRow, currCol, desiredRow, desiredCol);
           //direction = lookAndChange(elevations, currRow,currCol, direction, intervalNum);


            //get interval path
            intervalPath = findBestPath(elevations, currRow, currCol, desiredRow, desiredCol, direction, currPath, lowestPath, intervalNum);

            //loop through array and print current Row and Col each index
            //also, print the original coordinates for user
            for (int i = 0; i < intervalPath.size(); i++) {
                System.out.print("[" + ((canvas.getHeight() - intervalPath.get(i).getRow() - 1)) + ", " + intervalPath.get(i).getCol() + "] ");
            }

            //print total elevation change
            System.out.print("Current Interval Elevation Change = " + getTotalElevationChange(intervalPath));
            System.out.println("\n ----------------------------------------------");

            //add current elevation change to totalElevationChange
            totalElevationChange += getTotalElevationChange(intervalPath);

            //copy all spots from interval to completedPath
            for(int j = 0; j < intervalPath.size(); j++){
                completedPath.add(intervalPath.get(j));
            }

            //check if we've hit desired spot
            if(intervalPath.get(intervalPath.size() - 1).getRow() == desiredRow && intervalPath.get(intervalPath.size() - 1).getCol() == desiredCol){
                notFoundSpot = false;
            }

            //set new starting coordinates for next interval
            currRow = intervalPath.get(intervalPath.size() - 1).getRow();
            currCol = intervalPath.get(intervalPath.size() - 1).getCol();

            //reset paths for next interval
            intervalPath.removeAll(intervalPath);
            currPath.removeAll(currPath);
            lowestPath.removeAll(lowestPath);

            //rest currlower
            currLower = false;
        }

        //print total elevation change
        System.out.println("Total elevation change is: " + totalElevationChange + "\n");

        //return completedPath
        return completedPath;
    }

    //recursive function that finds best path in current interval
    public ArrayList<land> findBestPath(int[][] elevations, int currRow, int currCol, int desiredRow, int desiredCol, int direction, ArrayList<land> currPath,
                                        ArrayList<land> lowestPath, int intervalNum){

        //establish variables used for trying next land
        int firstRow = 0;
        int secondRow = 0;
        int thirdRow = 0;

        int firstCol = 0;
        int secondCol = 0;
        int thirdCol = 0;

        /*
        if: lists are equal ignore it

        else if: we've hit the desired location. This takes priority over everything else. Create a boolean value that
        acknowledges we've hit the desired location and only acknowledges other runs that do to. Set currLower to false as
        we know it's always gonna be false

        else if: Check if we've hit the max number for the interval. Make sure to check
        */
        if(currRow == desiredRow && currCol == desiredCol){
            //check if the desired location hasn't been found yet
            if(!found){
                found = true;

                lowestPath.removeAll(lowestPath);

                //add new lowest path objects
                for(int i = 0; i < currPath.size(); i++){
                    lowestPath.add(currPath.get(i));
                }

                currLower = false;
            }
            //if it has been found and lists are different, compare it like a regular array
            else {
                lowestPath = changeArray(desiredRow, desiredCol, currPath, lowestPath);
            }
            return lowestPath;
        }
        //check if end of interval
        else if(intervalNum == 0) {
            //check if currPath meets the requirements for being a lower elevation than the desired lands elevation
            if (isLower(currPath, elevations, desiredRow, desiredCol)) {
                //if not found and a currLower hasn't been found yet
                if (!currLower && !found) {
                    currLower = true;

                    lowestPath.removeAll(lowestPath);

                    //add new lowest path objects
                    for (int i = 0; i < currPath.size(); i++) {
                        lowestPath.add(currPath.get(i));
                    }
                }
                else {
                    //if currLower has already been found compare it like regular
                    if(!found) {
                        //call changeArray function to see if we need to update the lowest path and if so, change it
                        lowestPath = changeArray(desiredRow, desiredCol, currPath, lowestPath);
                    }
                }
            }
            else{
                //only compare if the desired spot hasn't been found
                if(!found) {
                    //call changeArray function to see if we need to update the lowest path and if so, change it
                    lowestPath = changeArray(desiredRow, desiredCol, currPath, lowestPath);
                }
                //only compare if the currLower spot hasn't been found
                if(!currLower){
                    lowestPath = changeArray(desiredRow, desiredCol, currPath, lowestPath);
                }
            }

            return lowestPath;
        }

        //possible spots depend on direction
        switch(direction){
            //goes up
            case(1):
                //rows and column spots for current direction
                firstRow = currRow + 1;
                secondRow = currRow + 1;
                thirdRow = currRow + 1;

                firstCol = currCol - 1;
                secondCol = currCol;
                thirdCol = currCol + 1;

                break;
            //goes down
            case(5):
                //rows and column spots for current direction
                firstRow = currRow - 1;
                secondRow = currRow - 1;
                thirdRow = currRow - 1;

                firstCol = currCol - 1;
                secondCol = currCol;
                thirdCol = currCol + 1;

                break;
            //goes right at a positive diagonal
            case(2):
                //rows and column spots for current direction
                firstRow = currRow + 1;
                secondRow = currRow + 1;
                thirdRow = currRow;

                firstCol = currCol;
                secondCol = currCol + 1;
                thirdCol = currCol + 1;

                break;
            //goes left at a positive diagonal
            case(6):
                //rows and column spots for current direction
                firstRow = currRow - 1;
                secondRow = currRow - 1;
                thirdRow = currRow;

                firstCol = currCol;
                secondCol = currCol - 1;
                thirdCol = currCol - 1;

                break;
            //right at a negative diagonal
            case(4):
                //rows and column spots for current direction
                firstRow = currRow;
                secondRow = currRow - 1;
                thirdRow = currRow - 1;

                firstCol = currCol + 1;
                secondCol = currCol + 1;
                thirdCol = currCol;

                break;
            //left at a negative diagonal
            case(8):
                //rows and column spots for current direction
                firstRow = currRow + 1;
                secondRow = currRow + 1;
                thirdRow = currRow;

                firstCol = currCol;
                secondCol = currCol - 1;
                thirdCol = currCol - 1;

                break;
            //right
            case(3):
                //rows and column spots for current direction
                firstRow = currRow + 1;
                secondRow = currRow;
                thirdRow = currRow - 1;

                firstCol = currCol + 1;
                secondCol = currCol + 1;
                thirdCol = currCol + 1;

                break;
            //left
            case(7):
                //rows and column spots for current direction
                firstRow = currRow - 1;
                secondRow = currRow;
                thirdRow = currRow + 1;

                firstCol = currCol - 1;
                secondCol = currCol - 1;
                thirdCol = currCol - 1;

                break;
        }

        //Get next three spots from tryLand
        tryLand(elevations, desiredRow, desiredCol, direction, currPath, lowestPath,
                intervalNum, firstRow, secondRow, thirdRow, firstCol, secondCol, thirdCol);

        //return lowestPath for final result
        return lowestPath;
    }

    //checks if array needs to be changed (if our current path is lower than the lowest path we've encountered so far) and if so change it
    public ArrayList<land> changeArray(int desiredRow, int desiredCol, ArrayList<land> currPath, ArrayList<land> lowestPath){
        //checks if no paths have been added yet and if so add currPath to lowestPath
        if (lowestPath.isEmpty()){
            for(int i = 0; i < currPath.size(); i++){
                lowestPath.add(currPath.get(i));
            }
        }

        //if lowestPath is filled but the current path we're testing has a lower elevation change that our lowest so far, update lowest
        else if(getTotalElevationChange(currPath) < getTotalElevationChange(lowestPath)){
            //remove all objects from lowestPath
            lowestPath.removeAll(lowestPath);

            //add new lowest path objects
            for(int i = 0; i < currPath.size(); i++){
                lowestPath.add(currPath.get(i));
            }
        }

        return lowestPath;
    }

    //tries all possible locations to move to. This function calls back to find best path which makes it recursive
    public void tryLand(int[][] elevations, int desiredRow, int desiredCol,
                        int direction, ArrayList<land> currPath, ArrayList<land> lowestPath, int intervalNum,
                        int firstRow, int secondRow, int thirdRow, int firstCol, int secondCol, int thirdCol){

        //establish booleans for checking if land exists
        boolean left = true;
        boolean mid = true;
        boolean right = true;

        //establish land objects for next possible spots
        land tryLeftLand = null;
        land tryMidLand = null;
        land tryRightLand = null;

        //try to see if it's possible to create land. if it's not, set variables to false
        try{
            tryLeftLand = new land(elevations[firstRow][firstCol], firstRow, firstCol);
        }
        catch(IndexOutOfBoundsException e){
            left = false;
        }
        try{
            tryMidLand = new land(elevations[secondRow][secondCol], secondRow, secondCol);
        }
        catch(IndexOutOfBoundsException e){
            mid = false;
        }
        try{
            tryRightLand = new land(elevations[thirdRow][thirdCol], thirdRow, thirdCol);
        }
        catch(IndexOutOfBoundsException e){
            right = false;
        }


        //try left land
        if(left) {
            currPath.add(tryLeftLand);
            findBestPath(elevations, firstRow, firstCol, desiredRow, desiredCol, direction, currPath, lowestPath, intervalNum - 1);

            //once you've gone through and returned the array and made changes needed, remove the last index so you can try the next path
            currPath.remove(currPath.size() - 1);
        }
        //try middle land
        if(mid) {
            currPath.add(tryMidLand);
            findBestPath(elevations, secondRow, secondCol, desiredRow, desiredCol, direction, currPath, lowestPath, intervalNum - 1);

            //once you've gone through and returned the array and made changes needed, remove the last index so you can try the next path
            currPath.remove(currPath.size() - 1);
        }
        //try right land
        if(right) {
            currPath.add(tryRightLand);
            findBestPath(elevations, thirdRow, thirdCol, desiredRow, desiredCol, direction, currPath, lowestPath, intervalNum - 1);

            //once you've gone through and returned the array and made changes needed, remove the last index so you can try the next path
            currPath.remove(currPath.size() - 1);
        }

    }

    //finds general direction path needs to go to hit desired path
    public int getDirection(int currRow, int currCol, int desiredRow, int desiredCol) {
        //establish slope variable
        double slope;

        //Find slope using slope formula (rise over run). If divide by 0 exception is caught set slope to three
        //cast the values so the answer is not in integer form
        try{
            slope = ((double)(desiredRow - currRow))/((double)(desiredCol - currCol));
        }
        catch(ArithmeticException e){
            slope = 5;
        }

        //check if going down or up
        if(slope > 4 || slope < -4){
            //going up
            if(desiredRow > currRow){
                return 1;
            }
            //going down
            else{
                return 5;
            }
        }
        //check if going at a positive diagonal
        else if(slope > .5){
            //going to the right
            if(desiredCol > currCol){
                return 2;
            }
            //going to the left
            else{
                return 6;
            }
        }
        //check if going at a negative diagonal
        else if(slope < -.5){
            if(desiredCol > currCol){
                //going to the right
                return 4;
            }
            else{
                //going to the left
                return 8;
            }
        }
        //check if going left or right exactly
        else{
            //going to the right
            if(desiredCol > currCol){
                return 3;
            }
            //going to the left
            else{
                return 7;
            }
        }
    }

    //get total elevation change between two points
    public int getTotalElevationChange(ArrayList<land> path){
        //establish difference variable
        int difference = 0;

        //loop through all points in path
        for(int i = 0; i < path.size() - 1; i++){
            //get current elevation in this path and the next elevation
            int currentElevation = path.get(i).getElevation();
            int nextElevation = path.get(i + 1).getElevation();

            //get difference and add it to total difference in elevation
            difference += Math.abs(currentElevation - nextElevation);

        }

        //return difference
        return difference;
    }

    //checks if currPaths final spot is at a lower elevation than the desired spot. This keeps us away from
    //obstacles that are too steep
    public boolean isLower(ArrayList<land> currPath, int[][] elevations, int desiredRow, int desiredCol){
        //get both elevations
        int endElevation = elevations[desiredRow][desiredCol];
        int currentElevation = currPath.get(currPath.size() - 1).getElevation();

        //if currentElevation is at a lower elevation than endElevation return true
        if (currentElevation < endElevation){
            return true;
        }

        return false;
    }

}

