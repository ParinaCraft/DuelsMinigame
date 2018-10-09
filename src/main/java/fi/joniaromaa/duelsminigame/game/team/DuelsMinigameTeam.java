package fi.joniaromaa.duelsminigame.game.team;

import org.bukkit.DyeColor;

import fi.joniaromaa.duelsminigame.player.DuelsMinigamePlayer;
import fi.joniaromaa.minigameframework.game.AbstractMinigame;
import fi.joniaromaa.minigameframework.team.AbstractMinigameTeam;

public class DuelsMinigameTeam extends AbstractMinigameTeam<DuelsMinigamePlayer>
{
	public DuelsMinigameTeam(AbstractMinigame<?, ?> game, String name, DyeColor color)
	{
		super(game, name, color);
	}
}
