package dev.jnic.analysis;

import static org.objectweb.asm.Opcodes.*;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Interpreter;
import org.objectweb.asm.tree.analysis.Value;

public class AliasFrame<V extends Value> extends Frame<V> {
   private final AliasSet[] aliases = new AliasSet[this.getLocals() + this.getMaxStackSize()];

   public AliasFrame(int localCount, int maxStackSize) {
      super(localCount, maxStackSize);
   }

   public AliasFrame(Frame<? extends V> frame) {
      this(frame.getLocals(), frame.getMaxStackSize());
      this.init(frame);
   }

   V peek(int index) {
      int stackSize = this.getStackSize();
      return this.getStack(stackSize - index - 1);
   }

   int topSlot() {
      return this.getLocals() + this.getStackSize() - 1;
   }

   V getSlot(int index) {
      if (this.getLocals() > index) {
         return this.getLocal(index);
      }

      int localCount = this.getLocals();
      return this.getStack(index - localCount);
   }

   void setSlot(int index, V value) {
      if (this.getLocals() > index) {
         this.setLocal(index, value);
      } else {
         int localCount = this.getLocals();
         this.setStack(index - localCount, value);
      }
   }

   public AliasSet aliasSet(int index) {
      if (this.aliases[index] != null) {
         return this.aliases[index];
      }

      AliasSet aliasGroup = AliasSet.singleton(index);
      this.aliases[index] = aliasGroup;
      return aliasGroup;
   }

   private void copyAlias(int index, int index2) {
      this.clearAlias(index);
      AliasSet aliasGroup = this.aliasSet(index2);
      aliasGroup.add(index);
      this.aliases[index] = aliasGroup;
   }

   private void clearAlias(int index) {
      if (this.aliases[index] != null) {
         this.aliases[index].remove(index);
         this.aliases[index] = null;
      }
   }

   private void replaceAlias(int index, AliasSet aliasSet) {
      this.clearAlias(index);
      this.aliases[index] = aliasSet;
   }

   @Override
   public void execute(AbstractInsnNode instruction, Interpreter<V> interpreter) throws AnalyzerException {
      int previousTop = this.topSlot();
      super.execute(instruction, interpreter);
      int currentTop = this.topSlot();
      this.updateAliases(instruction, previousTop, currentTop);
      for (int slot = currentTop + 1; slot <= previousTop; slot++) {
         this.clearAlias(slot);
      }
   }

