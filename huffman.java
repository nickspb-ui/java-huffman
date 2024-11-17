import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;

class Node implements Comparable<Node>, Serializable {
    byte c; // Символ
    int f; // Частота появления символа
    Node l, r; // Левый/правый потомки

    Node(byte symbol, int frequency) {
        this.c = symbol;
        this.f = frequency;
    }

    Node(int frequency, Node left, Node right) { // Для родительских узлов
        this.f = frequency;
        this.l = left;
        this.r = right;
    }

    @Override
    public int compareTo(Node other) {
        return Integer.compare(this.f, other.f);
    }
}

class HuffmanCoding {
    HashMap<Byte, String> codes = new HashMap<>(); // Кодовый словарь вида "символ": "битовая последовательность"
    Node root;

    public void buildTree(byte[] text) { // Построение дерева кодирования
        HashMap<Byte, Integer> frequencyMap = new HashMap<>(); // Частоты появления каждого символа сообщения
        byte curChar;
        int curCharVal;

        for (int i = 0; i < text.length; i++) { // Инициализация словаря с частотами
            curChar = (byte) text[i];
            curCharVal = frequencyMap.getOrDefault(curChar, -1);

            if (curCharVal == -1) {
                frequencyMap.put(curChar, 1);
            } else {
                frequencyMap.put(curChar, curCharVal + 1);
            }
        }

        if (frequencyMap.size() == 1) {
            codes.put(text[0], "0");
            return;
        }

        PriorityQueue<Node> nodesQueue = new PriorityQueue<>(); // Приоритетная очередь для узлов с частотами

        for (byte key : frequencyMap.keySet()) {
            nodesQueue.add(new Node(key, frequencyMap.get(key)));
        }

        while (nodesQueue.size() > 1) {
            // Пока в очереди не останется один узел, берём два узла с наименьшими частотами
            // из очереди, создаём для них родительский узел,
            // частота которого равна сумме частот этих двух, и кладём его в очередь
            Node left = nodesQueue.poll();
            Node right = nodesQueue.poll();
            Node parent = new Node(left.f + right.f, left, right);
            nodesQueue.add(parent);
        }

        root = nodesQueue.poll();
        buildCode(root, "");

        // for (byte key : codes.keySet()) {
        // System.out.println(key + ": " + codes.get(key));
        // }
    }

    private void buildCode(Node node, String code) { // Находим код для каждого символа
        if (node == null)
            return;
        if (node.l == null && node.r == null) { // Достигли узла из сообщения, записываем код в словарь
            codes.put(node.c, code);
        } else {
            buildCode(node.l, code + '0');
            buildCode(node.r, code + '1');
        }
    }

    public StringBuilder encode(byte[] text) { // Кодируем сообщение
        StringBuilder encodedText = new StringBuilder();

        for (int i = 0; i < text.length; i++) {
            encodedText.append(codes.get(text[i]));
        }

        return encodedText;
    }

    public byte[] translateToBits(StringBuilder encodedText) {
        byte[] bytesToOutput = new byte[(encodedText.length() + 7) / 8];
        // Для записи битовой последовательности в файл нужно целое число байт

        for (int i = 0; i < encodedText.length(); i++) {
            if (encodedText.charAt(i) == '1') {
                bytesToOutput[i / 8] |= 1 << (7 - (i % 8));
            }
        }

        return bytesToOutput;
    }

    public List<Byte> decode(byte[] bytes, int bitLength, HashMap<String, Byte> decodingBook) { // Декодирование
                                                                                                // сообщения
        List<Byte> decodedText = new ArrayList<Byte>();
        String encodedText = translateFewBytesToString(bytes, bitLength);
        StringBuilder currentBits = new StringBuilder();

        for (int i = 0; i < encodedText.length(); i++) {
            currentBits.append(encodedText.charAt(i));

            if (decodingBook.get(currentBits.toString()) != null) {
                decodedText.add(decodingBook.get(currentBits.toString()));
                currentBits.setLength(0);
            }
        }

        return decodedText;
    }

