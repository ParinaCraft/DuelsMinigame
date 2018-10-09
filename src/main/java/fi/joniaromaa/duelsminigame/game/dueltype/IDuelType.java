package fi.joniaromaa.duelsminigame.game.dueltype;

import org.bukkit.World;
import org.bukkit.entity.Player;

import fi.joniaromaa.duelsminigame.config.DuelsMinigameMapConfig;
import fi.joniaromaa.duelsminigame.game.DuelsMinigame;
import fi.joniaromaa.duelsminigame.player.DuelsMinigamePlayer;
import fi.joniaromaa.minigameframework.player.BukkitUser;

public interface IDuelType
{
	public String getName();

	public default void preGameSpawn(BukkitUser user) {}
	public default void preGameJoin(BukkitUser user) {}
	public default void preGameLeave(BukkitUser user) {}

	public default void gameStart(World world, DuelsMinigameMapConfig config) {}
	public default void setupPlayer(DuelsMinigamePlayer player) {}
	public default void cleanPlayer(Player player) {}
	public default void setupPreMatch(DuelsMinigame minigame, DuelsMinigamePlayer player) {}

	public boolean canFreelyMoveOnPreGame();
	public boolean canDropItems();
	public boolean canPlaceBlocks();
	public boolean canBreakBlocks();
}
