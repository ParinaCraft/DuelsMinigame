package fi.joniaromaa.duelsminigame.pregame;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.util.BlockVector;

import fi.joniaromaa.duelsminigame.DuelsPlugin;
import fi.joniaromaa.duelsminigame.config.DuelsMinigameConfig;
import fi.joniaromaa.duelsminigame.config.DuelsMinigameMapConfig;
import fi.joniaromaa.duelsminigame.user.dataset.UserStartLocationDataStorage;
import fi.joniaromaa.duelsminigame.utils.LangUtils;
import fi.joniaromaa.minigameframework.api.game.PreMinigameStatus;
import fi.joniaromaa.minigameframework.builder.MinigameWorldBuilder;
import fi.joniaromaa.minigameframework.config.MinigameConfig;
import fi.joniaromaa.minigameframework.config.MinigameMapConfig;
import fi.joniaromaa.minigameframework.game.AbstractPreMinigame;
import fi.joniaromaa.minigameframework.player.BukkitUser;
import fi.joniaromaa.minigameframework.world.BlockBreakContractTypeType;
import fi.joniaromaa.minigameframework.world.WorldWeatherType;
import fi.joniaromaa.parinacorelibrary.bukkit.data.WordlessLocation;
import fi.joniaromaa.parinacorelibrary.bukkit.scoreboard.ScoreboardDynamicScore;
import fi.joniaromaa.parinacorelibrary.bukkit.scoreboard.ScoreboardManager;
import fi.joniaromaa.parinacorelibrary.bukkit.scoreboard.ScoreboardViewer;
import fi.joniaromaa.parinacorelibrary.bukkit.utils.LocationUtils;
import fi.joniaromaa.parinacorelibrary.bukkit.utils.WorldUtils;
import net.md_5.bungee.api.ChatColor;

public class DuelsPreMinigame extends AbstractPreMinigame
{
	private Map<Location, Integer> spawnLocations;
	private World world;
	
	public DuelsPreMinigame(int gameId, DuelsMinigameConfig config)
	{
		this(gameId, config, config.getRandomMapConfig());
	}
	
	public DuelsPreMinigame(int gameId, MinigameConfig config, MinigameMapConfig mapConfig)
	{
		super(gameId, config, mapConfig);
		
		this.scoreboardManager = new ScoreboardManager(DuelsPlugin.getPlugin(), this::setupScoreboard);
	}
	
	public void setup() throws Exception
	{
		FileUtils.copyDirectory(Paths.get(DuelsPlugin.getPlugin().getDataFolder().getPath(), "maps", this.getMapConfig().getId(), "world").toFile(), new File("duels_minigame-" + this.getGameId()));
		
		this.world = MinigameWorldBuilder.builder().worldName("duels_minigame-" + this.getGameId())
			.voidOnlyGenerator()
			.saveChunks(false)
			.setWeatherType(WorldWeatherType.CLEAR)
			.doDaylightCycle(false)
			.doFireTick(false)
			.allowBlockPlace(this.getConfig().getDuelType().canPlaceBlocks())
			.blockBreakContractType(this.getConfig().getDuelType().canBreakBlocks() ? BlockBreakContractTypeType.WORLD : BlockBreakContractTypeType.USER_PLACED)
			.build(DuelsPlugin.getPlugin());
		
		this.world.setKeepSpawnInMemory(true);
		this.world.setPVP(true);
		this.world.setSpawnFlags(false, false);
		this.world.setTime(6000);
		
		for(Chunk chunk : this.world.getLoadedChunks())
		{
			if (Boolean.TRUE.equals(this.shouldUnloadChunk(chunk)))
			{
				chunk.unload(false, false);
			}
		}
		
		if (this.getMapConfig().hasGameArea())
		{
			WorldUtils.loadChunksBetween(this.world, (int)this.getMapConfig().getGameAreaBorder().getMin().getX() >> 4, (int)this.getMapConfig().getGameAreaBorder().getMin().getZ() >> 4, (int)this.getMapConfig().getGameAreaBorder().getMax().getX() >> 4, (int)this.getMapConfig().getGameAreaBorder().getMax().getZ() >> 4);
		}
		
		this.spawnLocations = new HashMap<>();
		for(WordlessLocation location : this.getMapConfig().getSpawnLocations())
		{
			this.spawnLocations.put(location.toLocation(this.world), 0);
		}
		
		super.setup();
	}
	
