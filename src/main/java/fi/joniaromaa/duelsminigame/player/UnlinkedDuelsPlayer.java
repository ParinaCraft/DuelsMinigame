package fi.joniaromaa.duelsminigame.player;

import org.bukkit.Location;

import fi.joniaromaa.duelsminigame.player.stats.DuelsMinigameStats;
import lombok.Getter;

/**
 * TEMP CLASS
 */
public class UnlinkedDuelsPlayer
{
	@Getter private DuelsMinigameStats stats;
	@Getter private Location loc;
	
	public UnlinkedDuelsPlayer(DuelsMinigameStats stats, Location loc)
	{
		this.stats = stats;
		this.loc = loc;
	}
}
