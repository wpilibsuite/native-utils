package edu.wpi.first.deployutils.deploy.sessions;

public interface IPSessionController extends SessionController {
    String getHost();
    int getPort();
}
