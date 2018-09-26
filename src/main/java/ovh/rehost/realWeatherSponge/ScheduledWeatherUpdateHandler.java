package ovh.rehost.realWeatherSponge;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.Optional;

public class ScheduledWeatherUpdateHandler implements Runnable {

    private int weatherID;
    private RealWeather plugin;

    ScheduledWeatherUpdateHandler(RealWeather plugin, int weatherID) {
        this.weatherID = weatherID;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (weatherID == -1) {
            plugin.getLogger().warn(plugin.getMessages().get("invalid-response-code"));
            return;
        }
        if (weatherID == -2) {
            plugin.getLogger().warn(plugin.getMessages().get("API-offline"));
        }
        for (String i : plugin.getAffectedWorlds()) {
            Optional<WorldProperties> oworldProperties = Sponge.getServer().getWorldProperties(i);
            if (!oworldProperties.isPresent()) {
                continue;
            }
            WorldProperties world = oworldProperties.get();
            world.setRaining(weatherID < 700);
            world.setThundering(weatherID < 300);
            world.setRainTime(plugin.getInterval() * 2); // twice longer than typical interval between checks, just to be sure.
            world.setThunderTime(plugin.getInterval() * 2);
        }
    }
}

