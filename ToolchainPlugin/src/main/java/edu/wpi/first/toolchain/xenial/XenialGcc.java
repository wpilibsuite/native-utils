package edu.wpi.first.toolchain.xenial;

import edu.wpi.first.toolchain.GccToolChain;
import edu.wpi.first.toolchain.ToolchainOptions;

public class XenialGcc extends GccToolChain {

    public XenialGcc(ToolchainOptions options) {
        super(options);
    }

    @Override
    protected String getTypeName() {
        return "XenialGcc";
    }
}
