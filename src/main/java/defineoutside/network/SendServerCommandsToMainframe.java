package defineoutside.network;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Vector;

public class SendServerCommandsToMainframe {
    Vector<ServerCommandWithName> queuedCommands = new Vector<>();
    Thread connectionToMain;

    public void startNetworkMonitoring(String host) {
        try {
            Runnable myRunnable = () -> {
                while (true) {
                    try {
                        Socket socketInfo = CreateConnectionToMainframe.connectToMainframe(host, 27469, HostApplication.getHostName() + "#SendCommands");

                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socketInfo.getOutputStream());

                        while (true) {
                            try {
                                for (ServerCommandWithName serverCommandWithName : queuedCommands) {
                                    objectOutputStream.writeObject(serverCommandWithName);
                                }
                            } catch (Exception e) {
                                break;
                            }
                        }

                        Thread.sleep(10000);
                    } catch (InterruptedException ex) {
                        // :(
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("Updater unable to connect to the game manager with host " + host + " and port 27469.  Restarting connection in 10 seconds!");

                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException ex) {
                            // :(
                        }
                    }
                }
            };

            connectionToMain = new Thread(myRunnable);
            connectionToMain.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
