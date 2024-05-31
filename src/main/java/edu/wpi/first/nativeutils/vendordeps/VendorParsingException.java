package edu.wpi.first.nativeutils.vendordeps;

import org.gradle.tooling.BuildException;

public class VendorParsingException extends Exception {
    public VendorParsingException(String file, VendorParsingError error) {
        switch (error) {
            case NoMavenUrl:
                System.err.println("The vendordep " + file + " is missing the required maven url.");
                break;

            case MissingCppDeps:
                System.err.println("The vendordep " + file + " is missing the required C++ dependencies key.");
                System.err.println("If you would not like to declare any Cpp deps use an empty list.");
                break;

            case MissingJniDeps:
                System.err.println("The vendordep " + file + " is missing the required Jni dependencies key.");
                System.err.println("If you would not like to declare any Jni deps use an empty list.");
                break;

            case MissingJavaDeps:
                System.err.println("The vendordep " + file + " is missing the required Java dependencies key.");
                System.err.println("If you would not like to declare any Java deps use an empty list.");
                break;

            default:
                throw new BuildException(
                        "Unhandled case in VendorParsingException. This is a bug and should be reported",
                        new Exception());
        }
    }

    // should only be called if we don't have access to a name yet
    public VendorParsingException(VendorParsingError error) {
        switch (error) {
            case MissingName:
                System.err.println("One of the vendordep files does not have a name");
                break;

            default:
                throw new BuildException(
                        "Unhandled case in VendorParsingException. This is a bug and should be reported",
                        new Exception());
        }
    }
}
