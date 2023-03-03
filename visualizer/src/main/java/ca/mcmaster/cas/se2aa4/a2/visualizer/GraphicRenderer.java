package ca.mcmaster.cas.se2aa4.a2.visualizer;

// imports the respective libraries and classes for GraphicRenderer

import ca.mcmaster.cas.se2aa4.a2.io.Structs.Polygon;
import ca.mcmaster.cas.se2aa4.a2.io.Structs.*;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.List;
import java.util.Objects;


// GraphicRenderer class which is a major class in rendering all structures generated and stored within the mesh structure to the svg file
// Can switch between default and debug mode using -X flag
public class GraphicRenderer {

    private static boolean debug_mode;
    // static boolean factor representing status of debug_mode


    //Major method that renders all structures of the mesh file and visualizes them on the svg file
    public void render(Mesh aMesh, Graphics2D canvas, String render_config) {

        // check for the -X flag in CLI
        if (Objects.equals(render_config, "-X")) {
            debug_mode = true;
        }
        canvas.setColor(Color.BLACK);
        Stroke stroke = new BasicStroke(0.5f);
        canvas.setStroke(stroke);

        int vertex_thickness, segment_thickness;


        //renders vertices
        List<Vertex> vertices = aMesh.getVerticesList();
        List<Segment> segments = aMesh.getSegmentsList();
        List<Polygon> polygons = aMesh.getPolygonsList();
        Segment segment;
        Vertex v1, v2, centroid, neighborCentroid;
        double centre_x, centre_y, centre_vertex1_x, centre_vertex1_y, centre_vertex2_x, centre_vertex2_y;
        Color oldColor;
        Ellipse2D point;
        Polygon neighborPolygon;
        Line2D L2D;

        // Rendering polygons
        for (Polygon polygon : polygons) {

            for (int segmentIdx : polygon.getSegmentIdxsList()) {
                segment = segments.get(segmentIdx);
                v1 = vertices.get(segment.getV1Idx());
                v2 = vertices.get(segment.getV2Idx());
                centroid = vertices.get(polygon.getCentroidIdx());

                // Rendering first vertex
                vertex_thickness = extractThickness(v1.getPropertiesList());
                centre_x = v1.getX() - (vertex_thickness / 2.0d);
                centre_y = v1.getY() - (vertex_thickness / 2.0d);
                oldColor = canvas.getColor();
                canvas.setColor(extractColor(v1.getPropertiesList(), false));
                point = new Ellipse2D.Double(centre_x, centre_y, vertex_thickness, vertex_thickness);
                canvas.fill(point);
                canvas.setColor(oldColor);

                // Rendering second vertex
                vertex_thickness = extractThickness(v2.getPropertiesList());
                centre_x = v2.getX() - (vertex_thickness / 2.0d);
                centre_y = v2.getY() - (vertex_thickness / 2.0d);
                oldColor = canvas.getColor();
                canvas.setColor(extractColor(v2.getPropertiesList(), false));
                point = new Ellipse2D.Double(centre_x, centre_y, vertex_thickness, vertex_thickness);
                canvas.fill(point);
                canvas.setColor(oldColor);

                // Rendering Segment
                segment_thickness = extractThickness(segment.getPropertiesList());
                centre_vertex1_x = v1.getX();
                centre_vertex1_y = v1.getY();
                centre_vertex2_x = v2.getX();
                centre_vertex2_y = v2.getY();
                canvas.setColor(extractColor(segment.getPropertiesList(), false));
                canvas.setStroke(new BasicStroke(segment_thickness));
                L2D = new Line2D.Double(centre_vertex1_x, centre_vertex1_y, centre_vertex2_x, centre_vertex2_y);
                canvas.draw(L2D);

                // Rendering Neighbourhood relationship if debug mode
                if (debug_mode) {
                    for(int neighborIdx: polygon.getNeighborIdxsList()) {
                        neighborPolygon = polygons.get(neighborIdx);
                        neighborCentroid = vertices.get(neighborPolygon.getCentroidIdx());
                        centre_vertex1_x = centroid.getX();
                        centre_vertex1_y = centroid.getY();
                        centre_vertex2_x = neighborCentroid.getX();
                        centre_vertex2_y = neighborCentroid.getY();
                        canvas.setColor(Color.lightGray);
                        canvas.setStroke(new BasicStroke(3));
                        L2D = new Line2D.Double(centre_vertex1_x, centre_vertex1_y, centre_vertex2_x, centre_vertex2_y);
                        canvas.draw(L2D);
                    }
                }

                // Rendering Centroid
                vertex_thickness = extractThickness(centroid.getPropertiesList());
                centre_x = centroid.getX() - (vertex_thickness / 2.0d);
                centre_y = centroid.getY() - (vertex_thickness / 2.0d);
                oldColor = canvas.getColor();
                canvas.setColor(extractColor(centroid.getPropertiesList(), true));
                point = new Ellipse2D.Double(centre_x, centre_y, vertex_thickness, vertex_thickness);
                canvas.fill(point);
                canvas.setColor(oldColor);
            }
        }


    }


    // extract the thickness property of the specific structure from its property list
    private int extractThickness(List<Property> properties) {
        int thickness_val = 0;

        for (Property p : properties) {
            if (p.getKey().equals("thickness")) {
                thickness_val = Integer.parseInt(p.getValue());
                break;
            }
        }

        return thickness_val;

    }


    // extract the color of the specific structure from its property list based on value entered by user as command line arguments during visualization
    // If debug mode is activated: return red or black color based on if the element is centroid
    // If debug mode is not activated: element's default RGB value is accessed
    private Color extractColor(List<Property> properties, boolean isCentroid) {
        String val = null;
        int red, green, blue, alpha = 0;

        if (debug_mode) {
            return isCentroid ? Color.RED : Color.BLACK;
        }

        for (Property p : properties) {
            if (p.getKey().equals("rgb_color")) {
                val = p.getValue();
            }
        }

        if (val == null) {
            return Color.BLACK;
        }

        String[] raw = val.split(",");
        red = Integer.parseInt(raw[0]);
        green = Integer.parseInt(raw[1]);
        blue = Integer.parseInt(raw[2]);
        alpha = Integer.parseInt(raw[3]);
        return new Color(red, green, blue, alpha);
    }

}