package me.Andre.MobHunt;

import me.Andre.API.ConfigManager;
import me.Andre.API.HashMapHelper;
import me.Andre.API.InventoryHelper;
import me.Andre.API.ScoreboardManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import java.util.*;

@SuppressWarnings({"unused", "ConstantConditions"})
public class Main extends JavaPlugin implements Listener {

    public GameManager gameManager;
    public InventoryHelper invh;
    public ConfigManager settings;
    public HashMapHelper hashMapHelper;
    public ScoreboardManager scoreboard;
    public long delay;
    public Map<Player, List<EntityType>> collectedType;
    public Map<String, Integer> points;
    public Map<Player, Integer> tasks;
    public Map<Player, List<Location>> locsInBasket;
    public Map<Player, Location> basketLocs;
    public BukkitScheduler scheduler;
    public List<Item> items;
    public Map<Player, Location> originalCompassTarget;






    public void msgPoints(){
        for(Map.Entry<String, Integer> point: points.entrySet()){
            getServer().broadcastMessage(point.getKey() + ": " + point.getValue());
        }
    }





    public void giveSpawnBasketToAllOnLinePlayers(){
        for(Player p: getServer().getOnlinePlayers()){
            p.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "How to Play: ");
            p.sendMessage(ChatColor.BOLD + "Left click" + "" + "" +  ChatColor.WHITE +  " on the ground to Spawn your basket!");
            p.sendMessage("Collect mobs to your hole as many as you can!");
            p.sendMessage("Every time you collect a NEW type of mob you'll get 5 points, Otherwise you'll only get 1 point");
            Location loc = p.getLocation().clone().add(0, 1, 0);
            invh.giveItem(p, ItemHandler.getSpawnBasket());
        }
    }

    private void initialize(){
        gameManager = new GameManager(this);
        originalCompassTarget = new HashMap<>();
        invh = new InventoryHelper();
        scoreboard = new ScoreboardManager("mobHunt", ChatColor.GOLD + "" + ChatColor.BOLD + "Mob Hunt");
        settings = new ConfigManager(this, "settings.yml");
        basketLocs = new HashMap<>();
        collectedType = new HashMap<>();
        points = new HashMap<>();
        locsInBasket = new HashMap<>();
        tasks = new HashMap<>();
        items = new ArrayList<>();
        scheduler = getServer().getScheduler();
        hashMapHelper = new HashMapHelper();
    }





    @Override
    public void onEnable() {
        initialize();
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        gameManager.reset();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("MHDisable")){
            gameManager.reset();
            return true;
        }
        else if(command.getName().equalsIgnoreCase("MHPoints")){
            msgPoints();
            return true;
        }
        else if(command.getName().equalsIgnoreCase("MHStart")){
            if(args.length != 1){
                sender.sendMessage(ChatColor.RED + "usage: /MHStart <5Minutes/10Minutes/20Minutes/30Minutes/45Minutes/1Hour/1Hour30Minutes/2Hour>");
                return false;
            }

            long aMinute = 20*60;

            switch (args[0]) {
                case "5Minutes":
                    delay = aMinute * 5L;
                    break;
                case "10Minutes":
                    delay = aMinute * 10L;
                    break;
                case "20Minutes":
                    delay = aMinute * 20L;
                    break;
                case "30Minutes":
                    delay = aMinute * 30L;
                    break;
                case "45Minutes":
                    delay = aMinute * 45L;
                    break;
                case "1Hour":
                    delay = aMinute * 60L;
                    break;
                case "1Hour30Minutes":
                    delay = aMinute * 90L;
                    break;
                case "2Hours":
                    delay = aMinute * 120L;
                    break;
                default:
                    return false;
            }

            for(Player player: getServer().getOnlinePlayers()){
                scoreboard.create(player);
                scoreboard.put(player.getName(), 0);
            }

            giveSpawnBasketToAllOnLinePlayers();
            for(Player p: getServer().getOnlinePlayers()){
                PlayerInventory inventory = p.getInventory();
                if(!inventory.contains(Material.COMPASS)){
                    invh.giveItem(p, new ItemStack(Material.COMPASS));
                }
            }
            getServer().broadcastMessage("Mob Hunt started for " + (delay/20/60) + " Minutes!");

            return true;
        }else if(command.getName().equalsIgnoreCase("MHSetBasketSize")){
            if(args.length != 1){
                sender.sendMessage("use: /MHSetBasketSize <number>");
                return false;
            }

            try {
                int size = Integer.parseInt(args[0]);
                settings.getConfig().set("basketSize", size);
                settings.saveConfig();
                return true;
            }catch (Exception err){
                sender.sendMessage(ChatColor.RED + "Please input a number.");
                return false;
            }
        }
        return false;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        List<String> arguments = new ArrayList<>();

        if(command.getName().equalsIgnoreCase("MHStart")){
            arguments.add("5Minutes");
            arguments.add("10Minutes");
            arguments.add("20Minutes");
            arguments.add("30Minutes");
            arguments.add("45Minutes");
            arguments.add("1Hour");
            arguments.add("1Hour30Minutes");
            arguments.add("2Hours");
            List<String> result = new ArrayList<>();
            if(args.length == 1){
                for(String a: arguments){
                    if(a.toLowerCase().startsWith(args[0].toLowerCase())){
                        result.add(a);
                    }
                }
                return result;
            }
        }
        return null;
    }

    @SuppressWarnings("ConstantConditions")
    @EventHandler
    public void onClick(PlayerInteractEvent event){

        if(event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {

            if (ItemHandler.isHolding(ItemHandler.getSpawnBasket(), event.getPlayer())) {
                Location loc = event.getClickedBlock().getLocation();

                while (!loc.getBlock().getType().isSolid()){
                    loc.subtract(0, 1,0);
                }

                event.setCancelled(true);
                spawnBasket(loc, Material.BEDROCK, settings.getConfig().getInt("basketSize"), event.getPlayer(), delay);
                event.getPlayer().getInventory().remove(ItemHandler.getSpawnBasket());

            }
        }
        else if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){

            if(ItemHandler.isHolding(ItemHandler.getSpawnBasket(), event.getPlayer()))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPickUpItems(EntityPickupItemEvent event){
        if(event.getEntity() instanceof Player){

            for(Item i: items){

                if(event.getItem().getItemStack().isSimilar(i.getItemStack()))
                    event.setCancelled(true);
            }
        }
    }

    public void spawnBasket(Location loc, Material mat, int distance, final Player player, long delay) {

        if (basketLocs.containsKey(player)) {
            basketLocs.replace(player, loc);
        } else {
            basketLocs.put(player, loc);
        }

        Location centerXp = loc.clone().add(distance, -distance, 0.0D);
        Location centerXn = loc.clone().add(-distance, -distance, 0.0D);
        Location centerYn = loc.clone().add(0.0D, -distance, 0.0D);
        Location centerZp = loc.clone().add(0.0D, -distance, distance);
        Location centerZn = loc.clone().add(0.0D, -distance, -distance);

        List<Location> locs1 = new ArrayList<>();

        for (double i = centerXn.getX() + 1; i < centerXp.getX(); i+=1) {
            for (double j = centerYn.getY() + 1; j <= loc.getY(); j+=1) {
                for (double k = centerZn.getZ() + 1; k < centerZp.getZ(); k+=1) {
                    Location loc1 = new Location(loc.getWorld(), i, j, k);
                    Block b = loc1.getBlock();
                    b.setType(Material.AIR);
                    locs1.add(b.getLocation());
                }
            }
        }

        locsInBasket.put(player, locs1);

        double horizon;
        for (horizon = centerXn.getZ() - distance; horizon < centerXn.getZ() + distance; horizon+=1) {
            double vertical;
            for (vertical = centerYn.getY(); vertical <= loc.getY(); vertical+=1) {
                Location curr = new Location(loc.getWorld(), centerXn.getX(), vertical, horizon);
                curr.getBlock().setType(mat);
            }
        }
        for (horizon = centerXp.getZ() + distance; horizon > centerXp.getZ() - distance; horizon-=1) {
            double vertical;
            for (vertical = centerYn.getY(); vertical <= loc.getY(); vertical+=1) {
                Location curr = new Location(loc.getWorld(), centerXp.getX(), vertical, horizon);
                curr.getBlock().setType(mat);
            }
        }
        for (horizon = centerZn.getX() + distance; horizon > centerZn.getX() - distance; horizon-=1) {
            double vertical;
            for (vertical = centerYn.getY(); vertical <= loc.getY(); vertical+=1) {
                Location curr = new Location(loc.getWorld(), horizon, vertical, centerZn.getZ());
                curr.getBlock().setType(mat);
            }
        }
        for (horizon = centerZp.getX() - distance; horizon < centerZp.getX() + distance; horizon+=1) {
            double vertical;
            for (vertical = centerYn.getY(); vertical <= loc.getY(); vertical+=1) {
                Location curr = new Location(loc.getWorld(), horizon, vertical, centerZp.getZ());
                curr.getBlock().setType(mat);
            }
        }
        double x;
        for (x = centerYn.getX() - distance; x <= centerYn.getX() + distance; x+=1) {
            double z;
            for (z = centerYn.getZ() - distance; z <= centerYn.getZ() + distance; z+=1) {
                Location curr = new Location(loc.getWorld(), x, centerYn.getY(), z);
                curr.getBlock().setType(mat);
            }
        }

        // LADDER
        for(double y = centerYn.getY() + 1; y <= loc.getY(); y++){
            Location loc2 = new Location(loc.getWorld(), loc.getX(), y, centerZp.getZ() - (distance - 1));
            loc2.getBlock().setType(Material.LADDER);
        }

        ItemStack is = ItemHandler.getRandomDiamondItems();
        final Item item = player.getWorld().dropItemNaturally(loc.clone().add(0.0D, 2.0D, 0.0D), is);
        items.add(item);

        // remember player's original compass target
        originalCompassTarget.put(player, player.getCompassTarget());

        // set compass target to player's basket
        player.setCompassTarget(basketLocs.get(player));

        hashMapHelper.putOrReplace(tasks, player, scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            int c = 0;

            public void run() {
                if (c > 2) {
                    if (player.getLocation().distance(basketLocs.get(player)) < 10.0D) {
                        List<Entity> nearByE = player.getNearbyEntities(5.0D, 5.0D, 5.0D);
                        for (Entity e : nearByE) {
                            if (!e.getType().name().contains("DROPPED")
                                    && !(e instanceof Player)
                                    && !e.getType().name().contains("BOAT")
                                    && !e.getType().name().contains("MINECART")) {
                                Location eLoc = e.getLocation().clone();
                                eLoc.setX(eLoc.getBlock().getX());
                                eLoc.setY(eLoc.getBlock().getY());
                                eLoc.setZ(eLoc.getBlock().getZ());
                                for (Location loc : locsInBasket.get(player)) {
                                    if (loc.getX() == eLoc.getX() && loc.getY() == eLoc.getY() && loc.getZ() == eLoc.getZ()) {
                                        int increment = gameManager.getPointIncrement(player, e.getType());
                                        e.remove();
                                        gameManager.increment(points, player.getName(), increment);
                                        getServer().broadcastMessage("" + ChatColor.GREEN + player.getName() +
                                                " collected a "  + ((increment == 5) ? ("" + ChatColor.BOLD + "new mob") : "mob") +
                                                ". + " + increment +  " point" + ((increment > 1) ? "s": ""));
                                        scoreboard.put(player.getName(), points.get(player.getName()));
                                    }
                                }
                            }
                        }
                    }
                }
                c++;
                item.setGravity(false);
                item.setVelocity(new Vector(0, 0, 0));
            }
        }, 0L, 20L), false);
        scheduler.scheduleSyncDelayedTask(this, gameManager::countDown, delay);
    }




}
