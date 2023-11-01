package enigma;

import java.util.Collection;
import java.util.Iterator;

import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author Sandya Wijaya
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _numRotors = numRotors;
        _pawls = pawls;
        _allRotors = new Rotor[allRotors.size()];
        _plugboard = new Permutation("()", _alphabet);
        Iterator allRotorIter = allRotors.iterator();
        for (int i = 0; i < allRotors.size(); i++) {
            _allRotors[i] = (Rotor) allRotorIter.next();
        }
        _rotors = new Rotor[numRotors];
        _allRotorNames = new String[_allRotors.length];
        for (int i = 0; i < _allRotors.length; i++) {
            _allRotorNames[i] = _allRotors[i].name();
        }
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _pawls;
    }

    /** Return Rotor #K, where Rotor #0 is the reflector, and Rotor
     *  #(numRotors()-1) is the fast Rotor.  Modifying this Rotor has
     *  undefined results. */
    Rotor getRotor(int k) {
        return _rotors[k];
    }

    Alphabet alphabet() {
        return _alphabet;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        for (int i = 0; i < rotors.length; i++) {
            for (int j = 0; j < _allRotors.length; j++) {
                if (rotors[i].equals(_allRotorNames[j])) {
                    _rotors[i] = _allRotors[j];
                }
            }
        }
        if (_rotors.length != rotors.length) {
            throw new EnigmaException("Misnamed rotors");
        }
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        if (setting.length() != numRotors() - 1) {
            throw new EnigmaException("Setting is of invalid length");
        }
        for (int i = 1; i < numRotors(); i++) {
            if (_alphabet.contains(setting.charAt(i - 1))) {
                if (_rotors[i] != null) {
                    _rotors[i].set(setting.charAt(i - 1));
                }
            } else {
                throw new EnigmaException("Not in alphabet");
            }
        }
    }

    /** Return the current plugboard's permutation. */
    Permutation plugboard() {
        return _plugboard;
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        advanceRotors();
        if (Main.verbose()) {
            System.err.printf("[");
            for (int r = 1; r < numRotors(); r += 1) {
                System.err.printf("%c",
                        alphabet().toChar(getRotor(r).setting()));
            }
            System.err.printf("] %c -> ", alphabet().toChar(c));
        }
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c -> ", alphabet().toChar(c));
        }
        c = applyRotors(c);
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c%n", alphabet().toChar(c));
        }
        return c;
    }

    /** Advance all rotors to their next position. */
    private void advanceRotors() {
        boolean[] advanceOrNot = new boolean[numRotors()];
        for (int i = 0; i < advanceOrNot.length; i++) {
            advanceOrNot[i] = false;
        }
        advanceOrNot[numRotors() - 1] = true;
        for (int j = numRotors() - 1; j > 0; j--) {
            if (_rotors[j] != null && _rotors[j].atNotch()) {
                advanceOrNot[j - 1] = true;
                if (_rotors[j - 1].rotates()) {
                    advanceOrNot[j] = true;
                }
            }
        }
        for (int k = 0; k < advanceOrNot.length; k++) {
            if (_rotors[k] != null && advanceOrNot[k]) {
                _rotors[k].advance();
            }
        }
    }

    /** Return the result of applying the rotors to the character C (as an
     *  index in the range 0..alphabet size - 1). */
    private int applyRotors(int c) {
        int result = c;
        for (int i = _numRotors - 1; i >= 0; i--) {
            if (_rotors[i] != null) {
                result = _rotors[i].convertForward(result);
            }
        }
        for (int j = 1; j < _numRotors; j++) {
            if (_rotors[j] != null) {
                result = _rotors[j].convertBackward(result);
            }
        }
        return result;
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        String result = "";
        for (int i = 0; i < msg.length(); i++) {
            char currChar = msg.charAt(i);
            if (_alphabet.contains(currChar)) {
                int intChar = _alphabet.toInt(currChar);
                char newChar = _alphabet.toChar(convert(intChar));
                result += newChar;
            }
        }
        return result;
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;

    /** Number of rotors. */
    private int _numRotors;

    /** Number of pawls. */
    private int _pawls;

    /** Rotors used. */
    private Rotor[] _rotors;

    /** Plugboard. */
    private Permutation _plugboard;

    /** All available rotors. */
    private Rotor[] _allRotors;

    /** String array of all rotor names. */
    private String[] _allRotorNames;

    /** Reflector. */
    private Reflector _reflector;
}
