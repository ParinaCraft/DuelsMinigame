package fi.joniaromaa.duelsminigame.config;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import fi.joniaromaa.duelsminigame.DuelsPlugin;
import fi.joniaromaa.duelsminigame.game.DuelsMinigame;
import fi.joniaromaa.duelsminigame.game.dueltype.ClassicDuelType;
import fi.joniaromaa.duelsminigame.game.dueltype.ComboDuelType;
import fi.joniaromaa.duelsminigame.game.dueltype.IDuelType;
import fi.joniaromaa.duelsminigame.game.dueltype.SkywarsDuelType;
import fi.joniaromaa.minigameframework.config.MinigameConfig;
import fi.joniaromaa.minigameframework.config.MinigameMapConfig;
import fi.joniaromaa.minigameframework.game.AbstractMinigame;
import lombok.Getter;

public class DuelsMinigameConfig extends MinigameConfig
{
	private HashMap<String, DuelsMinigameMapConfig> maps;
	
	private int gameType;
	private int teamSize;
	
	@Getter private IDuelType duelType;
	@Getter private int maxGameTime;

	public DuelsMinigameConfig(ConfigurationSection config)
	{
		this.maps = new HashMap<>();
		
		switch(config.getString("dueltype"))
		{
			case "classic":
			{
				this.duelType = new ClassicDuelType();
				break;
			}
			case "combo":
			{
				this.duelType = new ComboDuelType();
				break;
			}
			case "skywars":
			{
				this.duelType = new SkywarsDuelType(config.getConfigurationSection("skywars"));
				break;
			}
			default:
			{
				throw new RuntimeException("Unknown duel type!");
			}
		}
		
		this.gameType = config.getInt("gametype");
		this.teamSize = config.getInt("team-size");
		
		List<String> maps = config.getStringList("maps");
		if (maps != null)
		{
			for(String mapId : maps)
			{
				if (config.getString("dueltype").equals("skywars"))
				{
					this.maps.put(mapId, new DuelsMinigameSkywarsMapConfig(mapId, YamlConfiguration.loadConfiguration(Paths.get(DuelsPlugin.getPlugin().getDataFolder().getPath(), "maps", mapId, "config.yml").toFile())));
				}
				else
				{
					this.maps.put(mapId, new DuelsMinigameMapConfig(mapId, YamlConfiguration.loadConfiguration(Paths.get(DuelsPlugin.getPlugin().getDataFolder().getPath(), "maps", mapId, "config.yml").toFile())));
				}
			}
		}
		
		this.maxGameTime = config.getInt("max-game-time");
	}
	
	@Override
	public int getGameType()
	{
		return this.gameType;
	}

	@Override
	public int getTeamSize()
	{
		return this.teamSize;
	}

	@Override
	public Class<? extends AbstractMinigame<?, ?>> getMinigameClass()
	{
		return DuelsMinigame.class;
	}
	
	public Collection<DuelsMinigameMapConfig> getMaps()
	{
		return Collections.unmodifiableCollection(this.maps.values());
	}

	@Override
	public Collection<MinigameMapConfig> getMapConfigs()
	{
		return Collections.unmodifiableCollection(this.maps.values());
	}
	
	public DuelsMinigameMapConfig getRandomMapConfig()
	{
		return (DuelsMinigameMapConfig)super.getRandomMapConfig();
	}
}
