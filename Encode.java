import Huffman.HuffmanCoding;
import java.io.*;

public class Encode {
    public static void main(String[] args) {
        try {
            HuffmanCoding huffmanCoding = new HuffmanCoding();
            String inputName = args[0];
            String outputName = "encoded.bin";

            if (!new File(inputName).exists()) {
                System.out.println("Input file doesn't exist.");
                System.exit(-1);
            }

            huffmanCoding.encodeToFile(inputName, outputName);
            System.out.println("Message was encoded successfully.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
