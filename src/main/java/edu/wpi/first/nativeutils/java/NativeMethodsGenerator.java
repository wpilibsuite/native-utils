package edu.wpi.first.nativeutils.java;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class NativeMethodsGenerator {
  private static class Method {
    Method(String name, String descriptor) {
      this.name = name;
      descriptors = new ArrayList<>(1);
      descriptors.add(descriptor);
    }

    final String name;
    final List<String> descriptors;
  }

  private String m_className;
  private final Map<String, Method> m_methods = new TreeMap<>();

  private void parse(InputStream classStream) throws IOException {
    ClassReader reader = new ClassReader(classStream);
    reader.accept(new ClassVisitor(Opcodes.ASM9) {
      @Override
      public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        m_className = name;
      }

      @Override
      public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        // only look at native methods
        if ((access & Opcodes.ACC_NATIVE) == 0) {
          return null;
        }
        Method method = m_methods.get(name);
        if (method == null) {
          m_methods.put(name, new Method(name, descriptor));
        } else {
          method.descriptors.add(descriptor);
        }
        return null;
      }
    }, ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
  }

  private static String mangle(String in) {
    StringBuilder out = new StringBuilder();
    int length = in.length();

    for (int i = 0; i < length; i++) {
      char ch = in.charAt(i);
      if (isalnum(ch)) {
        out.append(ch);
        continue;
      }
      switch (ch) {
        case '/':
        case '.':
            out.append("_");
            break;
        case '_':
            out.append("_1");
            break;
        case ';':
            out.append("_2");
            break;
        case '[':
            out.append("_3");
            break;
        default:
            out.append(encodeChar(ch));
      }
    }
    return out.toString();
  }

  private static String encodeChar(char ch) {
    String s = Integer.toHexString(ch);
    int nzeros = 5 - s.length();
    char[] result = new char[6];
    result[0] = '_';
    for (int i = 1; i <= nzeros; i++) {
      result[i] = '0';
    }
    for (int i = nzeros + 1, j = 0; i < 6; i++, j++) {
      result[i] = s.charAt(j);
    }
    return new String(result);
  }

  private static boolean isalnum(char ch) {
    return ch <= 0x7f
        && ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9'));
  }

  private String makeNativeMethod(String mangledClassName, String methodName, String descriptor, boolean overloaded) {
    StringBuilder out = new StringBuilder();
    out.append("  { const_cast<char*>(\"");
    out.append(methodName);
    out.append("\"), const_cast<char*>(\"");
    out.append(descriptor);
    out.append("\"), reinterpret_cast<void*>(Java_");
    out.append(mangledClassName);
    out.append("_");
    out.append(mangle(methodName));
    if (overloaded) {
      out.append("__");
      // only the parameters portion of the descriptor is appended
      int begin = descriptor.indexOf('(') + 1;
      int end = descriptor.indexOf(')', begin);
      out.append(mangle(descriptor.substring(begin, end)));
    }
    out.append(") },");
    return out.toString();
  }

  public List<String> generate(Path filename) {
    try (InputStream classStream = Files.newInputStream(filename)) {
      parse(classStream);
    } catch(IOException e) {
      System.err.println("Could not read " + filename + ": " + e);
      return null;
    }

    String mangledClassName = mangle(m_className);
    List<String> out = new ArrayList<>(m_methods.size());

    out.add("static const JNINativeMethod nativeMethods_" + mangledClassName + "[] = {");

    for (Method method : m_methods.values()) {
      if (method.descriptors.size() == 1) {
        out.add(makeNativeMethod(mangledClassName, method.name, method.descriptors.get(0), false));
      } else {
        for (String descriptor : method.descriptors) {
          out.add(makeNativeMethod(mangledClassName, method.name, descriptor, true));
        }
      }
    }

    out.add("};");

    return out;
  }
}
