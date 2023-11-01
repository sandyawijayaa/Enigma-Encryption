package enigma;

import static enigma.EnigmaException.error;

/** An alphabet of encodable characters.  Provides a mapping from characters
 *  to and from indices into the alphabet.
 *  @author Sandya Wijaya
 */
class Alphabet {

    /**
     * A new alphabet containing CHARS. The K-th character has index
     * K (numbering from 0). No character may be duplicated.
     */
    Alphabet(String chars) {
        _chars = chars;
        for (int i = 0; i < _chars.length(); i++) {
            for (int j = i + 1; j < chars.length(); j++) {
                if (_chars.charAt(i) == _chars.charAt(j)) {
                    throw error("Character duplicate", chars.charAt(i));
                }
            }
        }
    }

    /** String of chars. */
    private String _chars;

    /**
     * A default alphabet of all upper-case characters.
     */
    Alphabet() {
        this("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    /**
     * Returns the size of the alphabet.
     */
    int size() {
        return _chars.length();
    }

    /**
     * Returns true if CH is in this alphabet.
     */
    boolean contains(char ch) {
        for (int i = 0; i < size(); i++) {
            if (_chars.charAt(i) == ch) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns character number INDEX in the alphabet, where
     * 0 <= INDEX < size().
     */
    char toChar(int index) {
        return _chars.charAt(index);
    }

    /**
     * Returns the index of character CH which must be in
     * the alphabet. This is the inverse of toChar().
     */
    int toInt(char ch) {
        for (int i = 0; i < size(); i++) {
            if (_chars.charAt(i) == ch) {
                return i;
            }
        }
        throw error("Not in alphabet", ch);
    }
}
