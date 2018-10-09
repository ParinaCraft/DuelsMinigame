package fi.joniaromaa.duelsminigame.game.dueltype;

import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fi.joniaromaa.duelsminigame.player.DuelsMinigamePlayer;
import fi.joniaromaa.parinacorelibrary.bukkit.builder.ItemStackBuilder;
import fi.joniaromaa.parinacorelibrary.bukkit.builder.PotionBuilder;
import net.minecraft.server.v1_8_R3.AttributeInstance;
import net.minecraft.server.v1_8_R3.AttributeModifier;
import net.minecraft.server.v1_8_R3.GenericAttributes;

public class ComboDuelType implements IDuelType
{
	private static final String NAME = "Combo";
	private static final AttributeModifier KB_MODIFIER = new AttributeModifier(UUID.fromString("d83a0e82-6e52-4f4a-8939-62150aa14dd5"), "Duels combo kb reduce", 0.2, 0);
	
	@Override
	public String getName()
	{
		return ComboDuelType.NAME;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void setupPlayer(DuelsMinigamePlayer player)
	{
		Player bukkitPlayer = player.getBukkitPlayer();
		bukkitPlayer.setGameMode(GameMode.ADVENTURE);
		
		PlayerInventory inventory = bukkitPlayer.getInventory();
		inventory.clear();
		inventory.setHeldItemSlot(0);
		inventory.setItem(0, ItemStackBuilder.builder().type(Material.DIAMOND_SWORD).addEnchantment(Enchantment.DAMAGE_ALL, 1).unbrekable(true).build());
		inventory.setItem(1, ItemStackBuilder.builder().type(Material.GOLDEN_APPLE).data((byte)1).amount(64).build());
		inventory.setItem(2, PotionBuilder.builder().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 60 * 3, 1, true, true)).amount(3).build()); //Speed II, 3mins
		
		//Extra set
		inventory.setItem(5, ItemStackBuilder.builder().type(Material.DIAMOND_BOOTS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 5).addEnchantment(Enchantment.DURABILITY, 5).build());
		inventory.setItem(6, ItemStackBuilder.builder().type(Material.DIAMOND_LEGGINGS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 5).addEnchantment(Enchantment.DURABILITY, 5).build());
		inventory.setItem(7, ItemStackBuilder.builder().type(Material.DIAMOND_CHESTPLATE).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 5).addEnchantment(Enchantment.DURABILITY, 5).build());
		inventory.setItem(8, ItemStackBuilder.builder().type(Material.DIAMOND_HELMET).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 5).addEnchantment(Enchantment.DURABILITY, 5).build());
		
		inventory.setBoots(ItemStackBuilder.builder().type(Material.DIAMOND_BOOTS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 5).addEnchantment(Enchantment.DURABILITY, 5).build());
		inventory.setLeggings(ItemStackBuilder.builder().type(Material.DIAMOND_LEGGINGS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 5).addEnchantment(Enchantment.DURABILITY, 5).build());
		inventory.setChestplate(ItemStackBuilder.builder().type(Material.DIAMOND_CHESTPLATE).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 5).addEnchantment(Enchantment.DURABILITY, 5).build());
		inventory.setHelmet(ItemStackBuilder.builder().type(Material.DIAMOND_HELMET).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 5).addEnchantment(Enchantment.DURABILITY, 5).build());
		
		AttributeInstance knockbackResistanceAttribute = ((CraftPlayer)bukkitPlayer).getHandle().getAttributeInstance(GenericAttributes.c);
		if (!knockbackResistanceAttribute.a(ComboDuelType.KB_MODIFIER)) //Does it have this modifier
		{
			knockbackResistanceAttribute.b(ComboDuelType.KB_MODIFIER); //Add the modifier
		}
		
		((CraftPlayer)bukkitPlayer).getHandle().maxNoDamageTicks = 0;
	}

	@Override
	public void cleanPlayer(Player player)
	{
		AttributeInstance knockbackResistanceAttribute = ((CraftPlayer)player).getHandle().getAttributeInstance(GenericAttributes.c);
		if (knockbackResistanceAttribute.a(ComboDuelType.KB_MODIFIER)) //Does it have this modifier
		{
			knockbackResistanceAttribute.c(ComboDuelType.KB_MODIFIER); //Add the modifier
		}
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