   private void updateAliases(AbstractInsnNode instruction, int previousTop, int currentTop) {
      switch (instruction.getOpcode()) {
         case ILOAD:
         case LLOAD:
         case FLOAD:
         case DLOAD:
         case ALOAD:
            this.copyAlias(currentTop, ((VarInsnNode)instruction).var);
            return;
         default:
            if (currentTop <= previousTop) {
               this.clearAlias(currentTop);
            }
            return;
         case ISTORE:
         case LSTORE:
         case FSTORE:
         case DSTORE:
         case ASTORE:
            int localIndex = ((VarInsnNode)instruction).var;
            this.copyAlias(localIndex, previousTop);
            if (this.getLocal(localIndex).getSize() == 2) {
               this.clearAlias(localIndex + 1);
            }

            if (localIndex > 0) {
               Value localValue = this.getLocal(localIndex - 1);
               if (localValue != null && localValue.getSize() == 2) {
                  this.clearAlias(localIndex - 1);
               }
            }
         case IASTORE:
         case LASTORE:
         case FASTORE:
         case DASTORE:
         case AASTORE:
         case BASTORE:
         case CASTORE:
         case SASTORE:
         case POP:
         case POP2:
         case IFEQ:
         case IFNE:
         case IFLT:
         case IFGE:
         case IFGT:
         case IFLE:
         case IF_ICMPEQ:
         case IF_ICMPNE:
         case IF_ICMPLT:
         case IF_ICMPGE:
         case IF_ICMPGT:
         case IF_ICMPLE:
         case IF_ACMPEQ:
         case IF_ACMPNE:
         case GOTO:
         case RET:
         case TABLESWITCH:
         case LOOKUPSWITCH:
         case IRETURN:
         case LRETURN:
         case FRETURN:
         case DRETURN:
         case ARETURN:
         case RETURN:
         case PUTSTATIC:
         case PUTFIELD:
         case ATHROW:
         case MONITORENTER:
         case MONITOREXIT:
         case IFNULL:
         case IFNONNULL:
            return;
         case DUP2:
            if (this.peek(0).getSize() != 2) {
               this.copyAlias(currentTop - 1, currentTop - 3);
               this.copyAlias(currentTop, currentTop - 2);
               return;
            }
         case DUP:
            this.copyAlias(currentTop, currentTop - 1);
            return;
         case DUP2_X1:
            if (this.peek(0).getSize() != 2) {
               this.copyAlias(currentTop, currentTop - 2);
               this.copyAlias(currentTop - 1, currentTop - 3);
               this.copyAlias(currentTop - 2, currentTop - 4);
               this.copyAlias(currentTop - 4, currentTop);
               this.copyAlias(currentTop - 5, currentTop - 1);
               return;
            }
         case DUP_X1:
            this.copyAlias(currentTop, currentTop - 1);
            this.copyAlias(currentTop - 1, currentTop - 2);
            this.copyAlias(currentTop - 2, currentTop);
            return;
         case DUP2_X2:
            if (this.peek(0).getSize() != 2) {
               this.copyAlias(currentTop, currentTop - 2);
               this.copyAlias(currentTop - 1, currentTop - 3);
               this.copyAlias(currentTop - 2, currentTop - 4);
               if (this.peek(2).getSize() == 2) {
                  this.copyAlias(currentTop - 4, currentTop);
                  this.copyAlias(currentTop - 5, currentTop - 1);
               } else {
                  this.copyAlias(currentTop - 3, currentTop - 5);
                  this.copyAlias(currentTop - 4, currentTop);
                  this.copyAlias(currentTop - 5, currentTop - 1);
               }
               return;
            }
         case DUP_X2:
            this.copyAlias(currentTop, currentTop - 1);
            this.copyAlias(currentTop - 1, currentTop - 2);
            if (this.peek(1).getSize() == 2) {
               this.copyAlias(currentTop - 2, currentTop);
            } else {
               this.copyAlias(currentTop - 2, currentTop - 3);
               this.copyAlias(currentTop - 3, currentTop);
            }
            return;
         case SWAP:
            AliasSet aliasSet = this.aliases[currentTop - 1];
            AliasSet aliasSet2 = this.aliases[currentTop];
            if (aliasSet != null) {
               aliasSet.remove(currentTop - 1);
               aliasSet.add(currentTop);
            }

            if (aliasSet2 != null) {
               aliasSet2.remove(currentTop);
               aliasSet2.add(currentTop - 1);
            }

            this.aliases[currentTop - 1] = aliasSet2;
            this.aliases[currentTop] = aliasSet;
            return;
         case IINC:
            this.clearAlias(((IincInsnNode)instruction).var);
            return;
         case INVOKEVIRTUAL:
         case INVOKESPECIAL:
         case INVOKESTATIC:
         case INVOKEINTERFACE:
            if (Type.getReturnType(((MethodInsnNode)instruction).desc).getSort() == Type.VOID || currentTop > previousTop) {
               return;
            }
            break;
         case INVOKEDYNAMIC:
            if (Type.getReturnType(((InvokeDynamicInsnNode)instruction).desc).getSort() == Type.VOID || currentTop > previousTop) {
               return;
            }
      }

      this.clearAlias(currentTop);
   }

   @Override
   public void clearStack() {
      int localCount = this.getLocals();
      int stackSize = this.getStackSize();

      for (int localCount2 = localCount; localCount2 < stackSize + localCount; localCount2++) {
         this.clearAlias(localCount2);
      }

      super.clearStack();
   }

   @Override
   public boolean merge(Frame<? extends V> frame, Interpreter<V> interpreter) throws AnalyzerException {
      boolean changed = super.merge(frame, interpreter);
      if (frame != null && !(frame instanceof AliasFrame)) {
         throw new ClassCastException("");
      }

      AliasFrame<?> other = (AliasFrame<?>)frame;
      int size = this.getLocals() + this.getStackSize();
      BitSet visited = new BitSet(size);

      for (int index = 0; index < size; index++) {
         if (!visited.get(index)) {
            AliasSet current = this.aliases[index];
            AliasSet incoming = other.aliases[index];
            if (current != null) {
               if (incoming == null) {
                  if (current.size() > 1) {
                     this.clearAlias(index);
                     changed = true;
                  }
               } else {
                  AliasSet difference = new AliasSet();

                  for (int member = current.nextSetBit(0); member >= 0; member = current.nextSetBit(member + 1)) {
                     if (incoming.contains(member)) {
                        visited.set(member);
                     } else {
                        difference.add(member);
                        this.replaceAlias(member, difference);
                        changed = true;
                     }
                  }
               }
            }
         }
      }

      return changed;
   }

   @Override
   public Frame<V> init(Frame<? extends V> frame) {
      super.init(frame);
      if (frame != null && !(frame instanceof AliasFrame)) {
         throw new ClassCastException("");
      }

      System.arraycopy(((AliasFrame<?>)frame).aliases, 0, this.aliases, 0, this.aliases.length);
      Map<AliasSet, AliasSet> copies = null;

      for (int index = 0; index < this.aliases.length; index++) {
         AliasSet group = this.aliases[index];
         if (group != null) {
            AliasSet copy = copies == null ? null : copies.get(group);
            if (copy == null && group.size() > 1) {
               if (copies == null) {
                  copies = new HashMap<>();
               }
               copy = group.copy();
               copies.put(group, copy);
            }
            this.aliases[index] = copy;
         }
      }

      return this;
   }
}
