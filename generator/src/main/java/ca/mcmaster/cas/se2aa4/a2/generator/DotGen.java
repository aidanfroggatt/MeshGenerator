package ca.mcmaster.cas.se2aa4.a2.generator;

import ca.mcmaster.cas.se2aa4.a2.io.Structs.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.triangulate.VoronoiDiagramBuilder;

import java.util.*;

public class DotGen {
    private final int WIDTH = 500;
    private final int HEIGHT = 500;
    private final int SQUARE_SIZE = 20;

    public Mesh generate(MeshType meshType, int numOfPolygons, int relaxationLevel) {
        return meshType == MeshType.GRID ? generateGridMesh() : generateIrregularMesh(numOfPolygons, relaxationLevel);
    }

    public Mesh generateGridMesh() {

        //create arraylists to store the respective vertices and segments
        List<Vertex> vertices = new ArrayList<Vertex>();
        List<Segment> segments = new ArrayList<Segment>();
        List<Polygon> polygons = new ArrayList<Polygon>();

        Vertex v1, v2, v3, v4;

        // counter variable to store the index to be used for each segment relation
        int segment_index = 0;

        // Number of rows and rows for grid mesh
        int num_of_cols = WIDTH / SQUARE_SIZE;
        int num_of_rows = HEIGHT / SQUARE_SIZE;

        // Indexes for vertices
        int v1Idx, v2Idx, v3Idx, v4Idx, i;

        // Current x and y value
        double curr_x;
        double curr_y;

        // Create all the vertices
        for (int x = 0; x < num_of_cols; x++) {
            for (int y = 0; y < num_of_rows; y++) {

                curr_x = (double) x * SQUARE_SIZE;
                curr_y = (double) y * SQUARE_SIZE;

                // build and add each vertex object within vertices array
                // add created segment within segments arraylist
                v1 = Vertex.newBuilder().setX(curr_x).setY(curr_y).build();
                v2 = Vertex.newBuilder().setX(curr_x + SQUARE_SIZE).setY(curr_y).build();
                v3 = Vertex.newBuilder().setX(curr_x).setY(curr_y + SQUARE_SIZE).build();
                v4 = Vertex.newBuilder().setX(curr_x + SQUARE_SIZE).setY(curr_y + SQUARE_SIZE).build();

                // Adding vertices
                v1Idx = addVertices(vertices, v1);
                v2Idx = addVertices(vertices, v2);
                v3Idx = addVertices(vertices, v3);
                v4Idx = addVertices(vertices, v4);

                segments.add(segment_index, Segment.newBuilder().setV1Idx(v1Idx).setV2Idx(v2Idx).build());
                segment_index++;
                segments.add(segment_index, Segment.newBuilder().setV1Idx(v1Idx).setV2Idx(v3Idx).build());
                segment_index++;


                // Initializing arraylists for polygon's segments indexes and neighbors
                ArrayList<Integer> neighbor_arraylist = new ArrayList<>();
                ArrayList<Integer> segment_arraylist = new ArrayList<>(Arrays.asList(segment_index - 2, segment_index - 1));
                if (y != 0) {
                    neighbor_arraylist.add((x * num_of_rows) + y - 1);
                }
                if (y == num_of_rows - 1) {
                    segments.add(segment_index, Segment.newBuilder().setV1Idx(v3Idx).setV2Idx(v4Idx).build());
                    segment_arraylist.add(segment_index);
                    segment_index++;
                } else {
                    segment_arraylist.add(segment_index);
                    neighbor_arraylist.add((x * num_of_rows) + y + 1);
                }
                if (x != 0) {
                    neighbor_arraylist.add(((x - 1) * num_of_rows) + y);
                }
                if (x == num_of_cols - 1) {
                    segments.add(Segment.newBuilder().setV1Idx(v4Idx).setV2Idx(v2Idx).build());
                    int idx = (segment_index - 1) + (num_of_rows * 2) - y;
                    if (idx == ((num_of_rows * num_of_cols) * 2 + num_of_cols + num_of_cols)) {
                        idx--;
                    }
                    segment_arraylist.add(idx);
                } else {
                    neighbor_arraylist.add(((x + 1) * num_of_rows) + y);
                    segment_arraylist.add((y * 2) + 2 + ((x + 1) * 2 * num_of_rows) + x);
                }

                polygons.add(Polygon.newBuilder().addAllSegmentIdxs(segment_arraylist).addAllNeighborIdxs(neighbor_arraylist).build());
            }
        }

        // Distribute colors randomly. Vertices are immutable, need to enrich them
        ArrayList<Vertex> verticesWithColors = new ArrayList<>();

        Random bag = new Random();
        for (Vertex v : vertices) {
            int red = bag.nextInt(255);
            int green = bag.nextInt(255);
            int blue = bag.nextInt(255);
            //changes transparency
            int alpha = 255;
            String colorCode = red + "," + green + "," + blue + "," + alpha;
            String v_thickness = "3";

            Property color = Property.newBuilder().setKey("rgb_color").setValue(colorCode).build();
            Property vertex_thickness = Property.newBuilder().setKey("thickness").setValue(v_thickness).build();
            Vertex colored = Vertex.newBuilder(v).addProperties(color).addProperties(vertex_thickness).build();
            verticesWithColors.add(colored);
        }

        // Attribute RGB colors to the segments, (will be changed to average color value)
        ArrayList<Segment> segmentsWithColors = new ArrayList<>();
        int[] v1Colors, v2Colors;
        for (Segment s : segments) {
            v1 = verticesWithColors.get(s.getV1Idx());
            v2 = verticesWithColors.get(s.getV2Idx());
            v1Colors = Helper.extractColor(v1);
            v2Colors = Helper.extractColor(v2);

            int red = (v1Colors[0] + v2Colors[0]) / 2;
            int green = (v1Colors[1] + v2Colors[1]) / 2;
            int blue = (v1Colors[2] + v2Colors[2]) / 2;
            //changes transparency
            int alpha = 150;
            String colorCode = red + "," + green + "," + blue + "," + alpha;
            String seg_thickness = "3";

            Property color = Property.newBuilder().setKey("rgb_color").setValue(colorCode).build();
            Property segment_thickness = Property.newBuilder().setKey("thickness").setValue(seg_thickness).build();
            Segment colored = Segment.newBuilder(s).addProperties(color).addProperties(segment_thickness).build();
            segmentsWithColors.add(colored);
        }

        // Add Centroids. Polygons are immutable, need to enrich them
        ArrayList<Polygon> polygonWithCentroids = new ArrayList<>();
        double x, y;
        for (Polygon polygon : polygons) {
            x = 0;
            y = 0;
            List<Integer> segmentIdxs = polygon.getSegmentIdxsList();
            for (i = 0; i < segmentIdxs.size(); i++) {
                Segment segment = segments.get(segmentIdxs.get(i));
                v1 = verticesWithColors.get(segment.getV1Idx());
                v2 = verticesWithColors.get(segment.getV2Idx());
                x += v1.getX();
                y += v1.getY();
                x += v2.getX();
                y += v2.getY();
            }
            x = x / (segmentIdxs.size() * 2);
            y = y / (segmentIdxs.size() * 2);
            int red = bag.nextInt(255);
            int green = bag.nextInt(255);
            int blue = bag.nextInt(255);
            int alpha = 100;
            String colorCode = red + "," + green + "," + blue + "," + alpha;
            String poly_cen_thickness = "5";

            Property colorProperty = Property.newBuilder().setKey("rgb_color").setValue(colorCode).build();
            Property centroid_thickness = Property.newBuilder().setKey("thickness").setValue(poly_cen_thickness).build();

            verticesWithColors.add(Vertex.newBuilder().setX(x).setY(y).addProperties(colorProperty).addProperties(centroid_thickness).build());
            polygonWithCentroids.add(Polygon.newBuilder(polygon).setCentroidIdx(verticesWithColors.size() - 1).build());
        }

        return Mesh.newBuilder().addAllVertices(verticesWithColors).addAllSegments(segmentsWithColors).addAllPolygons(polygonWithCentroids).build();
    }