    public void encodeToFile(String inputName, String outputName) throws IOException {
        byte[] text = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(inputName));

        buildTree(text);
        StringBuilder encodedText = encode(text);
        byte[] encodedData = translateToBits(encodedText), encodedSymbInBytes;
        int messageBitLength = encodedText.length();
        int maxCharLength = 0;

        for (String code : codes.values()) {
            if (code.length() > maxCharLength) {
                maxCharLength = code.length();
            }
        }

        maxCharLength = (maxCharLength + 7) / 8;

        DataOutputStream stream = new DataOutputStream(new FileOutputStream(outputName));
        stream.writeInt(codes.size());
        stream.writeInt(maxCharLength);

        for (byte code : codes.keySet()) {
            stream.write(code);
            stream.writeInt(codes.get(code).length());
            encodedSymbInBytes = translateToBits(new StringBuilder(codes.get(code)));
            stream.write(encodedSymbInBytes);
            for (int i = encodedSymbInBytes.length; i < maxCharLength; i++) {
                stream.write(0);
            }
        }

        stream.writeInt(messageBitLength);
        stream.write(encodedData);
        stream.close();
    }

    public int readIntFromFile(DataInputStream stream) throws IOException {
        Scanner scanner = new Scanner(stream);
        int val = scanner.nextInt();
        scanner.close();
        return val;
    }

    public String translateFewBytesToString(byte[] encodedSymbInBytes, int numOfBits) {
        StringBuilder encodedSymbStr = new StringBuilder();
        int currentLength = 0;

        for (byte b : encodedSymbInBytes) {
            for (int j = 7; j >= 0; j--) { // bytesToOutput[i / 8] |= 1 << (7 - (i % 8));
                int bit = (b >> j) & 1;
                encodedSymbStr.append(bit);
                currentLength++;
                if (currentLength == numOfBits) {
                    return encodedSymbStr.toString();
                }
            }
        }

        return encodedSymbStr.toString();
    }

    public List<Byte> decodeFromFile(String inputName) throws IOException, ClassNotFoundException {
        HashMap<String, Byte> decodingBook = new HashMap<>(); // Кодовый словарь вида "битовая последовательность":
                                                              // "символ"
        DataInputStream stream = new DataInputStream(new FileInputStream(inputName));
        int numOfSymbs = stream.readInt();
        int maxEncodedSymbLength = stream.readInt();
        byte symb;
        int encodedSymbLength;
        byte[] encodedSymbInBytes = new byte[maxEncodedSymbLength];

        for (int i = 0; i < numOfSymbs; i++) {
            symb = (byte) stream.read();
            encodedSymbLength = stream.readInt();
            stream.read(encodedSymbInBytes);

            decodingBook.put(translateFewBytesToString(encodedSymbInBytes, encodedSymbLength), symb);
        }

        int bitLength = stream.readInt();
        byte[] encodedData = stream.readAllBytes();
        stream.close();

        return decode(encodedData, bitLength, decodingBook);
    }
}

public class huffman {
    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                System.out.println("Enter command 'encode'/'decode' 'filename'");
                System.exit(-1);
            }
            String action = args[0];
            if (action.equals("encode")) {
                HuffmanCoding huffmanCoding = new HuffmanCoding();
                String inputName = args[1];
                String outputName = "encoded.bin";

                if (!new File(inputName).exists()) {
                    System.out.println("Input file doesn't exist.");
                    System.exit(-1);
                }

                huffmanCoding.encodeToFile(inputName, outputName);
                System.out.println("Message was encoded successfully to 'encoded.bin'");
            } else if (action.equals("decode")) {
                HuffmanCoding huffmanCoding = new HuffmanCoding();
                String inputName = args[1];
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

                System.out.println("Message was decoded successfully to 'output.txt'.");
            } else {
                System.out.println("Unknown command. Use 'encode'/'decode' 'filename'");
                System.exit(-1);
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
