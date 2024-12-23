import com.google.gson.Gson;
// import com.dampcake.bencode.Bencode; - available if you need it!
class Decoded{
	String stringDecoded=null;
	Integer intDecoded= null;
	Decoded(String stringDecoded){
		this.stringDecoded=stringDecoded;
	}
	Decoded(Integer intDecoded){
		this.intDecoded=intDecoded;
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
      int firstColonIndex = 0;
      for(int i = 0; i < bencodedString.length(); i++) { 
        if(bencodedString.charAt(i) == ':') {
          firstColonIndex = i;
          break;
        }
      }
      int length = Integer.parseInt(bencodedString.substring(0, firstColonIndex));
      return new Decoded(bencodedString.substring(firstColonIndex+1, firstColonIndex+1+length));
    } 
    else if(Character.isAlphabetic(bencodedString.charAt(0))) {
    	int firstColonIndex=0;
    	String ans= bencodedString.substring(firstColonIndex+1,bencodedString.length()-1);
    	if(ans.length()>1&&ans.charAt(0)=='0') {
    		return new Decoded("invalid");
    	}
    	return new Decoded(Integer.parseInt(ans));
    }
    else {
      throw new RuntimeException("Only strings are supported at the moment");
    }
  }
  
}
