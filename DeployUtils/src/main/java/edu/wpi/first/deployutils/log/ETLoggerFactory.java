package edu.wpi.first.deployutils.log;

import org.gradle.api.Project;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.internal.logging.text.StyledTextOutput;
import org.gradle.internal.logging.text.StyledTextOutputFactory;

public class ETLoggerFactory {
    public static ETLoggerFactory INSTANCE  = new ETLoggerFactory();

    private StyledTextOutput output = null;

    public void addColorOutput(Project project) {
        StyledTextOutputFactory factory = ((ProjectInternal)project).getServices().get(StyledTextOutputFactory.class);
        output = factory.create(this.getClass());
    }

    public ETLogger create(String name) {
        return new ETLogger(name, output);
    }

    public ETLogger create(String name, int indent) {
        return new ETLogger(name, output, indent);
    }
}
