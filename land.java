public class land {
    //create variables for land object
    int elevation;
    int x;
    int y;

    //constructor method
    public land(int elevation, int x, int y){
        this.elevation = elevation;
        this.x = x;
        this.y = y;
    }

    public int getElevation(){
        return elevation;
    }

    public int getRow(){
        return x;
    }

    public int getCol(){
        return y;
    }
}
