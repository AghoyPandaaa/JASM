import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

class Assembler {
    CPU cpu;
    Map<String, Integer> labels;
    Map<String, Integer> variables;
    String[] codeLines;
    int currentSegment;

    public Assembler() {
        cpu = new CPU();
        labels = new HashMap<>();
        variables = new HashMap<>();
        currentSegment = 0; // 0 = .code, 1 = .data, 2 = .stack
    }

    public void execute(String instruction, int currentLine) {
        String[] parts = instruction.trim().split("\\s+|,\\s*");
        String opcode = parts[0].toUpperCase();

        switch (opcode) {
            case "ADD":
                handleAdd(parts);
                break;
            case "SUB":
                handleSub(parts);
                break;
            case "MOV":
                handleMov(parts);
                break;
            case "MOVSX":
                handleMovsx(parts);
                break;
            case "MOVZX":
                handleMovzx(parts);
                break;
            case "XCHG":
                handleXchg(parts);
                break;
            case "NEG":
                handleNeg(parts);
                break;
            case "INC":
                handleInc(parts);
                break;
            case "DEC":
                handleDec(parts);
                break;
            case "OR":
                handleOr(parts);
                break;
            case "AND":
                handleAnd(parts);
                break;
            case "XOR":
                handleXor(parts);
                break;
            case "PUSH":
                handlePush(parts);
                break;
            case "POP":
                handlePop(parts);
                break;
            case "JMP":
                handleJmp(parts);
                break;
            case "JZ":
            case "JE":
                handleJz(parts);
                break;
            case "JNZ":
            case "JNE":
                handleJnz(parts);
                break;
            case "JG":
                handleJg(parts);
                break;
            case "JL":
                handleJl(parts);
                break;
            case "JGE":
                handleJge(parts);
                break;
            case "JLE":
                handleJle(parts);
                break;
            case "PRINT_REG":
                handlePrintReg(parts);
                break;
            case "PRINT_FLAG":
                handlePrintFlag(parts);
                break;
            case "SHOW_STACK":
                handleShowStack();
                break;
            case "SHOW_DATA":
                handleShowData();
                break;
            case "STC":
                handleStc();
                break;
            case "CLC":
                handleClc();
                break;
            case "OFFSET":
                handleOffset(parts);
                break;
            case "LENGTHOF":
                handleLengthof(parts);
                break;
            case "SIZEOF":
                handleSizeof(parts);
                break;
            case "TEST":
                handleTest(parts);
                break;
            case "CMP":
                handleCmp(parts);
                break;
            case "LOOP":
                handleLoop(parts);
                break;
            case ".CODE":
                currentSegment = 0;
                break;
            case ".DATA":
                currentSegment = 1;
                break;
            case ".STACK":
                currentSegment = 2;
                break;
            default:
                if (opcode.endsWith(":")) {
                    // This is a label definition, nothing to execute
                } else if (currentSegment == 1) {
                    handleVariableDefinition(parts);
                } else {
                    System.out.println("Unsupported instruction: " + opcode);
                }
                break;
        }
    }

    private void handleVariableDefinition(String[] parts) {
        String varName = parts[0];
        int value = Integer.parseInt(parts[2]);
        variables.put(varName.toUpperCase(), value);
    }

    private void handleAdd(String[] parts) {
        String dest = parts[1].toUpperCase();
        String src = parts[2].toUpperCase();
        int srcValue = getValue(src);
        int destValue = cpu.getRegister(dest);
        int result = destValue + srcValue;
        cpu.setRegister(dest, result);
        cpu.updateFlags(result, srcValue, destValue, true);
    }

    private void handleSub(String[] parts) {
        String dest = parts[1].toUpperCase();
        String src = parts[2].toUpperCase();
        int srcValue = getValue(src);
        int destValue = cpu.getRegister(dest);
        int result = destValue - srcValue;
        cpu.setRegister(dest, result);
        cpu.updateFlags(result, srcValue, destValue, false);
    }

