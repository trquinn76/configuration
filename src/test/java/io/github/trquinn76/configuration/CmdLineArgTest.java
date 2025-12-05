package io.github.trquinn76.configuration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CmdLineArgTest {

    @Test
    void createCmdLineArgTest() {
        CmdLineArg arg = new CmdLineArg("argument", "a");
        
        assertEquals("argument", arg.argument());
        assertEquals("a", arg.shortArgument());
        
        arg = new CmdLineArg("argument");
        
        assertEquals("argument", arg.argument());
        assertNull(arg.shortArgument());
    }

}
