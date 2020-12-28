import java.util.Map;

public class RiscVDisassembler {
    public static String disassemble(int addr, int command, Map<Integer, String> marks) {
        int opcode = command & 0b1111111;
        int funct3 = (command >>> 12) & 0b111;
        int funct7 = (command >>> 25) & 0b1111111;

        String commandName = "UNKNOWN";
        String args = "";
        switch (opcode) {
            case 0b0110111:
                commandName = "lui";
                args = disassembleUTypeCommand(command);
                break;
            case 0b0010111:
                commandName = "auipc";
                args = disassembleUTypeCommand(command);
                break;
            case 0b1101111:
                commandName = "jal";
                args = disassembleJTypeCommand(command);
                break;
            case 0b1100111:
                commandName = "jalr";
                args = disassembleITypeCommand(command, false);
                break;
            case 0b1100011:
                switch (funct3) {
                    case 0b000:
                        commandName = "beq";
                        break;
                    case 0b001:
                        commandName = "bne";
                        break;
                    case 0b100:
                        commandName = "blt";
                        break;
                    case 0b101:
                        commandName = "bge";
                        break;
                    case 0b110:
                        commandName = "bltu";
                        break;
                    case 0b111:
                        commandName = "bgeu";
                        break;
                }
                args = disassembleBTypeCommand(command);
                break;
            case 0b0000011:
                switch (funct3) {
                    case 0b000:
                        commandName = "lb";
                        break;
                    case 0b001:
                        commandName = "lh";
                        break;
                    case 0b010:
                        commandName = "lw";
                        break;
                    case 0b100:
                        commandName = "lbu";
                        break;
                    case 0b101:
                        commandName = "lhu";
                        break;
                }
                args = disassembleITypeCommand(command, false, true);
                break;
            case 0b0100011:
                switch (funct3) {
                    case 0b000:
                        commandName = "sb";
                        break;
                    case 0b001:
                        commandName = "sh";
                        break;
                    case 0b010:
                        commandName = "sw";
                        break;
                }
                args = disassembleSTypeCommand(command, true);
                break;
            case 0b0010011:
                switch (funct3) {
                    case 0b000:
                        commandName = "addi";
                        args = disassembleITypeCommand(command, true);
                        break;
                    case 0b010:
                        commandName = "slti";
                        args = disassembleITypeCommand(command, true);
                        break;
                    case 0b011:
                        commandName = "sltiu";
                        args = disassembleITypeCommand(command, false);
                        break;
                    case 0b100:
                        commandName = "xori";
                        args = disassembleITypeCommand(command, true);
                        break;
                    case 0b110:
                        commandName = "ori";
                        args = disassembleITypeCommand(command, true);
                        break;
                    case 0b111:
                        commandName = "andi";
                        args = disassembleITypeCommand(command, true);
                        break;
                    case 0b001:
                        switch (funct7) {
                            case 0b0000000:
                                commandName = "slli";
                                args = disassembleShiftCommand(command);
                                break;
                        }
                        break;
                    case 0b101:
                        switch (funct7) {
                            case 0b0000000:
                                commandName = "srli";
                                args = disassembleShiftCommand(command);
                                break;
                            case 0b0100000:
                            case 0b101:
                                commandName = "srai";
                                args = disassembleShiftCommand(command);
                                break;
                        }
                        break;
                }
                break;
            case 0b0110011:
                switch (funct7) {
                    case 0b0000000:
                        switch (funct3) {
                            case 0b000:
                                commandName = "add";
                                break;
                            case 0b001:
                                commandName = "sll";
                                break;
                            case 0b010:
                                commandName = "srt";
                                break;
                            case 0b011:
                                commandName = "sltu";
                                break;
                            case 0b100:
                                commandName = "xor";
                                break;
                            case 0b101:
                                commandName = "srl";
                                break;
                            case 0b110:
                                commandName = "or";
                                break;
                            case 0b111:
                                commandName = "and";
                                break;
                        }
                        args = disassembleRTypeCommand(command);
                        break;
                    case 0b0100000:
                        switch (funct3) {
                            case 0b000:
                                commandName = "sub";
                                break;
                            case 0b101:
                                commandName = "sra";
                                break;
                        }
                        args = disassembleRTypeCommand(command);
                        break;
                    case 0b0000001:
                        switch (funct3) {
                            case 0b000:
                                commandName = "mul";
                                break;
                            case 0b001:
                                commandName = "mulh";
                                break;
                            case 0b010:
                                commandName = "mulhsu";
                                break;
                            case 0b011:
                                commandName = "mulhu";
                                break;
                            case 0b100:
                                commandName = "div";
                                break;
                            case 0b101:
                                commandName = "divu";
                                break;
                            case 0b110:
                                commandName = "rem";
                                break;
                            case 0b111:
                                commandName = "remu";
                                break;
                        }
                        args = disassembleRTypeCommand(command);
                        break;
                }
                break;
            case 0b0001111:
                switch (funct3) {
                    case 0b000:
                        commandName = "fence";
                        args = disassembleFenceCommand(command);
                        break;
                    case 0b001:
                        commandName = "fence.i";
                        break;
                }
                break;
            case 0b1110011:
                if (funct3 == 0b000) {
                    switch ((command >>> 20) & 0b111111111111) {
                        case 0b0000000000000:
                            commandName = "ecall";
                            break;
                        case 0b0000000000001:
                            commandName = "ebreak";
                            break;
                    }
                    break;
                }
                switch (funct3) {
                    case 0b001:
                        commandName = "csrrw";
                        break;
                    case 0b010:
                        commandName = "csrrs";
                        break;
                    case 0b011:
                        commandName = "csrrc";
                        break;
                    case 0b101:
                        commandName = "csrrwi";
                        break;
                    case 0b110:
                        commandName = "csrrso";
                        break;
                    case 0b111:
                        commandName = "csrrci";
                        break;
                }
                args = disassembleRTypeCommand(command);
                break;
        }

        String mark = marks.get(addr);
        if (mark == null) {
            mark = "\t\t";
        } else {
            mark = "<" + mark + ">";
        }
        return String.format("%08x: %s\t%s\t%s", addr, mark, commandName, args);
    }

