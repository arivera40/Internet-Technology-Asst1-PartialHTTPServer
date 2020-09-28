package test_HTTP;

public class HttpParser {
	private String cmd;
	private String resource;
	private double version;
	
	public HttpParser() {
		resource = "";
		cmd = "";
		version = 0;
	}
	
	//returns int to indicate status of request
	public int parseRequest(String line) {
		String[] parsedLine = line.split(" ");
		if(parsedLine.length != 3) return -1; // 400 Bad Request
		
		//Command format checking
		if(parsedLine[0].equals("get") || parsedLine[0].equals("post") || parsedLine[0].equals("head") ||
				parsedLine[0].equals("delete") || parsedLine[0].equals("put") || parsedLine[0].equals("link") || parsedLine[0].equals("unlink")) {	
			return -1;	// 400 Bad Request
		}else if(parsedLine[0].equals("DELETE") || parsedLine[0].equals("PUT") || parsedLine[0].equals("LINK") || parsedLine[0].equals("UNLINK")) {
			return -2; // 501 Not Implemented
		}else if(!parsedLine[0].equals("GET") || !parsedLine[0].equals("POST") || !parsedLine[0].equals("HEAD")){
			return -1; // 400 Bad Request
		}else {
			cmd = parsedLine[0];
		}
		
		//Resource - Not sure if any format checking is necessary?
		resource = parsedLine[1];
		
		//Version number format checking
		if(parsedLine[2].indexOf("HTTP/") != 0 || !Character.isDigit(parsedLine[2].charAt(5)) ) {
			return -1;	// 400 Bad Request
		}else {
			boolean decimal = false;
			parsedLine[2] = parsedLine[2].substring(5);
			for(int i=1; i < parsedLine[2].length(); i++) {	//we know first index is a digit
				if(!Character.isDigit(parsedLine[2].charAt(i)) || parsedLine[2].charAt(i) != '.') {
					return -1; // 400 Bad Request - version number contains a alphabetic character instead of numeric
				}else if(parsedLine[2].charAt(i) == '.' && decimal == true) {
					return -1; // 400 Bad Request - multiple decimals
				}else if(parsedLine[2].charAt(i) == '.') {
					decimal = true;
				}
			}
			version = Double.parseDouble(parsedLine[2]);
			if(version > 1.0) {
				return -3; //505 version not supported
			}
		}
		
		return 0;
		
	}
	
	public String getResource() {
		return resource;
	}
	
	public String getCommand() {
		return cmd;
	}
	
	public double getVersion() {
		return version;
	}
}
