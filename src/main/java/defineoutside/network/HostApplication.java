package defineoutside.network;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class HostApplication {
    static String internalHostName = UUID.randomUUID().toString();
    static int serversCreated = 0;

    static Vector<ServerRunner> servers = new Vector<>();

    public static boolean shuttingDown = false;

    public static void main(String[] args) throws IOException, InterruptedException {
        Runtime.getRuntime().addShutdownHook(new ShutdownEverything());
        Thread input = new Input();
        input.start();

        File file = new File(Paths.get("").toAbsolutePath() + File.separator + "hostname.yml");
        Scanner hostnameReader = null;

        // Read the host names
        try {
            hostnameReader = new Scanner(file);
            internalHostName = hostnameReader.nextLine();
        } catch (FileNotFoundException e) {
            Files.writeString(Paths.get(file.getPath()), internalHostName);
            System.out.println("Wrote host name " + internalHostName);
        }

        System.out.println("Host name: " + internalHostName);

        ZipUpdater zipUpdater = new ZipUpdater();
        zipUpdater.setHostName(internalHostName);
        zipUpdater.startNetworkMonitoring("127.0.0.1");
    }

    public static void startServers() {
        try {
            long freeMemory = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getFreePhysicalMemorySize() / 1000000;
            System.out.println("Free memory" + freeMemory);

            int availableServers = (int) Math.floor(freeMemory / 1500d);
            System.out.println("Safe amount of servers to create " + availableServers);

            Path hostType = Paths.get(Paths.get("").toAbsolutePath() + File.separator + "hostservers.yml");
            File hostFile = new File(String.valueOf(hostType));
            List<String> defaultServers = new ArrayList<>();

            for (int i = 0; i < availableServers; i++) {
                defaultServers.add("gamelobby.zip");
            }

            if (!hostFile.exists()) {
                Files.write(hostType, defaultServers);
            }

            // Read all the lines of host.yml, and create a server for each line
            List<String> fileContent = new ArrayList<>(Files.readAllLines(hostType, StandardCharsets.UTF_8));
            for (int i = 0; i < fileContent.size(); i++) {
                Thread serverRunner = new ServerRunner(fileContent.get(i), serversCreated);
                serverRunner.start();

                serversCreated++;
            }

            try {
                FileUtils.cleanDirectory(new File(Paths.get("").toAbsolutePath() + File.separator + "servers"));
            } catch (IOException e) {
                e.printStackTrace();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void replaceServer(String template) {
        if (!shuttingDown) {
            Thread serverRunner = new ServerRunner(template, serversCreated);
            serverRunner.start();

            serversCreated++;
        }
    }

    public static String getHostName() {
        return internalHostName;
    }

    public static void addServer(ServerRunner thread) {
        servers.add(thread);
    }

    public static void updateServers(String template) {
        for (ServerRunner thread : servers) {
            if (thread.template.equalsIgnoreCase(template)) {
                try {
                    thread.output.write("shutdownforupdate" + "\n");
                    thread.output.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void sendServerCommand(int serverID, String command) {
        for (ServerRunner thread : servers) {
            if (thread.serverID == serverID) {
                try {
                    thread.output.write(command + "\n");
                    thread.output.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

