package edu.wpi.first.nativeutils.configs.internal;

import java.util.List;

public interface CombinedNativeLibraryConfig extends BaseNativeLibraryConfig {
    void setLibs(List<String> libs);
    List<String> getLibs();
}
