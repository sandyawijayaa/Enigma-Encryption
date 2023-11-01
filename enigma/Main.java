package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import ucb.util.CommandArgs;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Sandya Wijaya
 */
public final class Main {

    /**
     * Process a sequence of encryptions and decryptions, as
     * specified by ARGS, where 1 <= ARGS.length <= 3.
     * ARGS[0] is the name of a configuration file.
     * ARGS[1] is optional; when present, it names an input file
     * containing messages.  Otherwise, input comes from the standard
     * input.  ARGS[2] is optional; when present, it names an output
     * file for processed messages.  Otherwise, output goes to the
     * standard output. Exits normally if there are no errors in the input;
     * otherwise with code 1.
     */
    public static void main(String... args) {
        try {
            CommandArgs options =
                    new CommandArgs("--verbose --=(.*){1,3}", args);
            if (!options.ok()) {
                throw error("Usage: java enigma.Main [--verbose] "
                        + "[INPUT [OUTPUT]]");
            }

            _verbose = options.contains("--verbose");
            new Main(options.get("--")).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /**
     * Open the necessary files for non-option arguments ARGS (see comment
     * on main).
     */
    Main(List<String> args) {
        _config = getInput(args.get(0));

        if (args.size() > 1) {
            _input = getInput(args.get(1));
        } else {
            _input = new Scanner(System.in);
        }

        if (args.size() > 2) {
            _output = getOutput(args.get(2));
        } else {
            _output = System.out;
        }
    }

    /**
     * Return a Scanner reading from the file named NAME.
     */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /**
     * Return a PrintStream writing to the file named NAME.
     */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /**
     * Configure an Enigma machine from the contents of configuration
     * file _config and apply it to the messages in _input, sending the
     * results to _output.
     */
    private void process() {
        Machine enigma = readConfig();
        if (!_input.hasNext("\\*")) {
            throw new EnigmaException("Message with no config");
        }
        if (!_input.hasNext()) {
            throw new EnigmaException("File is empty");
        }
        String next = _input.nextLine();
        while (_input.hasNext()) {
            String setting = next;
            if (setting.contains("*")) {
                setUp(enigma, setting);
            }
            next = (_input.nextLine());
            if (!next.contains("*")) {
                String inputString = next.replaceAll(" ", "");
                String outputString = enigma.convert(inputString);
                printMessageLine(outputString);
            }
        }
    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            String alphabet = _config.next();
            if (alphabet.contains("*") || alphabet.contains("(")
                    || alphabet.contains(")")) {
                throw new EnigmaException("Config file wrong format");
            }
            _alphabet = new Alphabet(alphabet);
            if (!_config.hasNextInt()) {
                throw new EnigmaException("Config file wrong format");
            }
            int numRotors = _config.nextInt();
            if (!_config.hasNextInt()) {
                throw new EnigmaException("Config file wrong format");
            }
            int numPawls = _config.nextInt();
            while (_config.hasNextLine() && _config.hasNext(".+")) {
                Rotor newRotor = readRotor();
                _allRotors.add(newRotor);
            }
            return new Machine(_alphabet, numRotors, numPawls, _allRotors);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        try {
            rotorName = _config.next();
            notches = _config.next();
            perm = "";
            while (_config.hasNext("\\(.+\\)")) {
                perm = perm.concat(_config.next() + " ");
            }
            if (notches.charAt(0) == 'M') {
                return new MovingRotor(rotorName, new
                        Permutation(perm, _alphabet), notches.substring(1));
            } else if (notches.charAt(0) == 'N') {
                return new FixedRotor(rotorName, new
                        Permutation(perm, _alphabet));
            } else {
                return new Reflector(rotorName, new
                        Permutation(perm, _alphabet));
            }
        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {
        String[] settingsArr = settings.split(" ");
        String[] rotorsArr = new String[M.numRotors()];
        if (settingsArr.length - 1 < M.numRotors()) {
            throw new EnigmaException("Not enough settings");
        }
        for (int i = 1; i < M.numRotors() + 1; i++) {
            rotorsArr[i - 1] = settingsArr[i];
        }
        M.insertRotors(rotorsArr);
        if (!M.getRotor(0).reflecting()) {
            throw new EnigmaException("Rotor 0 should be a reflector");
        }
        M.setRotors(settingsArr[M.numRotors() + 1]);
        if (settingsArr.length > M.numRotors() + 1) {
            _hasPlugboard = true;
        }
        if (_hasPlugboard) {
            String plug = "";
            for (int i = M.numRotors() + 2; i < settingsArr.length; i++) {
                plug = plug.concat(settingsArr[i] + " ");
            }
            M.setPlugboard(new Permutation(plug, _alphabet));
        }
    }

    /** Return true iff verbose option specified. */
    static boolean verbose() {
        return _verbose;
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        for (int i = 0; i < msg.length(); i += 5) {
            int remainder = msg.length() - i;
            if (remainder <= 5) {
                _output.print(msg.substring(i, i + remainder));
            } else {
                _output.print(msg.substring(i, i + 5) + " ");
            }
        }
        _output.println();
    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;

    /** True if --verbose specified. */
    private static boolean _verbose;

    /** ArrayList of all rotors available. */
    private ArrayList<Rotor> _allRotors = new ArrayList<>();

    /** Perm, all cycles of a rotor. */
    private String perm;

    /** Name of a rotor. */
    private String rotorName;

    /** Notch of a rotor. */
    private String notches;

    /** True if has plugboard. */
    private boolean _hasPlugboard;
}
