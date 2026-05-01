package me.xxastaspastaxx.dimensions.addons.conditionalcommandsonuse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ConditionBlock {
    private final String type;
    private final List<ActionCondition> checks;

    public ConditionBlock(String type, List<ActionCondition> checks) {
        this.type = type == null ? "all" : type;
        this.checks = Collections.unmodifiableList(new ArrayList<ActionCondition>(checks));
    }

    public String getType() {
        return this.type;
    }

    public List<ActionCondition> getChecks() {
        return this.checks;
    }
}
