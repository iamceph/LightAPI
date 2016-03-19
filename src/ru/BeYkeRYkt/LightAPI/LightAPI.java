package ru.beykerykt.lightapi;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import ru.beykerykt.lightapi.chunks.Chunks;
import ru.beykerykt.lightapi.light.Lights;
import ru.beykerykt.lightapi.nms.NMSHelper;
import ru.beykerykt.lightapi.updater.Response;
import ru.beykerykt.lightapi.updater.Updater;
import ru.beykerykt.lightapi.updater.Version;
import ru.beykerykt.lightapi.utils.Metrics;

public class LightAPI extends JavaPlugin implements Listener {

	private static LightAPI plugin;
	private int chunk_update_delay_ticks;
	private int chunk_max_iterations_per_tick;
	private int ligthing_update_delay_ticks;
	private int lighting_max_iterations_per_tick;

	@Override
	public void onEnable() {
		// Config
		try {
			FileConfiguration fc = getConfig();
			if (!new File(getDataFolder(), "config.yml").exists()) {
				fc.options().header("LightAPI v" + getDescription().getVersion() + " Configuration" + "\nby BeYkeRYkt");
				fc.set("chunk.update-delay-ticks", 5);
				fc.set("chunk.max-iterations-per-tick", 20);
				// fc.set("lighting.async-calculation-lighting", true);
				fc.set("lighting.update-delay-ticks", 2);
				fc.set("lighting.max-iterations-per-tick", 20);
				saveConfig();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Init components
		this.chunk_update_delay_ticks = getConfig().getInt("chunk.update-delay-ticks");
		this.chunk_max_iterations_per_tick = getConfig().getInt("chunk.max-iterations-per-tick");
		this.ligthing_update_delay_ticks = getConfig().getInt("lighting.update-delay-ticks");
		this.lighting_max_iterations_per_tick = getConfig().getInt("lighting.max-iterations-per-tick");
		LightAPI.plugin = this;
		NMSHelper.init();
		Chunks.init();
		Lights.init();
		getServer().getPluginManager().registerEvents(this, this);

		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
			// nothing...
		}

		// Starting updater
		Bukkit.getScheduler().runTaskLaterAsynchronously(this, new Runnable() {
			@Override
			public void run() {
				Version version = Version.parse(getDescription().getVersion());
				String repo = "BeYkeRYkt/LightAPI";

				Updater updater;
				try {
					updater = new Updater(version, repo);

					Response response = updater.getResult();
					if (response == Response.SUCCESS) {
						log(Bukkit.getConsoleSender(), ChatColor.GREEN + "New update is available: " + ChatColor.YELLOW + updater.getLatestVersion() + ChatColor.GREEN + "!");
						log(Bukkit.getConsoleSender(), ChatColor.GREEN + "Changes: ");
						getServer().getConsoleSender().sendMessage(updater.getChanges());// for normal view
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, 60);
	}

	@Override
	public void onDisable() {
		Lights.shutdown();
		Chunks.shutdown();
	}

	public static LightAPI getInstance() {
		return plugin;
	}

	public void log(CommandSender sender, String message) {
		sender.sendMessage(ChatColor.YELLOW + "<Light" + ChatColor.RED + "API" + ChatColor.YELLOW + ">: " + ChatColor.WHITE + message);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		final Player player = event.getPlayer();

		if (player.isOp() || player.hasPermission("lightapi.updater")) {
			Bukkit.getScheduler().runTaskLaterAsynchronously(this, new Runnable() {

				@Override
				public void run() {
					Version version = Version.parse(getDescription().getVersion());
					String repo = "BeYkeRYkt/LightAPI";

					Updater updater;
					try {
						updater = new Updater(version, repo);

						Response response = updater.getResult();
						if (response == Response.SUCCESS) {
							log(player, ChatColor.GREEN + "New update is available: " + ChatColor.YELLOW + updater.getLatestVersion() + ChatColor.GREEN + "!");
							log(player, ChatColor.GREEN + "Changes: ");
							player.sendMessage(updater.getChanges());// for normal view
							// player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}, 60);
		}
	}

	public int getChunkUpdateDelayTicks() {
		return chunk_update_delay_ticks;
	}

	public int getChunkMaxIterationsPerTick() {
		return chunk_max_iterations_per_tick;
	}

	public int getLigthingUpdateDelayTicks() {
		return ligthing_update_delay_ticks;
	}

	public int getLightingMaxIterationsPerTick() {
		return lighting_max_iterations_per_tick;
	}
}
