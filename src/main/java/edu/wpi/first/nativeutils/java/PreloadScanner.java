package edu.wpi.first.nativeutils.java;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class PreloadScanner implements AutoCloseable {
  private static class JarClass {
    JarClass(JarFile file, JarEntry entry) {
      this.file = file;
      this.entry = entry;
    }

    final JarFile file;
    final JarEntry entry;
  }
  final List<JarFile> m_jarFiles = new ArrayList<>();
  final Map<String, JarClass> m_jarClasses = new HashMap<>();
  final Set<String> m_seen = new HashSet<>(500);
  final Set<String> m_ignored = new HashSet<>(500);
  final Set<String> m_ignoredPrefixes = new HashSet<>(20);

  public PreloadScanner() {
    // by default, ignore built-in (typically fast to load) and rarely used classes
    // this list is a trade-off between the startup time and potential later delays
    m_ignoredPrefixes.add("java/lang/");
    m_ignoredPrefixes.add("java/io/");
    m_ignoredPrefixes.add("java/math/");
    m_ignoredPrefixes.add("java/net/");
    m_ignoredPrefixes.add("java/nio/channels/");
    m_ignoredPrefixes.add("java/nio/charset/");
    m_ignoredPrefixes.add("java/nio/file/");
    m_ignoredPrefixes.add("java/security/");
    m_ignoredPrefixes.add("java/text/");
    m_ignoredPrefixes.add("java/time/");
    m_ignoredPrefixes.add("java/util/jar/");
    m_ignoredPrefixes.add("java/util/random/");
    m_ignoredPrefixes.add("java/util/regex/");
    m_ignoredPrefixes.add("java/util/spi/");
    m_ignoredPrefixes.add("java/util/stream/");
    m_ignoredPrefixes.add("java/util/zip/");
    m_ignoredPrefixes.add("javax/");
    m_ignoredPrefixes.add("jdk/internal/");
    m_ignoredPrefixes.add("com/sun/crypto/");
    m_ignoredPrefixes.add("sun/net/");
    m_ignoredPrefixes.add("sun/nio/");
    m_ignoredPrefixes.add("sun/security/");
    m_ignoredPrefixes.add("sun/util/");
  }

  private void addInternalName(List<String> toVisit, String nativeName) {
    if (nativeName == null) {
      return;
    }
    // Look inside array types
    if (nativeName.startsWith("[") || nativeName.endsWith(";")) {
      Type t = Type.getType(nativeName);
      if (t.getSort() == Type.ARRAY) {
        t = t.getElementType();
      }
      if (t.getSort() == Type.OBJECT) {
        nativeName = t.getInternalName();
      } else {
        return;
      }
    }
    // ignore B, I, etc.
    if (nativeName.length() == 1) {
      return;
    }
    if (m_seen.contains(nativeName) || m_ignored.contains(nativeName)) {
      return;
    }
    for (String prefix : m_ignoredPrefixes) {
      if (nativeName.startsWith(prefix)) {
        m_ignored.add(nativeName);  // add to ignored list to speed up duplicate hits
        return;
      }
    }
    if (m_seen.add(nativeName)) {
      toVisit.add(nativeName);
    }
  }

  private void scanInner(String className) {
    ClassReader reader;
    try (InputStream classStream = openClass(className)) {
      reader = new ClassReader(classStream);
      //System.out.println("Scanning " + className);
    } catch(IOException e) {
      System.err.println("Could not scan class " + className + ": " + e);
      return;
    }
    final List<String> toVisit = new ArrayList<String>(10);
    reader.accept(new ClassVisitor(Opcodes.ASM9) {
      @Override
      public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        //System.out.println("Visit: " + superName + Arrays.toString(interfaces));
        addInternalName(toVisit, superName);
        for (String i : interfaces) {
          addInternalName(toVisit, i);
        }
      }

      @Override
      public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        addInternalName(toVisit, descriptor);
        return null;
      }

      @Override
      public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        //System.out.println("Method: " + name + " " + descriptor + " " + signature);
        return new MethodVisitor(Opcodes.ASM9) {
          @Override
          public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
            //System.out.println("FieldInsn: " + owner);
            addInternalName(toVisit, owner);
          }

          @Override
          public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            //System.out.println("MethodInsn: " + owner + " " + name + " " + descriptor);
            addInternalName(toVisit, owner);
          }

          @Override
          public void visitTypeInsn(int opcode, String type) {
            //System.out.println("TypeInsn: " + type);
            addInternalName(toVisit, type);
          }
        };
      }
    }, ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
    for (String i : toVisit) {
      //System.out.println(i);
      scanInner(i);
    }
  }

  private InputStream openClass(String className) throws IOException {
    String filename = className + ".class";
    JarClass jarClass = m_jarClasses.get(filename);
    if (jarClass != null) {
      return jarClass.file.getInputStream(jarClass.entry);
    }
    return ClassLoader.getSystemResourceAsStream(filename);
  }

  @Override
  public void close() {
    m_jarClasses.clear();
    for (JarFile jarFile : m_jarFiles) {
      try {
        jarFile.close();
      } catch (IOException e) {
        // ignored
      }
    }
    m_jarFiles.clear();
  }

  public void addIgnoredPrefix(String prefix) {
    m_ignoredPrefixes.add(prefix);
  }

  public void removeIgnoredPrefix(String prefix) {
    m_ignoredPrefixes.remove(prefix);
  }

  public void ignoreClass(String className) {
    m_ignored.add(className);
  }

  public void addJar(File jar) {
    try {
      JarFile jarFile = new JarFile(jar);
      m_jarFiles.add(jarFile);
      Enumeration<JarEntry> e = jarFile.entries();
      while (e.hasMoreElements()) {
        JarEntry je = e.nextElement();
        if (je.isDirectory() || !je.getName().endsWith(".class")) {
          continue;
        }
        m_jarClasses.put(je.getName(), new JarClass(jarFile, je));
      }
    } catch (IOException e) {
      System.err.println("Could not open JAR file " + jar + ": " + e);
    }
  }

  public void scan(String className) {
    m_ignored.remove(className);
    scanInner(className.replace('.', '/'));
  }

  public List<String> getResults() {
    List<String> keys = new ArrayList<>(m_seen);
    Collections.sort(keys);
    List<String> results = new ArrayList<>(m_seen.size());
    for (String i : keys) {
      results.add(i.replace("/", "."));
    }
    return results;
  }
}
