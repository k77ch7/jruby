package org.jruby.ir.instructions;

import org.jruby.RubyFixnum;
import org.jruby.RubyModule;
import org.jruby.RubySymbol;
import org.jruby.ir.IRScopeType;
import org.jruby.ir.IRVisitor;
import org.jruby.ir.Operation;
import org.jruby.ir.operands.Operand;
import org.jruby.ir.runtime.IRRuntimeHelpers;
import org.jruby.ir.transformations.inlining.InlinerInfo;
import org.jruby.runtime.Block;
import org.jruby.runtime.DynamicScope;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

import java.util.Map;

public class AliasInstr extends Instr implements FixedArityInstr {
    private Operand newName;
    private Operand oldName;
    private final IRScopeType targetScopeType;

    // SSS FIXME: Implicit self arg -- make explicit to not get screwed by inlining!
    public AliasInstr(Operand newName, Operand oldName, IRScopeType targetScopeType) {
        super(Operation.ALIAS);

        this.newName = newName;
        this.oldName = oldName;
        this.targetScopeType = targetScopeType;
    }

    @Override
    public Operand[] getOperands() {
        return new Operand[] {getNewName(), getOldName()};
    }

    @Override
    public String toString() {
        return getOperation().toString() + "(" + ", " + getNewName() + ", " + getOldName() + ", target:" + targetScopeType + ")";
    }

    @Override
    public void simplifyOperands(Map<Operand, Operand> valueMap, boolean force) {
        oldName = getOldName().getSimplifiedOperand(valueMap, force);
        newName = getNewName().getSimplifiedOperand(valueMap, force);
    }

    @Override
    public Instr cloneForInlining(InlinerInfo ii) {
        return new AliasInstr(getNewName().cloneForInlining(ii), getOldName().cloneForInlining(ii), targetScopeType);
    }

    @Override
    public Object interpret(ThreadContext context, DynamicScope currDynScope, IRubyObject self, Object[] temp, Block block) {
        if (self instanceof RubyFixnum || self instanceof RubySymbol) {
            throw context.runtime.newTypeError("no class to make alias");
        }

        String newNameString = getNewName().retrieve(context, self, currDynScope, temp).toString();
        String oldNameString = getOldName().retrieve(context, self, currDynScope, temp).toString();

        RubyModule module = IRRuntimeHelpers.findInstanceMethodContainer(context, currDynScope, self, targetScopeType);
        module.defineAlias(newNameString, oldNameString);
        module.callMethod(context, "method_added", context.runtime.newSymbol(newNameString));

        return null;
    }

    @Override
    public void visit(IRVisitor visitor) {
        visitor.AliasInstr(this);
    }

    public Operand getNewName() {
        return newName;
    }

    public Operand getOldName() {
        return oldName;
    }
}
