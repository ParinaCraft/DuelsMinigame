package fi.joniaromaa.duelsminigame.game.dueltype;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import fi.joniaromaa.duelsminigame.player.DuelsMinigamePlayer;
import fi.joniaromaa.parinacorelibrary.bukkit.builder.ItemStackBuilder;

public class ClassicDuelType implements IDuelType
{
	private static final String NAME = "Classic";
	
	@Override
	public String getName()
	{
		return ClassicDuelType.NAME;
	}
	
	@Override
	public void setupPlayer(DuelsMinigamePlayer player)
	{
		Player bukkitPlayer = player.getBukkitPlayer();
		bukkitPlayer.setGameMode(GameMode.ADVENTURE);
		
		PlayerInventory inventory = bukkitPlayer.getInventory();
		inventory.clear();
		inventory.setHeldItemSlot(0);
		inventory.setItem(0, ItemStackBuilder.builder().type(Material.IRON_SWORD).unbrekable(true).build());
		inventory.setItem(1, ItemStackBuilder.builder().type(Material.FISHING_ROD).unbrekable(true).build());
		inventory.setItem(2, ItemStackBuilder.builder().type(Material.BOW).unbrekable(true).build());
		inventory.setItem(8, ItemStackBuilder.builder().type(Material.ARROW).amount(6).unbrekable(true).build());
		
		inventory.setBoots(ItemStackBuilder.builder().type(Material.IRON_BOOTS).unbrekable(true).build());
		inventory.setLeggings(ItemStackBuilder.builder().type(Material.IRON_LEGGINGS).unbrekable(true).build());
		inventory.setChestplate(ItemStackBuilder.builder().type(Material.IRON_CHESTPLATE).unbrekable(true).build());
		inventory.setHelmet(ItemStackBuilder.builder().type(Material.IRON_HELMET).unbrekable(true).build());
	}

	@Override
	public boolean canFreelyMoveOnPreGame()
	{
		return true;
	}

	@Override
	public boolean canDropItems()
	{
		return false;
	}

	@Override
	public boolean canPlaceBlocks()
	{
		return false;
	}

	@Override
	public boolean canBreakBlocks()
	{
		return false;
	}
}