    public Mesh generateIrregularMesh(int numOfPolygons, int relaxationLevel) {

        Random bag = new Random();
        GeometryFactory geometryFactory = new GeometryFactory();
        Property vertex_thickness = Property.newBuilder().setKey("thickness").setValue("3").build();

        //create arraylists to store the respective vertices and segments
        List<Vertex> vertices = new ArrayList<Vertex>();
        List<Segment> segmentList = new ArrayList<Segment>();
        List<Polygon> polygonList = new ArrayList<Polygon>();
        Collection<Coordinate> sites = new ArrayList<>();

        int red, green, blue, idx1, idx2, segmentIdx;
        double x, y, x1, y1, x2 = 0, y2 = 0;
        Point point;

        // Creating random sites
        for (int i = 0; i < numOfPolygons; i++) {
            x = bag.nextInt(WIDTH);
            y = bag.nextInt(HEIGHT);
            sites.add(new Coordinate(x, y));
        }

        // Initializing voronoi diagram builder
        VoronoiDiagramBuilder voronoiDiagramBuilder = new VoronoiDiagramBuilder();
        voronoiDiagramBuilder.setClipEnvelope(new Envelope(0, WIDTH, 0, HEIGHT));

        // Iterations for Lloyd relaxation
        for (int j = 0; j < relaxationLevel; j++) {

            // Clearing vertices and segments
            vertices.clear();
            segmentList.clear();

            // Generating Voronoi Diagram
            voronoiDiagramBuilder.setSites(sites);

            // Getting Polygons for Voronoi Diagram
            List<org.locationtech.jts.geom.Polygon> polygons = voronoiDiagramBuilder.getSubdivision().getVoronoiCellPolygons(geometryFactory);

            // Clearing sites
            sites.clear();

            // Creating segments and vertices from voronoi diagram
            Property blackColorProperty = Property.newBuilder().setKey("rgb_color").setValue("0,0,0,255").build();
            for (org.locationtech.jts.geom.Polygon polygon : polygons) {
                polygon = (org.locationtech.jts.geom.Polygon) polygon.convexHull();

                Coordinate[] coordinates = polygon.getCoordinates();
                Polygon.Builder polygonBuilder = Polygon.newBuilder();
                for (int i = 0; i < coordinates.length - 1; i++) {
                    x1 = cropValue(coordinates[i].x, true);
                    y1 = cropValue(coordinates[i].y, false);
                    x2 = cropValue(coordinates[i + 1].x, true);
                    y2 = cropValue(coordinates[i + 1].y, false);
                    idx1 = addVertices(vertices, Vertex.newBuilder().setX(x1).setY(y1).addProperties(blackColorProperty).addProperties(vertex_thickness).build());
                    idx2 = addVertices(vertices, Vertex.newBuilder().setX(x2).setY(y2).addProperties(blackColorProperty).addProperties(vertex_thickness).build());
                    segmentIdx = addSegments(segmentList, Segment.newBuilder().setV1Idx(idx1).setV2Idx(idx2).addProperties(blackColorProperty).addProperties(vertex_thickness).build());
                    polygonBuilder.addSegmentIdxs(segmentIdx);
                }

                point = polygon.getCentroid();
                x = cropValue(point.getX(), true);
                y = cropValue(point.getY(), false);

                // Creating new site for Voronoi Diagram
                sites.add(new Coordinate(x, y));

                // Creating new vertices
                red = bag.nextInt(255);
                green = bag.nextInt(255);
                blue = bag.nextInt(255);
                Property colorProperty = Property.newBuilder().setKey("rgb_color").setValue(red + "," + green + "," + blue + ",255").build();
                vertices.add(Vertex.newBuilder().setX(x).setY(y).addProperties(colorProperty).addProperties(vertex_thickness).build());

                for (int i = 0; i < polygons.size(); i++) {
                    if (polygon == polygons.get(i)) {
                        continue;
                    }
                    if (polygon.intersects(polygons.get(i))) {
                        polygonBuilder.addNeighborIdxs(i);
                    }
                }

                polygonList.add(polygonBuilder.setCentroidIdx(vertices.size() - 1).build());
            }

        }

        // Return mesh
        return Mesh.newBuilder().addAllVertices(vertices).addAllSegments(segmentList).addAllPolygons(polygonList).build();
    }

    public int addVertices(List<Vertex> vertices, Vertex vertex) {
        if (vertices.contains(vertex)) {
            return vertices.indexOf(vertex);
        }
        vertices.add(vertex);
        return vertices.size() - 1;
    }

    public int addSegments(List<Segment> segments, Segment segment) {
        if (segments.contains(segment)) {
            return segments.indexOf(segment);
        }
        segments.add(segment);
        return segments.size() - 1;
    }

    double cropValue(double val, boolean isWidth) {
        if (val < 0) {
            return 0;
        } else if (isWidth && val > WIDTH) {
            return WIDTH;
        } else if (!isWidth && val > HEIGHT) {
            return HEIGHT;
        }
        return Math.round(val * 100.0) / 100.0;
    }



}



