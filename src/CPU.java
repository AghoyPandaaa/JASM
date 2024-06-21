import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.Arrays;

class CPU {
    private byte[] registers;
    Map<String, Boolean> flags;
    Stack<Integer> stack;

    public CPU() {
        registers = new byte[32]; // 8 registers of 4 bytes each
        flags = new HashMap<>();
        stack = new Stack<>();
        resetRegisters();
        resetFlags();
    }

    private void resetRegisters() {
        for (int i = 0; i < registers.length; i++) {
            registers[i] = 0;
        }
    }

    private void resetFlags() {
        flags.put("CF", false);
        flags.put("PF", false);
        flags.put("AF", false);
        flags.put("ZF", false);
        flags.put("SF", false);
        flags.put("OF", false);
    }

    public void updateFlagsForAnd(int result) {
        flags.put("ZF", result == 0);
        flags.put("SF", result < 0);
        flags.put("OF", false);
        flags.put("CF", false);
    }

    public void updateFlagsForOrXor(int result) {
        flags.put("ZF", result == 0);
        flags.put("SF", result < 0);
        flags.put("OF", false);
        flags.put("CF", false);
    }



    public int getRegister(String name) {
        int index = getRegisterIndex(name);
        int size = getRegisterSize(name);
        ByteBuffer buffer = ByteBuffer.wrap(registers, index, size);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        if (size == 1) {
            return buffer.get() & 0xFF;
        } else if (size == 2) {
            return buffer.getShort() & 0xFFFF;
        } else {
            return buffer.getInt();
        }
    }

    public void setRegister(String name, int value) {
        int index = getRegisterIndex(name);
        int size = getRegisterSize(name);
        ByteBuffer buffer = ByteBuffer.wrap(registers, index, size);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        if (size == 1) {
            buffer.put((byte) value);
        } else if (size == 2) {
            buffer.putShort((short) value);
        } else {
            buffer.putInt(value);
        }
    }

    private int getRegisterIndex(String name) {
        switch (name) {
            case "EAX":
            case "AX":
            case "AH":
            case "AL":
                return 0;
            case "EBX":
            case "BX":
            case "BH":
            case "BL":
                return 4;
            case "ECX":
            case "CX":
            case "CH":
            case "CL":
                return 8;
            case "EDX":
            case "DX":
            case "DH":
            case "DL":
                return 12;
            case "ESI":
            case "SI":
                return 16;
            case "EDI":
            case "DI":
                return 20;
            case "EBP":
            case "BP":
                return 24;
            case "ESP":
            case "SP":
                return 28;
            default:
                throw new IllegalArgumentException("Invalid register: " + name);
        }
    }

    private int getRegisterSize(String name) {
        switch (name) {
            case "EAX":
            case "EBX":
            case "ECX":
            case "EDX":
            case "ESI":
            case "EDI":
            case "EBP":
            case "ESP":
                return 4;
            case "AX":
            case "BX":
            case "CX":
            case "DX":
            case "SI":
            case "DI":
            case "BP":
            case "SP":
                return 2;
            case "AH":
            case "AL":
            case "BH":
            case "BL":
            case "CH":
            case "CL":
            case "DH":
            case "DL":
                return 1;
            default:
                throw new IllegalArgumentException("Invalid register: " + name);
        }
    }

    public boolean getFlag(String name) {
        return flags.getOrDefault(name, false);
    }

    public void setFlag(String name, boolean value) {
        flags.put(name, value);
    }

    public void push(int value) {
        stack.push(value);
        // Update ESP
        setRegister("ESP", getRegister("ESP") - 4);
    }

    public int pop() {
        if (!stack.isEmpty()) {
            // Update ESP
            setRegister("ESP", getRegister("ESP") + 4);
            return stack.pop();
        } else {
            throw new IllegalStateException("Stack underflow");
        }
    }

    public boolean isRegister(String name) {
    return Arrays.asList("EAX", "AX", "AH", "AL", "EBX", "BX", "BH", "BL", "ECX", "CX", "CH", "CL", "EDX", "DX", "DH", "DL", "ESI", "SI", "EDI", "DI", "EBP", "BP", "ESP", "SP").contains(name);
}

    public void updateFlags(int result, int operand1, int operand2, boolean isAddition) {
    flags.put("ZF", result == 0);
    flags.put("SF", result < 0);
    flags.put("PF", Integer.bitCount(result & 0xFF) % 2 == 0);
    if (isAddition) {
        flags.put("CF", (operand1 > 0 && operand2 > 0 && result < 0) || (operand1 < 0 && operand2 < 0 && result > 0));
        flags.put("OF", ((operand1 ^ result) & (operand2 ^ result) & 0x80000000) != 0);
        flags.put("AF", ((operand1 ^ operand2 ^ result) & 0x10) != 0);
    } else {
        flags.put("CF", operand1 < operand2);
        flags.put("OF", ((operand1 ^ operand2) & (operand1 ^ result) & 0x80000000) != 0);
        flags.put("AF", ((operand1 ^ operand2 ^ result) & 0x10) != 0);
    }
}
}
