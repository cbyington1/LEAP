import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import java.awt.*;
import java.io.*;
import java.util.*;


public class datReader extends JPanel {
    //create canvas
    private static BufferedImage canvas;

    //keep track of highest and lowest values in file
    static int highest = 0;
    static int lowest = 10000;

    //width and height of picture
    static int width;
    static int height;

    //filename
    static String fileName;

    public datReader(String fileName) throws IOException {
        //set fileName
        this.fileName = fileName;

        //set Width and Height
        width = getWidth(fileName);
        height = getHeight(fileName);

        canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        //scans in file and creates 2D array from file using getNumber method
        Scanner sc = new Scanner(new File(fileName));
        int[][] elevations = getNumbers(sc);

        //set shades of each pixel
        setShades(elevations);

        int currRow = 0;
        int currCol = 0;
        land root = new land(elevations[currRow][currCol], currRow, currCol);

        int desiredRow = 10;
        int desiredCol = 0;

    }

    public int getHeight(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));

        int counter = 0;

        while (reader.readLine() != null) {
            counter++;
        }
        return counter;
    }


    public int getWidth(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));

        int counter = 0;
        String line = reader.readLine();
        boolean inNum = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c != ' ' && c != 9) {
                if (!inNum) {
                    counter++;
                }
                inNum = true;
            } else {
                inNum = false;
            }
        }
        return counter;
    }


    //gets numbers from file
    public int[][] getNumbers(Scanner sc) {
        //create new 2D array
        int[][] elevations = new int[width][height];

        //loop thorugh all numbers
        while (sc.hasNextInt()) {
            for (int j = height - 1; j >= 0; j--) {
                for (int i = 0; i < width; i++) {
                    //set elevation at column i row j to the next number
                    elevations[i][j] = sc.nextInt();
                    //check for highest number in file
                    if (highest < elevations[i][j]) {
                        highest = elevations[i][j];
                    }
                    //check for lowest number in file
                    if (lowest > elevations[i][j]) {
                        lowest = elevations[i][j];
                    }
                }
            }
        }
        //return 2D array
        return elevations;
    }

    //paints pixels
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(canvas, null, null);

        File outputfile = new File(fileName.substring(0, fileName.indexOf(".")) + ".png");
        try {
            ImageIO.write(canvas, "png", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //sets value to corresponding shade
    public void setShades(int[][] elevations) {

        for (int j = height - 1; j >= 0; j--) {
            for (int i = 0; i < width; i++) {
                //file gave values incorrectly so flip j and i
                canvas.setRGB(i, j, getPixelShade(elevations[i][j]));
            }
        }
    }

    //calculates shade based on the highest and lowest shade
    public int getPixelShade(int elevation) {
        //find the value it takes to go from 1 rgb value to the next
        double difference = highest - lowest;
        difference = Math.ceil(difference / 255);

        //find true rgb value taking into account the total difference and using the ratio to get the amount in rgb
        int shadeNum = (elevation - lowest) / (int) difference;
        Color myShade = new Color(shadeNum, shadeNum, shadeNum);

        //convert color to int and return
        int shade = myShade.getRGB();
        return shade;
    }
}

