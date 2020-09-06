package io.github.hydos.traverbric;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.swifthq.swiftapi.config.ConfigManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TraverBric implements DedicatedServerModInitializer {

    public static final Logger LOGGER = LogManager.getLogger("TraverBric");
    public static boolean useBungee;

    @Override
    public void onInitializeServer() {
        useBungee = ConfigManager.readWithDefault("traverbric", Boolean.FALSE);
        if(useBungee){
            LOGGER.warn("Whilst this makes it possible to use BungeeCord, unless access to your server is properly restricted, it also opens up the ability for hackers to connect with any username they choose.");
            LOGGER.warn("Please see http://www.spigotmc.org/wiki/firewall-guide/ for further information.");
        }
    }
}
