package ovh.rehost.realWeatherSponge;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Optional;

class RealweatherCommand implements CommandExecutor {

    private final RealWeather plugin;

    RealweatherCommand(RealWeather plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource commandSource, CommandContext commandContext) throws CommandException {
        if (commandSource.hasPermission("rh.realweather.reload")) {
            Optional<String> argument = commandContext.<String>getOne("reload");
            if (argument.isPresent() && argument.get().equalsIgnoreCase("reload")) {
                this.plugin.getLogger().info(plugin.getMessages().get("reloading-config"));
                this.plugin.reloadPlugin();
                commandSource.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(plugin.getMessages().get("reloaded-config")));
            }
        }
        return CommandResult.success();
    }
}