    private static String disassembleShiftCommand(int command) {
        String args;

        int rd = (command >>> 7) & 0b11111;
        int rs1 = (command >>> 15) & 0b11111;
        int shamt = (command >>> 20) & 0b11111;

        args = getRegisterName(rd) + ", " + getRegisterName(rs1) + ", " + shamt;

        return args;
    }

    private static String disassembleRTypeCommand(int command) {
        String args;

        int rd = (command >>> 7) & 0b11111;
        int rs1 = (command >>> 15) & 0b11111;
        int rs2 = (command >>> 20) & 0b11111;

        args = getRegisterName(rd) + ", " + getRegisterName(rs1) + ", " + getRegisterName(rs2);

        return args;
    }

    private static String disassembleITypeCommand(int command, boolean signExtended, boolean brackets) {
        String args;

        int rd = (command >>> 7) & 0b11111;
        int rs1 = (command >>> 15) & 0b11111;
        int imm = (command >>> 20) & 0b111111111111;

        if (signExtended) {
            imm -= (imm >>> 11) << 12;
        }

        if (brackets) {
            args = getRegisterName(rd) + ", " + imm + "(" + getRegisterName(rs1) + ")";
        } else {
            args = getRegisterName(rd) + ", " + getRegisterName(rs1) + ", " + imm;
        }

        return args;
    }

    private static String disassembleITypeCommand(int command, boolean signExtended) {
        return disassembleITypeCommand(command, signExtended, false);
    }

    private static String disassembleSTypeCommand(int command, boolean brackets) {
        String args;

        int imm1 = (command >>> 7) & 0b11111;
        int rs1 = (command >>> 15) & 0b11111;
        int rs2 = (command >>> 20) & 0b11111;
        int imm2 = (command >>> 25) & 0b1111111;

        int imm = (imm2 << 5) | imm1;

        if (brackets) {
            args = getRegisterName(rs2) + ", " + imm + "(" + getRegisterName(rs1) + ")";
        } else {
            args = getRegisterName(rs1) + ", " + getRegisterName(rs2) + ", " + imm;
        }

        return args;
    }

    private static String disassembleBTypeCommand(int command) {
        String args;

        int imm1 = (command >>> 7) & 0b11111;
        int rs1 = (command >>> 15) & 0b11111;
        int rs2 = (command >>> 20) & 0b11111;
        int imm2 = (command >>> 25) & 0b1111111;

        int imm = (imm2 >>> 6) & 0b1;
        imm <<= 1;
        imm |= imm1 & 0b1;
        imm <<= 6;
        imm |= imm2 & 0b111111;
        imm <<= 4;
        imm |= (imm1 >>> 1) & 0b1111;
        imm <<= 1;

        args = getRegisterName(rs1) + ", " + getRegisterName(rs2) + ", " + imm;

        return args;
    }

    private static String disassembleUTypeCommand(int command) {
        String args;

        int rd = (command >>> 7) & 0b11111;
        int imm = command & ~0b111111111111;

        args = getRegisterName(rd) + ", " + Integer.toUnsignedString(imm);

        return args;
    }

    private static String disassembleJTypeCommand(int command) {
        String args;

        int rd = (command >>> 7) & 0b11111;
        int imm0 = (command >>> 12) & 0b11111111111111111111;

        int imm = (imm0 >>> 19) & 0b1;
        imm <<= 8;
        imm |= imm0 & 0b11111111;
        imm0 >>>= 8;
        imm <<= 1;
        imm |= imm0 & 0b1;
        imm0 >>>= 1;
        imm <<= 10;
        imm |= imm0 & 0b1111111111;
        imm <<= 1;

        args = getRegisterName(rd) + ", " + imm;

        return args;
    }

    private static String disassembleFenceCommand(int command) {
        String args = "";

        int succ = (command >>> 20) & 0b1111;
        int pred = (command >>> 24) & 0b1111;

        args = pred + ", " + succ;

        return args;
    }

    private static String getRegisterName(int reg) {
        if (reg < 0) {
            throw new IllegalArgumentException("Invalid register number");
        }
        switch (reg) {
            case 0:
                return "zero";
            case 1:
                return "ra";
            case 2:
                return "sp";
            case 3:
                return "gp";
            case 4:
                return "tp";
        }
        if (reg <= 7) {
            return "t" + (reg - 5);
        }
        if (reg <= 9) {
            return "s" + (reg - 8);
        }
        if (reg <= 17) {
            return "a" + (reg - 10);
        }
        if (reg <= 27) {
            return "s" + (reg - 16);
        }
        if (reg <= 31) {
            return "t" + (reg - 25);
        }
        throw new IllegalArgumentException("Incorrect register: " + reg);
    }
}
