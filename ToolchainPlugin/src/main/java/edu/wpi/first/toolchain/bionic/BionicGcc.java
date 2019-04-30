package edu.wpi.first.toolchain.bionic;

import edu.wpi.first.toolchain.GccToolChain;
import edu.wpi.first.toolchain.ToolchainOptions;

public class BionicGcc extends GccToolChain {

    public BionicGcc(ToolchainOptions options) {
        super(options);
    }

    @Override
    protected String getTypeName() {
        return "BionicGcc";
    }
}