    private void handleMov(String[] parts) {
        String dest = parts[1].toUpperCase();
        String src = parts[2].toUpperCase();
        int srcValue = getValue(src);
        cpu.setRegister(dest, srcValue);
    }

    private void handleMovsx(String[] parts) {
        String dest = parts[1].toUpperCase();
        String src = parts[2].toUpperCase();
        int srcValue = cpu.getRegister(src);
        int result = (srcValue << 24) >> 24; // Sign extend from 8 bits
        cpu.setRegister(dest, result);
    }

    private void handleMovzx(String[] parts) {
        String dest = parts[1].toUpperCase();
        String src = parts[2].toUpperCase();
        int srcValue = cpu.getRegister(src);
        int result = srcValue & 0xFF; // Zero extend from 8 bits
        cpu.setRegister(dest, result);
    }

    private void handleXchg(String[] parts) {
        String reg1 = parts[1].toUpperCase();
        String reg2 = parts[2].toUpperCase();
        int value1 = cpu.getRegister(reg1);
        int value2 = cpu.getRegister(reg2);
        cpu.setRegister(reg1, value2);
        cpu.setRegister(reg2, value1);
    }

    private void handleNeg(String[] parts) {
        String reg = parts[1].toUpperCase();
        int value = cpu.getRegister(reg);
        int result = -value;
        cpu.setRegister(reg, result);
        cpu.updateFlags(result, 0, value, false);
    }

    private void handleInc(String[] parts) {
        String reg = parts[1].toUpperCase();
        int value = cpu.getRegister(reg);
        int result = value + 1;
        cpu.setRegister(reg, result);
        cpu.updateFlags(result, 1, value, true);
    }

    private void handleDec(String[] parts) {
        String reg = parts[1].toUpperCase();
        int value = cpu.getRegister(reg);
        int result = value - 1;
        cpu.setRegister(reg, result);
        cpu.updateFlags(result, 1, value, false);
    }

    private void handleOr(String[] parts) {
        String dest = parts[1].toUpperCase();
        String src = parts[2].toUpperCase();
        int srcValue = getValue(src);
        int destValue = cpu.getRegister(dest);
        int result = destValue | srcValue;
        cpu.setRegister(dest, result);
        cpu.updateFlagsForOrXor(result);
    }

    private void handleAnd(String[] parts) {
        String dest = parts[1].toUpperCase();
        String src = parts[2].toUpperCase();
        int srcValue = getValue(src);
        int destValue = cpu.getRegister(dest);
        int result = destValue & srcValue;
        cpu.setRegister(dest, result);
        cpu.updateFlagsForAnd(result);
    }

    private void handleXor(String[] parts) {
        String dest = parts[1].toUpperCase();
        String src = parts[2].toUpperCase();
        int srcValue = getValue(src);
        int destValue = cpu.getRegister(dest);
        int result = destValue ^ srcValue;
        cpu.setRegister(dest, result);
        cpu.updateFlagsForOrXor(result);
    }

    private void handlePush(String[] parts) {
        String reg = parts[1].toUpperCase();
        int value = cpu.getRegister(reg);
        cpu.push(value);
    }

    private void handlePop(String[] parts) {
        String reg = parts[1].toUpperCase();
        int value = cpu.pop();
        cpu.setRegister(reg, value);
    }

    private void handleJmp(String[] parts) {
        String label = parts[1];
        executeLabel(label);
    }

    private void handleJz(String[] parts) {
        if (cpu.getFlag("ZF")) {
            handleJmp(parts);
        }
    }

    private void handleJnz(String[] parts) {
        if (!cpu.getFlag("ZF")) {
            handleJmp(parts);
        }
    }

    private void handleJg(String[] parts) {
        if (!cpu.getFlag("ZF") && (cpu.getFlag("SF") == cpu.getFlag("OF"))) {
            handleJmp(parts);
        }
    }

    private void handleJl(String[] parts) {
        if (cpu.getFlag("SF") != cpu.getFlag("OF")) {
            handleJmp(parts);
        }
    }

    private void handleJge(String[] parts) {
        if (cpu.getFlag("SF") == cpu.getFlag("OF")) {
            handleJmp(parts);
        }
    }

