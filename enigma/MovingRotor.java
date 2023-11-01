package enigma;

import static enigma.EnigmaException.*;

/** Class that represents a rotating rotor in the enigma machine.
 *  @author Sandya Wijaya
 */
class MovingRotor extends Rotor {

    /** A rotor named NAME whose permutation in its default setting is
     *  PERM, and whose notches are at the positions indicated in NOTCHES.
     *  The Rotor is initally in its 0 setting (first character of its
     *  alphabet).
     */
    MovingRotor(String name, Permutation perm, String notches) {
        super(name, perm);
        _notches = notches;
    }

    /** Notches of rotors. */
    private String _notches;

    @Override
    void advance() {
        this.set(this.setting() + 1);
    }

    @Override
    String notches() {
        return _notches;
    }


    @Override
    boolean rotates() {
        return true;
    }
}
