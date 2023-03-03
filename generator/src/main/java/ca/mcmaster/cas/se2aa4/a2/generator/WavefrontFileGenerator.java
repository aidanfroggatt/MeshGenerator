package ca.mcmaster.cas.se2aa4.a2.generator;

import ca.mcmaster.cas.se2aa4.a2.io.Structs;

import java.io.FileWriter;
import java.io.IOException;

public class WavefrontFileGenerator {
    public static void createWavefrontFile(String filename, Structs.Mesh mesh) throws IOException {
        if (filename == null) {
            return;
        }
        FileWriter myWriter = new FileWriter(filename);

        // Iterate over each vertex
        double red, green, blue;
        for (Structs.Vertex vertex : mesh.getVerticesList()) {
            int[] colors = Helper.extractColor(vertex);
            red = (double) colors[0] / 255.00;
            green = (double) colors[1] / 255.00;
            blue = (double) colors[2] / 255.00;
            myWriter.write("v " + vertex.getX() + " " + vertex.getY() + " 1"+" "+red+" "+green+" "+blue);
            myWriter.write(System.getProperty("line.separator"));
        }

        // Iterate over each segment
        for (Structs.Segment segment : mesh.getSegmentsList()) {
            myWriter.write("l " + (segment.getV1Idx() + 1) + " " + (segment.getV2Idx()) + 1);
            myWriter.write(System.getProperty("line.separator"));
        }

        String polygonLine;
        Structs.Segment segment;
        for (Structs.Polygon polygon : mesh.getPolygonsList()) {
            polygonLine = "f";
            for (int idx : polygon.getSegmentIdxsList()) {
                segment = mesh.getSegments(idx);
                polygonLine += " " + (segment.getV1Idx() + 1) + " " + (segment.getV2Idx() + 1);
            }
            myWriter.write(polygonLine);
            myWriter.write(System.getProperty("line.separator"));
        }

        myWriter.close();
    }
}
