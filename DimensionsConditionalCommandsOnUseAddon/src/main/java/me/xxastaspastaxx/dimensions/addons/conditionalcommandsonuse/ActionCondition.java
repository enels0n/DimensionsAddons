package me.xxastaspastaxx.dimensions.addons.conditionalcommandsonuse;

public final class ActionCondition {
    private final String type;
    private final String input;
    private final String output;
    private final boolean negate;

    public ActionCondition(String type, String input, String output, boolean negate) {
        this.type = type == null ? "string equals" : type;
        this.input = input == null ? "" : input;
        this.output = output == null ? "" : output;
        this.negate = negate;
    }

    public String getType() {
        return this.type;
    }

    public String getInput() {
        return this.input;
    }

    public String getOutput() {
        return this.output;
    }

    public boolean isNegate() {
        return this.negate;
    }
}
