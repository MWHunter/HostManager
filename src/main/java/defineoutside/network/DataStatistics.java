package defineoutside.network;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class DataStatistics implements Serializable {
    double AppCPU;
    double SystemCPU;
    double appMemoryUsed;
    double appMemoryAvailable;
    double systemMemoryAvailable;
    double systemMemoryTotal;
    HashMap<UUID, List<UUID>> playersAndGames = new HashMap<>();
    HashMap<UUID, String> gamesAndGametypes = new HashMap<>();

    public DataStatistics(double AppCPU, double SystemCPU, double appMemoryUsed, double appMemoryAvailable, double systemMemoryAvailable, double systemMemoryTotal,
                          HashMap<UUID, List<UUID>> playersAndGames, HashMap<UUID, String> gamesAndGametypes) {
        this.AppCPU = AppCPU;
        this.SystemCPU = SystemCPU;
        this.appMemoryUsed = appMemoryUsed;
        this.appMemoryAvailable = appMemoryAvailable;
        this.systemMemoryAvailable = systemMemoryAvailable;
        this.systemMemoryTotal = systemMemoryTotal;
        this.playersAndGames = playersAndGames;
        this.gamesAndGametypes = gamesAndGametypes;
    }

    public double getAppCPU() {
        return AppCPU;
    }

    public double getSystemCPU() {
        return SystemCPU;
    }

    public double getAppMemoryUsed() {
        return appMemoryUsed;
    }

    public double getAppMemoryAvailable() {
        return appMemoryAvailable;
    }

    public double getSystemMemoryAvailable() {
        return systemMemoryAvailable;
    }

    public double getSystemMemoryTotal() {
        return systemMemoryTotal;
    }

    public HashMap<UUID, List<UUID>> getPlayersAndGames() {
        return playersAndGames;
    }

    public HashMap<UUID, String> getGamesAndGametypes() {
        return gamesAndGametypes;
    }
}