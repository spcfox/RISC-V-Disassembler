import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class ElfReader implements AutoCloseable {
    private final DataBufferedReader reader;
    private final static byte[] MAGIC_NUMBERS = new byte[]{0x7f, 0x45, 0x4c, 0x46};
    private int eShoff, eShNum, eShStrndx;
    private int symtableOffset = -1;
    private int symtableSize = -1;
    private int strtableOffset = -1;
    private int strtableSize = -1;
    private int textOffset = -1;
    private int textSize = -1;
    private int textAddr = -1;
    private char[] stringTable;
    private Map<Integer, String> marks;

    public ElfReader(DataBufferedReader reader) {
        this.reader = reader;
        reader.enableLittleEndian();
    }

    public ElfReader(String filename) throws IOException {
        this(new DataBufferedReader(filename));
    }

    public void disassemble(Writer writer) throws IOException {
        readFileHeader();
        readStringTableHeader();
        readStringTable();
        readSectionsHeaders();
        readSymTable();
        disassembleText(writer);
    }

    private void readFileHeader() throws IOException {
        for (byte b : MAGIC_NUMBERS) {
            if (reader.read() != b) {
                throw new IOException("Invalid magic numbers");
            }
        }

        if (reader.read() != 1) { // EI_CLASS
            throw new IOException("Invalid format: disassembler supports only 32-bit files");
        }
        if (reader.read() != 1) { // EI_DATA
            throw new IOException("Invalid format: RISC-V supports only little endian");
        }
        if (reader.read() != 1) { // EI_VERSION
            throw new IOException("Invalid elf version");
        }
        reader.skipBytes(11);

        if (reader.readInt(2) != 0xf3) { // e_machine
            throw new IOException("Invalid format: disassembler supports only RISC-V files");
        }

        if (reader.readInt(4) != 1) { // e_version
            throw new IOException("Invalid version");
        }
        reader.skipBytes(8);
        eShoff = reader.readInt(4);
        reader.skipBytes(12);
        eShNum = reader.readInt(2);
        eShStrndx = reader.readInt(2);
    }

    private void readStringTableHeader() throws IOException {
        reader.jump(eShoff + 40 * eShStrndx);
        int shType;
        reader.skipBytes(4);
        shType = reader.readInt(4);
        if (shType != 3) {
            throw new IOException("Invalid e_shndx in file header");
        }
        reader.skipBytes(8);
        strtableOffset = reader.readInt(4);
        strtableSize = reader.readInt(4);
        reader.skipBytes(16);
    }

    private void readStringTable() throws IOException {
        if (strtableOffset < -1) {
            throw new IOException("String table not found");
        }
        reader.jump(strtableOffset);

        stringTable = new char[strtableSize];
        for (int i = 0; i < strtableSize; i++) {
            stringTable[i] = (char) reader.read();
        }
        if (stringTable[strtableSize - 1] != 0) {
            throw new IOException("Incorrect string table format");
        }
    }

    private void readSectionsHeaders() throws IOException {
        reader.jump(eShoff);
        for (int i = 0; i < eShNum; i++) {
            int shName, shType, shAddr, shOffset, shSize;
            shName = reader.readInt(4);
            shType = reader.readInt(4);

            if (shType != 2 && shType != 3 && !getName(shName).equals(".text")) {
                reader.skipBytes(32);
                continue;
            }

            reader.skipBytes(4);
            shAddr = reader.readInt(4);
            shOffset = reader.readInt(4);
            shSize = reader.readInt(4);
            reader.skipBytes(16);

            if (shType == 2) {
                symtableOffset = shOffset;
                symtableSize = shSize;
            } else if (shType == 3) {
                if (shOffset != strtableOffset) {
                    throw new IOException("Invalid format: file contains for multiple string tables");
                }
            } else {
                textAddr = shAddr;
                textOffset = shOffset;
                textSize = shSize;
            }
        }
        if (textOffset == -1) {
            throw new IOException("No .text section in the file");
        }
        if (symtableOffset == -1) {
            throw new IOException("No symbol table in the file");
        }
    }

    private void readSymTable() throws IOException {
        marks = new HashMap<>();
        if (symtableOffset == -1) {
            return;
        }
        reader.jump(symtableOffset);

        for (int i = 0; i < symtableSize; i += 16) {
            int stName, stValue, stInfo;
            stName = reader.readInt(4);
            stValue = reader.readInt(4);
            reader.skipBytes(4);
            stInfo = reader.read();
            reader.skipBytes(3);

            if ((stInfo & 0xf) == 2) {
                marks.put(stValue, getName(stName));
            }
        }
    }

    private void disassembleText(Writer writer) throws IOException {
        if (strtableOffset == -1) {
            throw new IOException("Section .text not found");
        }

        int commands = textSize / 4;
        reader.jump(textOffset);
        for (int i = 0; i < commands; i++) {
            writer.write(RiscVDisassembler.disassemble(textAddr + 4 * i, reader.readInt(4), marks));
            writer.write(System.lineSeparator());
        }
    }

    private String getName(int shName) {
        StringBuilder sb = new StringBuilder();
        while (stringTable[shName] != 0){
            sb.append(stringTable[shName++]);
        }
        return sb.toString();
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
