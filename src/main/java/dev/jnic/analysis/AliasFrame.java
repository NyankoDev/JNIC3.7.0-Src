package dev.jnic.analysis;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
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

   public AliasFrame(int index, int index2) {
      super(index, index2);
   }

   public AliasFrame(Frame<? extends V> frame) {
      this(frame.getLocals(), frame.getMaxStackSize());
      this.init(frame);
   }

   V peek(int index) {
      int stackSize = this.getStackSize();
      return this.getStack(stackSize + ~index);
   }

   int topSlot() {
      return this.getLocals() + this.getStackSize() - 1;
   }

   Value getSlot(int index) {
      if (this.getLocals() > index) {
         return this.getLocal(index);
      }

      int localCount = this.getLocals();
      return this.getStack(index - localCount);
   }

   void setSlot(int index, Value value) {
      if (this.getLocals() > index) {
         this.setLocal(index, (V)value);
      } else {
         int localCount = this.getLocals();
         this.setStack(index - localCount, (V)value);
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
      if (this.aliases[index] != null) {
         this.aliases[index].remove(index);
         this.aliases[index] = aliasSet;
      } else {
         this.aliases[index] = aliasSet;
      }
   }

   @Override
   public void execute(AbstractInsnNode instruction, Interpreter interpreter) throws AnalyzerException {
      int topSlot;
      int topSlot2;
      label77: {
         topSlot = this.topSlot();
         super.execute(instruction, interpreter);
         topSlot2 = this.topSlot();
         switch (instruction.getOpcode()) {
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
               this.copyAlias(topSlot2, ((VarInsnNode)instruction).var);
               break label77;
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
            case 31:
            case 32:
            case 33:
            case 34:
            case 35:
            case 36:
            case 37:
            case 38:
            case 39:
            case 40:
            case 41:
            case 42:
            case 43:
            case 44:
            case 45:
            case 46:
            case 47:
            case 48:
            case 49:
            case 50:
            case 51:
            case 52:
            case 53:
            case 59:
            case 60:
            case 61:
            case 62:
            case 63:
            case 64:
            case 65:
            case 66:
            case 67:
            case 68:
            case 69:
            case 70:
            case 71:
            case 72:
            case 73:
            case 74:
            case 75:
            case 76:
            case 77:
            case 78:
            case 96:
            case 97:
            case 98:
            case 99:
            case 100:
            case 101:
            case 102:
            case 103:
            case 104:
            case 105:
            case 106:
            case 107:
            case 108:
            case 109:
            case 110:
            case 111:
            case 112:
            case 113:
            case 114:
            case 115:
            case 116:
            case 117:
            case 118:
            case 119:
            case 120:
            case 121:
            case 122:
            case 123:
            case 124:
            case 125:
            case 126:
            case 127:
            case 128:
            case 129:
            case 130:
            case 131:
            case 133:
            case 134:
            case 135:
            case 136:
            case 137:
            case 138:
            case 139:
            case 140:
            case 141:
            case 142:
            case 143:
            case 144:
            case 145:
            case 146:
            case 147:
            case 148:
            case 149:
            case 150:
            case 151:
            case 152:
            case 168:
            case 178:
            case 180:
            case 187:
            case 188:
            case 189:
            case 190:
            case 192:
            case 193:
            case 196:
            case 197:
            default:
               if (topSlot2 <= topSlot) {
                  this.clearAlias(topSlot2);
               }
               break label77;
            case 54:
            case 55:
            case 56:
            case 57:
            case 58:
               int localIndex = ((VarInsnNode)instruction).var;
               this.copyAlias(localIndex, topSlot);
               if (this.getLocal(localIndex).getSize() == 2) {
                  this.clearAlias(localIndex + 1);
               }

               if (localIndex > 0) {
                  Value localValue = this.getLocal(localIndex - 1);
                  if (localValue != null && localValue.getSize() == 2) {
                     this.clearAlias(localIndex - 1);
                  }
               }
            case 79:
            case 80:
            case 81:
            case 82:
            case 83:
            case 84:
            case 85:
            case 86:
            case 87:
            case 88:
            case 153:
            case 154:
            case 155:
            case 156:
            case 157:
            case 158:
            case 159:
            case 160:
            case 161:
            case 162:
            case 163:
            case 164:
            case 165:
            case 166:
            case 167:
            case 169:
            case 170:
            case 171:
            case 172:
            case 173:
            case 174:
            case 175:
            case 176:
            case 177:
            case 179:
            case 181:
            case 191:
            case 194:
            case 195:
            case 198:
            case 199:
               break label77;
            case 92:
               if (this.peek(0).getSize() != 2) {
                  this.copyAlias(topSlot2 - 1, topSlot2 - 3);
                  this.copyAlias(topSlot2, topSlot2 - 2);
                  break label77;
               }
            case 89:
               this.copyAlias(topSlot2, topSlot2 - 1);
               break label77;
            case 93:
               if (this.peek(0).getSize() != 2) {
                  this.copyAlias(topSlot2, topSlot2 - 2);
                  this.copyAlias(topSlot2 - 1, topSlot2 - 3);
                  this.copyAlias(topSlot2 - 2, topSlot2 - 4);
                  this.copyAlias(topSlot2 - 4, topSlot2);
                  this.copyAlias(topSlot2 - 5, topSlot2 - 1);
                  break label77;
               }
            case 90:
               this.copyAlias(topSlot2, topSlot2 - 1);
               this.copyAlias(topSlot2 - 1, topSlot2 - 2);
               this.copyAlias(topSlot2 - 2, topSlot2);
               break label77;
            case 94:
               if (this.peek(0).getSize() != 2) {
                  this.copyAlias(topSlot2, topSlot2 - 2);
                  this.copyAlias(topSlot2 - 1, topSlot2 - 3);
                  this.copyAlias(topSlot2 - 2, topSlot2 - 4);
                  if (this.peek(2).getSize() == 2) {
                     this.copyAlias(topSlot2 - 4, topSlot2);
                     this.copyAlias(topSlot2 - 5, topSlot2 - 1);
                  } else {
                     this.copyAlias(topSlot2 - 3, topSlot2 - 5);
                     this.copyAlias(topSlot2 - 4, topSlot2);
                     this.copyAlias(topSlot2 - 5, topSlot2 - 1);
                  }
                  break label77;
               }
            case 91:
               this.copyAlias(topSlot2, topSlot2 - 1);
               this.copyAlias(topSlot2 - 1, topSlot2 - 2);
               if (this.peek(1).getSize() == 2) {
                  this.copyAlias(topSlot2 - 2, topSlot2);
               } else {
                  this.copyAlias(topSlot2 - 2, topSlot2 - 3);
                  this.copyAlias(topSlot2 - 3, topSlot2);
               }
               break label77;
            case 95:
               AliasSet aliasSet = this.aliases[topSlot2 - 1];
               AliasSet aliasSet2 = this.aliases[topSlot2];
               if (aliasSet != null) {
                  aliasSet.remove(topSlot2 - 1);
                  aliasSet.add(topSlot2);
               }

               if (aliasSet2 != null) {
                  aliasSet2.remove(topSlot2);
                  aliasSet2.add(topSlot2 - 1);
               }

               this.aliases[topSlot2 - 1] = aliasSet2;
               this.aliases[topSlot2] = aliasSet;
               break label77;
            case 132:
               this.clearAlias(((IincInsnNode)instruction).var);
               break label77;
            case 182:
            case 183:
            case 184:
            case 185:
               if (Type.getReturnType(((MethodInsnNode)instruction).desc).getSort() == 0 || topSlot2 > topSlot) {
                  break label77;
               }
               break;
            case 186:
               if (Type.getReturnType(((InvokeDynamicInsnNode)instruction).desc).getSort() == 0 || topSlot2 > topSlot) {
                  break label77;
               }
         }

         this.clearAlias(topSlot2);
      }

      for (int nextIndex = topSlot2 + 1; nextIndex <= topSlot; nextIndex++) {
         this.clearAlias(nextIndex);
      }
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

      System.arraycopy(((AliasFrame)frame).aliases, 0, this.aliases, 0, this.aliases.length);
      Map<AliasSet, AliasSet> copies = new HashMap<>();

      for (int index = 0; index < this.aliases.length; index++) {
         AliasSet group = this.aliases[index];
         if (group != null) {
            AliasSet[] aliases2 = this.aliases;
            AliasSet aliasSet;
            if (group.size() == 1) {
               aliasSet = null;
            } else {
               Function<AliasSet, AliasSet> mapper = value -> value.copy();
               aliasSet = copies.computeIfAbsent(group, mapper);
            }

            aliases2[index] = aliasSet;
         }
      }

      return this;
   }
}
