import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;

class Decoded {
    String stringDecoded = null;
    Long longDecoded = null;
    ArrayList<Object> listDecoded = null;
    HashMap<String, Object> dictDecoded=null;

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
    private static final Gson gson = new Gson();

    public static void main(String[] args) throws Exception {
        String command = args[0];
        if ("decode".equals(command)) {
            String bencodedValue = args[1];
            Decoded decoded;
            try {
                decoded = decodeBencode(bencodedValue);
            } catch (RuntimeException e) {
                System.out.println(e.getMessage());
                return;
            }
            String res = gson.toJson(decoded);
            System.out.println(res.substring(res.indexOf(':') + 1, res.length() - 1));
        } else {
            System.out.println("Unknown command: " + command);
        }
    }

    static Decoded decodeBencode(String bencodedString) {
        if (bencodedString.startsWith("i")) {
            return new Decoded(decodeLongBencode(bencodedString));
        } else if (bencodedString.startsWith("l")) {
            return new Decoded(decodeListBencode(bencodedString));
        }else if(bencodedString.startsWith("d")) {
        	return new Decoded(decodeDictBencode(bencodedString));
        }
        else if (Character.isDigit(bencodedString.charAt(0))) {
            return new Decoded(decodeStringBencode(bencodedString));
        } else {
            throw new RuntimeException("Invalid bencoded format");
        }
    }

    static String decodeStringBencode(String bencodedString) {
        int colonIndex = bencodedString.indexOf(':');
        int length = Integer.parseInt(bencodedString.substring(0, colonIndex));
        return bencodedString.substring(colonIndex + 1, colonIndex + 1 + length);
    }

    static Long decodeLongBencode(String bencodedString) {
        String value = bencodedString.substring(1, bencodedString.indexOf('e'));
        return Long.parseLong(value);
    }
    
    static HashMap<String,Object> decodeDictBencode(String bencodedString){
    	HashMap<String,Object> dictionary=new HashMap<String, Object>();
//    	System.out.println(dictionary);
    	int index=1;
//    	System.out.println(index);
    	int key=1;
		String DictKey=null;
		Object DictValue=null;
    	while(index<bencodedString.length()-1) {
    		char current=bencodedString.charAt(index);
//    		System.out.println(index+"------------------"+current);
    		//let key=1;
    		//if key=1 then means we have key other wise we have value;\
    		
    		
    		if(Character.isDigit(current)) {
    			String strValue=decodeStringBencode(bencodedString.substring(index));
//    			System.out.println(strValue+"---------------------------------------------");
    			if(key==1) {
    				DictKey=strValue;
    				key=0;
    			}else {
    				DictValue=strValue;
    				key=1;
    			}
    			index += strValue.length() + String.valueOf(strValue.length()).length() + 1;
//    			System.out.println(index);
    			
    		}
    		else if(current == 'i') {
    			String intPart = bencodedString.substring(index);
                Long intValue = decodeLongBencode(intPart);
//                System.out.println(intPart+"-----------------"+intValue);
                DictValue=intValue;
//                System.out.println("DictValue: "+DictValue);
                index += String.valueOf(intValue).length() + 2;
                key=1;
    		}
    		else if(current=='l') {
    			String sublistStr=findNextBencodeBlock(bencodedString.substring(index));
    			ArrayList<Object> list=decodeListBencode(bencodedString);
    			DictValue=list;
    			index += sublistStr.length();
    			key=1;
    			
    		}
    		else if(current=='d') {
//    			String sublist = findNextBencodeBlock(bencodedString.substring(index));
//                list.add(decodeListBencode(sublist));
//                index += sublist.length();
    			
    			String subDict=findNextBencodeBlock(bencodedString.substring(index));
    			DictValue=decodeDictBencode(subDict);
    			index=index+subDict.length();
    			key=1;
    			
    		}else {
                throw new RuntimeException("Invalid bencoded list format");
            }
    		if(key==1) {
    			dictionary.put(DictKey,DictValue);
    		}
    	}
    	
    	
    	
    	
		return dictionary;
    	
    }

    static ArrayList<Object> decodeListBencode(String bencodedString) {
        ArrayList<Object> list = new ArrayList<>();
        int index = 1; // Skip 'l'
        while (index < bencodedString.length() - 1) { // Skip last 'e'
            char current = bencodedString.charAt(index);
            if (Character.isDigit(current)) {
                String strValue = decodeStringBencode(bencodedString.substring(index));
                list.add(strValue);
                index += strValue.length() + String.valueOf(strValue.length()).length() + 1; // Advance index
            } else if (current == 'i') {
                String intPart = bencodedString.substring(index);
                Long intValue = decodeLongBencode(intPart);
                list.add(intValue);
                index += String.valueOf(intValue).length() + 2; // Account for 'i' and 'e'
            } else if (current == 'l') {
                String sublist = findNextBencodeBlock(bencodedString.substring(index));
                list.add(decodeListBencode(sublist));
                index += sublist.length();
            }else if (current == 'd') {
            	String subDict=findNextBencodeBlock(bencodedString.substring(index));
                list.add(decodeDictBencode(subDict));
                index += subDict.length();
            }
            else {
                throw new RuntimeException("Invalid bencoded list format");
            }
        }
        return list;
    }

    static String findNextBencodeBlock(String bencodedString) {
        int balance = 0;
        int endIndex = 0;
        for (int i = 0; i < bencodedString.length(); i++) {
            char current = bencodedString.charAt(i);
            if (current == 'l'||current=='i'||current=='d') balance++;
            if (current == 'e') balance--;
            if(current==':') {
            	
            	int startIndex=i-1;
            	while(Character.isDigit(bencodedString.charAt(startIndex)) ) {
            		startIndex--;
            	}
            	int len=Integer.valueOf(bencodedString.substring(startIndex+1,i)); 
            	i=i+len;
            }
            if (balance == 0) {
                endIndex = i + 1;
                break;
            }
        }
        return bencodedString.substring(0, endIndex);
    }
}
