package me.Andre.MobHunt;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class GameManager {
    Main plugin;

    public GameManager(Main plugin){
        this.plugin = plugin;
    }

    public void increment(Map<String, Integer> map, String player, Integer addition){
        if(map.containsKey(player)){
            map.replace(player, map.get(player) + addition);
        }
        else {
            map.put(player, addition);
        }
    }

    public void countDown(){
        plugin.scheduler.scheduleSyncRepeatingTask(plugin, new Runnable() {
            int count = 10;
            @Override
            public void run() {
                plugin.getServer().broadcastMessage(ChatColor.RED + "time left: " + count + " seconds");
                count--;
                if(count < 0){
                    stopGame();
                }
            }
        }, 0L, 20L);
    }

    private void stopGame(){
        plugin.scheduler.cancelTasks(plugin);

        // remove floating items
        Map<String, Integer> sorted = plugin.hashMapHelper.sortByValue(plugin.points, true);
        plugin.getServer().broadcastMessage("" + ChatColor.GOLD + ChatColor.BOLD + "Final Result!");

        // leaderboard text color
        int i = 1;
        String[] col = {ChatColor.GOLD + "" + ChatColor.GOLD,
                ChatColor.GOLD + "",
                ChatColor.YELLOW + ""};

        // sort and display final result
        for (Map.Entry<String, Integer> point : sorted.entrySet()){
            plugin.getServer().broadcastMessage(((i > col.length) ? "": col[i -1]) + i + ". " + point.getKey() + ": " + point.getValue());
            i ++;
        }

        // reset compass target
        for(Map.Entry<Player, Location> oct: plugin.originalCompassTarget.entrySet()){
            oct.getKey().setCompassTarget(oct.getValue());
        }

        plugin.scoreboard.remove();

        plugin.delay = 0L;

        clearAll();
    }

    public void reset(){
        plugin.scheduler.cancelTasks(plugin);

        clearAll();

        plugin.scoreboard.remove();

        plugin.delay = 0L;
    }

    private void clearAll(){
        plugin.items.clear();
        plugin.basketLocs.clear();
        plugin.collectedType.clear();
        plugin.locsInBasket.clear();
        plugin.items.clear();
        plugin.tasks.clear();
        plugin.points.clear();
        plugin.originalCompassTarget.clear();

        for(Block b: plugin.bedrocks){
            b.setType(Material.AIR);
        }

        for(Item item: plugin.items){
            item.remove();
        }
        plugin.bedrocks.clear();
    }

    public int getPointIncrement(Player player, EntityType et){

        // If the player collected a 'NEW' type of mobs, they'll get 5 points, otherwise they'll get 1 point
        List<EntityType> et1 = new ArrayList<>();
        et1.add(et);
        if(plugin.collectedType.get(player) != null) {
            if(!plugin.collectedType.get(player).contains(et)){
                if(plugin.collectedType.containsKey(player)){
                    List<EntityType> ct = plugin.collectedType.get(player);
                    ct.add(et);
                    plugin.collectedType.replace(player, ct);
                }else{
                    plugin.collectedType.put(player, et1);
                }
                return 5;
            }else {
                return 1;
            }
        }else{
            plugin.collectedType.put(player, et1);
            return 5;
        }
    }
}
