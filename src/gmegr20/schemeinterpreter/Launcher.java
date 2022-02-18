package gmegr20.schemeinterpreter;

import java.io.File;
import java.io.FileNotFoundException;

import static gmegr20.schemeinterpreter.Constants.*;

public class Launcher {
    private static final Interpreter interpreter = new Interpreter();

    public static void main(String[] args) {
        printWelcome();
        if(args.length == 0) {
            try {
                interpreter.evaluateFile(new File(DEFAULT_PATH));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            try {
                interpreter.evaluateFile((new File(args[0])));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private static void printWelcome() {
         System.out.println(WELCOME_TEXT);
    }
}
