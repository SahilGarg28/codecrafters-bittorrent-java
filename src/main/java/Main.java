import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;

class Decoded {
    // Holds the decoded values as different data types
    String stringDecoded = null;
    Long longDecoded = null;
    ArrayList<Object> listDecoded = null;
    HashMap<String, Object> dictDecoded = null;

    // Constructors for initializing different types of decoded values
    Decoded(String stringDecoded) {
        this.stringDecoded = stringDecoded;
    }

    Decoded(Long longDecoded) {
        this.longDecoded = longDecoded;
    }

    Decoded(ArrayList<Object> listDecoded) {
        this.listDecoded = listDecoded;
    }

    Decoded(HashMap<String, Object> dictDecoded) {
        this.dictDecoded = dictDecoded;
    }
}

public class Main {
    private static final Gson gson = new Gson(); // For converting objects to JSON format

    public static void main(String[] args) throws Exception {
        // Get the command-line arguments
        String command = args[0];
        if ("decode".equals(command)) {
            String bencodedValue = args[1];
            Decoded decoded;
            try {
                // Attempt to decode the bencoded string
                decoded = decodeBencode(bencodedValue);
            } catch (RuntimeException e) {
                // Handle invalid bencoded input
                System.out.println(e.getMessage());
                return;
            }
            // Convert the decoded object to JSON and print the result
            String res = gson.toJson(decoded);
            System.out.println(res.substring(res.indexOf(':') + 1, res.length() - 1));
        } else {
            // Handle unknown commands
            System.out.println("Unknown command: " + command);
        }
    }

    static Decoded decodeBencode(String bencodedString) {
        // Determine the type of bencoded data and decode accordingly
        if (bencodedString.startsWith("i")) {
            return new Decoded(decodeLongBencode(bencodedString));
        } else if (bencodedString.startsWith("l")) {
            return new Decoded(decodeListBencode(bencodedString));
        } else if (bencodedString.startsWith("d")) {
            return new Decoded(decodeDictBencode(bencodedString));
        } else if (Character.isDigit(bencodedString.charAt(0))) {
            return new Decoded(decodeStringBencode(bencodedString));
        } else {
            // Throw an error for unsupported or invalid formats
            throw new RuntimeException("Invalid bencoded format");
        }
    }

    static String decodeStringBencode(String bencodedString) {
        // Decode a bencoded string (e.g., "4:test" -> "test")
        int colonIndex = bencodedString.indexOf(':'); // Find the ':' separating length and value
        int length = Integer.parseInt(bencodedString.substring(0, colonIndex)); // Parse the length
        return bencodedString.substring(colonIndex + 1, colonIndex + 1 + length); // Extract the string
    }

    static Long decodeLongBencode(String bencodedString) {
        // Decode a bencoded integer (e.g., "i42e" -> 42)
        String value = bencodedString.substring(1, bencodedString.indexOf('e')); // Extract value between 'i' and 'e'
        return Long.parseLong(value); // Convert the string to a long
    }

    static HashMap<String, Object> decodeDictBencode(String bencodedString) {
        // Decode a bencoded dictionary (e.g., "d3:key5:valuee")
        HashMap<String, Object> dictionary = new HashMap<>(); // Initialize the dictionary
        int index = 1; // Skip the initial 'd'
        int key = 1; // Tracks whether the current value is a key (1) or value (0)
        String DictKey = null;
        Object DictValue = null;

        while (index < bencodedString.length() - 1) { // Process until the closing 'e'
            char current = bencodedString.charAt(index);
            
            if (Character.isDigit(current)) {
                // Handle string keys or values
                String strValue = decodeStringBencode(bencodedString.substring(index));
                if (key == 1) {
                    DictKey = strValue; // Assign as key
                    key = 0;
                } else {
                    DictValue = strValue; // Assign as value
                    key = 1;
                }
                index += strValue.length() + String.valueOf(strValue.length()).length() + 1; // Move index past string
            } else if (current == 'i') {
                // Handle integer values
                String intPart = bencodedString.substring(index);
                Long intValue = decodeLongBencode(intPart);
                DictValue = intValue; // Assign as value
                index += String.valueOf(intValue).length() + 2; // Move index past integer
                key = 1;
            } else if (current == 'l') {
                // Handle nested lists
                String sublistStr = findNextBencodeBlock(bencodedString.substring(index));
                ArrayList<Object> list = decodeListBencode(sublistStr);
                DictValue = list; // Assign as value
                index += sublistStr.length(); // Move index past list
                key = 1;
            } else if (current == 'd') {
                // Handle nested dictionaries
                String subDict = findNextBencodeBlock(bencodedString.substring(index));
                DictValue = decodeDictBencode(subDict); // Recursively decode dictionary
                index += subDict.length(); // Move index past dictionary
                key = 1;
            } else {
                throw new RuntimeException("Invalid bencoded dictionary format");
            }

            if (key == 1) {
                // Add key-value pair to the dictionary
                dictionary.put(DictKey, DictValue);
            }
        }

        return dictionary; // Return the decoded dictionary
    }

    static ArrayList<Object> decodeListBencode(String bencodedString) {
        // Decode a bencoded list (e.g., "l4:spam4:eggse")
        ArrayList<Object> list = new ArrayList<>(); // Initialize the list
        int index = 1; // Skip the initial 'l'
        while (index < bencodedString.length() - 1) { // Process until the closing 'e'
            char current = bencodedString.charAt(index);
            if (Character.isDigit(current)) {
                // Handle string elements
                String strValue = decodeStringBencode(bencodedString.substring(index));
                list.add(strValue);
                index += strValue.length() + String.valueOf(strValue.length()).length() + 1; // Move index past string
            } else if (current == 'i') {
                // Handle integer elements
                String intPart = bencodedString.substring(index);
                Long intValue = decodeLongBencode(intPart);
                list.add(intValue);
                index += String.valueOf(intValue).length() + 2; // Move index past integer
            } else if (current == 'l') {
                // Handle nested lists
                String sublist = findNextBencodeBlock(bencodedString.substring(index));
                list.add(decodeListBencode(sublist));
                index += sublist.length(); // Move index past list
            } else if (current == 'd') {
                // Handle nested dictionaries
                String subDict = findNextBencodeBlock(bencodedString.substring(index));
                list.add(decodeDictBencode(subDict));
                index += subDict.length(); // Move index past dictionary
            } else {
                throw new RuntimeException("Invalid bencoded list format");
            }
        }
        return list; // Return the decoded list
    }

    static String findNextBencodeBlock(String bencodedString) {
        // Finds the next complete bencoded block (e.g., a full list, dictionary, or string)
        int balance = 0;
        int endIndex = 0;
        int lastIndex = 0;
        for (int i = 0; i < bencodedString.length(); i++) {
            char current = bencodedString.charAt(i);
            if (current == 'l' || current == 'i' || current == 'd') balance++;
            if (current == 'e') balance--;

            if (current == ':') {
                // Handle strings by skipping over their content
                int startIndex = i - 1;
                while (Character.isDigit(bencodedString.charAt(startIndex))) {
                    startIndex--;
                }
                if (lastIndex > startIndex) {
                    startIndex = lastIndex;
                }
                int len = Integer.parseInt(bencodedString.substring(startIndex + 1, i));
                i = i + len; // Skip over the string content
                lastIndex = i;
            }

            if (balance == 0) {
                endIndex = i + 1; // Found the end of the block
                break;
            }
        }
        return bencodedString.substring(0, endIndex); // Return the complete block
    }
}
