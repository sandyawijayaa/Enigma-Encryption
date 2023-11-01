package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;

import static enigma.TestUtils.*;

/** The suite of all JUnit tests for the Permutation class.
 *  @author Sandya Wijaya
 */
public class PermutationTest {

    public Permutation getNewPermutation(String cycles, Alphabet alphabet) {
        return new Permutation(cycles, alphabet);
    }

    public Alphabet getNewAlphabet(String chars) {
        return new Alphabet(chars);
    }

    public Alphabet getNewAlphabet() {
        return new Alphabet();
    }


    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /* ***** TESTING UTILITIES ***** */

    private Permutation perm;
    private String alpha = UPPER_STRING;

    /** Check that perm has an alphabet whose size is that of
     *  FROMALPHA and TOALPHA and that maps each character of
     *  FROMALPHA to the corresponding character of FROMALPHA, and
     *  vice-versa. TESTID is used in error messages. */
    private void checkPerm(String testId,
                           String fromAlpha, String toAlpha) {
        int N = fromAlpha.length();
        assertEquals(testId + " (wrong length)", N, perm.size());
        for (int i = 0; i < N; i += 1) {
            char c = fromAlpha.charAt(i), e = toAlpha.charAt(i);
            assertEquals(msg(testId, "wrong translation of '%c'", c),
                         e, perm.permute(c));
            assertEquals(msg(testId, "wrong inverse of '%c'", e),
                         c, perm.invert(e));
            int ci = alpha.indexOf(c), ei = alpha.indexOf(e);
            assertEquals(msg(testId, "wrong translation of %d", ci),
                         ei, perm.permute(ci));
            assertEquals(msg(testId, "wrong inverse of %d", ei),
                         ci, perm.invert(ei));
        }
    }

    /* ***** TESTS ***** */

    @Test
    public void checkIdTransform() {
        perm = new Permutation("", UPPER);
        checkPerm("identity", UPPER_STRING, UPPER_STRING);
    }

    @Test
    public void permuteTest() {
        Alphabet alphaNew = new Alphabet();
        String str = "(ALTPHQXRU) (BKNW) (CMOY) (DFG) (IV) (JZ) (S)";
        Permutation p = new Permutation(str, alphaNew);
        int output1 = p.permute(0);
        assertEquals(11, output1);
        int output2 = p.permute(22);
        assertEquals(1, output2);
    }

    @Test
    public void selfPermuteTest() {
        Alphabet alphaNew = new Alphabet();
        String str = "(BA)";
        Permutation p = new Permutation(str, alphaNew);
        int output1 = p.invert(0);
        assertEquals(1, output1);
        int output2 = p.invert(2);
        assertEquals(2, output2);
    }

    @Test
    public void invertTest() {
        Alphabet alphaNew = new Alphabet();
        String str = "(ALTPHQXRU) (BKNW) (CMOY) (DFG) (IV) (JZ) (S)";
        Permutation p = new Permutation(str, alphaNew);
        int output1 = p.invert(0);
        assertEquals(20, output1);
        int output2 = p.invert(18);
        assertEquals(18, output2);
    }

    @Test
    public void testAlphabet() {
        Alphabet alphaNew = getNewAlphabet("ABCD");
        Permutation p = getNewPermutation("", alphaNew);
        assertEquals(alphaNew, p.alphabet());
        Alphabet beta = getNewAlphabet();
        Permutation p2 = getNewPermutation("", beta);
        assertEquals(beta, p2.alphabet());

        Permutation p3 = getNewPermutation("(B)", getNewAlphabet("B"));
        assertEquals('B', p3.invert('B'));
        assertEquals(0, p3.invert(0));
        assertEquals('B', p3.permute('B'));
        assertEquals(0, p3.permute(0));
    }

    @Test
    public void testOverIndex() {
        Alphabet a = getNewAlphabet();
        Permutation p = getNewPermutation("(ABCDEFGHIJKLMNOPQRSTUVXYZ)", a);
        assertEquals(2, p.permute(27));
        assertEquals(4, p.permute(29));
        assertEquals(2, p.invert(29));
        assertEquals(4, p.invert(31));
    }

    @Test
    public void testDerangement() {
        Permutation p1 = getNewPermutation("(BACD)", getNewAlphabet("ABCDE"));
        assertFalse(p1.derangement());
        Permutation p2 = getNewPermutation("(ABC)", getNewAlphabet("ABCD"));
        assertFalse(p2.derangement());
        Permutation p3 = getNewPermutation("(AB)", getNewAlphabet("AB"));
        assertTrue(p3.derangement());
    }
}
