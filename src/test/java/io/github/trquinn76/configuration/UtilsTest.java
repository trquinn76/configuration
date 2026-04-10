package io.github.trquinn76.configuration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UtilsTest {

    @Test
    void isNullTest() {
        assertTrue(Utils.isNullOrBlank(null));
    }
    
    @Test
    void isEmptyTest() {
        assertTrue(Utils.isNullOrBlank(""));
    }
    
    @Test
    void isBlankTest() {
        assertTrue(Utils.isNullOrBlank("  "));
    }
    
    @Test
    void isPopulatedTest() {
        assertFalse(Utils.isNullOrBlank("populated"));
        assertFalse(Utils.isNullOrBlank("  a  "));
    }

}
