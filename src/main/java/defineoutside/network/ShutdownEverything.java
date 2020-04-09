package defineoutside.network;

public class ShutdownEverything extends Thread {
    @Override
    public void run() {
        HostApplication.shuttingDown = true;
        for (ServerRunner serverRunner : HostApplication.servers) {
            try {
                serverRunner.process.destroyForcibly();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
