// IN2011 Computer Networks
// Coursework 2023/2024
//
// Construct the hashID for a string

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class HashID {
	public static void main(String[] args) {
		String s1 = "Hello World!";
		String s2 = "martin.brain@city.ac.uk:MyCoolImplementation,1.41,test-node-2";
		System.out.println(s1 + "\n\t> " + generate(s1));
		System.out.println(s2 + "\n\t> " + generate(s2));
		String h1 = "0f033be6cea034bd45a0352775a219ef5dc7825ce55d1f7dae9762d80ce64411";
		String h2 = "0f0139b167bb7b4a416b8f6a7e0daa7e24a08172b9892171e5fdc615bb7f999b";
		System.out.println(h1 + '\n' + h2 + "\n\t> Distance: " + calculateDistance(h1, h2));
	}

    public static byte[] computeHashID(String line) throws Exception {
		if (line.endsWith("\n")) {
	    	// What this does and how it works is covered in a later lecture
	    	MessageDigest md = MessageDigest.getInstance("SHA-256");
	    	md.update(line.getBytes(StandardCharsets.UTF_8));
	    	return md.digest();
		} else {
	    	// 2D#4 computes hashIDs of lines, i.e. strings ending with '\n'
	    	throw new Exception("No new line at the end of input to HashID");
		}
    }

	public static String bytesToHex(byte[] bytes) {
		BigInteger bigInt = new BigInteger(1, bytes);
		String hex = bigInt.toString(16);
		int paddingLength = (bytes.length * 2) - hex.length();
		if (paddingLength > 0) {
			return String.format("%0" + paddingLength + "d", 0) + hex;
		} else {
			return hex;
		}
    }

	public static String generate(String line) {
        try {
			if (!line.endsWith("\n")) line += '\n';
            return bytesToHex(computeHashID(line));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

	public static int calculateDistance(String hashID1, String hashID2) {
		BigInteger bigInt1 = new BigInteger(hashID1, 16);
		BigInteger bigInt2 = new BigInteger(hashID2, 16);
		BigInteger distance = bigInt1.xor(bigInt2);
		if (distance.equals(BigInteger.ZERO)) return 0;
		String binaryDistance = distance.toString(2);
        return binaryDistance.length();
	}
}
