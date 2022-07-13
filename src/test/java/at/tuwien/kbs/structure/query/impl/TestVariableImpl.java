package at.tuwien.kbs.structure.query.impl;

import at.tuwien.kbs.structure.query.Variable;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class TestVariableImpl {

    @Test
    public void testEqualVariables() {
        Variable v1 = new VariableImpl("x");
        Variable v2 = new VariableImpl("x");

        assertEquals(v1, v2);
    }

    @Test
    public void testUnequalVariables() {
        Variable v1 = new VariableImpl("x");
        Variable v2 = new VariableImpl("y");

        assertNotEquals(v1, v2);
    }

    @Test
    public void testSetOfEqualVariables() {
        Variable v1 = new VariableImpl("x");
        Variable v2 = new VariableImpl("x");

        assertEquals(v1, v2);

        HashSet<Variable> set1 = new HashSet<>(Arrays.asList(v1, v2));
        HashSet<Variable> set2 = new HashSet<>();
        set2.add(v1);

        assertEquals(set1, set2);
    }
}
