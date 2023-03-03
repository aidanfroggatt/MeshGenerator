package ca.mcmaster.cas.se2aa4.a2.generator;

import org.apache.commons.cli.*;

import java.util.Random;

public class OptionsHandler {

    public static final String MESH_TYPE_OPTION_TEXT = "mesh";
    public static final String NUM_OF_POLYGONS_OPTION_TEXT = "numPoly";
    public static final String RELAXATION_LEVEL_OPTION_TEXT = "relaxationLevel";
    public static final String OUTPUT_FILE_OPTION_TEXT = "out";
    public static final String WAVEFRONT_FILE_OPTION_TEXT = "wavefront";
    public static final String HELP_OPTION_TEXT_SHORT = "h";
    public static final String HELP_OPTION_TEXT_LONG = "help";

    private static Options options;

    public static CommandLine getCmd(String[] args) {
        try {

            // Creating options for command line
            options = new Options();
            options.addOption(new Option(MESH_TYPE_OPTION_TEXT, true, "Type of Mesh"));
            options.addOption(new Option(NUM_OF_POLYGONS_OPTION_TEXT, true, "Number of polygons"));
            options.addOption(new Option(RELAXATION_LEVEL_OPTION_TEXT, true, "Level of Relaxation for Lloyd relaxation"));
            options.addOption(new Option(OUTPUT_FILE_OPTION_TEXT, true, "Output file name and directory"));
            options.addOption(new Option(WAVEFRONT_FILE_OPTION_TEXT, true, "Wavefront Output file name and directory"));
            options.addOption(new Option(HELP_OPTION_TEXT_SHORT, HELP_OPTION_TEXT_LONG, false, "Shows usage of options"));

            //Create a parser
            CommandLineParser parser = new DefaultParser();

            //parse the options passed as command line arguments
            CommandLine cmd = parser.parse(options, args);

            return cmd;
        } catch (ParseException e) {
            return null;
        }
    }

    public static MeshType getMeshType(CommandLine cmd) {
        if (cmd == null || !cmd.hasOption(MESH_TYPE_OPTION_TEXT)) {
            return MeshType.GRID;
        }
        return cmd.getOptionValue(MESH_TYPE_OPTION_TEXT).equalsIgnoreCase("irregular") ? MeshType.IRREGULAR : MeshType.GRID;
    }

    public static int getNumOfPolygons(CommandLine cmd) {
        if (cmd == null || !cmd.hasOption(NUM_OF_POLYGONS_OPTION_TEXT)) {
            return new Random().nextInt(50) + 50;
        }
        return Integer.parseInt(cmd.getOptionValue(NUM_OF_POLYGONS_OPTION_TEXT));
    }

    public static int getRelaxationLevel(CommandLine cmd) {
        if (cmd == null || !cmd.hasOption(RELAXATION_LEVEL_OPTION_TEXT)) {
            return 10;
        }
        return Integer.parseInt(cmd.getOptionValue(RELAXATION_LEVEL_OPTION_TEXT));
    }

    public static String getOutputFileName(CommandLine cmd) {
        if (cmd == null || !cmd.hasOption(OUTPUT_FILE_OPTION_TEXT)) {
            return "sample.mesh";
        }
        return cmd.getOptionValue(OUTPUT_FILE_OPTION_TEXT);
    }

    public static String getWavefrontFileName(CommandLine cmd) {
        if (cmd == null || !cmd.hasOption(WAVEFRONT_FILE_OPTION_TEXT)) {
            return null;
        }
        return cmd.getOptionValue(WAVEFRONT_FILE_OPTION_TEXT);
    }

    public static boolean hasHelp(CommandLine cmd) {
        if (cmd == null || !cmd.hasOption(HELP_OPTION_TEXT_SHORT) || !cmd.hasOption(HELP_OPTION_TEXT_LONG)) {
            return false;
        }
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("java -jar generator.jar", options);
        return true;
    }
}
