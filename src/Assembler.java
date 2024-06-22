import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.lang.Exception;

class Assembler {
    CPU cpu;
    Map<String, Integer> labels;
    Map<String, Variable> variables = new HashMap<>();
    String[] codeLines;
    int currentSegment;

    public Assembler() {
        cpu = new CPU();
        labels = new HashMap<>();
        variables = new HashMap<>();
        currentSegment = 0; // 0 = .code, 1 = .data, 2 = .stack
    }

    public void execute(String instruction, int currentLine) throws Exception {
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
                if (parts.length != 3) {
                    throw new Exception("Syntax error: Invalid number of operands for MOV operation");
                }
                String dest = parts[1].toUpperCase();
                String src = parts[2].toUpperCase();
                if (!isRegister(dest) && !isVariable(dest) && !isIndirect(dest)) {
                    throw new Exception("Syntax error: Invalid destination operand for MOV operation");
                }
                if (!isRegister(src) && !isVariable(src) && !isNumeric(src) && !isIndirect(src)) {
                    throw new Exception("Syntax error: Invalid source operand for MOV operation");
                }
                if (isRegister(dest) && isRegister(src) && getRegisterSize(dest) != getRegisterSize(src)) {
                    throw new Exception("Syntax error: Size mismatch between source and destination registers for MOV operation");
                }
                // Implement the MOV operation
                int srcValue;
                if (isIndirect(src)) {
                    int address = getIndirectAddress(src);
                    srcValue = cpu.getMemory(address);
                } else {
                    srcValue = getValue(src);
                }
                if (isIndirect(dest)) {
                    int address = getIndirectAddress(dest);
                    cpu.setMemory(address, srcValue);
                } else {
                    cpu.setRegister(dest, srcValue);
                }
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
            case "PTR":
                handlePtr(parts);
                break;
            case "PRINT":
                if (parts.length != 2) {
                    throw new Exception("Syntax error: Invalid number of operands for PRINT operation");
                }
                String operand = parts[1].toUpperCase();
                if (isRegister(operand)) {
                    System.out.println(cpu.getRegister(operand));
                } else if (isVariable(operand)) {
                    System.out.println(variables.get(operand).value);
                } else {
                    String[] operandParts = operand.split(" ");
                    if (operandParts.length != 2) {
                        throw new Exception("Syntax error: Invalid operand for PRINT operation");
                    }
                    String keyword = operandParts[0];
                    String var = operandParts[1].toUpperCase();
                    switch (keyword) {
                        case "LENGTHOF":
                            handleLengthof(new String[]{keyword, var});
                            break;
                        case "SIZEOF":
                            handleSizeof(new String[]{keyword, var});
                            break;
                        case "PTR":
                            handlePtr(new String[]{keyword, var});
                            break;
                        case "OFFSET":
                            handleOffset(new String[]{keyword, var});
                            break;
                        default:
                            throw new Exception("Syntax error: Invalid operand for PRINT operation");
                    }
                }
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

    private void handleVariableDefinition(String[] parts) throws Exception {
        String varName = parts[0].toUpperCase();
        String dataType = parts[1].toUpperCase();
        long size;
        long parsedValue;
        long length = 1; // Default length for a single variable

        switch (dataType) {
            case "BYTE": // Define Byte
            case "SBYTE": // Define Signed Byte
                size = 1;
                break;
            case "WORD": // Define Word
            case "SWORD": // Define Signed Word
                size = 2;
                break;
            case "DWORD": // Define Doubleword
            case "SDWORD": // Define Signed Doubleword
                size = 4;
                break;
            case "QWORD": // Define Quadword
                size = 8;
                break;
            default:
                throw new Exception("Unsupported data type: " + dataType);
        }

        if (parts[2].startsWith("[")) {
            // This is an array definition
            length = Integer.parseInt(parts[2].substring(1, parts[2].length() - 1));
            for (int i = 0; i < length; i++) {
                long address = 0; // You need to implement logic to calculate the address
                Variable variable = new Variable(address, size, length, 0);
                variables.put(varName + "[" + i + "]", variable);
            }
        } else {
            // This is a single variable definition
            parsedValue = Long.parseLong(parts[2]);
            long address = 0; // You need to implement logic to calculate the address
            Variable variable = new Variable(address, size, length, parsedValue);
            variables.put(varName, variable);
        }
    }
    private void handleAdd(String[] parts) throws Exception {
        if (parts.length != 3) {
            throw new Exception("Syntax error: Invalid number of operands for ADD operation");
        }
        String dest = parts[1].toUpperCase();
        String src = parts[2].toUpperCase();
        if (!isRegister(dest) && !isVariable(dest) && !isIndirect(dest)) {
            throw new Exception("Syntax error: Invalid destination operand for ADD operation");
        }
        if (!isRegister(src) && !isVariable(src) && !isNumeric(src)) {
            throw new Exception("Syntax error: Invalid source operand for ADD operation");
        }
        if (isRegister(dest) && isRegister(src) && getRegisterSize(dest) != getRegisterSize(src)) {
            throw new Exception("Syntax error: Size mismatch between source and destination registers for ADD operation");
        }
        // Implement the ADD operation
        int srcValue = getValue(src);
        int destValue = 0;
        int result = 0;
        if (isIndirect(dest)) {
            int address = getIndirectAddress(dest);
            destValue = cpu.getMemory(address);
            result = destValue + srcValue;
            cpu.setMemory(address, result);
        } else {
            destValue = getValue(dest);
            result = destValue + srcValue;
            setValue(dest, result);
        }
        cpu.updateFlags(result, srcValue, destValue, true);
    }

    private void handleSub(String[] parts) throws Exception {
    if (parts.length != 3) {
        throw new Exception("Syntax error: Invalid number of operands for SUB operation");
    }
    String dest = parts[1].toUpperCase();
    String src = parts[2].toUpperCase();
    if (!isRegister(dest) && !isVariable(dest)) {
        throw new Exception("Syntax error: Invalid destination operand for SUB operation");
    }
    if (!isRegister(src) && !isVariable(src) && !isNumeric(src)) {
        throw new Exception("Syntax error: Invalid source operand for SUB operation");
    }
    if (isRegister(dest) && isRegister(src) && getRegisterSize(dest) != getRegisterSize(src)) {
        throw new Exception("Syntax error: Size mismatch between source and destination registers for SUB operation");
    }
    // Implement the SUB operation
    int srcValue = getValue(src);
    int destValue = getValue(dest);
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

    private void handleMovsx(String[] parts) throws Exception {
    if (parts.length != 3) {
        throw new Exception("Syntax error: Invalid number of operands for MOVSX operation");
    }
    String dest = parts[1].toUpperCase();
    String src = parts[2].toUpperCase();
    if (!isRegister(dest) || (!isRegister(src) && !isVariable(src))) {
        throw new Exception("Syntax error: Invalid operand for MOVSX operation");
    }
    int srcValue = getValue(src);
    if (getRegisterSize(dest) == 32 && getRegisterSize(src) == 16) {
        srcValue = (srcValue << 16) >> 16; // Sign extend from 16 bits
    } else if (getRegisterSize(dest) == 32 && getRegisterSize(src) == 8) {
        srcValue = (srcValue << 24) >> 24; // Sign extend from 8 bits
    } else if (getRegisterSize(dest) == 16 && getRegisterSize(src) == 8) {
        srcValue = (srcValue << 8) >> 8; // Sign extend from 8 bits
    } else {
        throw new Exception("Syntax error: Size mismatch between source and destination registers for MOVSX operation");
    }
    cpu.setRegister(dest, srcValue);
}

private void handleMovzx(String[] parts) throws Exception {
    if (parts.length != 3) {
        throw new Exception("Syntax error: Invalid number of operands for MOVZX operation");
    }
    String dest = parts[1].toUpperCase();
    String src = parts[2].toUpperCase();
    if (!isRegister(dest) || (!isRegister(src) && !isVariable(src))) {
        throw new Exception("Syntax error: Invalid operand for MOVZX operation");
    }
    int srcValue = getValue(src);
    if (getRegisterSize(dest) == 32 && getRegisterSize(src) == 16) {
        srcValue = srcValue & 0xFFFF; // Zero extend from 16 bits
    } else if (getRegisterSize(dest) == 32 && getRegisterSize(src) == 8) {
        srcValue = srcValue & 0xFF; // Zero extend from 8 bits
    } else if (getRegisterSize(dest) == 16 && getRegisterSize(src) == 8) {
        srcValue = srcValue & 0xFF; // Zero extend from 8 bits
    } else {
        throw new Exception("Syntax error: Size mismatch between source and destination registers for MOVZX operation");
    }
    cpu.setRegister(dest, srcValue);
}

    private void handleXchg(String[] parts) throws Exception {
    if (parts.length != 3) {
        throw new Exception("Syntax error: Invalid number of operands for XCHG operation");
    }
    String dest = parts[1].toUpperCase();
    String src = parts[2].toUpperCase();
    if (!isRegister(dest) && !isVariable(dest)) {
        throw new Exception("Syntax error: Invalid destination operand for XCHG operation");
    }
    if (!isRegister(src) && !isVariable(src) && !isNumeric(src)) {
        throw new Exception("Syntax error: Invalid source operand for XCHG operation");
    }
    if (isRegister(dest) && isRegister(src) && getRegisterSize(dest) != getRegisterSize(src)) {
        throw new Exception("Syntax error: Size mismatch between source and destination registers for XCHG operation");
    }
    // Implement the XCHG operation
    int destValue = getValue(dest);
    int srcValue = getValue(src);
    setValue(dest, srcValue);
    setValue(src, destValue);
}

    private void handleNeg(String[] parts) throws Exception {
    if (parts.length != 2) {
        throw new Exception("Syntax error: Invalid number of operands for NEG operation");
    }
    String operand = parts[1].toUpperCase();
    if (!isRegister(operand) && !isVariable(operand)) {
        throw new Exception("Syntax error: Invalid operand for NEG operation");
    }
    // Implement the NEG operation
    int operandValue = getValue(operand);
    int result = -operandValue;
    setValue(operand, result);
    cpu.updateFlags(result, 0, operandValue, false);
}

    private void handleInc(String[] parts) throws Exception {
    if (parts.length != 2) {
        throw new Exception("Syntax error: Invalid number of operands for INC operation");
    }
    String operand = parts[1].toUpperCase();
    if (!isRegister(operand) && !isVariable(operand)) {
        throw new Exception("Syntax error: Invalid operand for INC operation");
    }
    // Implement the INC operation
    int operandValue = getValue(operand);
    int result = operandValue + 1;
    setValue(operand, result);
    cpu.updateFlags(result, 1, operandValue, true);
    cpu.setFlag("CF", cpu.getFlag("CF")); // Preserve the original Carry flag
}

private void handleDec(String[] parts) throws Exception {
    if (parts.length != 2) {
        throw new Exception("Syntax error: Invalid number of operands for DEC operation");
    }
    String operand = parts[1].toUpperCase();
    if (!isRegister(operand) && !isVariable(operand)) {
        throw new Exception("Syntax error: Invalid operand for DEC operation");
    }
    // Implement the DEC operation
    int operandValue = getValue(operand);
    int result = operandValue - 1;
    setValue(operand, result);
    cpu.updateFlags(result, 1, operandValue, false);
    cpu.setFlag("CF", cpu.getFlag("CF")); // Preserve the original Carry flag
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
        String operand = parts[1].toUpperCase();
        int value = getValue(operand);
        cpu.push(value);
    }

    private void handlePop(String[] parts) {
        String operand = parts[1].toUpperCase();
        int value = cpu.pop();
        setValue(operand, value);
    }

    private void handleJmp(String[] parts) {
        String label = parts[1];
        try {
            executeLabel(label);
        } catch (Exception e) {
            System.out.println("Error executing label " + label + ": " + e.getMessage());
        }
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

    private void handleJc(String[] parts) {
        if (cpu.getFlag("CF")) {
            handleJmp(parts);
        }
    }

    private void handleJnc(String[] parts) {
        if (!cpu.getFlag("CF")) {
            handleJmp(parts);
        }
    }

    private void handleJo(String[] parts) {
        if (cpu.getFlag("OF")) {
            handleJmp(parts);
        }
    }

    private void handleJno(String[] parts) {
        if (!cpu.getFlag("OF")) {
            handleJmp(parts);
        }
    }

    private void handleJs(String[] parts) {
        if (cpu.getFlag("SF")) {
            handleJmp(parts);
        }
    }

    private void handleJns(String[] parts) {
        if (!cpu.getFlag("SF")) {
            handleJmp(parts);
        }
    }

    private void handleJp(String[] parts) {
        if (cpu.getFlag("PF")) {
            handleJmp(parts);
        }
    }

    private void handleJnp(String[] parts) {
        if (!cpu.getFlag("PF")) {
            handleJmp(parts);
        }
    }

    private void handleJe(String[] parts) {
        if (cpu.getFlag("ZF")) {
            handleJmp(parts);
        }
    }

    private void handleJne(String[] parts) {
        if (!cpu.getFlag("ZF")) {
            handleJmp(parts);
        }
    }

    private void handleJcxz(String[] parts) {
        if (cpu.getRegister("CX") == 0) {
            handleJmp(parts);
        }
    }

    private void handleJecxz(String[] parts) {
        if (cpu.getRegister("ECX") == 0) {
            handleJmp(parts);
        }
    }

    private void handleJrcxz(String[] parts) {
        if (cpu.getRegister("RCX") == 0) {
            handleJmp(parts);
        }
    }

    private void handleJa(String[] parts) {
        if (!cpu.getFlag("CF") && !cpu.getFlag("ZF")) {
            handleJmp(parts);
        }
    }

    private void handleJae(String[] parts) {
        if (!cpu.getFlag("CF")) {
            handleJmp(parts);
        }
    }

    private void handleJb(String[] parts) {
        if (cpu.getFlag("CF")) {
            handleJmp(parts);
        }
    }

    private void handleJbe(String[] parts) {
        if (cpu.getFlag("CF") || cpu.getFlag("ZF")) {
            handleJmp(parts);
        }
    }

    private void handleJg(String[] parts) {
        if (!cpu.getFlag("ZF") && (cpu.getFlag("SF") == cpu.getFlag("OF"))) {
            handleJmp(parts);
        }
    }

    private void handleJge(String[] parts) {
        if (cpu.getFlag("SF") == cpu.getFlag("OF")) {
            handleJmp(parts);
        }
    }

    private void handleJl(String[] parts) {
        if (cpu.getFlag("SF") != cpu.getFlag("OF")) {
            handleJmp(parts);
        }
    }

    private void handleJle(String[] parts) {
        if (cpu.getFlag("ZF") || (cpu.getFlag("SF") != cpu.getFlag("OF"))) {
            handleJmp(parts);
        }
    }


    private void handleLoopz(String[] parts) {
        String label = parts[1];
        int ecx = cpu.getRegister("ECX");
        if (ecx != 0 && cpu.getFlag("ZF")) {
            cpu.setRegister("ECX", ecx - 1);
            try {
                executeLabel(label);
            } catch (Exception e) {
                System.out.println("Error executing label " + label + ": " + e.getMessage());
            }
        }
    }

    private void handleLoope(String[] parts) {
        handleLoopz(parts); // LOOPE is equivalent to LOOPZ
    }



    private void handleLoopnz(String[] parts) {
        String label = parts[1];
        int ecx = cpu.getRegister("ECX");
        if (ecx != 0 && !cpu.getFlag("ZF")) {
            cpu.setRegister("ECX", ecx - 1);
            try {
                executeLabel(label);
            } catch (Exception e) {
                System.out.println("Error executing label " + label + ": " + e.getMessage());
            }
        }
    }

    private void handleLoopne(String[] parts) {
        handleLoopnz(parts); // LOOPNE is equivalent to LOOPNZ
    }




    private void handlePrintReg(String[] parts) throws Exception{
        if (parts.length != 2) {
            throw new Exception("Syntax error: Invalid operand for PRINT_REG operation");
        }
        String reg = parts[1].toUpperCase();
        if (isRegister(reg)) {
            System.out.println(reg + ": " + cpu.getRegister(reg));
        } else {
            System.out.println("Register not found: " + reg);
        }
    }

    private void handlePrintFlag(String[] parts) throws Exception {
        if (parts.length != 2) {
            throw new Exception("Syntax error: Invalid operand for PRINT_FLAG operation");
        }
        String flag = parts[1].toUpperCase();
        if (cpu.getFlag(flag)) {
            System.out.println(flag + ": " + cpu.getFlag(flag));
        } else {
            System.out.println("Flag not set: " + flag);
        }
    }

    private void handleShowStack() {
        System.out.println("Stack: " + cpu.stack);
    }

    private void handleShowData() {
        System.out.println("Data Segment: " + variables);
    }

    private void executeLabel(String label) throws Exception {
        Integer lineIndex = labels.get(label);
        if (lineIndex == null) {
            throw new Exception("Label not found: " + label);
        }
        for (int i = lineIndex + 1; i < codeLines.length; i++) {
            String line = codeLines[i].trim();
            if (line.endsWith(":")) {
                labels.put(line.substring(0, line.length() - 1), i);
                line = line.substring(0, line.length() - 1).trim(); // Remove the label
            }
            if (!line.isEmpty()) { // If there's an instruction on this line, execute it
                try {
                    execute(line, i);
                } catch (Exception e) {
                    System.out.println("Error executing line " + i + ": " + e.getMessage());
                    break;
                }
            }
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
                try {
                    execute(line, i);
                } catch (Exception e) {
                    System.out.println("Error executing line " + i + ": " + e.getMessage());
                    break;
                }
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
            return (int)variables.get(operand).value;
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

    private void handleLengthof(String[] parts) throws Exception {
        if (parts.length != 2) {
            throw new Exception("Syntax error: Invalid operand for LENGTHOF operation");
        }
        String var = parts[1].toUpperCase();
        if (isVariable(var)) {
            long length = variables.get(var).length;
            System.out.println("LENGTHOF " + var + ": " + length);
        } else {
            System.out.println("Variable not found: " + var);
        }
    }

    private void handleSizeof(String[] parts) throws Exception {
        if (parts.length != 2) {
            throw new Exception("Syntax error: Invalid operand for SIZEOF operation");
        }
        String var = parts[1].toUpperCase();
        if (isVariable(var)) {
            long size = variables.get(var).size;
            System.out.println("SIZEOF " + var + ": " + size);
        } else {
            System.out.println("Variable not found: " + var);
        }
    }

    private void handlePtr(String[] parts) throws Exception {
        if (parts.length != 2) {
            throw new Exception("Syntax error: Invalid operand for PTR operation");
        }
        String var = parts[1].toUpperCase();
        if (isVariable(var)) {
            long address = variables.get(var).address;
            System.out.println("PTR " + var + ": " + address);
        } else {
            System.out.println("Variable not found: " + var);
        }
    }

    private void handleOffset(String[] parts) throws Exception {
        if (parts.length != 2) {
            throw new Exception("Syntax error: Invalid operand for OFFSET operation");
        }
        String var = parts[1].toUpperCase();
        if (isVariable(var)) {
            long address = variables.get(var).address;
            System.out.println("OFFSET " + var + ": " + address);
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
            try {
                executeLabel(label);
            } catch (Exception e) {
                System.out.println("Error executing label " + label + ": " + e.getMessage());
            }
        }
    }


    private boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    private int getRegisterSize(String reg) {
    if (reg.equals("AX") || reg.equals("BX") || reg.equals("CX") || reg.equals("DX")) {
        return 16;
    } else if (reg.equals("AL") || reg.equals("BL") || reg.equals("CL") || reg.equals("DL")) {
        return 8;
    } else {
        return 32;
    }
}


    private void setValue(String operand, int value) {
    if (isRegister(operand)) {
        cpu.setRegister(operand, value);
    } else if (isVariable(operand)) {
        Variable variable = variables.get(operand);
        variable.value = value;
        variables.put(operand, variable);
    }
}


// In the Assembler class:

    private boolean isIndirect(String operand) {
        return operand.startsWith("[") && operand.endsWith("]");
    }

    private int getIndirectAddress(String operand) {
        String register = operand.substring(1, operand.length() - 1).toUpperCase();
        return cpu.getRegister(register);
    }
}


