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
		if(line == null || line.length() == 0) return -1; //considered malformed, but will probably never happen
		if(Character.isWhitespace(line.charAt(0))) return -1; //considered malformed
		
		String[] parsedLine = line.split(" ");
		if(parsedLine.length != 3) return -1;
		
		//Command format checking MUST ACKNOWLEDGE OTHER EXISTING HTTP/1.0 COMMANDS 
		if(parsedLine[0].equals("DELETE") || parsedLine[0].equals("PUT") || parsedLine[0].equals("LINK") || parsedLine[0].equals("UNLINK")) {	
			return -2;	//command not supported
		}else if(!parsedLine[0].equals("GET") || !parsedLine[0].equals("POST") || !parsedLine[0].equals("HEAD")) {
			return -3; // bad request
		}else {
			cmd = parsedLine[0];
		}
		
		//Version number format checking
		if(parsedLine[2].indexOf("HTTP/") != 0 || parsedLine[2].indexOf(".") != 6 || !Character.isDigit(parsedLine[2].charAt(5)) ) {
			return -1;	//malformed
		}else {
			boolean decimal = false;
			parsedLine[2] = parsedLine[2].substring(5);
			for(int i=1; i < parsedLine[2].length(); i++) {	//we know first index is a digit
				if(!Character.isDigit(parsedLine[2].charAt(i)) || parsedLine[2].charAt(i) != '.') {
					return -1; //malformed
				}else if(parsedLine[2].charAt(i) == '.' && decimal == true) {
					return -1; //malformed
				}else if(parsedLine[2].charAt(i) == '.') {
					decimal = true;
				}
			}
			version = Double.parseDouble(parsedLine[2]);
			if(version > 1.0) {
				return -4; //version not supported
			}
		}
		
		//Resource format checking NOT YET IMPLEMENTED
		resource = parsedLine[1];
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
