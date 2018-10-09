package fi.joniaromaa.duelsminigame.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import fi.joniaromaa.minigameframework.config.MinigameMapConfig;
import fi.joniaromaa.parinacorelibrary.bukkit.data.BorderVector;
import fi.joniaromaa.parinacorelibrary.bukkit.data.WordlessLocation;
import lombok.Getter;

public class DuelsMinigameMapConfig extends MinigameMapConfig
{
	@Getter private final String id;
	
	private int playerLimit;

	private HashMap<Integer, Integer> preGameStartTimes;
	private LinkedList<WordlessLocation> spawnLocations;
	
	@Getter private BorderVector gameAreaBorder;
	@Getter private boolean gameAreaRestricted;
	@Getter private boolean unloadAfterOver;
	
	public DuelsMinigameMapConfig(String id, YamlConfiguration config)
	{
		this.id = id;
		
		this.playerLimit = config.getInt("game.player-limit");
		
		this.preGameStartTimes = new HashMap<>();
		
		ConfigurationSection preGameStartTimes = config.getConfigurationSection("game.pre-game-start-times");
		for(String key : preGameStartTimes.getKeys(false))
		{
			this.preGameStartTimes.put(new Integer(key), preGameStartTimes.getInt(key));
		}
		
		this.spawnLocations = new LinkedList<>();
		
		ConfigurationSection spawnLocations = config.getConfigurationSection("game.spawn-locations");
		for(String key : spawnLocations.getKeys(false))
		{
			this.spawnLocations.add((WordlessLocation)spawnLocations.get(key));
		}
		
		ConfigurationSection area = config.getConfigurationSection("game.area");
		if (area != null)
		{
			if (area.contains("border"))
			{
				this.gameAreaBorder = (BorderVector)area.get("border");
			}
			
			this.gameAreaRestricted = area.getBoolean("restricted");
			this.unloadAfterOver = area.getBoolean("unload-after-over");
		}
	}
	
	@Override
	public int getPlayerLimit()
	{
		return this.playerLimit;
	}

	@Override
	public Map<Integer, Integer> getPreGameStartTimes()
	{
		return Collections.unmodifiableMap(this.preGameStartTimes);
	}
	
	public List<WordlessLocation> getSpawnLocations()
	{
		return Collections.unmodifiableList(this.spawnLocations);
	}
	
	public boolean hasGameArea()
	{
		return this.gameAreaBorder != null;
	}
}
