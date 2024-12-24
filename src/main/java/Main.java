import java.util.ArrayList;

import com.google.gson.Gson;
// import com.dampcake.bencode.Bencode; - available if you need it!
class Decoded{
	String stringDecoded=null;
	Long longDecoded= null;
	ArrayList<Object> listDecoded=null;
	Decoded(String stringDecoded){
		this.stringDecoded=stringDecoded;
	}
	Decoded(Long longDecoded){
		this.longDecoded=longDecoded;
	}
	Decoded(ArrayList<Object> listDecoded){
		this.listDecoded=listDecoded;
	}
}

public class Main {
  private static final Gson gson = new Gson();
  

  public static void main(String[] args) throws Exception {
    // You can use print statements as follows for debugging, they'll be visible when running tests.
//    System.err.println("Logs from your program will appear here!");
    
    String command = args[0];
    if("decode".equals(command)) {
      //  Uncomment this block to pass the first stage
        String bencodedValue = args[1];
        Decoded decoded;
//        System.out.println(bencodedValue);
        try {
          decoded = decodeBencode(bencodedValue);
        } catch(RuntimeException e) {
          System.out.println(e.getMessage());
          return;
        }
        String res=gson.toJson(decoded);
        System.out.println(res.substring(res.indexOf(':')+1,res.length()-1));
//        System.out.println(gson.toJson(decoded));

    } else {
      System.out.println("Unknown command: " + command);
    }

  }

  static Decoded decodeBencode(String bencodedString) {
    if (Character.isDigit(bencodedString.charAt(0))) {
      return new Decoded(decodeStringBencode(bencodedString));
    } 
    else if(Character.isAlphabetic(bencodedString.charAt(0))) {
    	if(bencodedString.charAt(0)=='i') {
        	return new Decoded(decodeLongBencode(bencodedString));
        }
    	else if(bencodedString.charAt(0)=='l') {
    		ArrayList<Object> decodedList=new ArrayList<Object>();
    		int startIndex=1;
    		int lastIndex=bencodedString.length()-2;
    		while(startIndex<lastIndex) {
    		if (Character.isDigit(bencodedString.charAt(startIndex))) {
    			String strValue=decodeStringBencode(bencodedString.substring(startIndex));
    			int length=strValue.length();
    			decodedList.add(strValue) ;
    			startIndex=startIndex+length+2;
    		} 
    		else {
    			long longValue=decodeLongBencode(bencodedString.substring(startIndex));
    			int length=String.valueOf(longValue).length();
    			decodedList.add(longValue);
    			startIndex=startIndex+length+2;
    
    		}
    		}
    		return new Decoded(decodedList);
    		
    	}
    	else {
    		throw new RuntimeException("Only");
    	}
    }
    else {
      throw new RuntimeException("Only strings are supported at the moment");
    }
  }
  static String decodeStringBencode(String bencodedString) {
	  int firstColonIndex = 0;
      for(int i = 0; i < bencodedString.length(); i++) { 
        if(bencodedString.charAt(i) == ':') {
          firstColonIndex = i;
          break;
        }
      }
      int length = Integer.parseInt(bencodedString.substring(0, firstColonIndex));
      return bencodedString.substring(firstColonIndex+1, firstColonIndex+1+length);
  }
  
  static Long decodeLongBencode(String bencodedString) {
	  int firstColonIndex=0;
  	String ans= bencodedString.substring(firstColonIndex+1,bencodedString.indexOf('e'));
  	if(ans.length()>1&&ans.charAt(0)=='0') {
  		return null;
  	}
  	return Long.parseLong(ans);
  }
  
  
  
}