	public Boolean shouldUnloadChunk(Chunk chunk)
	{
		if (!this.getMapConfig().hasGameArea() || !this.getMapConfig().isGameAreaRestricted())
		{
			return null;
		}
		
		BlockVector gameMinBorder = this.getMapConfig().getGameAreaBorder().getMin();
		BlockVector gameMaxBorder = this.getMapConfig().getGameAreaBorder().getMax();
		
		int x = chunk.getX();
		int z = chunk.getZ();
		if (gameMinBorder != null && gameMaxBorder != null && LocationUtils.outsideChunkCoords(x, z, (int)gameMinBorder.getX() >> 4, (int)gameMinBorder.getZ() >> 4, (int)gameMaxBorder.getX() >> 4, (int)gameMaxBorder.getZ() >> 4))
		{
			return true;
		}
		
		return false;
	}
	
	private void setupScoreboard(ScoreboardViewer viewer)
	{
		Objective sideBar = viewer.getScoreboard().registerNewObjective("sideBar", "dummy");
		sideBar.setDisplayName(ChatColor.AQUA + "Duels");
		sideBar.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		sideBar.getScore(" ").setScore(7);
		viewer.addDynamicScore(new ScoreboardDynamicScore(viewer, sideBar, this::updateScoreboardPlayerCount, 6));
		sideBar.getScore("  ").setScore(5);
		viewer.addDynamicScore(new ScoreboardDynamicScore(viewer, sideBar, this::updateScoreboardTime, 4));
		sideBar.getScore("   ").setScore(3);
		sideBar.getScore(LangUtils.getText(viewer.getPlayer().spigot().getLocale(), "scoreboard.map", this.getMapConfig().getId())).setScore(2);
		sideBar.getScore("    ").setScore(1);
		sideBar.getScore(ChatColor.AQUA + "parina" + ChatColor.GREEN + "craft.net").setScore(0);
	}
	
	private void updateScoreboardPlayerCount(ScoreboardDynamicScore dynamicScore)
	{
		dynamicScore.set(LangUtils.getText(dynamicScore.getScoreboardViewer().getPlayer().spigot().getLocale(), "scoreboard.players", this.getPlayersCount(), this.getPlayersLimit()));
	}
	
	private void updateScoreboardTime(ScoreboardDynamicScore dynamicScore)
	{
		if (this.getStatus() == PreMinigameStatus.WAITING_FOR_PLAYERS)
		{
			dynamicScore.set(LangUtils.getText(dynamicScore.getScoreboardViewer().getPlayer().spigot().getLocale(), "scoreboard.waiting-for-players"));
		}
		else
		{
			dynamicScore.set(LangUtils.getText(dynamicScore.getScoreboardViewer().getPlayer().spigot().getLocale(), "scoreboard.countdown-starting", this.getTimeLeftToStartInSecs() + "s"));
		}
	}

	@Override
	public Optional<Location> onPlayerSpawn(Player player)
	{
		Entry<Location, Integer> entry = this.spawnLocations.entrySet().stream().min((o1, o2) -> Integer.compare(o1.getValue(), o2.getValue())).get();
		
		Location location = entry.getKey();
		Integer value = entry.getValue();
		
		this.spawnLocations.put(location, value + 1);
		
		BukkitUser user = this.getPlayer(player);
		user.getUser().setDataStorage(new UserStartLocationDataStorage(location));
		
		this.getConfig().getDuelType().preGameSpawn(user);
		
		return Optional.of(entry.getKey());
	}
	
	@Override
	public void onPlayerJoin(Player player)
	{
		BukkitUser user = this.getPlayer(player);

		this.getConfig().getDuelType().preGameJoin(user);
		
		super.onPlayerJoin(player);
	}
	
	@Override
	public void onPlayerQuit(Player player)
	{
		BukkitUser user = this.getPlayer(player);
		
		UserStartLocationDataStorage startLocation = user.getUser().removeDataStorage(UserStartLocationDataStorage.class).orElse(null);
		if (startLocation != null)
		{
			this.spawnLocations.put(startLocation.getLocation(), this.spawnLocations.get(startLocation.getLocation()) - 1);
		}
		
		this.getConfig().getDuelType().preGameLeave(user);
		
		super.onPlayerQuit(player);
	}

	@Override
	public void onCriticalException(Throwable e)
	{
		this.world.getPlayers().forEach((p) -> p.kickPlayer("Critical error"));
		
		DuelsPlugin.getPlugin().getServer().unloadWorld(this.world, false);
		
		try
		{
			FileUtils.deleteDirectory(this.world.getWorldFolder());
		}
		catch (IOException e1) //Failed to delete the directory
		{
			e1.printStackTrace();
		}
	}
	
	@Override
	public DuelsMinigameConfig getConfig()
	{
		return (DuelsMinigameConfig)super.getConfig();
	}
	
	@Override
	public DuelsMinigameMapConfig getMapConfig()
	{
		return (DuelsMinigameMapConfig)super.getMapConfig();
	}

	@Override
	public World getGameWorld()
	{
		return this.world;
	}
}
