/**
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 - 2016
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *  
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package ru.beykerykt.lightapi;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import ru.beykerykt.lightapi.chunks.ChunkCache;
import ru.beykerykt.lightapi.chunks.ChunkInfo;
import ru.beykerykt.lightapi.events.DeleteLightEvent;
import ru.beykerykt.lightapi.events.SetLightEvent;
import ru.beykerykt.lightapi.events.UpdateChunkEvent;
import ru.beykerykt.lightapi.request.DataRequest;
import ru.beykerykt.lightapi.request.RequestSteamMachine;
import ru.beykerykt.lightapi.server.ServerModInfo;
import ru.beykerykt.lightapi.server.ServerModManager;
import ru.beykerykt.lightapi.server.exceptions.UnknownModImplementationException;
import ru.beykerykt.lightapi.server.exceptions.UnknownNMSVersionException;
import ru.beykerykt.lightapi.server.nms.craftbukkit.*;
import ru.beykerykt.lightapi.updater.Response;
import ru.beykerykt.lightapi.updater.UpdateType;
import ru.beykerykt.lightapi.updater.Updater;
import ru.beykerykt.lightapi.updater.Version;
import ru.beykerykt.lightapi.utils.BungeeChatHelperClass;
import ru.beykerykt.lightapi.utils.Metrics;

public class LightAPI extends JavaPlugin implements Listener {

	private static LightAPI plugin;
	private static RequestSteamMachine machine;
	private int configVer = 3;
	private int update_delay_ticks;
	private int max_iterations_per_tick;

	private boolean enableUpdater;
	private String repo = "Qveshn/LightAPI";
	private int delayUpdate = 40;
	private boolean viewChangelog;

	@SuppressWarnings("static-access")
	@Override
	public void onLoad() {
		this.plugin = this;
		this.machine = new RequestSteamMachine();

		ServerModInfo craftbukkit = new ServerModInfo("CraftBukkit");
		craftbukkit.getVersions().put("v1_8_R3", CraftBukkit_v1_8_R3.class);
		craftbukkit.getVersions().put("v1_9_R1", CraftBukkit_v1_9_R1.class);
		craftbukkit.getVersions().put("v1_9_R2", CraftBukkit_v1_9_R2.class);
		craftbukkit.getVersions().put("v1_10_R1", CraftBukkit_v1_10_R1.class);
		craftbukkit.getVersions().put("v1_11_R1", CraftBukkit_v1_11_R1.class);
		craftbukkit.getVersions().put("v1_12_R1", CraftBukkit_v1_12_R1.class);
		craftbukkit.getVersions().put("v1_13_R1", CraftBukkit_v1_13_R1.class);
		craftbukkit.getVersions().put("v1_13_R2", CraftBukkit_v1_13_R2.class);
		craftbukkit.getVersions().put("v1_14_R1", CraftBukkit_v1_14_R1.class);
		ServerModManager.registerServerMod(craftbukkit);

		ServerModInfo spigot = new ServerModInfo("Spigot");
		spigot.getVersions().put("v1_8_R3", CraftBukkit_v1_8_R3.class);
		spigot.getVersions().put("v1_9_R1", CraftBukkit_v1_9_R1.class);
		spigot.getVersions().put("v1_9_R2", CraftBukkit_v1_9_R2.class);
		spigot.getVersions().put("v1_10_R1", CraftBukkit_v1_10_R1.class);
		spigot.getVersions().put("v1_11_R1", CraftBukkit_v1_11_R1.class);
		spigot.getVersions().put("v1_12_R1", CraftBukkit_v1_12_R1.class);
		spigot.getVersions().put("v1_13_R1", CraftBukkit_v1_13_R1.class);
		spigot.getVersions().put("v1_13_R2", CraftBukkit_v1_13_R2.class);
		spigot.getVersions().put("v1_14_R1", CraftBukkit_v1_14_R1.class);
		ServerModManager.registerServerMod(spigot);

		ServerModInfo paperspigot = new ServerModInfo("PaperSpigot");
		paperspigot.getVersions().put("v1_8_R3", CraftBukkit_v1_8_R3.class);
		ServerModManager.registerServerMod(paperspigot);

		ServerModInfo paper = new ServerModInfo("Paper");
		paper.getVersions().put("v1_9_R1", CraftBukkit_v1_9_R1.class);
		paper.getVersions().put("v1_9_R2", CraftBukkit_v1_9_R2.class);
		paper.getVersions().put("v1_10_R1", CraftBukkit_v1_10_R1.class);
		paper.getVersions().put("v1_11_R1", CraftBukkit_v1_11_R1.class);
		paper.getVersions().put("v1_12_R1", CraftBukkit_v1_12_R1.class);
		paper.getVersions().put("v1_13_R1", CraftBukkit_v1_13_R1.class);
		paper.getVersions().put("v1_13_R2", CraftBukkit_v1_13_R2.class);
		paper.getVersions().put("v1_14_R1", CraftBukkit_v1_14_R1.class);
		ServerModManager.registerServerMod(paper);

		ServerModInfo tacospigot = new ServerModInfo("TacoSpigot");
		// tacospigot.getVersions().put("v1_8_R3", PaperSpigot_v1_8_R3.class); - call errors with anti-xray - obfuscate
		tacospigot.getVersions().put("v1_9_R1", CraftBukkit_v1_9_R1.class);
		tacospigot.getVersions().put("v1_9_R2", CraftBukkit_v1_9_R2.class);
		tacospigot.getVersions().put("v1_10_R1", CraftBukkit_v1_10_R1.class);
		tacospigot.getVersions().put("v1_11_R1", CraftBukkit_v1_11_R1.class);
		tacospigot.getVersions().put("v1_12_R1", CraftBukkit_v1_12_R1.class);
		tacospigot.getVersions().put("v1_13_R1", CraftBukkit_v1_13_R1.class);
		tacospigot.getVersions().put("v1_13_R2", CraftBukkit_v1_13_R2.class);
		tacospigot.getVersions().put("v1_14_R1", CraftBukkit_v1_14_R1.class);
		ServerModManager.registerServerMod(tacospigot);
		
		ServerModInfo akarin = new ServerModInfo("Akarin");
		akarin.getVersions().put("v1_9_R1", CraftBukkit_v1_9_R1.class);
		akarin.getVersions().put("v1_9_R2", CraftBukkit_v1_9_R2.class);
		akarin.getVersions().put("v1_10_R1", CraftBukkit_v1_10_R1.class);
		akarin.getVersions().put("v1_11_R1", CraftBukkit_v1_11_R1.class);
		akarin.getVersions().put("v1_12_R1", CraftBukkit_v1_12_R1.class);
		akarin.getVersions().put("v1_13_R1", CraftBukkit_v1_13_R1.class);
		akarin.getVersions().put("v1_13_R2", CraftBukkit_v1_13_R2.class);
		akarin.getVersions().put("v1_14_R1", CraftBukkit_v1_14_R1.class);
		ServerModManager.registerServerMod(akarin);
	}

	@Override
	public void onEnable() {
		// Config
		try {
			FileConfiguration fc = getConfig();
			File file = new File(getDataFolder(), "config.yml");
			if (file.exists()) {
				if (fc.getInt("version") < configVer) {
					file.delete(); // got a better idea?
					generateConfig(file);
				}
			} else {
				generateConfig(file);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Init config
		this.update_delay_ticks = getConfig().getInt("update-delay-ticks");
		this.max_iterations_per_tick = getConfig().getInt("max-iterations-per-tick");
		this.enableUpdater = getConfig().getBoolean("updater.enable");
		this.repo = getConfig().getString("updater.repo");
		this.delayUpdate = getConfig().getInt("updater.update-delay-ticks");
		this.viewChangelog = getConfig().getBoolean("updater.view-changelog");

		// init nms
		try {
			ServerModManager.init();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e1) {
			e1.printStackTrace();
			getServer().getPluginManager().disablePlugin(this);
			return;
		} catch (UnknownNMSVersionException e) {
			log(Bukkit.getConsoleSender(), ChatColor.RED + "Could not find handler for this Bukkit " + ChatColor.WHITE + e.getModName() + ChatColor.RED + " implementation " + ChatColor.WHITE + e.getNmsVersion() + ChatColor.RED + " version.");
			e.printStackTrace();
			getServer().getPluginManager().disablePlugin(this);
			return;
		} catch (UnknownModImplementationException e) {
			log(Bukkit.getConsoleSender(), ChatColor.RED + "Could not find handler for this Bukkit implementation: " + ChatColor.WHITE + e.getModName());
			e.printStackTrace();
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		ChunkCache.CHUNK_INFO_QUEUE.clear(); // workaround concurrent ClassLoader.definePackage due to registerEvents
		machine.start(LightAPI.getInstance().getUpdateDelayTicks(), LightAPI.getInstance().getMaxIterationsPerTick()); // TEST
		getServer().getPluginManager().registerEvents(this, this);

		if (enableUpdater) {
			// Starting updater
			runUpdater(getServer().getConsoleSender(), delayUpdate);
		}

		// init metrics
		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
			// nothing...
		}
	}

	@Override
	public void onDisable() {
		machine.shutdown();
		ChunkCache.CHUNK_INFO_QUEUE.clear();
	}

	public void log(CommandSender sender, String message) {
		sender.sendMessage(ChatColor.AQUA + "<LightAPI>: " + ChatColor.WHITE + message);
	}

	public static LightAPI getInstance() {
		return plugin;
	}

	public static boolean createLight(Location location, int lightlevel, boolean async) {
		return createLight(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), lightlevel, async);
	}

	public static boolean createLight(final World world, final int x, final int y, final int z, final int lightlevel, boolean async) {
		if (getInstance().isEnabled()) {
			final SetLightEvent event = new SetLightEvent(world, x, y, z, lightlevel, async);
			Bukkit.getPluginManager().callEvent(event);

			if (!event.isCancelled()) {
				Block adjacent = getAdjacentAirBlock(world.getBlockAt(event.getX(), event.getY(), event.getZ()));
				final int lx = adjacent.getX();
				final int ly = adjacent.getY();
				final int lz = adjacent.getZ();

				if (event.isAsync()) {
					machine.addToQueue(new DataRequest() {
						@Override
						public void process() {
							ServerModManager.getNMSHandler().createLight(event.getWorld(), event.getX(), event.getY(), event.getZ(), event.getLightLevel());
							ServerModManager.getNMSHandler().recalculateLight(event.getWorld(), lx, ly, lz);
						}
					});
					return true;
				}
				ServerModManager.getNMSHandler().createLight(event.getWorld(), event.getX(), event.getY(), event.getZ(), event.getLightLevel());
				ServerModManager.getNMSHandler().recalculateLight(event.getWorld(), lx, ly, lz);
				return true;
			}
		}
		return false;
	}

	public static boolean deleteLight(Location location, boolean async) {
		return deleteLight(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), async);
	}

	public static boolean deleteLight(final World world, final int x, final int y, final int z, boolean async) {
		if (getInstance().isEnabled()) {
			final DeleteLightEvent event = new DeleteLightEvent(world, x, y, z, async);
			Bukkit.getPluginManager().callEvent(event);

			if (!event.isCancelled()) {
				if (event.isAsync()) {
					machine.addToQueue(new DataRequest() {
						@Override
						public void process() {
							ServerModManager.getNMSHandler().deleteLight(event.getWorld(), event.getX(), event.getY(), event.getZ());
						}
					});
					return true;
				}
				ServerModManager.getNMSHandler().deleteLight(event.getWorld(), event.getX(), event.getY(), event.getZ());
				return true;
			}
		}
		return false;
	}

	public static List<ChunkInfo> collectChunks(Location location) {
		return collectChunks(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}

	public static List<ChunkInfo> collectChunks(final World world, final int x, final int y, final int z) {
		if (getInstance().isEnabled()) {
			return ServerModManager.getNMSHandler().collectChunks(world, x, y, z);
		}
		return null;
	}

	@Deprecated
	public static boolean updateChunks(ChunkInfo info) {
		return updateChunk(info);
	}

	public static boolean updateChunk(ChunkInfo info) {
		if (getInstance().isEnabled()) {
			UpdateChunkEvent event = new UpdateChunkEvent(info);
			Bukkit.getPluginManager().callEvent(event);
			if (!event.isCancelled()) {
				if (ChunkCache.CHUNK_INFO_QUEUE.contains(event.getChunkInfo())) {
					int index = ChunkCache.CHUNK_INFO_QUEUE.indexOf(event.getChunkInfo());
					ChunkInfo previous = ChunkCache.CHUNK_INFO_QUEUE.get(index);
					if (previous.getChunkYHeight() > event.getChunkInfo().getChunkYHeight()) {
						event.getChunkInfo().setChunkYHeight(previous.getChunkYHeight());
					}
					ChunkCache.CHUNK_INFO_QUEUE.remove(index);
				}
				ChunkCache.CHUNK_INFO_QUEUE.add(event.getChunkInfo());
				return true;
			}
		}
		return false;
	}

	public static boolean updateChunks(Location location, Collection<? extends Player> players) {
		return updateChunks(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), players);
	}

	public static boolean updateChunks(World world, int x, int y, int z, Collection<? extends Player> players) {
		if (getInstance().isEnabled()) {
			for (ChunkInfo info : collectChunks(world, x, y, z)) {
				info.setReceivers(players);
				updateChunk(info);
			}
			return true;
		}
		return false;
	}

	public static boolean updateChunk(Location location, Collection<? extends Player> players) {
		return updateChunk(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), players);
	}

	public static boolean updateChunk(World world, int x, int y, int z, Collection<? extends Player> players) {
		if (getInstance().isEnabled()) {
			ServerModManager.getNMSHandler().sendChunkUpdate(world, x >> 4, y, z >> 4, players);
			return true;
		}
		return false;
	}

	private static BlockFace[] SIDES = { BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };

	public static Block getAdjacentAirBlock(Block block) {
		for (BlockFace face : SIDES) {
			if (block.getY() == 0x0 && face == BlockFace.DOWN)
				continue;
			if (block.getY() == 0xFF && face == BlockFace.UP)
				continue;

			Block candidate = block.getRelative(face);

			if (candidate.getType().isTransparent()) {
				return candidate;
			}
		}
		return block;
	}

	private void generateConfig(File file) {
		FileConfiguration fc = getConfig();
		if (!file.exists()) {
			fc.options().header("LightAPI v" + getDescription().getVersion() + " Configuration" + "\nby BeYkeRYkt");
			fc.set("version", configVer);
			fc.set("update-delay-ticks", 2);
			fc.set("max-iterations-per-tick", 400);
			fc.set("updater.enable", true);
			fc.set("updater.repo", "Qveshn/LightAPI");
			fc.set("updater.update-delay-ticks", 40);
			fc.set("updater.view-changelog", false);
			saveConfig();
		}
	}

	public int getUpdateDelayTicks() {
		return update_delay_ticks;
	}

	public void setUpdateDelayTicks(int update_delay_ticks) {
		this.update_delay_ticks = update_delay_ticks;
	}

	public int getMaxIterationsPerTick() {
		return max_iterations_per_tick;
	}

	public void setMaxIterationsPerTick(int max_iterations_per_tick) {
		this.max_iterations_per_tick = max_iterations_per_tick;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		final Player player = event.getPlayer();

		if (enableUpdater) {
			if (player.hasPermission("lightapi.updater")) {
				runUpdater(player, delayUpdate);
			}
		}
	}

	private void runUpdater(final CommandSender sender, int delay) {
		Bukkit.getScheduler().runTaskLaterAsynchronously(this, new Runnable() {

			@Override
			public void run() {
				Version version = Version.parse(getDescription().getVersion());
				Updater updater;
				try {
					updater = new Updater(version, repo, true);

					Response response = updater.getResult();
					if (response == Response.SUCCESS) {
						log(sender, ChatColor.WHITE + "New update is available: " + ChatColor.YELLOW + updater.getLatestVersion() + ChatColor.WHITE + "!");
						UpdateType update = UpdateType.compareVersion(updater.getVersion().toString());
						log(sender, ChatColor.WHITE + "Repository: " + repo);
						log(sender, ChatColor.WHITE + "Update type: " + update.getName());
						if (update == UpdateType.MAJOR) {
							log(sender, ChatColor.RED + "WARNING ! A MAJOR UPDATE! Not updating plugins may produce errors after starting the server! Notify developers about update.");
						}
						if (viewChangelog) {
							log(sender, ChatColor.WHITE + "Changes: ");
							sender.sendMessage(updater.getChanges());// for normal view
						}
					} else if (response == Response.REPO_NOT_FOUND) {
						log(sender, ChatColor.RED + "Repo not found! Check that your repo exists!");
					} else if (response == Response.REPO_NO_RELEASES) {
						log(sender, ChatColor.RED + "Releases not found! Check your repo!");
					} else if (response == Response.NO_UPDATE) {
						log(sender, ChatColor.GREEN + "You are running the latest version!");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, delay);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("lightapi")) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				if (args.length == 0) {
					if (BungeeChatHelperClass.hasBungeeChatAPI()) {
						BungeeChatHelperClass.sendMessageAboutPlugin(player, this);
					} else {
						player.sendMessage(ChatColor.AQUA + " ------- <LightAPI " + ChatColor.WHITE + getDescription().getVersion() + "> ------- ");
						player.sendMessage(ChatColor.AQUA + " Current version: " + ChatColor.WHITE + getDescription().getVersion());
						player.sendMessage(ChatColor.AQUA + " Server name: " + ChatColor.WHITE + getServer().getName());
						player.sendMessage(ChatColor.AQUA + " Server version: " + ChatColor.WHITE + getServer().getVersion());
						player.sendMessage(ChatColor.AQUA + " Source code: " + ChatColor.WHITE + "http://github.com/BeYkeRYkt/LightAPI/");
						player.sendMessage(ChatColor.AQUA + " Developer: " + ChatColor.WHITE + "BeYkeRYkt");
						player.sendMessage("");
						player.sendMessage(ChatColor.WHITE + " Licensed under: " + ChatColor.AQUA + "MIT License");
					}
				} else {
					if (args[0].equalsIgnoreCase("update")) {
						if (player.hasPermission("lightapi.updater") || player.isOp()) {
							runUpdater(player, 2);
						} else {
							log(player, ChatColor.RED + "You don't have permission!");
						}
					} else {
						log(player, ChatColor.RED + "Hmm... This command does not exist. Are you sure write correctly?");
					}
				}
			} else if (sender instanceof ConsoleCommandSender) {
				ConsoleCommandSender console = (ConsoleCommandSender) sender;
				if (args.length == 0) {
					console.sendMessage(ChatColor.AQUA + " ------- <LightAPI " + ChatColor.WHITE + getDescription().getVersion() + "> ------- ");
					console.sendMessage(ChatColor.AQUA + " Current version: " + ChatColor.WHITE + getDescription().getVersion());
					console.sendMessage(ChatColor.AQUA + " Server name: " + ChatColor.WHITE + getServer().getName());
					console.sendMessage(ChatColor.AQUA + " Server version: " + ChatColor.WHITE + getServer().getVersion());
					console.sendMessage(ChatColor.AQUA + " Source code: " + ChatColor.WHITE + "http://github.com/BeYkeRYkt/LightAPI/");
					console.sendMessage(ChatColor.AQUA + " Developer: " + ChatColor.WHITE + "BeYkeRYkt");
					console.sendMessage("");
					console.sendMessage(ChatColor.WHITE + " Licensed under: " + ChatColor.AQUA + "MIT License");
				} else {
					if (args[0].equalsIgnoreCase("update")) {
						runUpdater(console, 2);
					} else {
						log(console, ChatColor.RED + "Hmm... This command does not exist. Are you sure write correctly?");
					}
				}
			}
		}
		return true;
	}
}
