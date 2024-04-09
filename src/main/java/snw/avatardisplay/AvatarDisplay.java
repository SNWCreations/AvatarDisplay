package snw.avatardisplay;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

public final class AvatarDisplay extends JavaPlugin implements Listener {
    private static final String POS_A_HOPPER = "pos_a_hopper";
    private static final String POS_B_HOPPER = "pos_b_hopper";
    private static final String POS_A_BLOCK = "pos_a_block";
    private static final String POS_B_BLOCK = "pos_b_block";
    private OperationMode operationMode;

    public enum OperationMode {
        LOUDOU_A(true, "a"),
        LOUDOU_B(true, "b"),
        FANGKUAI_A(false, "a"),
        FANGKUAI_B(false, "b");
        public final boolean hopper;
        public final String side;

        OperationMode(boolean hopper, String side) {
            this.hopper = hopper;
            this.side = side;
        }
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        operationMode = null;
        saveConfig();
    }

    private static boolean blockXYZEquals(Location a, Location b) {
        if (a != null) {
            if (b != null) {
                return a.getBlockX() == b.getBlockX() &&
                        a.getBlockY() == b.getBlockY() &&
                        a.getBlockZ() == b.getBlockZ();
            }
        }
        return false;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String a = args[0];
        operationMode = OperationMode.valueOf(a.toUpperCase());
        sender.sendMessage("GO!");
        return true;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location blockLocation = block.getLocation();
        if (operationMode != null) {
            Player player = event.getPlayer();
            if (operationMode.hopper) {
                if (block.getType() == Material.HOPPER) {
                    String key = "pos_" + operationMode.side + "_hopper";
                    getConfig().set(key, blockLocation);
                }
            } else {
                String key = "pos_" + operationMode.side + "_block";
                getConfig().set(key, blockLocation);
            }
            event.setCancelled(true);
            operationMode = null;
            player.sendMessage("OK");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPickupItem(InventoryPickupItemEvent event) {
        Inventory inventory = event.getInventory();
        Item item = event.getItem();
        if (inventory.getHolder() instanceof Hopper hopper) {
            Location location = hopper.getLocation();
            String key = item.getItemStack().getType().getKey().toString();
            String fuhuokaPrefix = "tzz:fuhuoka_";
            String avatarPrefix = "tzz:touxiang_";
            if (key.startsWith(fuhuokaPrefix)) {
                String name = key.substring(fuhuokaPrefix.length());
                Location posA = getConfig().getLocation(POS_A_HOPPER);
                Location posB = getConfig().getLocation(POS_B_HOPPER);
                String aOrB;
                Location replacePos;
                if (blockXYZEquals(location, posA)) {
                    aOrB = "a";
                    replacePos = getConfig().getLocation(POS_A_BLOCK);
                } else if (blockXYZEquals(location, posB)) {
                    aOrB = "b";
                    replacePos = getConfig().getLocation(POS_B_BLOCK);
                } else {
                    return;
                }
                if (replacePos == null) {
                    Bukkit.broadcastMessage("ERROR: position not configured");
                    return;
                }
                String finalMaterial = (avatarPrefix + name + "_" + aOrB).replace(":", "_").toUpperCase();
                Material material = Material.matchMaterial(finalMaterial);
                if (material != null) {
                    replacePos.getBlock().setType(material);
                    event.setCancelled(true);
                    event.getItem().remove();
                } else {
                    Bukkit.broadcastMessage("ERROR: material not found");
                    Bukkit.broadcastMessage("name: " + name);
                }
            }
        }
    }
}
