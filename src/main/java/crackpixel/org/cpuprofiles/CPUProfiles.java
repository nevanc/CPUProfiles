package crackpixel.org.cpuprofiles;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import crackpixel.org.cpuprofiles.commands.Karma;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collection;

public final class CPUProfiles extends Plugin implements Listener {
    @Override
    public void onEnable() {
        // Plugin startup logic
        try {
            makeConfig();
        } catch (IOException e) {
            System.out.println(e.toString());
        }
        getProxy().registerChannel("cpu:profiles");
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new Karma(this));
        ProxyServer.getInstance().getPluginManager().registerListener(this, this);
        // Database stuff nowx
        getLogger().info("Ready");
    }

    public void makeConfig() throws IOException {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        File file = new File(getDataFolder(), "config.yml");


        if (!file.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PostLoginEvent postLoginEvent) throws IOException {
        // comment so i can push to gh
        Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
        if (!configuration.contains("players." + postLoginEvent.getPlayer().getName())) {
            configuration.set("players." + postLoginEvent.getPlayer().getName() + ".karma", 0);
            configuration.set("players." + postLoginEvent.getPlayer().getName() + ".gold", 0);
            configuration.set("players." + postLoginEvent.getPlayer().getName() + ".ap", 0);
            getLogger().info("New player " + postLoginEvent.getPlayer().getName());
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, new File(getDataFolder(), "config.yml"));
        } else {
            getLogger().info("Player " + postLoginEvent.getPlayer().getName() + " already exists");
        }
    }

    public void sendCustomData(ProxiedPlayer player, String mode, String username, int data) {
        Collection<ProxiedPlayer> networkPlayers = ProxyServer.getInstance().getPlayers();
        // perform a check to see if globally are no players
        if (networkPlayers == null || networkPlayers.isEmpty()) {
            return;
        }
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(mode); // the channel could be whatever you want
        out.writeUTF(username); // this data could be whatever you want
        out.writeInt(data); // this data could be whatever you want

        // we send the data to the server
        // using ServerInfo the packet is being queued if there are no players in the server
        // using only the server to send data the packet will be lost if no players are in it
        player.getServer().getInfo().sendData("cpu:profiles", out.toByteArray());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getProxy().unregisterChannel("cpu:profiles");
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getTag().equalsIgnoreCase("cpu:profiles")) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
        String mode = in.readUTF();
        // the receiver is a server when the proxy talks to a server
        if (event.getReceiver() instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) event.getReceiver();
            Server receiver = player.getServer();
            try {
                String user = in.readUTF();
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
                if (mode.equalsIgnoreCase("karma:get")) out.writeUTF("karma"); // Type
                if (mode.equalsIgnoreCase("ap:get")) out.writeUTF("ap");
                if (mode.equalsIgnoreCase("gold:get")) out.writeUTF("gold");
                out.writeUTF(user); // User, then data
                if (mode.equalsIgnoreCase("karma:get"))
                    out.writeInt((int) configuration.get("players." + user + ".karma"));
                if (mode.equalsIgnoreCase("ap:get")) out.writeInt((int) configuration.get("players." + user + ".ap"));
                if (mode.equalsIgnoreCase("gold:get"))
                    out.writeInt((int) configuration.get("players." + user + ".gold"));
                receiver.sendData("cpuprofiles:p2s", out.toByteArray());
            } catch (IOException err) {
                getLogger().info(err.toString());
            }
        }
    }
}
