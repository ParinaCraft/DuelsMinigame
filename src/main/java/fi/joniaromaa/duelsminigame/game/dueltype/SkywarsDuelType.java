package fi.joniaromaa.duelsminigame.game.dueltype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockVector;

import fi.joniaromaa.duelsminigame.config.DuelsMinigameMapConfig;
import fi.joniaromaa.duelsminigame.config.DuelsMinigameSkywarsLoot;
import fi.joniaromaa.duelsminigame.config.DuelsMinigameSkywarsMapConfig;
import fi.joniaromaa.duelsminigame.game.DuelsMinigame;
import fi.joniaromaa.duelsminigame.player.DuelsMinigamePlayer;
import fi.joniaromaa.duelsminigame.user.dataset.UserStartLocationDataStorage;
import fi.joniaromaa.minigameframework.player.BukkitUser;

public class SkywarsDuelType implements IDuelType
{
	private static final String NAME = "Skywars";
	
	private List<DuelsMinigameSkywarsLoot> spawnIslandChestLoot;
	private List<DuelsMinigameSkywarsLoot> midIslandChestLoot;
	
	public SkywarsDuelType(ConfigurationSection config)
	{
		this.spawnIslandChestLoot = new ArrayList<>();
		this.midIslandChestLoot = new ArrayList<>();
		
		ConfigurationSection chestLoot = config.getConfigurationSection("chest-loot");
		if (chestLoot != null)
		{
			ConfigurationSection spawnIslandsChestLoot = chestLoot.getConfigurationSection("spawn-islands");
			if (spawnIslandsChestLoot != null)
			{
				for(String key : spawnIslandsChestLoot.getKeys(false))
				{
					this.spawnIslandChestLoot.add(new DuelsMinigameSkywarsLoot(spawnIslandsChestLoot.getConfigurationSection(key)));
				}
			}
			
			ConfigurationSection midChestLoot = chestLoot.getConfigurationSection("mid");
			if (midChestLoot != null)
			{
				for(String key : midChestLoot.getKeys(false))
				{
					this.midIslandChestLoot.add(new DuelsMinigameSkywarsLoot(midChestLoot.getConfigurationSection(key)));
				}
			}
		}
	}

	@Override
	public String getName()
	{
		return SkywarsDuelType.NAME;
	}

	@Override
	public void preGameSpawn(BukkitUser user)
	{
		UserStartLocationDataStorage startLocation = user.getUser().getDataStorage(UserStartLocationDataStorage.class).orElse(null);
		if (startLocation != null)
		{
			this.makeSkywarsBox(startLocation.getLocation(), Material.STAINED_GLASS);
		}
	}

	@Override
	public void preGameLeave(BukkitUser user)
	{
		UserStartLocationDataStorage startLocation = user.getUser().getDataStorage(UserStartLocationDataStorage.class).orElse(null);
		if (startLocation != null)
		{
			this.makeSkywarsBox(startLocation.getLocation(), Material.AIR);
		}
	}
	
	@Override
	public void gameStart(World world, DuelsMinigameMapConfig config)
	{
		List<ItemStack> spawnIslandItems = this.getItems(this.spawnIslandChestLoot, 15, "SWORD", "BLOCKS");
		for(List<BlockVector> chests : ((DuelsMinigameSkywarsMapConfig)config).getSpawnIslandChests())
		{
			int itemsPerChest = (int)Math.ceil(spawnIslandItems.size() / (double)chests.size());
			
			int pointer = 0;
			for(BlockVector chest : chests)
			{
				Block chestBlock = chest.toLocation(world).getBlock();
				if (chestBlock.getType() != Material.CHEST)
				{
					chestBlock.setType(Material.CHEST);
				}
				
				this.randomizeToChest((Chest)chestBlock.getState(), spawnIslandItems, pointer, Math.min(pointer + itemsPerChest, spawnIslandItems.size()));
				
				pointer += itemsPerChest;
			}
		}
		
		for(BlockVector chest : ((DuelsMinigameSkywarsMapConfig)config).getMidChests())
		{
			Block chestBlock = chest.toLocation(world).getBlock();
			if (chestBlock.getType() != Material.CHEST)
			{
				chestBlock.setType(Material.CHEST);
			}
			
			this.randomizeToChest((Chest)chestBlock.getState(), this.getItems(this.midIslandChestLoot, 5));
		}
	}

