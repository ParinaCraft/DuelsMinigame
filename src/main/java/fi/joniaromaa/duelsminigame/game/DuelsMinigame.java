package fi.joniaromaa.duelsminigame.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.bukkit.Chunk;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.util.BlockVector;
import org.github.paperspigot.Title;

import com.google.common.collect.Lists;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import fi.joniaromaa.duelsminigame.DuelsPlugin;
import fi.joniaromaa.duelsminigame.config.DuelsMinigameConfig;
import fi.joniaromaa.duelsminigame.config.DuelsMinigameMapConfig;
import fi.joniaromaa.duelsminigame.game.team.DuelsMinigameTeam;
import fi.joniaromaa.duelsminigame.gui.DuelRematchPlayerInterfaceInventory;
import fi.joniaromaa.duelsminigame.gui.data.DuelsRematchVoteTeam;
import fi.joniaromaa.duelsminigame.player.DuelsMinigamePlayer;
import fi.joniaromaa.duelsminigame.user.dataset.UserStartLocationDataStorage;
import fi.joniaromaa.duelsminigame.utils.LangUtils;
import fi.joniaromaa.minigameframework.MinigamePlugin;
import fi.joniaromaa.minigameframework.config.MinigameConfig;
import fi.joniaromaa.minigameframework.config.MinigameMapConfig;
import fi.joniaromaa.minigameframework.game.AbstractMinigame;
import fi.joniaromaa.minigameframework.player.BukkitUser;
import fi.joniaromaa.parinacorelibrary.bukkit.scoreboard.ScoreboardDynamicScore;
import fi.joniaromaa.parinacorelibrary.bukkit.scoreboard.ScoreboardManager;
import fi.joniaromaa.parinacorelibrary.bukkit.scoreboard.ScoreboardViewer;
import fi.joniaromaa.parinacorelibrary.bukkit.utils.LocationUtils;
import fi.joniaromaa.parinacorelibrary.common.utils.TimeUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class DuelsMinigame extends AbstractMinigame<DuelsMinigameTeam, DuelsMinigamePlayer>
{
	private DuelsMinigameStatus status;
	private int timeLeft;
	private boolean skipTimeLeft;
	
	private DuelRematchPlayerInterfaceInventory rematch;
	
	public DuelsMinigame(int gameId, MinigameConfig config, MinigameMapConfig mapConfig, World world, Collection<BukkitUser> users, boolean privateGame)
	{
		super(gameId, config, mapConfig, world, users, privateGame);
		
		this.scoreboardManager = new ScoreboardManager(DuelsPlugin.getPlugin(), this::setupScoreboard);
	}
	
	private void setupScoreboard(ScoreboardViewer viewer)
	{
		Objective sideBar = viewer.getScoreboard().registerNewObjective("sideBar", "dummy");
		sideBar.setDisplayName(ChatColor.AQUA + "Duels");
		sideBar.setDisplaySlot(DisplaySlot.SIDEBAR);

		sideBar.getScore("   ").setScore(5);
		viewer.addDynamicScore(new ScoreboardDynamicScore(viewer, sideBar, this::updateScoreboardTimeLeft, 4));
		sideBar.getScore("    ").setScore(3);
		sideBar.getScore(LangUtils.getText(viewer.getPlayer().spigot().getLocale(), "scoreboard.map", this.getMapConfig().getId())).setScore(2);
		sideBar.getScore("     ").setScore(1);
		sideBar.getScore(ChatColor.AQUA + "parina" + ChatColor.GREEN + "craft.net").setScore(0);
	}
	
	@Override
	protected void sortToTeams(Collection<BukkitUser> users)
	{
		this.teams.clear();
		this.players.clear();
		this.stats.clear();
		
		Map<Location, List<BukkitUser>> usersBySpawnLocation = new HashMap<>();
		for(BukkitUser user : users)
		{
			usersBySpawnLocation.computeIfAbsent(user.getUser().getDataStorage(UserStartLocationDataStorage.class).get().getLocation(), (k) -> new ArrayList<>()).add(user);
		}

		if (usersBySpawnLocation.size() > 2)
		{
			throw new UnsupportedOperationException("Too many teams");
		}
		
		List<DuelsMinigameTeam> teamsToFill = this.buildTeams();
		for(Entry<Location, List<BukkitUser>> teamMembers : usersBySpawnLocation.entrySet())
		{
			DuelsMinigameTeam team = teamsToFill.remove(0);
			for(BukkitUser teamMember : teamMembers.getValue())
			{
				DuelsMinigamePlayer player = this.createPlayer(teamMember);
				player.setSpawnLocation(teamMembers.getKey());
				
				this.teams.put(team.getName(), team);
				
				team.addTeamMember(player);
				
				player.setTeam(team);
				
				this.teams.put(team.getName(), team);
				this.players.put(player.getUniqueId(), player);
				this.stats.put(player.getUniqueId(), player.getStats());
			}
		}
	}

	@Override
	protected List<DuelsMinigameTeam> buildTeams()
	{
		return Lists.newArrayList(new DuelsMinigameTeam(this, "Blue", DyeColor.BLUE), new DuelsMinigameTeam(this, "Red", DyeColor.RED));
	}

	@Override
	protected DuelsMinigamePlayer createPlayer(BukkitUser user)
	{
		return new DuelsMinigamePlayer(this, user.getBukkitPlayer());
	}
	
	private void setStatus(DuelsMinigameStatus status, int timeLeft)
	{
		this.status = status;
		this.timeLeft = timeLeft;
		this.skipTimeLeft = true;
	}

	@Override
	public void start()
	{
		this.getConfig().getDuelType().gameStart(this.getWorld(), this.getMapConfig());
		
		for(DuelsMinigamePlayer minigamePlayer : this.getAlivePlayers())
		{
			Player bukkitPlayer = minigamePlayer.getBukkitPlayer();
			
			if (this.getConfig().getDuelType().canFreelyMoveOnPreGame())
			{
				bukkitPlayer.teleport(minigamePlayer.getSpawnLocation());
			}
			
			//Make sure both users can see each other
			this.removeSpectator(bukkitPlayer);
			
			((CraftPlayer)bukkitPlayer).getHandle().invulnerableTicks = 0; //Make sure we can hit him!
			
			this.getConfig().getDuelType().setupPlayer(minigamePlayer);
			
			this.scoreboardManager.addPlayer(bukkitPlayer);
		}
		
		this.setStatus(DuelsMinigameStatus.RUNNING, this.getConfig().getMaxGameTime() * 20);
		
		super.start();
	}
	
	private void updateScoreboardTimeLeft(ScoreboardDynamicScore dynamicScore)
	{
		if (this.status == DuelsMinigameStatus.RUNNING)
		{
			dynamicScore.set(LangUtils.getText(dynamicScore.getScoreboardViewer().getPlayer().spigot().getLocale(), "scoreboard.time-left", TimeUtils.getHumanReadableSimplePeriod((int)Math.ceil(this.timeLeft / 20d))));
		}
		else if (this.status == DuelsMinigameStatus.REMATCH)
		{
			dynamicScore.set(LangUtils.getText(dynamicScore.getScoreboardViewer().getPlayer().spigot().getLocale(), "scoreboard.countdown-starting", TimeUtils.getHumanReadableSimplePeriod((int)Math.ceil(this.timeLeft / 20d))));
		}
		else
		{
			dynamicScore.set(LangUtils.getText(dynamicScore.getScoreboardViewer().getPlayer().spigot().getLocale(), "scoreboard.game-over"));
		}
	}
	
	public Boolean shouldUnloadChunk(Chunk chunk)
	{
		if (!this.getMapConfig().hasGameArea() || !this.getMapConfig().isGameAreaRestricted())
		{
			return null;
		}
		
		if (this.getMapConfig().isUnloadAfterOver())
		{
			return false;
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

	@Override
	public Optional<Location> onPlayerSpawn(Player player)
	{
		return Optional.of(this.getMapConfig().getSpawnLocations().get(0).toLocation(this.getWorld()));
	}
	
	@Override
	public void onPlayerQuit(Player player)
	{
		DuelsMinigamePlayer minigamePlayer = this.getMinigamePlayer(player);
		if (minigamePlayer != null)
		{
			minigamePlayer.setAlive(false);
		}
		
		super.onPlayerQuit(player);
	}
	
	@Override
	public void makeSpectator(Player player)
	{
		super.makeSpectator(player);
		
		this.getConfig().getDuelType().cleanPlayer(player);
	}
	
	@Override
	public void onTick()
	{
		this.skipTimeLeft = false;
		
		if (this.status == DuelsMinigameStatus.RUNNING)
		{
			int aliveTeamsCount = this.getAliveTeamsCount();
			if (aliveTeamsCount == 1)
			{
				StringBuilder builder = new StringBuilder();
				for(DuelsMinigameTeam team : this.getAliveTeams())
				{
					for(DuelsMinigamePlayer player : team.getTeamMembers())
					{
						if (builder.length() > 0)
						{
							builder.append(", ");
						}
						
						builder.append(player.getUser().getColoredDisplayName());
					}
				}
				
				this.sendTitle(Title.builder()
						.title(builder.toString())
						.subtitle(ChatColor.GREEN + "on voittanut!")
						.build());

				this.setStatus(DuelsMinigameStatus.FIGHT_OVER, 60); //REMVOE HARDCORED SHIT
			}
			else if (this.timeLeft <= 0 || aliveTeamsCount <= 0)
			{
				this.sendTitle(Title.builder()
						.title(ChatColor.YELLOW + "Tasapeli")
						.build());
				
				this.setStatus(DuelsMinigameStatus.FIGHT_OVER, 60); //REMVOE HARDCORED SHIT
			}
		}
		else if (this.status == DuelsMinigameStatus.FIGHT_OVER)
		{
			if (this.timeLeft <= 0)
			{
				if (this.getPlayers().stream().allMatch((p) -> p.getBukkitPlayer().isOnline()))
				{
					LinkedList<DuelsRematchVoteTeam> teams = new LinkedList<>();
					for(DuelsMinigameTeam team : this.getTeams())
					{
						DuelsRematchVoteTeam voteTeam = new DuelsRematchVoteTeam(team.getName());
						for(DuelsMinigamePlayer teamMate : team.getTeamMembers())
						{
							voteTeam.addMember(teamMate.getUniqueId(), teamMate.getUser().getDisplayName());
						}
						
						teams.add(voteTeam);
					}

					this.rematch = new DuelRematchPlayerInterfaceInventory(this, teams);
					for(DuelsMinigamePlayer player : this.getPlayers())
					{
						player.getBukkitPlayer().openInventory(this.rematch.getInventory());
					}
					
					this.setStatus(DuelsMinigameStatus.VOTE, 200); //DONT HARDCORE THIS
				}
				else
				{
					this.setStatus(DuelsMinigameStatus.ENDED, 100); //TODO: DONT HARDCORE
				}
			}
			else if (this.timeLeft == 60)
			{
				for(Entity entity : this.getWorld().getEntities())
				{
					if (!(entity instanceof Player))
					{
						entity.remove();
					}
				}
				
				for(DuelsMinigamePlayer player : this.getPlayers())
				{
					Player bukkitPlayer = player.getBukkitPlayer();
					bukkitPlayer.setHealth(bukkitPlayer.getMaxHealth());
					bukkitPlayer.setFireTicks(0);
					
					((CraftPlayer)bukkitPlayer).getHandle().invulnerableTicks = Integer.MAX_VALUE;
				}
			}
		}
		else if (this.status == DuelsMinigameStatus.VOTE)
		{
			if (this.timeLeft <= 0)
			{
				this.rematch = null;
				
				this.setStatus(DuelsMinigameStatus.ENDED, 100); //TODO: DONT HARDCORE
				
				String command = "/queue duels_1v1_" + this.getConfig().getDuelType().getName().toLowerCase();
				
				TextComponent playAgain = new TextComponent("Pelaa uudelleen!");
				playAgain.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(command).create()));
				playAgain.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
				playAgain.setBold(true);
				playAgain.setColor(ChatColor.AQUA);
				
				for(DuelsMinigamePlayer player : this.getPlayers())
				{
					Player bukkitPlayer = player.getBukkitPlayer();
					
					bukkitPlayer.closeInventory();
					bukkitPlayer.spigot().sendMessage(playAgain);
				}
			}
			else
			{
				for(DuelsMinigamePlayer player : this.getPlayers())
				{
					if (!this.rematch.getInventory().getViewers().contains(player.getBukkitPlayer()))
					{
						player.getBukkitPlayer().openInventory(this.rematch.getInventory());
					}
				}
			}
		}
		else if (this.status == DuelsMinigameStatus.ENDED)
		{
			if (this.timeLeft == 100)
			{
				this.sendTitle(Title.builder()
						.title(ChatColor.RED + "Peli p��ttyi")
						.build());
			}
			else if (this.timeLeft == 0)
			{
				ByteArrayDataOutput out = ByteStreams.newDataOutput();
				out.writeUTF("Connect");
				out.writeUTF("lobby");
				
				this.sendPluginMessage(DuelsPlugin.getPlugin(), "BungeeCord", out.toByteArray());
			}
			else if (this.timeLeft <= -120 || (this.getAlivePlayersCount() <= 0 && this.getSpectatorsCount() <= 0))
			{
				MinigamePlugin.getPlugin().getGameManager().deleteGame(this);
			}
			else if (this.timeLeft <= -100)
			{
				this.kick("Bye!");
			}
		}
		else if (this.status == DuelsMinigameStatus.REMATCH)
		{
			if (this.getPlayers().stream().anyMatch((p) -> !p.getBukkitPlayer().isOnline()))
			{
				this.setStatus(DuelsMinigameStatus.ENDED, 100);
			}
			else if (this.timeLeft <= 0)
			{
				this.getPlayers().forEach((p) -> p.setAlive(true));
				
				this.start();
			}
			else if (this.timeLeft % 20 == 0)
			{
				int secs = this.timeLeft / 20;
				if (secs <= 5 || secs == 10)
				{
					ChatColor color = null;
					if (secs <= 3)
					{
						color = ChatColor.RED;
					}
					else if (secs <= 5)
					{
						color = ChatColor.YELLOW;
					}
					else
					{
						color = ChatColor.GREEN;
					}
					
					if (secs <= 60) //Only show seconds with title
					{
						this.sendTitle(Title.builder()
								.title(color.toString() + secs)
								.fadeIn(5)
								.stay(20)
								.fadeOut(5)
								.build());
					}
					
					this.sendTranslatableMessage("minigame", "game.countdown-starting", color + TimeUtils.getHumanReadableSimplePeriod(secs));
				}
			}
		}
		
		super.onTick();
		
		if (!this.skipTimeLeft)
		{
			this.timeLeft--; //Do last
		}
	}
	
	public void voteEnded()
	{
		if (this.status == DuelsMinigameStatus.VOTE)
		{
			this.timeLeft = 0;
		}
	}
	
	@SuppressWarnings("deprecation")
	public void doRematch()
	{
		if (this.status == DuelsMinigameStatus.VOTE)
		{
			if (this.getPlayers().stream().allMatch((p) -> p.getBukkitPlayer().isOnline()))
			{
				//Kill all entities
				for(Entity entity : this.getWorld().getEntities())
				{
					if (!(entity instanceof Player))
					{
						entity.remove();
					}
				}
				
				if (this.getConfig().getDuelType().canPlaceBlocks() || this.getConfig().getDuelType().canBreakBlocks())
				{
					Chunk[] loadedChunks = this.getWorld().getLoadedChunks();
					for(Chunk chunk : loadedChunks)
					{
						chunk.unload(false, false);
						chunk.load();
						
						chunk.getWorld().refreshChunk(chunk.getX(), chunk.getZ()); //Update hacky
					}
				}
				
				for(DuelsMinigamePlayer player : this.getPlayers())
				{
					this.getConfig().getDuelType().setupPreMatch(this, player);
				}
				
				this.setStatus(DuelsMinigameStatus.REMATCH, 100); //TODO: Dont hardcore this
			}
			else
			{
				this.setStatus(DuelsMinigameStatus.ENDED, 100); //TODO: Dont hardcore this
			}
		}
	}

	@Override
	public void onCriticalException(Throwable e)
	{
		this.stats.clear(); //Avoid bad stats saving
	
		this.kick("Critical error");
	
		this.cleanup();
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
}
