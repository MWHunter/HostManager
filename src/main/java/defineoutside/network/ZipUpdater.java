package defineoutside.network;


import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.Socket;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ZipUpdater {
    Thread connectionToMain;
    String hostName;
    boolean firstRun = true;

    static HashMap<String, String> templateHashes = new HashMap<>();
    static ConcurrentHashMap<String, String> remoteTemplateHashes = new ConcurrentHashMap<>();
    File templates = new File(Paths.get("").toAbsolutePath() + File.separator + "templates");

    public void startNetworkMonitoring(String host) {
        try {
            Runnable myRunnable = () -> {
                while (true) {
                    try {
                        // Read the templates and the versions
                        templates.mkdir();

                        MessageDigest shaDigest = MessageDigest.getInstance("SHA-1");

                        templateHashes.clear();

                        // Try to read directories
                        for (Iterator<File> it = FileUtils.iterateFiles(templates, null, false); it.hasNext(); ) {
                            File nextFile = it.next();

                            // See if this is a valid template, and not a stray file
                            if (!nextFile.isDirectory() && nextFile.getName().contains(".zip")) {
                                templateHashes.put(nextFile.getName(), getFileChecksum(shaDigest, nextFile));
                            }
                        }

                        Socket socketInfo = CreateConnectionToMainframe.connectToMainframe(host, 27469, hostName + "#Host");

                        try {
                            ObjectInputStream objectInputStream = new ObjectInputStream(socketInfo.getInputStream());
                            remoteTemplateHashes = (ConcurrentHashMap<String, String>) objectInputStream.readObject();
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }

                        List<String> filesToUpdate = new ArrayList<>();
                        List<String> filesToDelete = new ArrayList<>();

                        // Check to see if we have an up to date version of each thing
                        for (Map.Entry<String, String> fileRemote : remoteTemplateHashes.entrySet()) {
                            Boolean foundFile = false;
                            for (Map.Entry<String, String> fileHome : templateHashes.entrySet()) {
                                if (fileHome.getKey().equals(fileRemote.getKey())) { // The files have the same name
                                    if (fileHome.getValue().equals(fileRemote.getValue())) { // The files have the same name and hash
                                        foundFile = true;
                                        break;
                                    } else { // The file has the same name, but not the same hash
                                        foundFile = true;
                                        filesToUpdate.add(fileRemote.getKey());
                                    }
                                }
                            }
                            if (!foundFile) {
                                // The file was not found on the local server
                                filesToUpdate.add(fileRemote.getKey());
                            }
                        }

                        // Check if we need to delete anything
                        for (String homeFile : templateHashes.keySet()) {
                            if (!remoteTemplateHashes.containsKey(homeFile)) {
                                filesToDelete.add(homeFile);
                            }
                        }

                        for (String deleteFile : filesToDelete) {
                            File deleteThisFile = new File(templates + File.separator + deleteFile);
                            deleteThisFile.delete();
                        }

                        for (String updateFileName : filesToUpdate) {
                            CreateConnectionToMainframe createConnectionToMainframe = new CreateConnectionToMainframe();
                            Socket networkInfo = createConnectionToMainframe.connectToMainframe(host, 27469, hostName + "#Fetch");

                            DataOutputStream dataOutputStream = new DataOutputStream(networkInfo.getOutputStream());
                            dataOutputStream.writeUTF(updateFileName);

                            try {
                                InputStream in = networkInfo.getInputStream();
                                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(templates + File.separator + updateFileName)));

                                int c = 0;
                                byte[] buff = new byte[8192];

                                while ((c = in.read(buff)) > 0) { // read something from input stream into buffer
                                    // if something was read
                                    bos.write(buff, 0, c);
                                }

                                in.close();
                                bos.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            HostApplication.updateServers(updateFileName);
                        }

                        filesToUpdate.clear();
                        filesToDelete.clear();

                        if (firstRun) {
                            HostApplication.startServers();
                            firstRun = false;
                        }
                        Thread.sleep(10000);

                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("Updater unable to connect to the game manager with host " + host + " and port 27469.  Restarting connection in 10 seconds!");

                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException ex) {
                            //
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

    public void setHostName(String name) {
        hostName = name;
    }

    // Borrowed from https://howtodoinjava.com/java/io/how-to-generate-sha-or-md5-file-checksum-hash-in-java/
    private static String getFileChecksum(MessageDigest digest, File file) throws IOException {
        //Get file input stream for reading the file content
        FileInputStream fis = new FileInputStream(file);

        //Create byte array to read data in chunks
        byte[] byteArray = new byte[1024];
        int bytesCount = 0;

        //Read file data and update in message digest
        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        }
        ;

        //close the stream; We don't need it now.
        fis.close();

        //Get the hash's bytes
        byte[] bytes = digest.digest();

        //This bytes[] has bytes in decimal format;
        //Convert it to hexadecimal format
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        //return complete hash
        return sb.toString();
    }
}
