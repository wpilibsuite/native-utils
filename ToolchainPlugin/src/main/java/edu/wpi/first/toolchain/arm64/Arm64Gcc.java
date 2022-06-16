package edu.wpi.first.toolchain.arm64;

import edu.wpi.first.toolchain.GccToolChain;
import edu.wpi.first.toolchain.ToolchainOptions;

public class Arm64Gcc extends GccToolChain {

    public Arm64Gcc(ToolchainOptions options) {
        super(options);
    }

    @Override
    protected String getTypeName() {
        return "Arm64Gcc";
    }
}
