package io.github.trquinn76.configuration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UtilsTest {

    @Test
    void isNullTest() {
        assertTrue(Utils.isNullOrEmpty(null));
    }
    
    @Test
    void isEmptyTest() {
        assertTrue(Utils.isNullOrEmpty(""));
    }
    
    @Test
    void isBlankTest() {
        assertTrue(Utils.isNullOrEmpty("  "));
    }
    
    @Test
    void isPopulatedTest() {
        assertFalse(Utils.isNullOrEmpty("populated"));
        assertFalse(Utils.isNullOrEmpty("  a  "));
    }

}
