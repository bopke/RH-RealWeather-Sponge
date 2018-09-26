package ovh.rehost.realWeatherSponge;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Plugin(id = "realweather", name = "RH-RealWeather", authors = "Michał \"Bopke\" Kubik", version = "1.1", dependencies = @Dependency(id = "spongeapi", version = "7.1.0"))
public class RealWeather {

    private String country;
    private String city;
    private String apikey;
    private int interval;

    private List<String> affectedWorlds;
    private Map<String, String> messages = new HashMap<>();

    private Task task;

    @Inject
    private Logger logger;

    @Inject
    @DefaultConfig(sharedRoot = true)
    private Path config;

    @Listener
    public void onServerStart(GameStartedServerEvent event) {

        // rh-realweather command
        CommandSpec RealWeatherCommandSpec = CommandSpec.builder()
                .description(Text.of("Administrative command for RH-RealWeather"))
                .permission("rh.realweather.reload")
                .arguments(GenericArguments.string(Text.of("reload")))
                .executor(new RealweatherCommand(this))
                .build();
        Sponge.getCommandManager().register(this, RealWeatherCommandSpec, "rh-realweather");

        createConfigIfNotExists();
        loadNewConfigValues();
        task = makeTask();
    }

    private Task makeTask() {
        return Sponge.getScheduler().createTaskBuilder()
                .execute(new ScheduledWeatherStateUpdateHandler(this))
                .async()
                .intervalTicks(interval)
                .name("RealWeatherChecker")
                .submit(this);
    }

    private void reloadTask() {
        task.cancel();
        task = makeTask();
    }

    void reloadPlugin() {
        loadNewConfigValues();
        reloadTask();
    }

    private void createConfigIfNotExists() {
        if (!config.toFile().exists()) {
            try {
                InputStream in = this.getClass().getResourceAsStream("/config.conf");
                OutputStream out = new FileOutputStream(config.toFile());
                byte[] buff = new byte[1024];
                int len;
                while ((len = in.read(buff)) > 0) {
                    out.write(buff, 0, len);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadNewConfigValues() {
        try {
            ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder().setPath(config).build();
            ConfigurationNode rootNode = loader.load().getNode();
            affectedWorlds = rootNode.getNode("worlds").getList(TypeToken.of(String.class));
            city = rootNode.getNode("city").getString();
            country = rootNode.getNode("country").getString();
            apikey = rootNode.getNode("API_Key").getString();
            interval = rootNode.getNode("interval").getInt();
            ConfigurationNode mess = rootNode.getNode("messages");
            messages.put("localisation-information", mess.getNode("localisation-information").getString());
            messages.put("invalid-response-code", mess.getNode("invalid-response-code").getString());
            messages.put("reloading-config", mess.getNode("reloading-config").getString());
            messages.put("reloaded-config", mess.getNode("reloaded-config").getString());
            messages.put("API-offline", mess.getNode("API-offline").getString());
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
        }
    }

    @Inject
    private void setLogger(Logger logger) {
        this.logger = logger;
    }

    String getCity() {
        return this.city;
    }

    String getCountry() {
        return this.country;
    }

    String getApikey() {
        return this.apikey;
    }

    int getInterval() {
        return this.interval;
    }

    Map<String, String> getMessages() {
        return this.messages;
    }

    List<String> getAffectedWorlds() {
        return this.affectedWorlds;
    }

    Logger getLogger() {
        return this.logger;
    }
}