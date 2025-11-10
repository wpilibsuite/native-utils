package org.wpilib.nativeutils.resources;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.gradle.workers.WorkAction;

public abstract class ResourceGenerationAction implements WorkAction<ResourceWorkParameters>  {
    @Override
    public void execute() {
        ResourceWorkParameters parameters = getParameters();
        File inputFile = parameters.getSourceFile().getAsFile().get();
        File outputFile = parameters.getOutputFile().getAsFile().get();
        String funcName = parameters.getFuncName().get();
        String namespace = parameters.getNamespace().get();

        try (BufferedWriter writer = Files.newBufferedWriter(outputFile.toPath(), StandardCharsets.UTF_8)) {
            writer.append("#include <stddef.h>\n");
            writer.append("#include <string_view>\n");
            writer.append("extern \"C\" {\n");
            writer.append("static const unsigned char contents[] = { ");
            int sizeCount = 0;

            try (InputStream input = Files.newInputStream(inputFile.toPath())) {
                byte[] toRead = new byte[0xFFFF];
                while (true) {
                    int read = input.read(toRead);
                    if (read <= 0) {
                        break;
                    }
                    for (int i = 0; i < read; i++) {
                        String fmt = String.format("0x%02x, ", toRead[i]);
                        writer.append(fmt);
                    }
                    sizeCount += read;
                }
            }
            writer.append("};\n");

            writer.append("const unsigned char* ");
            if (namespace != null && !namespace.isEmpty()) {
                writer.append(namespace);
                writer.append("_");
            }
            writer.append(funcName);
            writer.append("(size_t* len) {\n");

            writer.append("  *len = ");
            writer.append(Integer.toString(sizeCount));
            writer.append(";\n  return contents;\n}\n");
            writer.append("}  // extern \"C\"\n");

            if (namespace != null && !namespace.isEmpty()) {
                writer.append("namespace ");
                writer.append(namespace);
                writer.append("{\n");
            }
            writer.append("std::string_view ");
            writer.append(funcName);
            writer.append("() {\n");
            writer.append("  return {reinterpret_cast<const char*>(contents), ");
            writer.append(Integer.toString(sizeCount));
            writer.append("};\n}\n");

            if (namespace != null && !namespace.isEmpty()) {
                writer.append("}  // namespace ");
                writer.append(namespace);
                writer.newLine();
            }
            writer.flush();

        } catch (IOException iox) {
            throw new RuntimeException(iox);
        }
    }
}
