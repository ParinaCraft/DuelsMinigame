package fi.joniaromaa.duelsminigame.config;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.BlockVector;

import lombok.Getter;

public class DuelsMinigameSkywarsMapConfig extends DuelsMinigameMapConfig
{
	@Getter private List<List<BlockVector>> spawnIslandChests;
	@Getter private List<BlockVector> midChests;
	
	public DuelsMinigameSkywarsMapConfig(String id, YamlConfiguration config)
	{
		super(id, config);

		this.spawnIslandChests = new ArrayList<>();
		this.midChests = new ArrayList<>();
		
		ConfigurationSection skywars = config.getConfigurationSection("game.skywars");
		if (skywars != null)
		{
			ConfigurationSection spawnIslands = skywars.getConfigurationSection("spawn-islands");
			if (spawnIslands != null)
			{
				for(String key : spawnIslands.getKeys(false))
				{
					ConfigurationSection spawnIsland = spawnIslands.getConfigurationSection(key);
					if (spawnIsland != null)
					{
						List<BlockVector> chests = new ArrayList<>();
						
						ConfigurationSection chests_ = spawnIsland.getConfigurationSection("chests");
						if (chests_ != null)
						{
							for(String chest : chests_.getKeys(false))
							{
								chests.add((BlockVector)chests_.get(chest));
							}
						}
						
						this.spawnIslandChests.add(chests);
					}
				}
			}
			
			ConfigurationSection mid = skywars.getConfigurationSection("mid");
			if (mid != null)
			{
				ConfigurationSection chests_ = mid.getConfigurationSection("chests");
				if (chests_ != null)
				{
					for(String key : chests_.getKeys(false))
					{
						this.midChests.add((BlockVector)chests_.get(key));
					}
				}
			}
		}
	}
}
