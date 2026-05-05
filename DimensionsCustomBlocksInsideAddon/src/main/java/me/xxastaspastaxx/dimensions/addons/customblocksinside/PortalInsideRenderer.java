package me.xxastaspastaxx.dimensions.addons.customblocksinside;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import me.xxastaspastaxx.dimensions.completePortal.CompletePortal;
import me.xxastaspastaxx.dimensions.customportal.CustomPortal;
import me.xxastaspastaxx.dimensions.events.CustomPortalBreakEvent;
import me.xxastaspastaxx.dimensions.events.CustomPortalIgniteEvent;
import net.enelson.sopcustomblocks.SopCustomBlocks;
import net.enelson.sopcustomblocks.managers.blocks.BlockManager;
import net.enelson.sopcustomblocks.managers.blocks.CustomBlock;

public class PortalInsideRenderer implements Listener {

    private static final String HIDE_PORTAL_INSIDE_TAG = "hidePortalInside";
    private static final String CONFIG_PATH = "Addon.SopCustomBlocksInside.BlockId";

    @EventHandler
    public void onIgnite(CustomPortalIgniteEvent event) {
        CompletePortal portal = event.getCompletePortal();
        if (portal == null) return;

        String customBlockId = resolveCustomBlockId(portal);
        if (customBlockId == null || customBlockId.isBlank()) return;

        portal.setTag(HIDE_PORTAL_INSIDE_TAG, true);

        if (!(event.getEntity() instanceof Player player)) return;

        BlockManager blockManager = SopCustomBlocks.getInstance().getBlockManager();

        for (Location location : collectInsideLocations(portal)) {
            CustomBlock existing = blockManager.getBlock(location);
            if (existing != null) {
                blockManager.breakBlock(existing, null);
            }
            blockManager.addBlock(customBlockId, location, player);
        }
    }

    @EventHandler
    public void onBreak(CustomPortalBreakEvent event) {
        CompletePortal portal = event.getCompletePortal();
        if (portal == null) return;

        String customBlockId = resolveCustomBlockId(portal);
        if (customBlockId == null || customBlockId.isBlank()) return;

        BlockManager blockManager = SopCustomBlocks.getInstance().getBlockManager();

        for (Location location : collectInsideLocations(portal)) {
            CustomBlock existing = blockManager.getBlock(location);
            if (existing != null && customBlockId.equalsIgnoreCase(existing.getId())) {
                blockManager.breakBlock(existing, null);
            }
        }
    }

    private List<Location> collectInsideLocations(CompletePortal portal) {
        List<Location> list = new ArrayList<>();

        Vector min = portal.getPortalGeometry().getInsideMin();
        Vector max = portal.getPortalGeometry().getInsideMax();
        boolean zAxis = portal.getPortalGeometry().iszAxis();
        World world = portal.getWorld();

        for (int y = (int) min.getY(); y <= (int) max.getY(); y++) {
            if (zAxis) {
                int x = (int) min.getX();
                for (int z = (int) min.getZ(); z <= (int) max.getZ(); z++) {
                    list.add(new Location(world, x, y, z));
                }
            } else {
                int z = (int) min.getZ();
                for (int x = (int) min.getX(); x <= (int) max.getX(); x++) {
                    list.add(new Location(world, x, y, z));
                }
            }
        }

        return list;
    }

    private String resolveCustomBlockId(CompletePortal portal) {
        CustomPortal customPortal = portal.getCustomPortal();
        if (customPortal == null) return null;

        String portalId = customPortal.getPortalId();
        if (portalId == null || portalId.isBlank()) return null;

        File portalsFolder = new File("plugins/Dimensions/Portals");
        File portalFile = new File(portalsFolder, portalId + ".yml");
        if (!portalFile.exists()) return null;

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(portalFile);
        String id = yaml.getString(CONFIG_PATH);

        if (id == null) return null;
        id = id.trim();

        return id.isEmpty() ? null : id;
    }
}
