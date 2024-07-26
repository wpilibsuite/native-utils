package edu.wpi.first.deployutils.deploy.target.discovery.action;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import edu.wpi.first.deployutils.deploy.context.DefaultDeployContext;
import edu.wpi.first.deployutils.deploy.context.DeployContext;
import edu.wpi.first.deployutils.deploy.sessions.SshSessionController;
import edu.wpi.first.deployutils.deploy.target.RemoteTarget;
import edu.wpi.first.deployutils.deploy.target.discovery.DiscoveryState;
import edu.wpi.first.deployutils.deploy.target.location.SshDeployLocation;
import edu.wpi.first.deployutils.log.ETLogger;
import edu.wpi.first.deployutils.log.ETLoggerFactory;

public class SshDiscoveryAction extends AbstractDiscoveryAction {
    private DiscoveryState state = DiscoveryState.NOT_STARTED;

    private ETLogger log;

    public SshDiscoveryAction(SshDeployLocation dloc) {
        super(dloc);
    }

    @Override
    public DiscoveryState getState() {
        return state;
    }

    private SshDeployLocation sshLocation() {
        return (SshDeployLocation) getDeployLocation();
    }

    @Override
    public DeployContext discover() {
        SshDeployLocation location = sshLocation();
        RemoteTarget target = location.getTarget();
        String address = location.getAddress();
        log = ETLoggerFactory.INSTANCE.create("SshDiscoverAction[" + address + "]");

        log.info("Discovery started...");
        state = DiscoveryState.STARTED;

        // Split host into host:port, using 22 as the default port if none provided
        String[] splitHost = address.split(":");
        String hostname = splitHost[0];
        int port = splitHost.length > 1 ? Integer.parseInt(splitHost[1]) : 22;
        log.info("Parsed Host: HOST = " + hostname + ", PORT = " + port);

        String resolvedHost = resolveHostname(hostname, location.isIpv6());
        state = DiscoveryState.RESOLVED;

        SshSessionController session = new SshSessionController(resolvedHost, port, location.getUser(), location.getPassword(), target.getTimeout(), location.getTarget().getMaxChannels(), getDeployLocation().getTarget().getStorageServiceProvider().get());
        session.open();
        log.info("Found " + resolvedHost + "! at " + address);
        state = DiscoveryState.CONNECTED;

        DeployContext ctx = new DefaultDeployContext(session, log, location, target.getDirectory());
        log.info("Context constructed");

        verify(ctx);
        return ctx;
    }

    // TODO: This should be injected to make testing easier.
    private String resolveHostname(String hostname, boolean allowIpv6) {
        String resolvedHost = hostname;
        boolean hasResolved = false;
        try {
            for (InetAddress addr : InetAddress.getAllByName(hostname)) {
                if (!addr.isMulticastAddress()) {
                    if (!allowIpv6 && addr instanceof Inet6Address) {
                        log.info("Resolved address " + addr.getHostAddress() + " ignored! (IPv6)");
                    } else {
                        log.info("Resolved " + addr.getHostAddress());
                        resolvedHost = addr.getHostAddress();
                        hasResolved = true;
                        break;
                    }
                }
            }
        } catch (UnknownHostException e) {
            throw new RuntimeException("Unknown Host", e);
        }

        if (!hasResolved)
            log.info("No host resolution! Using original...");

        return resolvedHost;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
