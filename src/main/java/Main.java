import com.dampcake.bencode.Bencode;
import com.dampcake.bencode.Type;
import com.google.gson.Gson;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
public class Main {
  private static final Gson gson = new Gson();
  public static void main(String[] args) throws Exception {
    String command = args[0];
    if (command.equals("decode")) {
      String bencodedValue = args[1];
      String decoded;
      switch (bencodedValue.charAt(0)) {
                                case 'i' -> {
					Bencode bencode = new Bencode(true);
					decoded = "" + bencode.decode(bencodedValue.getBytes(), Type.NUMBER);
				}
				case 'l' -> {
					Bencode bencode = new Bencode(false);
					decoded = gson.toJson(bencode.decode(bencodedValue.getBytes(), Type.LIST));
				}
				case 'd' -> {
					Bencode bencode = new Bencode(false);
					decoded = gson.toJson(bencode.decode(bencodedValue.getBytes(), Type.DICTIONARY));
				}
				default -> {
					try {
						decoded = gson.toJson(decodeBencode(bencodedValue));
					} catch (RuntimeException e) {
						System.out.println(e.getMessage());
						return;
					}
				}
			}
			System.out.println(decoded);
		} else if (command.equals("info")) {
			String filePath = args[1];
			Torrent torrent = new Torrent(Files.readAllBytes(Path.of(filePath)));
			System.out.println("Tracker URL: " + torrent.announce);
			System.out.println("Length: " + torrent.length);
		} else {
			System.out.println("Unknown command: " + command);
		}
	}
	private static String decodeBencode(String bencodedString) {
		if (Character.isDigit(bencodedString.charAt(0))) {
			int firstColonIndex = 0;
			for (int i = 0; i < bencodedString.length(); i++) {
				if (bencodedString.charAt(i) == ':') {
					firstColonIndex = i;
					break;
				}
			}
			int length = Integer.parseInt(bencodedString.substring(0, firstColonIndex));
			return bencodedString.substring(firstColonIndex + 1, firstColonIndex + 1 + length);
		} else {
			throw new RuntimeException("Only strings are supported at the moment");
		}
	}
}
class Torrent {
	public String announce;
	public long length;
	public Torrent(byte[] bytes) {
		Bencode bencode = new Bencode(false);
		Map<String, Object> root = bencode.decode(bytes, Type.DICTIONARY);
		Map<String, Object> info = (Map<String, Object>) root.get("info");
		announce = (String) root.get("announce");
		length = (long) info.get("length");
	}
}