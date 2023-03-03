
// Imports the respective libraries for the CLIManager class
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


// The CLIManager class is used to fetch, analyze and organize the command line arguments entered by user during execution.
// It is used in various classes to handle CLI arguments and assign respective mesh generation and visualization options
public class CLIManager {

    protected final Map<String,String> CLI_arguments = new HashMap<String,String>();
    // hashmap to store all values and associated keys of CLI args entered by user


    // class constructor to add all CLI arguments to the object's hashmap and clean any arguments which are null
    public CLIManager(String[] argument_keys, String[] argument_vals) {
        for(int i = 0; i < argument_vals.length; i++){
            this.CLI_arguments.put(argument_keys[i], argument_vals[i]);
        }
        cleanArgs();
    }


    // method to cleanUp the command line argument hashmap to avoid any conflicting keys or values not read or provided by user
    public void cleanArgs(){
        this.CLI_arguments.values().removeIf(Objects:: isNull);
        this.CLI_arguments.values().removeIf(String:: isEmpty);
    }



}
