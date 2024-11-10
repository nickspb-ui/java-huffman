import Huffman.HuffmanCoding;
import java.io.*;
import java.util.List;

public class Decode {
    public static void main(String[] args) {
        try {
            HuffmanCoding huffmanCoding = new HuffmanCoding();
            String inputName = args[0];
            String decodedOutputName = "output.txt";

            if (!new File(inputName).exists()) {
                System.out.println("Input file doesn't exist.");
                System.exit(-1);
            }

            List<Byte> decodedText = huffmanCoding.decodeFromFile(inputName);

            FileOutputStream stream = new FileOutputStream(decodedOutputName);

            for (Byte b : decodedText) {
                stream.write(b);
            }
            stream.close();

            System.out.println("Message was decoded successfully.");

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