    private void handleJle(String[] parts) {
        if (cpu.getFlag("ZF") || (cpu.getFlag("SF") != cpu.getFlag("OF"))) {
            handleJmp(parts);
        }
    }

    private void handlePrintReg(String[] parts) {
        String reg = parts[1].toUpperCase();
        System.out.println(reg + ": " + cpu.getRegister(reg));
    }

    private void handlePrintFlag(String[] parts) {
        String flag = parts[1].toUpperCase();
        System.out.println(flag + ": " + cpu.getFlag(flag));
    }

    private void handleShowStack() {
        System.out.println("Stack: " + cpu.stack);
    }

    private void handleShowData() {
        System.out.println("Data Segment: " + variables);
    }

    private void executeLabel(String label) {
        int lineIndex = labels.get(label);
        for (int i = lineIndex + 1; i < codeLines.length; i++) {
            String line = codeLines[i].trim();
            if (line.endsWith(":")) {
                labels.put(line.substring(0, line.length() - 1), i);
                continue;
            }
            execute(line, i);
        }
    }

    public void runCode(String code) {
        codeLines = code.split("\n");
        // First pass: identify labels
        for (int i = 0; i < codeLines.length; i++) {
            String line = codeLines[i].trim();
            if (line.endsWith(":")) {
                labels.put(line.substring(0, line.length() - 1), i);
            }
        }
        // Second pass: execute instructions
        for (int i = 0; i < codeLines.length; i++) {
            String line = codeLines[i].trim();
            if (!line.endsWith(":")) {
                execute(line, i);
            }
        }
    }

    private boolean isRegister(String name) {
        return cpu.isRegister(name);
    }

    private boolean isVariable(String name) {
        return variables.containsKey(name);
    }

    private int getValue(String operand) {
        if (isRegister(operand)) {
            return cpu.getRegister(operand);
        } else if (isVariable(operand)) {
            return variables.get(operand);
        } else {
            return Integer.parseInt(operand);
        }
    }

    private void printFlags() {
        System.out.println("Flags: " + cpu.flags);
    }

    private void handleStc() {
        cpu.setFlag("CF", true);
    }

    private void handleClc() {
        cpu.setFlag("CF", false);
    }

    private void handleOffset(String[] parts) {
        String var = parts[1].toUpperCase();
        if (isVariable(var)) {
            int offset = variables.get(var);
            System.out.println("OFFSET " + var + ": " + offset);
        } else {
            System.out.println("Variable not found: " + var);
        }
    }

    private void handleLengthof(String[] parts) {
        String var = parts[1].toUpperCase();
        if (isVariable(var)) {
            int length = 1; // Assuming each variable is of length 1 for simplicity
            System.out.println("LENGTHOF " + var + ": " + length);
        } else {
            System.out.println("Variable not found: " + var);
        }
    }

    private void handleSizeof(String[] parts) {
        String var = parts[1].toUpperCase();
        if (isVariable(var)) {
            int size = 4; // Assuming each variable is of size 4 bytes for simplicity
            System.out.println("SIZEOF " + var + ": " + size);
        } else {
            System.out.println("Variable not found: " + var);
        }
    }

    private void handleTest(String[] parts) {
        String op1 = parts[1].toUpperCase();
        String op2 = parts[2].toUpperCase();
        int value1 = getValue(op1);
        int value2 = getValue(op2);
        int result = value1 & value2;
        cpu.updateFlags(result, value2, value1, true);
    }

    private void handleCmp(String[] parts) {
        String op1 = parts[1].toUpperCase();
        String op2 = parts[2].toUpperCase();
        int value1 = getValue(op1);
        int value2 = getValue(op2);
        int result = value1 - value2;
        cpu.updateFlags(result, value2, value1, false);
    }

    private void handleLoop(String[] parts) {
        String label = parts[1];
        int cx = cpu.getRegister("CX");
        if (cx != 0) {
            cpu.setRegister("CX", cx - 1);
            executeLabel(label);
        }
    }
}
