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

public class Gold extends Command {
    private final CPUProfiles plugin;
    public Gold(CPUProfiles cpuProfiles) {
        super("Gold");
        this.plugin = cpuProfiles;
    }
    public void execute(CommandSender sender, String[] args) {
        if ((sender instanceof ProxiedPlayer)) {
            ProxiedPlayer p = (ProxiedPlayer) sender;
            if (!args[0].isEmpty()) {
               if(!p.hasPermission("cpuprofiles.gold.change")) {
                    p.sendMessage(new TextComponent(ChatColor.RED + "You don't have permission to do this!"));
                   return;
               }
               try {
                    String usernameToChange = args[0];
                    int karmaToChange = Integer.valueOf(args[1]);
                    Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(plugin.getDataFolder(), "config.yml"));
                    configuration.set("players." + usernameToChange + ".pre-edit-gold", configuration.get("players." + usernameToChange + ".gold"));
                    configuration.set("players." + usernameToChange + ".gold", karmaToChange);
                    ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, new File(plugin.getDataFolder(), "config.yml"));
                    p.sendMessage(new TextComponent(ChatColor.AQUA + "Set " + args[0] + "'s gold to " + args[1]+"!"));
               } catch (IOException err) {
                    plugin.getLogger().info(err.toString());
               }
               return;
            }
            try {
                Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(plugin.getDataFolder(), "config.yml"));
                p.sendMessage(new TextComponent(ChatColor.GREEN + "You have " + configuration.get("players." + p.getName() + ".gold") + " gold!"));
                p.sendMessage(new TextComponent(ChatColor.GOLD + "Buy more Crackpixel gold at https://store.crackpixel.org !"));
            } catch (IOException e) {
                plugin.getLogger().info(e.toString());
            }
        }
    }
}
