import ca.mcmaster.cas.se2aa4.a2.generator.DotGen;
import ca.mcmaster.cas.se2aa4.a2.generator.MeshType;
import ca.mcmaster.cas.se2aa4.a2.generator.OptionsHandler;
import ca.mcmaster.cas.se2aa4.a2.generator.WavefrontFileGenerator;
import ca.mcmaster.cas.se2aa4.a2.io.MeshFactory;
import ca.mcmaster.cas.se2aa4.a2.io.Structs.Mesh;
import org.apache.commons.cli.*;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        DotGen generator = new DotGen();
        CommandLine commandLine = OptionsHandler.getCmd(args);

        if(OptionsHandler.hasHelp(commandLine)) {
            return;
        }

        MeshType meshType = OptionsHandler.getMeshType(commandLine);
        int numOfPolygons = OptionsHandler.getNumOfPolygons(commandLine);
        int relaxationLevel = OptionsHandler.getRelaxationLevel(commandLine);
        String outputFileName = OptionsHandler.getOutputFileName(commandLine);
        Mesh myMesh = generator.generate(meshType, numOfPolygons, relaxationLevel);
        MeshFactory factory = new MeshFactory();
        factory.write(myMesh, outputFileName);
        WavefrontFileGenerator.createWavefrontFile(OptionsHandler.getWavefrontFileName(commandLine), myMesh);
    }

}
