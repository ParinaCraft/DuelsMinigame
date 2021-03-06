package fi.joniaromaa.duelsminigame;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.io.FileUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.SpigotConfig;

import fi.joniaromaa.duelsminigame.config.DuelsConfig;
import fi.joniaromaa.duelsminigame.listeners.BlockListener;
import fi.joniaromaa.duelsminigame.listeners.EntityListener;
import fi.joniaromaa.duelsminigame.listeners.PlayerListener;
import fi.joniaromaa.duelsminigame.listeners.WorldListener;
import fi.joniaromaa.duelsminigame.utils.LangUtils;
import fi.joniaromaa.minigameframework.MinigamePlugin;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.DedicatedServer;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.WorldSettings.EnumGamemode;

public class DuelsPlugin extends JavaPlugin
{
	@Getter private static DuelsPlugin plugin;
	
	@Getter private DuelsConfig pluginConfig;
	
	public DuelsPlugin()
	{
		DuelsPlugin.plugin = this;
	}
	
	@Override
	public void onLoad()
	{
		for (File file : new File(".").listFiles())
		{
			if (file.isDirectory() && file.getName().startsWith("duels_minigame-"))
			{
				try
				{
					FileUtils.deleteDirectory(file);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		MinecraftServer mcServer = ((CraftServer)this.getServer()).getServer();
		mcServer.getPropertyManager().setProperty("spawn-animals", false);
		mcServer.getPropertyManager().setProperty("spawn-npcs", false);
		mcServer.getPropertyManager().setProperty("pvp", true);
		mcServer.getPropertyManager().setProperty("allow-flight", false);
		mcServer.getPropertyManager().setProperty("force-gamemode", true);
		mcServer.getPropertyManager().setProperty("gamemode", 2);
		mcServer.getPropertyManager().setProperty("difficulty", 1);
		mcServer.getPropertyManager().setProperty("allow-nether", false);
		mcServer.getPropertyManager().setProperty("spawn-monsters", false);
		mcServer.getPropertyManager().setProperty("generate-structures", false);
		mcServer.getPropertyManager().savePropertiesFile();
		
		try
		{
			Field bukkitConfigField = CraftServer.class.getDeclaredField("configuration");
			bukkitConfigField.setAccessible(true);
			
			YamlConfiguration bukkitConfig = (YamlConfiguration)bukkitConfigField.get(this.getServer());
			bukkitConfig.set("settings.allow-end", false);
			
			Method bukkitConfigSaveMethod = CraftServer.class.getDeclaredMethod("saveConfig");
			bukkitConfigSaveMethod.setAccessible(true);
			bukkitConfigSaveMethod.invoke(this.getServer());
		}
		catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e)
		{
			e.printStackTrace();
		}
		
		mcServer.setSpawnAnimals(false);
		mcServer.setSpawnNPCs(false);
		mcServer.setPVP(false);
		mcServer.setAllowFlight(false);
		mcServer.setForceGamemode(true);
		mcServer.setGamemode(EnumGamemode.ADVENTURE);
		
		if (mcServer instanceof DedicatedServer)
		{
			try
			{
				Field generateStructuresField = DedicatedServer.class.getDeclaredField("generateStructures");
				generateStructuresField.setAccessible(true);
				generateStructuresField.set(mcServer, false);
			}
			catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e)
			{
				e.printStackTrace();
			}
		}

		SpigotConfig.config.set("world-settings.default.arrow-despawn-rate", 300); //Keep arrows around 15s
		SpigotConfig.config.set("world-settings.default.enable-zombie-pigmen-portal-spawns", false);
		SpigotConfig.config.set("world-settings.default.anti-xray.enabled", false);
		SpigotConfig.config.set("world-settings.default.save-structure-info", false);
		
		try
		{
			Field spigotConfigFile = SpigotConfig.class.getDeclaredField("CONFIG_FILE");
			spigotConfigFile.setAccessible(true);
			SpigotConfig.config.save((File)spigotConfigFile.get(null));
		}
		catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | IOException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void onEnable()
	{
		LangUtils.init("fi_FI");
		
		this.saveDefaultConfig();
		this.pluginConfig = new DuelsConfig(this.getConfig());
		
		try
		{
			MinigamePlugin.getPlugin().configure(this.pluginConfig);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
			
			throw new RuntimeException("Failed to configure minigame!");
		}

		this.getServer().getPluginManager().registerEvents(new PlayerListener(), this);
		this.getServer().getPluginManager().registerEvents(new EntityListener(), this);
		this.getServer().getPluginManager().registerEvents(new BlockListener(), this);
		this.getServer().getPluginManager().registerEvents(new WorldListener(), this);
		
		this.getServer().getMessenger().registerOutgoingPluginChannel(DuelsPlugin.getPlugin(), "BungeeCord");
		this.getServer().getMessenger().registerOutgoingPluginChannel(DuelsPlugin.getPlugin(), "Queue");
	}
}
