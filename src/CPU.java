import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

class CPU {
    private Map<String, Integer> registers;
    Map<String, Boolean> flags;
    Stack<Integer> stack;

    public CPU() {
        registers = new HashMap<>();
        flags = new HashMap<>();
        stack = new Stack<>();
        resetRegisters();
        resetFlags();
    }

    private void resetRegisters() {
        registers.put("EAX", 0);
        registers.put("EBX", 0);
        registers.put("ECX", 0);
        registers.put("EDX", 0);
        registers.put("ESI", 0);
        registers.put("EDI", 0);
        registers.put("EBP", 0);
        registers.put("ESP", 0);
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
        return registers.getOrDefault(name, 0);
    }

    public void setRegister(String name, int value) {
        registers.put(name, value);
    }

    public boolean getFlag(String name) {
        return flags.getOrDefault(name, false);
    }

    public void setFlag(String name, boolean value) {
        flags.put(name, value);
    }

    public void push(int value) {
        stack.push(value);
    }

    public int pop() {
        return stack.isEmpty() ? 0 : stack.pop();
    }

    public boolean isRegister(String name) {
        return registers.containsKey(name);
    }

    public void updateFlags(int result, int operand1, int operand2, boolean isAddition) {
        flags.put("ZF", result == 0);
        flags.put("SF", result < 0);
        if (isAddition) {
            flags.put("CF", (operand1 > 0 && operand2 > 0 && result < 0) || (operand1 < 0 && operand2 < 0 && result > 0));
            flags.put("OF", ((operand1 ^ result) & (operand2 ^ result) & 0x80000000) != 0);
        } else {
            flags.put("CF", operand1 < operand2);
            flags.put("OF", ((operand1 ^ operand2) & (operand1 ^ result) & 0x80000000) != 0);
        }
    }
}
