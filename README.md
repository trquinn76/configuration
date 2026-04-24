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

This library is intended to allow straight forward definition of configuration keys, which contain a collection of key values appropriate for
various different configuration sources.

## Usage

Create a static block, which contains the Configuration Key definitions:

    static {
        ConfigKey.newKeyBuilder("alpha").cmdLineArgument("-alpha").envVar("ALPHA").configFileProp("com.project.alpha").buildAndAddKey();
        ConfigKey.newKeyBuilder("beta").cmdLineArgument("-beta").envVar("BETA").configFileProp("com.project.beta").buildAndAddKey();
        ConfigKey.newKeyBuilder("gamma").cmdLineArgument("-gamma").cmdLineArgumentShhort("-g").envVar("GAMMA").configFileProp("com.project.gamma").buildAndAddKey();
        ConfigKey.newKeyBuilder("delta").cmdLineArgument("-d").envVar("DELTA").configFileProp("com.project.delta").buildAndAddKey();
    }

> A `static` block is used, as these are run when the class is initialised, which is usually before the `main()` function is invoked.
> See [The Java® Language Specification Third Edition 8.7. Static Initializers](https://docs.oracle.com/javase/specs/jls/se6/html/classes.html#8.7).
> This has remained consistent through all versions of Java.
>
> In particular using a static block like this will ensure that the Configuration Keys exist when an instance of `Configuration` is used
> in a non-static context.
> 
> Property files may also be set in the same static block. See example below.

Then create an instance of `Configuration`. Ideally, `Configuration` should be a singleton, as it caches values as it retrieves them.
Use the instance of `Configuration` to retrieve configuration values at run time:

    Configuration config = new Configuration();
    
    String alphaConfig = config.get("alpha");
    String betaConfig = config.get("beta");
    int gammaConfig = config.getInt("gamma");
    boolean deltaConfig = config.getBoolean("delta");
    
### Storing Command Line Arguments

When the developer has control of the `static void main(String[] args)` entry point function, they should used the `storeCommandLineArgs()`
function to store the Command Line Arguments. This allows the Command Line Arguments to be parsed for configuration values later.

Java does not provide a reliable means of getting the Command Line Arguments outside of the `static void main()` function.
`Configuration` will attempt to retrieve them via a call to `RuntimeMXBean.getInputArguments()` if the arguments
have not been set via this function. However, that is not reliable, and will vary between JVM implementations.

Where possible this function should be called in the `static void main(String[] args)` function, like: `Configuration.storeCommandLineArgs(args)`

When using `Configuration` in libraries, this is not possible, and avoiding Command Line Arguments is wisest. Command Line Properties are
still available in this case.

### Adding Configuration Property Files

There are no property files defined by default as sources of configuration. To search property files for configuration value, the file needs to
be added to `Configuration`. This may be done with any of the following static functions of `Configuration`:

* `appendPropertyFile()` adds the property file name to the end of the list of property file names.
* `prependPropertyFile` adds the property file name to the start of the list of property file names.
* `insertPropertyFile()` adds the property file name at the given index of the list of property file names.
* `setPropertyFiles()` sets the list of property file names. Will replace any existing list.

    static {
        Configuration.appendPropertyFile("config.property");
        Configuration.appendPropertyFile("application.property");
    }

These functions remove duplicate values, so a given property file will only be searched for configuration keys once.

The list of property files is searched for configuration keys in order, with the first value found returned as the configuration value. The
`propertyFiles()` function will return the current list of property files in the order they will be searched.

Property files on the classpath (eg: embedded in JAR files) will be found. Property files outside the classpath will also be found provided
the file name includes a suitable relative or absolute path.

Defined property files which do not exist are ignored, and do not raise an error. This allows libraries to define preferred property files
(eg: application.property). Then if the application they are added to do not define that file, configuration can still work via other configuration
files, or other sources of configuration.

#### Property Keys

It is strongly recommended that property file keys use Reverse Domain Name Notation.

## Future enhancement

A possible future enhancement would be to support YAML and JSON configuration files, rather than just simple Property files. This was
considered, but not implemented at this time.

Better support for retrieving Command Line Arguments. This is currently limited by existing JVM's.


