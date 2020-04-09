package defineoutside.network;

import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class ServerRunner extends Thread {
    String originalTemplate;
    UUID serverUUID = UUID.randomUUID();
    Process process;
    String template;
    String gametype = "GameLobby";
    int serverID;
    BufferedWriter output;

    File unzipFiles;

    public ServerRunner(String template, int serverID) {
        originalTemplate = template; // For calling this method again later

        if (template.contains("-")) { // - indicates a subtype or a patch
            gametype = template.substring(template.indexOf("-") + 1);
            this.template = template.substring(0, template.indexOf("-"));
        } else {
            this.template = template;
        }

        this.serverID = serverID;
    }

    @Override
    public void run() {
        ServerSocket findPort;
        try {
            ZipFile zipFile = new ZipFile(Paths.get("").toAbsolutePath() + File.separator + "templates" + File.separator + template);
            String unzipFolder = Paths.get("").toAbsolutePath() + File.separator + "servers" + File.separator + serverUUID;

            zipFile.extractAll(unzipFolder);

            unzipFiles = new File(unzipFolder + File.separator + template.substring(0, template.indexOf(".zip")));

            // I've left this file in the template twice in testing and it breaks everything.  I'll just delete it so I don't have to worry about this.
            File serverUUIDFile = new File(unzipFiles + File.separator + "plugins" + File.separator + "DefineAPI" + File.separator + "serveruuid.yml");
            FileUtils.deleteQuietly(serverUUIDFile);
            Files.writeString(Paths.get(serverUUIDFile.getAbsolutePath()), "UUID: " + serverUUID);

            findPort = new ServerSocket(0);
            int openPort = findPort.getLocalPort();
            findPort.close();

            Path serverProperties = Paths.get(unzipFiles + File.separator + "server.properties");
            List<String> fileContent = new ArrayList<>(Files.readAllLines(serverProperties, StandardCharsets.UTF_8));
            for (int i = 0; i < fileContent.size(); i++) {
                if (fileContent.get(i).contains("server-port=")) {
                    fileContent.set(i, "server-port=" + openPort);
                    break;
                }
            }

            Files.write(serverProperties, fileContent, StandardCharsets.UTF_8);

            // Tells DefineAPI what to "patch" the server with on it's load.  For example, the gamelobby world or the lobby world.
            Path serverType = Paths.get(unzipFiles + File.separator + "plugins" + File.separator + "DefineAPI" + File.separator + "main.yml");
            Files.writeString(serverType, "Mainworld: " + gametype);

            runServer(unzipFiles);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void runServer(File directory) {
        try {
            // Add the server to the list of servers using the template, for future updating
            HostApplication.addServer(this);

            System.out.println(serverID + "> Downloading the latest 1.15.2 paper jar!");

            File serverJar = new File(directory + File.separator + "server.jar");
            FileUtils.copyURLToFile(new URL("https://papermc.io/api/v1/paper/1.15.2/latest/download"), serverJar);

            System.out.println(serverID + "> Jar downloaded, launching the server");

            // Java flags are from https://steinborn.me/posts/tuning-minecraft-openj9/
            ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", "-Xms1024M", "-Xmx1024M", "-Xmns512M", "-Xmns816M", "-Xgc:concurrentScavenge", "-Xgc:dnssExpectedTimeRatioMaximum=3",
                    "-Xgc:scvNoAdaptiveTenure", "-Xdisableexplicitgc", serverJar.getAbsolutePath());
            processBuilder.directory(directory);
            process = processBuilder.start();

            // Get the process input
            output = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

            // output the process
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println(serverID + "> " + line);
            }

            // The server is done at this point
            process.destroyForcibly(); // So kill the children
            HostApplication.servers.remove(this); // Remove this reference for garbage collection to clean THIS up
            FileUtils.cleanDirectory(directory); // Delete any evidence that this server existed
            HostApplication.replaceServer(originalTemplate); // And just make another template
        } catch (Exception e) {
            if (process != null) {
                e.printStackTrace();
                process.destroyForcibly();
            }
        }
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public void sendCommand(String command) {
        if (output != null) {
            try {
                output.write(command);
                System.out.println("Wrote " + command);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
