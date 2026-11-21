package dev.jnic.analysis;

import java.util.List;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Interpreter;

class NullnessInterpreter extends Interpreter<NullnessValue> {
   public static final NullnessInterpreter INSTANCE = new NullnessInterpreter();
   private static final String UNSUPPORTED_MESSAGE = "Unsupported JSR/RET instruction";

   private NullnessInterpreter() {
      super(589824);
   }

   public NullnessValue newValue(Type type) {
      if (type == Type.VOID_TYPE) {
         return null;
      } else {
         return type != null && type.getSize() == 2 ? NullnessValue.UNKNOWN2 : NullnessValue.UNKNOWN1;
      }
   }

   public NullnessValue newParameterValue(boolean enabled, int index, Type type) {
      return enabled && index == 0 ? NullnessValue.NOT_NULL : this.newValue(type);
   }

   public NullnessValue newOperation(AbstractInsnNode instruction) throws AnalyzerException {
      int opcode = instruction.getOpcode();
      if (opcode > 167) {
         if (opcode == 168) {
            String unsupportedMessage = UNSUPPORTED_MESSAGE;
            throw new AnalyzerException(instruction, unsupportedMessage);
         } else if (opcode == 178) {
            return NullnessValue.unknownValue(Type.getType(((FieldInsnNode)instruction).desc));
         } else if (opcode != 187) {
            throw new AssertionError();
         } else {
            return NullnessValue.NOT_NULL;
         }
      } else {
         if (Integer.compareUnsigned(opcode - 1, 17) > 0) {
            throw new AssertionError();
         }

         switch (opcode - 1) {
            case 0:
               return NullnessValue.NULL;
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 10:
            case 11:
            case 12:
            case 15:
            case 16:
               break;
            case 17:
               Object constant = ((LdcInsnNode)instruction).cst;
               if (constant == null) {
                  break;
               }

               if ((constant == null ? true : constant instanceof String)
                  || (constant == null ? true : constant instanceof Type)
                  || (constant == null ? true : constant instanceof Handle)) {
                  return NullnessValue.NOT_NULL;
               }

               if ((constant == null ? false : !(constant instanceof Double)) && (constant == null ? false : !(constant instanceof Long))) {
                  break;
               }
            case 8:
            case 9:
            case 13:
            case 14:
               return NullnessValue.UNKNOWN2;
            default:
               throw new AssertionError();
         }

         return NullnessValue.UNKNOWN1;
      }
   }

   public NullnessValue copyOperation(AbstractInsnNode instruction, NullnessValue nullnessValue) {
      return nullnessValue;
   }

   public NullnessValue unaryOperation(AbstractInsnNode instruction, NullnessValue nullnessValue) {
      switch (instruction.getOpcode()) {
         case 0:
         case 1:
         case 2:
         case 3:
         case 4:
         case 5:
         case 6:
         case 7:
         case 8:
         case 9:
         case 10:
         case 11:
         case 12:
         case 13:
         case 14:
         case 15:
         case 16:
         case 17:
         case 18:
         case 19:
         case 20:
         case 21:
         case 22:
         case 23:
         case 24:
         case 25:
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
         case 54:
         case 55:
         case 56:
         case 57:
         case 58:
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
         case 89:
         case 90:
         case 91:
         case 92:
         case 93:
         case 94:
         case 95:
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
         case 148:
         case 149:
         case 150:
         case 151:
         case 152:
         case 159:
         case 160:
         case 161:
         case 162:
         case 163:
         case 164:
         case 165:
         case 166:
         case 167:
         case 168:
         case 169:
         case 177:
         case 178:
         case 181:
         case 182:
         case 183:
         case 184:
         case 185:
         case 186:
         case 187:
         case 196:
         case 197:
         case 200:
         case 201:
         case 202:
         case 203:
         case 204:
         case 205:
         case 206:
         case 207:
         case 208:
         case 209:
         case 210:
         case 211:
         case 212:
         case 213:
         case 214:
         case 215:
         case 216:
         case 217:
         case 218:
         case 219:
         case 220:
         case 221:
         case 222:
         case 223:
         case 224:
         case 225:
         case 226:
         case 227:
         case 228:
         case 229:
         case 230:
         case 231:
         case 232:
         case 233:
         case 234:
         case 235:
         case 236:
         case 237:
         case 238:
         case 239:
         case 240:
         case 241:
         case 242:
         case 243:
         case 244:
         case 245:
         case 246:
         case 247:
         case 248:
         case 249:
         case 250:
         case 251:
         case 252:
         case 253:
         case 254:
         case 255:
            throw new AssertionError();
         case 116:
         case 118:
         case 132:
         case 134:
         case 136:
         case 137:
         case 139:
         case 142:
         case 144:
         case 145:
         case 146:
         case 147:
         case 190:
         case 193:
            return NullnessValue.UNKNOWN1;
         case 117:
         case 119:
         case 133:
         case 135:
         case 138:
         case 140:
         case 141:
         case 143:
            return NullnessValue.UNKNOWN2;
         case 153:
         case 154:
         case 155:
         case 156:
         case 157:
         case 158:
         case 170:
         case 171:
         case 172:
         case 173:
         case 174:
         case 175:
         case 176:
         case 179:
         case 191:
         case 194:
         case 195:
         case 198:
         case 199:
            return null;
         case 180:
            return NullnessValue.unknownValue(Type.getType(((FieldInsnNode)instruction).desc));
         case 188:
         case 189:
            return NullnessValue.NOT_NULL;
         case 192:
            return nullnessValue;
         default:
            throw new AssertionError();
      }
   }

