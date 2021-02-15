package edu.wpi.first.nativeutils.rules;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.gradle.api.Named;

public class FrcNativeBinaryExtension implements Named {
    private final List<String> dependencies = new ArrayList<>();
    private final List<String> optionalDependencies = new ArrayList<>();
    private final String name;

    public List<String> getDependencies() {
        return dependencies;
    }

    public List<String> getOptionalDependencies() {
        return optionalDependencies;
    }

    @Override
    public String getName() {
        return name;
    }

    @Inject
    public FrcNativeBinaryExtension(String name) {
        this.name = name;
    }


}
