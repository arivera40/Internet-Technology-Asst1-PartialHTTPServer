package test_HTTP;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class HttpParser {
	private String cmd;
	private String resource;
	private double version;
	
	private File webroot;
	
	public HttpParser() {
		resource = "";
		cmd = "";
		version = 0;
		final File webroot = new File(".");
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
				return -3; //version not supported
			}
		}
		
		return 0;
		
	}
	
	private String getMIME() {
		if(resource.endsWith("html") || resource.endsWith("plain")) {
			return "text/" + ((resource.endsWith("html")) ? "html" : "plain");
		}else if(resource.endsWith("gif") || resource.endsWith("jpeg") || resource.endsWith("png")) {
			return "image/" + ((resource.endsWith("gif")) ? "gif" : (resource.endsWith("jpeg")) ? "jpeg" : "png");
		}else if(resource.endsWith("octet-stream") || resource.endsWith("pdf") || resource.endsWith("x-gzip") || resource.endsWith("zip")) {
			return "application/" + ((resource.endsWith("octet-stream")) ? "octet-stream" : (resource.endsWith("pdf")) ? "pdf" :
				(resource.endsWith("x-gzip")) ? "x-gzip" : "zip");
		}else {
			return "application/octet-stream";
		}
	}
	
	private byte[] getRequestedFile(File file, int length) {
		byte[] data = new byte[length];
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			fileInputStream.read(data);
			fileInputStream.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		return data;
	}
	
	public void getHttpResponse(PrintWriter head, BufferedOutputStream body) {
		File file = new File(webroot, resource);
		if(file.exists() == false) {
			head.println("HTTP/1.0 404 Not Found" + "\n\r");
			head.flush();
			return;
		}
		if(!file.canRead()) {
			head.println("HTTP/1.0 403 Forbidden" + "\n\r");
			head.flush();
			return;
		}
		int contentLength = (int) file.length();
		long lastModified = file.lastModified();
		String type = getMIME();
		
		if(cmd.equals("GET") || cmd.equals("POST")) {
			byte[] requestedFileData = getRequestedFile(file, contentLength);
			head.println("HTTP/1.0 200 OK");
			head.println("Content-Type: " + type);
			head.println("Content-Length: " + contentLength);
			head.println("Last-Modified: " + lastModified);	
			head.println("Content-Encoding: identity" + "\n\r");
			head.flush();
			try {
				body.write(requestedFileData, 0, contentLength);
				body.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}else {
			head.println("HTTP/1.0 200 OK");
			head.println("Content-Type: " + type);			
			head.println("Content-Length: " + contentLength);
			head.println("Last-Modified: " + lastModified);
			head.println("Content-Encoding: identity" + "\n\r");
			head.flush();
		}
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
