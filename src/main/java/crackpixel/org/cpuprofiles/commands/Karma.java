package crackpixel.org.cpuprofiles.commands;

import crackpixel.org.cpuprofiles.CPUProfiles;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Karma extends Command {
    private final CPUProfiles plugin;
    public Karma(CPUProfiles cpuProfiles) {
        super("Karma");
        this.plugin = cpuProfiles;
    }
    public void execute(CommandSender sender, String[] args) {
        if ((sender instanceof ProxiedPlayer)) {
            ProxiedPlayer p = (ProxiedPlayer) sender;
            if (args.length != 0) {
               if(!p.hasPermission("cpuprofiles.karma.change")) {
                    p.sendMessage(new TextComponent(ChatColor.RED + "You don't have permission to do this!"));
                   return;
               }
               try {
                    String usernameToChange = args[0];
                    int karmaToChange = Integer.valueOf(args[1]);
                    Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(plugin.getDataFolder(), "config.yml"));
                    configuration.set("players." + usernameToChange + ".pre-edit-karma", configuration.get("players." + usernameToChange + ".karma"));
                    configuration.set("players." + usernameToChange + ".karma", karmaToChange);
                    ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, new File(plugin.getDataFolder(), "config.yml"));
                    p.sendMessage(new TextComponent(ChatColor.AQUA + "Set " + args[0] + "'s karma to " + args[1]+"!"));
               } catch (IOException err) {
                    plugin.getLogger().info(err.toString());
               }
               return;
            }
            try {
                Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(plugin.getDataFolder(), "config.yml"));
                p.sendMessage(new TextComponent(ChatColor.GREEN + "You have " + configuration.get("players." + p.getName() + ".karma") + " karma!"));
            } catch (IOException e) {
                plugin.getLogger().info(e.toString());
            }
        }
    }
}
