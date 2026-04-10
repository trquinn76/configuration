# Configuration

This library provides the means to define and retrieve configuration keys and values from the full set of typical configuration sources:

* Command Line Arguments
* Command Line Properties
* Environment Variables
* Property Files
* Default Values

## Purpose

Not wanting to replicate configuration code across multiple projects, and not finding any existing Configuration library which supports all the
normal configuration sources, have written this library to serve as a suitable Configuration library.

So this library is intended to allow straight forward definition of configuration keys, which contain a collection of key values appropriate for
various different configuration sources.

## Usage

Create a static block, which contains the Configuration Key definitions:

    static {
        ConfigKey.newKeyBuilder("alpha").cmdLineArgument("-alpha").envVar("ALPHA").configFileProp("com.project.alpha").buildAndAddKey();
        ConfigKey.newKeyBuilder("beta").cmdLineArgument("-beta").envVar("BETA").configFileProp("com.project.beta").buildAndAddKey();
        ConfigKey.newKeyBuilder("gamma").cmdLineArgument("-gamma").cmdLineArgumentShhort("-g").envVar("GAMMA").configFileProp("com.project.gamma").buildAndAddKey();
        ConfigKey.newKeyBuilder("delta").cmdLineArgument("-d").envVar("DELTA").configFileProp("com.project.delta").buildAndAddKey();
        
        Configuration.appendPropertyFile("propertyfile.property");
        // eg: a typical property file might be:
        Configuration.appendPropertyFile("application.property");
    }

> A `static` block is used, as these are run when the class is initialised, which is usually before the `main()` function is invoked.
> See [The Java® Language Specification Third Edition 8.7. Static Initializers](https://docs.oracle.com/javase/specs/jls/se6/html/classes.html#8.7).
> This has remained consistent through all versions of Java.
> 
> In particular using a static block like this will ensure that the Configuration Keys exist when an instance of `Configuration` is used
> in a non-static context.

Then create an instance of `Configuration`. Ideally, `Configuration` should be a singleton, as it caches values as it retrieves them.
Use the instance of `Configuration` to retrieve configuration values at run time:

    Configuration config = new Configuration();
    
    String alphaConfig = config.get("alpha");
    String betaConfig = config.get("beta");
    int gammaConfig = config.getInt("gamma");
    boolean deltaConfig = config.getBoolean("delta");

### Adding Configuration Property Files

There are no property files defined by default as sources of configuration. To search property files for configuration value, the file needs to
be added to `Configuration`. This may be done with any of the following static functions of `Configuration`:

* `appendPropertyFile()` adds the property file name to the end of the list of property file names.
* `prependPropertyFile` adds the property file name to the start of the list of property file names.
* `insertPropertyFile()` adds the property file name at the given index of the list of property file names.
* `setPropertyFiles()` sets the list of property file names. Will replace any existing list.

These functions remove duplicate values, so a given property file will only be searched for configuration keys once.

The list of property files is searched for configuration keys in order, with the first value found returned as the configuration value.

Property files need to be in the classpath to be found.



