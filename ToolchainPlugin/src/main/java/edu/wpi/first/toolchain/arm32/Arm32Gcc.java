package edu.wpi.first.toolchain.arm32;

import edu.wpi.first.toolchain.GccToolChain;
import edu.wpi.first.toolchain.ToolchainOptions;

public class Arm32Gcc extends GccToolChain {

    public Arm32Gcc(ToolchainOptions options) {
        super(options);
    }

    @Override
    protected String getTypeName() {
        return "Arm32Gcc";
    }
}
