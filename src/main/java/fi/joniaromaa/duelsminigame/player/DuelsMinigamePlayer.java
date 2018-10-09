package fi.joniaromaa.duelsminigame.player;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import fi.joniaromaa.duelsminigame.game.team.DuelsMinigameTeam;
import fi.joniaromaa.duelsminigame.player.stats.DuelsMinigameStats;
import fi.joniaromaa.minigameframework.game.AbstractMinigame;
import fi.joniaromaa.minigameframework.player.AbstractMinigamePlayer;
import lombok.Getter;
import lombok.Setter;

public class DuelsMinigamePlayer extends AbstractMinigamePlayer<DuelsMinigameTeam>
{
	@Getter @Setter private Location spawnLocation;
	
	public DuelsMinigamePlayer(AbstractMinigame<?, ?> minigame, Player bukkitPlayer)
	{
		super(minigame, bukkitPlayer, new DuelsMinigameStats());
	}
	
	@Override
	public void onDied()
	{
		super.onDied();
		
		this.setAlive(false);
		
		this.getGame().makeSpectator(this.getBukkitPlayer());
		
		Player bukkitPlayer = this.getBukkitPlayer();
		if (bukkitPlayer.getLocation().getY() <= 0)
		{
			bukkitPlayer.teleport(this.getSpawnLocation());
		}
	}
}
