package defineoutside.network;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Input extends Thread {
    @Override
    public void run() {
        Scanner input = new Scanner(System.in);
        while (true) {
            String command = input.nextLine();

            System.out.println("Received command " + command);

            List<String> args = new ArrayList<>();

            Pattern regex = Pattern.compile("\"([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"|'([^'\\\\]*(?:\\\\.[^'\\\\]*)*)'|[^\\s]+");
            Matcher regexMatcher = regex.matcher(command);
            while (regexMatcher.find()) {
                args.add(regexMatcher.group().replaceAll("\"(.+)\"", "$1").replace("\\", ""));
            }

            try {
                if (args.get(0).equalsIgnoreCase("servers")) {
                    for (ServerRunner serverRunner : HostApplication.servers) {
                        System.out.println(serverRunner.serverID + " - " + serverRunner.serverUUID);
                    }
                }

                if (args.get(0).equalsIgnoreCase("stop")) {
                    Thread thread = new ShutdownEverything();
                    thread.start();
                }

                if (args.get(0).contains("command")) {
                    int serverInt = Integer.parseInt(args.get(1));
                    String argument = "";

                    for (int x = 2; x < args.size(); x++) {
                        argument += args.get(x);
                    }

                    System.out.println("Sending the command " + argument);
                    HostApplication.sendServerCommand(serverInt, argument);
                }

                if (args.get(0).equalsIgnoreCase("help")) {
                    System.out.println("Valid commands: openhosts, queue, servers, updatetemplates, help");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}