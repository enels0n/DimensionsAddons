package me.xxastaspastaxx.dimensions.addons.conditionalcommandsonuse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CommandAction {
    private final ConditionBlock conditions;
    private final List<String> commands;
    private final boolean cancelUse;

    public CommandAction(ConditionBlock conditions, List<String> commands, boolean cancelUse) {
        this.conditions = conditions;
        this.commands = Collections.unmodifiableList(new ArrayList<String>(commands));
        this.cancelUse = cancelUse;
    }

    public ConditionBlock getConditions() {
        return this.conditions;
    }

    public List<String> getCommands() {
        return this.commands;
    }

    public boolean isCancelUse() {
        return this.cancelUse;
    }
}
