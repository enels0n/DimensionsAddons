package me.xxastaspastaxx.dimensions.addons.conditionalcommandsonuse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import me.clip.placeholderapi.PlaceholderAPI;
import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddon;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddonPriority;
import me.xxastaspastaxx.dimensions.completePortal.CompletePortal;
import me.xxastaspastaxx.dimensions.customportal.CustomPortal;
import me.xxastaspastaxx.dimensions.events.CustomPortalUseEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class DimensionsConditionalCommandsOnUseAddon extends DimensionsAddon implements Listener {
    private static final String OPTION_KEY = "conditional-commands-on-use.actions";

    private Dimensions plugin;
    private boolean placeholderApiEnabled;

    public DimensionsConditionalCommandsOnUseAddon() {
        super(
            "DimensionsConditionalCommandsOnUseAddon",
            "1.0.0",
            "Executes configurable commands when Dimensions portals are used, with advanced conditions.",
            DimensionsAddonPriority.NORMAL
        );
    }

    @Override
    public boolean onLoad(Dimensions pl) {
        return true;
    }

    @Override
    public void onEnable(Dimensions pl) {
        this.plugin = pl;
        this.placeholderApiEnabled = pl.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
        pl.getServer().getPluginManager().registerEvents(this, pl);
    }

    @Override
    public void registerPortal(YamlConfiguration portalConfig, CustomPortal portal) {
        ConfigurationSection section = portalConfig.getConfigurationSection("Addon.CommandsOnUse");
        if (section == null) {
            section = portalConfig.getConfigurationSection("Addon.ConditionalCommandsOnUse");
        }
        if (section == null) {
            return;
        }

        List<CommandAction> actions = readActions(section.getConfigurationSection("actions"));
        if (!actions.isEmpty()) {
            DimensionsAddon.setOption(portal, OPTION_KEY, actions);
        }
    }

    @EventHandler
    public void onCustomPortalUse(CustomPortalUseEvent event) {
        Object option = DimensionsAddon.getOption(event.getCompletePortal(), OPTION_KEY);
        if (!(option instanceof List)) {
            return;
        }

        @SuppressWarnings("unchecked")
        List<CommandAction> actions = (List<CommandAction>) option;
        if (actions.isEmpty()) {
            return;
        }

        CommandContext context = new CommandContext(event.getCompletePortal(), event.getDestinationPortal(), event.getEntity());
        for (CommandAction action : actions) {
            if (!testConditions(action.getConditions(), context)) {
                continue;
            }

            for (String rawCommand : action.getCommands()) {
                executeCommand(rawCommand, context);
            }
            if (action.isCancelUse()) {
                event.setCancelled(true);
            }
            return;
        }
    }

    private List<CommandAction> readActions(ConfigurationSection section) {
        if (section == null) {
            return Collections.emptyList();
        }
        List<CommandAction> actions = new ArrayList<CommandAction>();
        for (String key : section.getKeys(false)) {
            ConfigurationSection actionSection = section.getConfigurationSection(key);
            if (actionSection == null) {
                continue;
            }
            List<String> commands = actionSection.getStringList("commands");
            if (commands.isEmpty()) {
                continue;
            }
            ConditionBlock conditions = readConditionBlock(actionSection.getConfigurationSection("conditions"));
            boolean cancelUse = actionSection.getBoolean("cancel-use", false);
            actions.add(new CommandAction(conditions, commands, cancelUse));
        }
        return actions;
    }

    private ConditionBlock readConditionBlock(ConfigurationSection section) {
        if (section == null) {
            return new ConditionBlock("all", Collections.<ActionCondition>emptyList());
        }
        List<ActionCondition> checks = new ArrayList<ActionCondition>();
        ConfigurationSection checksSection = section.getConfigurationSection("checks");
        if (checksSection != null) {
            for (String key : checksSection.getKeys(false)) {
                ConfigurationSection checkSection = checksSection.getConfigurationSection(key);
                if (checkSection == null) {
                    continue;
                }
                checks.add(new ActionCondition(
                    checkSection.getString("type", "string equals"),
                    checkSection.getString("input", ""),
                    checkSection.getString("output", ""),
                    checkSection.getBoolean("negate", false)
                ));
            }
        } else {
            List<Map<?, ?>> list = section.getMapList("checks");
            for (Map<?, ?> map : list) {
                checks.add(new ActionCondition(
                    stringValue(map.get("type"), "string equals"),
                    stringValue(map.get("input"), ""),
                    stringValue(map.get("output"), ""),
                    Boolean.parseBoolean(stringValue(map.get("negate"), "false"))
                ));
            }
        }
        return new ConditionBlock(section.getString("type", "all"), checks);
    }

    private boolean testConditions(ConditionBlock block, CommandContext context) {
        if (block == null || block.getChecks().isEmpty()) {
            return true;
        }
        boolean any = "any".equalsIgnoreCase(block.getType());
        for (ActionCondition check : block.getChecks()) {
            boolean result = testCondition(check, context);
            if (any && result) {
                return true;
            }
            if (!any && !result) {
                return false;
            }
        }
        return !any;
    }

    private boolean testCondition(ActionCondition check, CommandContext context) {
        String input = resolve(check.getInput(), context);
        String output = resolve(check.getOutput(), context);
        boolean result;
        String type = check.getType().trim().toLowerCase(Locale.ROOT);
        switch (type) {
            case "string equals":
            case "equals":
                result = input.equalsIgnoreCase(output);
                break;
            case "string contains":
            case "contains":
                result = input.toLowerCase(Locale.ROOT).contains(output.toLowerCase(Locale.ROOT));
                break;
            case "string matches":
            case "matches":
                result = input.matches(output);
                break;
            case "number >":
                result = parseDouble(input) > parseDouble(output);
                break;
            case "number >=":
                result = parseDouble(input) >= parseDouble(output);
                break;
            case "number <":
                result = parseDouble(input) < parseDouble(output);
                break;
            case "number <=":
                result = parseDouble(input) <= parseDouble(output);
                break;
            case "number equals":
            case "number ==":
                result = Double.compare(parseDouble(input), parseDouble(output)) == 0;
                break;
            case "boolean equals":
                result = Boolean.parseBoolean(input) == Boolean.parseBoolean(output);
                break;
            default:
                result = false;
                break;
        }
        return check.isNegate() ? !result : result;
    }

    private void executeCommand(String rawCommand, CommandContext context) {
        String resolved = resolve(rawCommand, context);
        if (resolved.startsWith("[console] ")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), resolved.substring(10));
            return;
        }
        if (resolved.startsWith("[player] ")) {
            Player player = context.getPlayer();
            if (player != null) {
                Bukkit.dispatchCommand(player, resolved.substring(9));
            }
            return;
        }
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), resolved);
    }

    private String resolve(String raw, CommandContext context) {
        String resolved = raw;
        for (Map.Entry<String, String> entry : context.createReplacements().entrySet()) {
            resolved = resolved.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        if (this.placeholderApiEnabled && context.getPlayer() != null) {
            resolved = PlaceholderAPI.setPlaceholders(context.getPlayer(), resolved);
        }
        for (Map.Entry<String, String> entry : context.createReplacements().entrySet()) {
            resolved = resolved.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return resolved;
    }

    private double parseDouble(String input) {
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException exception) {
            return 0.0D;
        }
    }

    private String stringValue(Object value, String fallback) {
        return value == null ? fallback : String.valueOf(value);
    }
}
