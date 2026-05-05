package me.xxastaspastaxx.dimensions.addons.customblocksinside;

import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Method;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddon;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddonPriority;
import me.xxastaspastaxx.dimensions.completePortal.CompletePortal;
import me.xxastaspastaxx.dimensions.customportal.CustomPortal;
import me.xxastaspastaxx.dimensions.events.CustomPortalBreakEvent;
import me.xxastaspastaxx.dimensions.events.CustomPortalIgniteEvent;
import me.xxastaspastaxx.dimensions.events.CustomPortalUseEvent;
import net.enelson.sopcustomblocks.SopCustomBlocks;
import net.enelson.sopcustomblocks.managers.blocks.BlockManager;
import net.enelson.sopcustomblocks.managers.blocks.CustomBlock;

public class DimensionsCustomBlocksInsideAddon extends DimensionsAddon implements Listener {

    private static final String HIDE_PORTAL_INSIDE_TAG = "hidePortalInside";
    private static final String OPTION_KEY = "sopcustomblocksinside.id";

    private Dimensions plugin;

    public DimensionsCustomBlocksInsideAddon() {
        super(
                "DimensionsCustomBlocksInsideAddon",
                "1.0.0",
                "Replaces Dimensions portal inside with SopCustomBlocks blocks",
                DimensionsAddonPriority.NORMAL
        );
    }

    @Override
    public boolean onLoad(Dimensions pl) {
        return pl.getServer().getPluginManager().getPlugin("SopCustomBlocks") != null;
    }

    @Override
    public void onEnable(Dimensions pl) {
        this.plugin = pl;
        pl.getServer().getPluginManager().registerEvents(this, pl);
    }

    @Override
    public void onDisable() {
        if (plugin == null || Dimensions.getCompletePortalManager() == null) {
            return;
        }

        for (CompletePortal portal : new ArrayList<CompletePortal>(Dimensions.getCompletePortalManager().getCompletePortals())) {
            cleanupPortalInside(portal);
        }
    }

    @Override
    public void registerPortal(YamlConfiguration portalConfig, CustomPortal portal) {
        String customBlockId = portalConfig.getString("Addon.SopCustomBlocksInside.BlockId");
        if (customBlockId != null) {
            customBlockId = customBlockId.trim();
        }

        if (customBlockId == null || customBlockId.isEmpty()) {
            return;
        }

        DimensionsAddon.setOption(portal, OPTION_KEY, customBlockId);
    }

    @EventHandler
    public void onPortalIgnite(CustomPortalIgniteEvent event) {
        CompletePortal portal = event.getCompletePortal();
        if (portal == null) {
            return;
        }

        String customBlockId = getCustomBlockId(portal);
        if (customBlockId == null || customBlockId.isBlank()) {
            return;
        }

        portal.setTag(HIDE_PORTAL_INSIDE_TAG, true);

        BlockManager blockManager = SopCustomBlocks.getInstance().getBlockManager();
        Player player = event.getEntity() instanceof Player ? (Player) event.getEntity() : null;
        float yaw = resolvePortalYaw(portal);

        for (Location location : collectInsideLocations(portal)) {
            CustomBlock existing = blockManager.getBlock(location);
            if (existing != null) {
                blockManager.breakBlock(existing, null);
            }
            addPortalInsideBlock(blockManager, customBlockId, location, yaw, player);
        }
    }

    @EventHandler
    public void onPortalBreak(CustomPortalBreakEvent event) {
        cleanupPortalInside(event.getCompletePortal());
    }

    @EventHandler
    public void onPortalUse(CustomPortalUseEvent event) {
        CompletePortal portal = event.getCompletePortal();
        if (portal == null) {
            return;
        }

        String customBlockId = getCustomBlockId(portal);
        if (customBlockId == null || customBlockId.isBlank()) {
            return;
        }

        BlockManager blockManager = SopCustomBlocks.getInstance().getBlockManager();
        if (blockManager == null || event.getEntity() == null) {
            return;
        }

        if (blockManager.isCustomBlockArmorStand(event.getEntity()) || blockManager.getBlock(event.getEntity()) != null) {
            event.setCancelled(true);
        }
    }

    private String getCustomBlockId(CompletePortal portal) {
        Object value = DimensionsAddon.getOption(portal, OPTION_KEY);
        if (value == null) {
            return null;
        }
        return String.valueOf(value);
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

    private float resolvePortalYaw(CompletePortal portal) {
        if (portal == null || portal.getPortalGeometry() == null) {
            return 0.0f;
        }

        return portal.getPortalGeometry().iszAxis() ? -90.0f : 0.0f;
    }

    private void addPortalInsideBlock(BlockManager blockManager, String customBlockId, Location location, float yaw, Player player) {
        try {
            Method method = blockManager.getClass().getMethod("addBlock", String.class, Location.class, float.class, float.class);
            method.invoke(blockManager, customBlockId, location, yaw, 0.0f);
            return;
        } catch (ReflectiveOperationException ignored) {
            // Fall back to the legacy API when the server still has an older SopCustomBlocks build.
        }
        blockManager.addBlock(customBlockId, location, player);
    }

    private void cleanupPortalInside(CompletePortal portal) {
        if (portal == null) {
            return;
        }

        String customBlockId = getCustomBlockId(portal);
        if (customBlockId == null || customBlockId.isBlank()) {
            return;
        }

        BlockManager blockManager = SopCustomBlocks.getInstance().getBlockManager();

        for (Location location : collectInsideLocations(portal)) {
            CustomBlock existing = blockManager.getBlock(location);
            if (existing != null && customBlockId.equalsIgnoreCase(existing.getId())) {
                blockManager.breakBlock(existing, null);
            }
        }
    }
}