	@Override
	public void setupPlayer(DuelsMinigamePlayer player)
	{
		this.makeSkywarsBox(player.getSpawnLocation(), Material.AIR);
		
		Player bukkitPlayer = player.getBukkitPlayer();
		bukkitPlayer.setGameMode(GameMode.SURVIVAL);
		
		PlayerInventory inventory = bukkitPlayer.getInventory();
		inventory.clear();
		inventory.setArmorContents(null);
		inventory.setHeldItemSlot(0);

		((CraftPlayer)bukkitPlayer).getHandle().invulnerableTicks = 100; //5s of no damage to avoid the first fall damage
		
		bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, 4, false, false));
	}

	@Override
	public void setupPreMatch(DuelsMinigame minigame, DuelsMinigamePlayer player)
	{
		Location spawnLocation = player.getSpawnLocation();
		if (spawnLocation != null)
		{
			this.makeSkywarsBox(spawnLocation, Material.STAINED_GLASS);
			
			Player bukkitPlayer = player.getBukkitPlayer();
			
			minigame.removeSpectator(bukkitPlayer);
			
			bukkitPlayer.closeInventory();
			bukkitPlayer.teleport(spawnLocation);
			bukkitPlayer.setGameMode(GameMode.ADVENTURE);
			bukkitPlayer.getInventory().clear();
			bukkitPlayer.getInventory().setArmorContents(null);
			
			bukkitPlayer.setFallDistance(0);
		}
	}
	
	private List<ItemStack> getItems(List<DuelsMinigameSkywarsLoot> fromToPick, int amount, String... requiredTags)
	{
		HashSet<String> missingTags = new HashSet<>();
		for(String tag : requiredTags)
		{
			missingTags.add(tag);
		}
		
		List<ItemStack> items = new ArrayList<>();
		
		Random random = new Random();
		while (items.size() < amount)
		{
			Collections.shuffle(fromToPick, random); //Randomize everything before we go
			
			for(DuelsMinigameSkywarsLoot loot : fromToPick)
			{
				if (loot.getChance() > random.nextFloat() * 100)
				{
					items.add(loot.getItem().clone());
					
					missingTags.removeAll(loot.getTags());
					
					break;
				}
			}
		}
		
		while (missingTags.size() > 0) //If we have missing tags then lets do extra items
		{
			Collections.shuffle(fromToPick, random); //Randomize everything before we go
			
			for(DuelsMinigameSkywarsLoot loot : fromToPick)
			{
				if (loot.getChance() > random.nextFloat() * 100)
				{
					boolean canAdd = false;
					for(String tag : loot.getTags())
					{
						if (missingTags.contains(tag))
						{
							canAdd = true;
						}
					}
					
					if (canAdd)
					{
						items.add(loot.getItem().clone());
						
						missingTags.removeAll(loot.getTags());
						
						break;
					}
				}
			}
		}
		
		return items;
	}
	
	private void randomizeToChest(Chest chest, List<ItemStack> items)
	{
		this.randomizeToChest(chest, items, 0, items.size());
	}
	
	private void randomizeToChest(Chest chest, List<ItemStack> items, int start, int end)
	{
		Inventory inventory = chest.getBlockInventory();
		inventory.clear();
		
		Random random = new Random();
		for(int i = start; i < end; i++)
		{
			ItemStack item = items.get(i);
			
			while (true)
			{
				int slot = random.nextInt(inventory.getSize());
				
				ItemStack slotItem = inventory.getItem(slot);
				if (slotItem == null || slotItem.getType() == Material.AIR)
				{
					inventory.setItem(slot, item.clone());
					break;
				}
			}
		}
	}

	@Override
	public boolean canFreelyMoveOnPreGame()
	{
		return false;
	}

	@Override
	public boolean canDropItems()
	{
		return true;
	}

	@Override
	public boolean canPlaceBlocks()
	{
		return true;
	}

	@Override
	public boolean canBreakBlocks()
	{
		return true;
	}
	
	private void makeSkywarsBox(Location location, Material material)
	{
		Location loc = location.clone();
		loc.add(0, 3, 0).getBlock().setType(material);
		loc.subtract(0, 4, 0).getBlock().setType(material);
		
		this.makeSkywarsBoxHelper(location, material, 1, 0);
		this.makeSkywarsBoxHelper(location, material, 0, 1);
		this.makeSkywarsBoxHelper(location, material, -1, 0);
		this.makeSkywarsBoxHelper(location, material, 0, -1);
	}
	
	private void makeSkywarsBoxHelper(Location location, Material material, double addX, double addZ)
	{
		location.add(addX, 0, addZ);
		for(int i = 0; i < 3; i++)
		{
			location.add(0, i, 0).getBlock().setType(material);
			location.subtract(0, i, 0);
		}
		
		location.subtract(addX, 0, addZ);
	}
}
