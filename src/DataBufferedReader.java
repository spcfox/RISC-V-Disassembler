import java.io.*;

public class DataBufferedReader implements AutoCloseable {
    private final String filename;
    private BufferedInputStream input;
    private int position;
    private boolean bigEndian = true;

    public DataBufferedReader(String filename) throws IOException {
        this.filename = filename;
        reset();
    }

    public void reset() throws IOException {
        if (input != null) {
            input.close();
        }
        position = 0;
        input = new BufferedInputStream(new FileInputStream(filename));
    }

    public int getPosition() {
        return position;
    }

    public int read() throws IOException {
        int nextByte = input.read();
        if (nextByte == -1) {
            throw new EOFException("Try to read after end file");
        }
        position++;
        return nextByte;
    }

    public int readInt(int length) throws IOException {
        if (length > 4) {
            throw new IllegalArgumentException("Int cannot be more than 4 bytes");
        }
        int[] bytes = new int[length];
        for (int i = 0; i < length; i++) {
            if (bigEndian) {
                bytes[i] = read();
            } else {
                bytes[length - i - 1] = read();
            }
        }

        int result = 0;
        for (int x : bytes) {
            result <<= 8;
            result |= x;
        }
        return result;
    }

    public void skipBytes(int count) throws IOException {
        position += count;
        input.readNBytes(count);
    }

    public void enableBigEndian() {
        bigEndian = true;
    }

    public void enableLittleEndian() {
        bigEndian = false;
    }

    public boolean hasNext() throws IOException {
        return input.available() > 0;
    }

    @Override
    public void close() throws IOException {
        input.close();
    }

    public void jump(int index) throws IOException {
        if (index < position) {
            reset();
        }
        skipBytes(index - getPosition());
    }
}
