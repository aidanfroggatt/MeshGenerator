package ca.mcmaster.cas.se2aa4.a2.generator;

import ca.mcmaster.cas.se2aa4.a2.io.Structs;

import java.util.List;

public class Helper {
    public static int[] extractColor(Structs.Vertex vertex) {
        List<Structs.Property> properties = vertex.getPropertiesList();
        String val = null;
        int red, green, blue;

        for (Structs.Property p : properties) {
            if (p.getKey().equals("rgb_color")) {
                val = p.getValue();
            }
        }

        String[] raw = val.split(",");
        red = Integer.parseInt(raw[0]);
        green = Integer.parseInt(raw[1]);
        blue = Integer.parseInt(raw[2]);
        return new int[]{red, green, blue};
    }
}
