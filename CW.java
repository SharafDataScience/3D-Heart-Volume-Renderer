import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.Color;

public class CW {
    private int[][][] data;
    private int gridSize;
    private double scale;
    
    //Constructor to initialize the grid size, allocate data array and generate the heart volume. 
    public CW(int gridSize, double threshold) {
        this.gridSize = gridSize;
        this.data = new int[gridSize][gridSize][gridSize];
        this.scale = 4.0 / (gridSize - 1);
        generateHeartVolume(threshold);
    }

    
    //Populates the 3D data array with values based on the heart equation threshold.
    private void generateHeartVolume(double threshold) {
        for (int z = 0; z < gridSize; z++) {
            for (int y = 0; y < gridSize; y++) {
                for (int x = 0; x < gridSize; x++) {
                    double xScaled = (x - gridSize / 2) * scale;
                    double yScaled = (y - gridSize / 2) * scale;
                    double zScaled = (z - gridSize / 2) * scale;
                    data[z][y][x] = heartEquation(xScaled, yScaled, zScaled) <= threshold ? 1 : 0;
                }
            }
        }
    }

    //Heart-shaped equation to define the volume of the heart.
    private double heartEquation(double x, double y, double z) {
        // Calculate each component of the equation
        double x2 = x * x;
        double y2 = y * y;
        double z2 = z * z;
        double term1 = x2 + 9.0 / 4.0 * y2 + z2 - 1;
        double term1Cubed = Math.pow(term1, 3);
        double x2z3 = x2 * z * z * z;
        double y2z3 = 9.0 / 80.0 * y2 * z * z * z;
    
        // Assemble the full equation
        return term1Cubed - x2z3 - y2z3;
    }
    


    //Calculates the gradient of the heart equation at a point to determine the surface normal.
    private double[] gradient(double x, double y, double z) {
        double epsilon = 0.01;
        double gradX = (heartEquation(x + epsilon, y, z) - heartEquation(x - epsilon, y, z)) / (2 * epsilon);
        double gradY = (heartEquation(x, y + epsilon, z) - heartEquation(x, y - epsilon, z)) / (2 * epsilon);
        double gradZ = (heartEquation(x, y, z + epsilon) - heartEquation(x, y, z - epsilon)) / (2 * epsilon);
        return new double[]{gradX, gradY, gradZ};
    }

    //Renders a 2D projection of the 3D volume onto an image using ray casting.
    public BufferedImage render(int width, int height, double threshold) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        double dx = (double)gridSize / width;
        double dz = (double)gridSize / height;

        for (int z = 0; z < height; z++) {
            for (int x = 0; x < width; x++) {
                int pixelColor = rayCast(x * dx, z * dz, threshold);
                image.setRGB(x, height - 1 - z, pixelColor);
            }
        }
        return image;
    }

   
    //Casts rays through the volume data to compute pixel colors based on intersections with the heart surface.
    private int rayCast(double x, double z, double threshold) {
        for (double y = -2; y <= 2; y += scale) {
            double yScaled = gridSize / 2 + y / scale;
            if (yScaled >= 0 && yScaled < gridSize && interpolate(data, x, yScaled, z) > threshold) {
                double[] grad = gradient(x * scale - 2, y, z * scale - 2);
                double intensity = Math.sqrt(grad[0] * grad[0] + grad[1] * grad[1] + grad[2] * grad[2]) / 10;
                intensity = Math.min(1.0, intensity);
                return new Color(1.0f - (float) intensity, 1.0f - (float) intensity, 1.0f - (float) intensity, 1.0f).getRGB();  // Inverted colors for the heart
            }
        }
        return Color.BLACK.getRGB(); 
    }

    //Interpolates data values within the 3D grid at a given point for smoother rendering.
    private double interpolate(int[][][] data, double x, double y, double z) {
        int xBase = (int) x, yBase = (int) y, zBase = (int) z;
        double xFraction = x - xBase, yFraction = y - yBase, zFraction = z - zBase;

        
        if (xBase >= gridSize - 1 || yBase >= gridSize - 1 || zBase >= gridSize - 1)
            return 0;

        double v000 = data[zBase][yBase][xBase];
        double v001 = data[zBase][yBase][xBase + 1];
        double v010 = data[zBase][yBase + 1][xBase];
        double v011 = data[zBase][yBase + 1][xBase + 1];
        double v100 = data[zBase + 1][yBase][xBase];
        double v101 = data[zBase + 1][yBase][xBase + 1];
        double v110 = data[zBase + 1][yBase + 1][xBase];
        double v111 = data[zBase + 1][yBase + 1][xBase + 1];

        double c00 = v000 * (1 - xFraction) + v001 * xFraction;
        double c01 = v010 * (1 - xFraction) + v011 * xFraction;
        double c10 = v100 * (1 - xFraction) + v101 * xFraction;
        double c11 = v110 * (1 - xFraction) + v111 * xFraction;

        double c0 = c00 * (1 - yFraction) + c01 * yFraction;
        double c1 = c10 * (1 - yFraction) + c11 * yFraction;

        return c0 * (1 - zFraction) + c1 * zFraction;
    }

    //Main method to test rendering of the heart volume image.
    public static void main(String[] args) {
        try {
            int gridSize = 256, width = 512, height = 512;
            double threshold = 0.0; 

            if (args.length > 0) {
                gridSize = Integer.parseInt(args[0]);
                width = Integer.parseInt(args[1]);
                height = Integer.parseInt(args[1]);
            }

            CW renderer = new CW(gridSize, threshold);
            BufferedImage image = renderer.render(width, height, threshold);
            File outputfile = new File("result.tiff");
            ImageIO.write(image, "tiff", outputfile);
            System.out.println("Isosurface image saved to " + outputfile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error: Unable to save the image file. Check the file path and permissions.");
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("Error: Invalid input for parameters. Please enter correct values.");
            e.printStackTrace();
        }
    }
}
