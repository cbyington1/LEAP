/*
Title: LEAP (Lowest Elevation Algorithm by Parts)
Author: Camden Byington
Date: 3/17/2023

Arguments: [Starting Row], [Starting Col], [End Row], [End Col], [Interval Number], [File name]

imageReader:
This program is an A* algorithm which seeks to calculate a practical path with the lowest elevation change between two points on a black and white
topographic map where shade represents elevation. It begins at the start of the path and uses an interval system,
in combination with several heuristics and elevation as a cost function, to identify each possible route within the interval,
and then combines all the best routes within each interval to get the final path. Below are the heuristics used to optimize the path finding.

    Interval System:
    The interval system is used to reduce the time complexity of the algorithm, which is necessary for longer distances.
    The user is able to specify the desired interval length, thus reducing the time complexity,
    however, the path generated may not be as optimal as you'd like as it is important to note that larger intervals will generally provide a better path.

    Slope preference:
    To avoid obstacles with steep, uphill slopes, the program prioritizes decline slopes between the current and desired positions.
    It then checks for the lowest elevation change to avoid steep declines. This approach may result in a slightly worse elevation change for short distances,
    but for longer distances, it can lead to greater decreases in the elevation change and ultimately better routes.

    Direction:
    To keep the path on track, it needs to head in the general direction of the desired spot. The Direction is found by
    calculating the slope and then determining whether the desired location is lower or higher than the current position, so
    the path knows whether to go up or down the slope. Through the recursive process, the direction is constantly being updated so if the path
    strays to far, it can readjust it's direction.

    Landmarks:
    In case of path finding giving sub optimal results, you can manually guide the path to certain spots to give more optimized
    path results

datReader:
This program converts any text-based map into an image file. To use the program, first run it with the text file as an argument,
and then run it again with the newly created PNG file. The file will have the same name, just with the ".png" extension instead. Make sure to change the argument!

Land:
Class that creates a land object. Land objects have a row, column, and elevation level. This object makes it easier to get data and
apply it to the paths in the imageReader program. Land objects also provide methods for getting their row, column, and elevation level.
 */

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class TopoPath{

    public static void main(String[] args) throws IOException {
        //starting points
        int currRow = Integer.parseInt(args[0]);
        int currCol = Integer.parseInt(args[1]);

        //ending points
        int desiredRow = Integer.parseInt(args[2]);;
        int desiredCol = Integer.parseInt(args[3]);;

        //done in n intervals
        int intervalNum = Integer.parseInt(args[4]);;

        //Name of file used for reading map
        String fileName = args[5];

        if (fileName.contains(".dat") || fileName.contains(".txt")) {
            //create JFrame object for drawing map
            JFrame frame = new JFrame();

            //create panel object with filename and add changes to frame object
            datReader panel = new datReader(fileName);
            frame.add(panel, BorderLayout.CENTER);

            //set title of frame
            frame.setTitle("LEAP");

            //Set frame to close when operation is complete
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            //set frame size by the width and height of data in file
            frame.setSize(panel.width, panel.height);
            frame.setLocationRelativeTo(null);

            //make frame visible
            frame.setVisible(true);
        }
        else {
            //create JFrame object for drawing map
            JFrame frame = new JFrame();

            //create file object for picture file
            File file = new File(fileName);

            //create image object through file
            BufferedImage image = ImageIO.read(file);

            //set border frame from object
            frame.getContentPane().setLayout(new BorderLayout());

            //establish scanner and landmark variables
            Scanner scanner = new Scanner(System.in);
            int maxLandmarks = 5;
            int landmarkCount = 0;
            int[][] landmarks = new int[maxLandmarks][2];

            //prompt user
            System.out.println("Enter landmarks (maximum of 5). Enter 'stop' to finish.");

            //loop until max amount of landmarks is reached
            while (landmarkCount < maxLandmarks) {
                //prompt for row
                System.out.print("Enter landmark row position: ");
                String rowInput = scanner.nextLine();

                if (rowInput.equalsIgnoreCase("stop")) {
                    break;
                }

                //prompt for column
                System.out.print("Enter landmark column position: ");
                String colInput = scanner.nextLine();

                if (colInput.equalsIgnoreCase("stop")) {
                    break;
                }

                //take in values
                int row = Integer.parseInt(rowInput);
                int col = Integer.parseInt(colInput);

                //define in 2d array
                landmarks[landmarkCount][0] = row;
                landmarks[landmarkCount][1] = col;

                //increment landmark counter
                landmarkCount++;
            }

            //print total amount of landmarks
            System.out.println("Total landmarks entered: " + landmarkCount + "\n");

            //create reader object
            imageReader reader = new imageReader(image, currRow, currCol, desiredRow, desiredCol, intervalNum, landmarks, landmarkCount);

            //get content from object and update map
            frame.getContentPane().add(reader);

            //set height and width of file
            int width = reader.canvas.getWidth();
            int height = reader.canvas.getHeight();

            //set frame size
            frame.setSize(width, height);
            frame.setLocationRelativeTo(null);

            //make frame visible
            frame.setVisible(true);
        }

        System.out.println(fileName);
    }
}
