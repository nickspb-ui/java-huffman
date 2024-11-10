package Huffman;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

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

public class HuffmanCoding {
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

        for (byte key : codes.keySet()) {
            System.out.println(key + ": " + codes.get(key));
        }
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

    public List<Byte> decode(byte[] bytes, int bitLength) { // Декодируем сообщение
        List<Byte> decodedText = new ArrayList<Byte>();
        Node curNode = root;
        int j = 0;

        for (byte curByte : bytes) {
            for (int i = 7; i >= 0; i--) {
                if (j == bitLength) {
                    break;
                }
                if (((curByte >> i) & 1) == 0) {
                    curNode = curNode.l;
                } else {
                    curNode = curNode.r;
                }
                if (curNode.l == null && curNode.r == null) {
                    decodedText.add(curNode.c);
                    curNode = root;
                }
                j++;
            }
        }

        return decodedText;
    }

    public void encodeToFile(String inputName, String outputName) throws IOException {
        byte[] text = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(inputName));

        buildTree(text);
        StringBuilder encodedText = encode(text);
        byte[] encodedData = translateToBits(encodedText);
        int bitLength = encodedText.length();

        ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(outputName));
        stream.writeObject(root);
        stream.writeInt(bitLength);
        stream.write(encodedData);
        stream.close();
    }

    public List<Byte> decodeFromFile(String inputName) throws IOException, ClassNotFoundException {
        ObjectInputStream stream = new ObjectInputStream(new FileInputStream(inputName));
        root = (Node) stream.readObject();
        int bitLength = stream.readInt();
        byte[] encodedData = stream.readAllBytes();
        stream.close();

        return decode(encodedData, bitLength);
    }
}
