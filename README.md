# LEAP (Lowest Elevation Algorithm by Parts)

## Overview
LEAP is an A* algorithm designed to find the most practical path with the lowest elevation change between two points on a black and white topographic map. The algorithm incorporates an interval system, heuristics, and elevation as a cost function to optimize pathfinding. This repository contains programs for image reading, data reading, and a Land class.

## Programs

### `imageReader.java`
`imageReader` is the main program for pathfinding. It utilizes the A* algorithm along with the following heuristics:

- **Interval System**: Reduces time complexity by allowing users to specify the desired interval length. Larger intervals generally provide a better path but may sacrifice optimality.
- **Slope Preference**: Prioritizes decline slopes between current and desired positions to avoid steep uphill obstacles. This approach may lead to better routes for longer distances.
- **Direction**: Determines the general direction of the desired spot by calculating slopes. Constantly updates direction to keep the path on track.
- **Landmarks**: Allows manual guidance of the path to certain spots for more optimized results.

### `datReader.java`
`datReader` converts any text-based map into an image file. To use the program:
1. Run it with the text file as an argument.
2. Run it again with the newly created PNG file (with ".png" extension instead of ".dat").

### `Land.java`
The `Land` class creates a land object with row, column, and elevation level attributes. It facilitates data retrieval and application to paths in the `imageReader` program.

## Usage
To use LEAP, follow these steps:
1. Ensure you have Java installed.
2. Clone this repository.
3. Compile the Java files.
4. Execute the `imageReader` program with appropriate arguments.

## Example Image
![Image 1](https://i.imgur.com/fHop91S.png)