   public NullnessValue binaryOperation(AbstractInsnNode instruction, NullnessValue nullnessValue, NullnessValue nullnessValue2) {
      switch (instruction.getOpcode()) {
         case 0:
         case 1:
         case 2:
         case 3:
         case 4:
         case 5:
         case 6:
         case 7:
         case 8:
         case 9:
         case 10:
         case 11:
         case 12:
         case 13:
         case 14:
         case 15:
         case 16:
         case 17:
         case 18:
         case 19:
         case 20:
         case 21:
         case 22:
         case 23:
         case 24:
         case 25:
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
         case 54:
         case 55:
         case 56:
         case 57:
         case 58:
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
         case 89:
         case 90:
         case 91:
         case 92:
         case 93:
         case 94:
         case 95:
         case 116:
         case 117:
         case 118:
         case 119:
         case 132:
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
         case 153:
         case 154:
         case 155:
         case 156:
         case 157:
         case 158:
         case 167:
         case 168:
         case 169:
         case 170:
         case 171:
         case 172:
         case 173:
         case 174:
         case 175:
         case 176:
         case 177:
         case 178:
         case 179:
         case 180:
         case 182:
         case 183:
         case 184:
         case 185:
         case 186:
         case 187:
         case 188:
         case 189:
         case 190:
         case 191:
         case 192:
         case 193:
         case 194:
         case 195:
         case 196:
         case 197:
         case 198:
         case 199:
         case 200:
         case 201:
         case 202:
         case 203:
         case 204:
         case 205:
         case 206:
         case 207:
         case 208:
         case 209:
         case 210:
         case 211:
         case 212:
         case 213:
         case 214:
         case 215:
         case 216:
         case 217:
         case 218:
         case 219:
         case 220:
         case 221:
         case 222:
         case 223:
         case 224:
         case 225:
         case 226:
         case 227:
         case 228:
         case 229:
         case 230:
         case 231:
         case 232:
         case 233:
         case 234:
         case 235:
         case 236:
         case 237:
         case 238:
         case 239:
         case 240:
         case 241:
         case 242:
         case 243:
         case 244:
         case 245:
         case 246:
         case 247:
         case 248:
         case 249:
         case 250:
         case 251:
         case 252:
         case 253:
         case 254:
         case 255:
            throw new AssertionError();
         case 46:
         case 48:
         case 50:
         case 51:
         case 52:
         case 53:
         case 96:
         case 98:
         case 100:
         case 102:
         case 104:
         case 106:
         case 108:
         case 110:
         case 112:
         case 114:
         case 120:
         case 122:
         case 124:
         case 126:
         case 128:
         case 130:
         case 148:
         case 149:
         case 150:
         case 151:
         case 152:
            return NullnessValue.UNKNOWN1;
         case 47:
         case 49:
         case 97:
         case 99:
         case 101:
         case 103:
         case 105:
         case 107:
         case 109:
         case 111:
         case 113:
         case 115:
         case 121:
         case 123:
         case 125:
         case 127:
         case 129:
         case 131:
            return NullnessValue.UNKNOWN2;
         case 159:
         case 160:
         case 161:
         case 162:
         case 163:
         case 164:
         case 165:
         case 166:
         case 181:
            return null;
         default:
            throw new AssertionError();
      }
   }

   public NullnessValue ternaryOperation(AbstractInsnNode instruction, NullnessValue nullnessValue, NullnessValue nullnessValue2, NullnessValue nullnessValue3) {
      return null;
   }

   public NullnessValue naryOperation(AbstractInsnNode instruction, List<? extends NullnessValue> list) {
      if (instruction == null) {
         throw new AssertionError();
      } else if (instruction == null ? true : instruction instanceof MethodInsnNode) {
         return NullnessValue.unknownValue(Type.getReturnType(((MethodInsnNode)instruction).desc));
      } else if (instruction == null ? true : instruction instanceof InvokeDynamicInsnNode) {
         return NullnessValue.unknownValue(Type.getReturnType(((InvokeDynamicInsnNode)instruction).desc));
      } else if (instruction == null ? true : instruction instanceof MultiANewArrayInsnNode) {
         return NullnessValue.NOT_NULL;
      } else {
         throw new AssertionError();
      }
   }

   public void returnOperation(AbstractInsnNode instruction, NullnessValue nullnessValue, NullnessValue nullnessValue2) {
   }

   public NullnessValue merge(NullnessValue nullnessValue, NullnessValue nullnessValue2) {
      nullnessValue.getClass();
      return nullnessValue != nullnessValue2 && nullnessValue != NullnessValue.UNKNOWN2 ? NullnessValue.UNKNOWN1 : nullnessValue;
   }
}
