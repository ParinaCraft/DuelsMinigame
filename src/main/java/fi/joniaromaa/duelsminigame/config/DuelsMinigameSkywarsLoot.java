package fi.joniaromaa.duelsminigame.config;

import java.util.HashSet;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import lombok.Getter;

public class DuelsMinigameSkywarsLoot
{
	@Getter private ItemStack item;
	@Getter private float chance;
	@Getter private HashSet<String> tags;
	
	public DuelsMinigameSkywarsLoot(ConfigurationSection config)
	{
		this.item = config.getItemStack("item");
		this.chance = (float)config.getDouble("chance");
		this.tags = new HashSet<String>(config.getStringList("tags"));
	}
}
