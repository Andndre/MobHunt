package me.Andre.MobHunt;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Random;

public class ItemHandler {
    private static final Random random = new Random();


    public static boolean isHolding(ItemStack itemStack, Player player){
        PlayerInventory inventory = player.getInventory();
        if(inventory.getItemInMainHand().isSimilar(itemStack)){
            return true;
        }
        return inventory.getItemInOffHand().isSimilar(itemStack);
    }

    public static ItemStack getRandomDiamondItems(){
        Material mat = Material.values()[random.nextInt(Material.values().length)];

        while (!mat.name().contains("DIAMOND")){
            mat = Material.values()[random.nextInt(Material.values().length)];
        }

        ItemStack result = new ItemStack(mat, 1);
        ItemMeta meta = result.getItemMeta();
        meta.addEnchant(Enchantment.LUCK, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        result.setItemMeta(meta);

        return result;
    }

    public static ItemStack getSpawnBasket(){
        ItemStack spawnBasket;
        spawnBasket = new ItemStack(Material.DIAMOND_SHOVEL, 1);
        ItemMeta meta  = spawnBasket.getItemMeta();
        meta.setDisplayName("Basket Spawner");
        meta.setLore(Arrays.asList(ChatColor.GREEN + "Left Click on ground Spawn your Basket", "You can only use this once"));
        meta.addEnchant(Enchantment.LUCK, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        spawnBasket.setItemMeta(meta);

        return spawnBasket;
    }
}
