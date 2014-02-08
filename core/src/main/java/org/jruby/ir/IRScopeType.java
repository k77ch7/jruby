package org.jruby.ir;

public enum IRScopeType {
    CLOSURE, EVAL_SCRIPT, INSTANCE_METHOD, CLASS_METHOD, MODULE_BODY, CLASS_BODY, METACLASS_BODY, SCRIPT_BODY;

    public static IRScopeType fromOrdinal(int value) {
        return value < 0 || value >= values().length ? null : values()[value];
    }

    public boolean isMethod() {
        return this == INSTANCE_METHOD || this == CLASS_METHOD;
    }

    public boolean isMethodContainer() {
        switch (this) {
            case MODULE_BODY:
            case CLASS_BODY:
            case METACLASS_BODY:
            case SCRIPT_BODY:
                return true;

            default:
                return false;
        }
    }
}
