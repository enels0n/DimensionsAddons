package me.xxastaspastaxx.dimensions.addons.conditionalcommandsonuse;

import java.util.LinkedHashMap;
import java.util.Map;
import me.xxastaspastaxx.dimensions.completePortal.CompletePortal;
import me.xxastaspastaxx.dimensions.customportal.CustomPortal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public final class CommandContext {
    private final CompletePortal completePortal;
    private final CompletePortal destinationPortal;
    private final Entity entity;
    private final Player player;

    public CommandContext(CompletePortal completePortal, CompletePortal destinationPortal, Entity entity) {
        this.completePortal = completePortal;
        this.destinationPortal = destinationPortal;
        this.entity = entity;
        this.player = entity instanceof Player ? (Player) entity : null;
    }

    public CompletePortal getCompletePortal() {
        return this.completePortal;
    }

    public CompletePortal getDestinationPortal() {
        return this.destinationPortal;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Map<String, String> createReplacements() {
        Map<String, String> replacements = new LinkedHashMap<String, String>();
        CustomPortal sourceType = this.completePortal == null ? null : this.completePortal.getCustomPortal();
        CustomPortal destinationType = this.destinationPortal == null ? null : this.destinationPortal.getCustomPortal();

        replacements.put("entity_type", this.entity == null ? "" : this.entity.getType().name());
        replacements.put("entity_name", this.entity == null ? "" : this.entity.getName());
        replacements.put("entity_uuid", this.entity == null ? "" : this.entity.getUniqueId().toString());
        replacements.put("is_player", String.valueOf(this.player != null));
        replacements.put("player_name", this.player == null ? "" : this.player.getName());
        replacements.put("player_uuid", this.player == null ? "" : this.player.getUniqueId().toString());
        replacements.put("portal_id", sourceType == null ? "" : sourceType.getPortalId());
        replacements.put("portal_display_name", sourceType == null ? "" : sourceType.getDisplayName());
        replacements.put("portal_world", this.completePortal == null || this.completePortal.getWorld() == null ? "" : this.completePortal.getWorld().getName());
        replacements.put("destination_portal_id", destinationType == null ? "" : destinationType.getPortalId());
        replacements.put("destination_portal_display_name", destinationType == null ? "" : destinationType.getDisplayName());
        replacements.put("destination_world", this.destinationPortal == null || this.destinationPortal.getWorld() == null ? "" : this.destinationPortal.getWorld().getName());
        return replacements;
    }
}
