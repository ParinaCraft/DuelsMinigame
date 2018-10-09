package fi.joniaromaa.duelsminigame.config;

import org.bukkit.configuration.file.FileConfiguration;

import fi.joniaromaa.duelsminigame.pregame.DuelsPreMinigame;
import fi.joniaromaa.minigameframework.config.MinigameConfig;
import fi.joniaromaa.minigameframework.config.MinigameManagerConfig;
import fi.joniaromaa.minigameframework.game.AbstractPreMinigame;

public class DuelsConfig extends MinigameManagerConfig
{
	private int concurrentGameLimit;
	private DuelsMinigameConfig minigameConfig;
	
	public DuelsConfig(FileConfiguration config)
	{
		this.concurrentGameLimit = config.getInt("game.concurrent-game-limit");
		
		this.minigameConfig = new DuelsMinigameConfig(config.getConfigurationSection("minigame"));
	}

	@Override
	public int getConcurrentGameLimit()
	{
		return this.concurrentGameLimit;
	}

	@Override
	public Class<? extends AbstractPreMinigame> getPreMinigameClass()
	{
		return DuelsPreMinigame.class;
	}

	@Override
	public MinigameConfig getMinigameConfig()
	{
		return this.minigameConfig;
	}
}
