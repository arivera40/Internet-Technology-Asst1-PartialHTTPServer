package mypack;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;

public class HttpParser {
	private String cmd;
	private String resource;
	private Date ifModified;
	private double version;
	
	private File webroot;
	
	public HttpParser() {
		resource = "";
		ifModified = null;
		cmd = "";
		version = 0;
		webroot = new File(".");
	}
	
	//returns int to indicate status of request
	public int parseRequest(String request) {
		System.out.println("Enters parseRequest method");
		String[] parsedLine = request.split(" |\r\n");
		///////////////////////
		for(int i=0; i < parsedLine.length; i++){
			System.out.println(parsedLine[i]);
		}
		//////////////////////
		if(parsedLine.length < 3) {
			System.out.println("Returns not enough tokens in request header");
			return -1;	// 400 Bad Request
		}
		
		//Has possible If-Modified parameter
		if(parsedLine.length > 3) {
			String date = "";
			//if tag is valid then check for valid date
			System.out.println("parsedLine[3] = " + parsedLine[3]);
			// System.out.println("parsedLine[3].length() == " + parsedLine[3].length());
			if(parsedLine[3].equals("If-Modified-Since:")) {
				for(int i=4; i < parsedLine.length; i++) {
					date += (i != parsedLine.length - 1) ? parsedLine[i] + " " : parsedLine[i];
				}
				String[] tempSplit = date.split(" ");
				// System.out.println("tempSplit.length = " + tempSplit.length);
				//if array size is 6 then most likely valid date so it is stored for comparison later
				if(tempSplit.length == 6) {
					try {
						SimpleDateFormat form = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss Z");
						form.setTimeZone(TimeZone.getTimeZone("GMT"));
						ifModified = form.parse(date);
						System.out.println("ifModified = " + ifModified);
					}catch(java.text.ParseException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		//Command format checking
		if(parsedLine[0].equals("get") || parsedLine[0].equals("post") || parsedLine[0].equals("head") ||
				parsedLine[0].equals("delete") || parsedLine[0].equals("put") || parsedLine[0].equals("link") || parsedLine[0].equals("unlink")) {	
			System.out.println("Returns because of lowercase command");
			return -1;	// 400 Bad Request
		}else if(parsedLine[0].equals("DELETE") || parsedLine[0].equals("PUT") || parsedLine[0].equals("LINK") || parsedLine[0].equals("UNLINK")) {
			System.out.println("Returns because command not implemented");
			return -2; // 501 Not Implemented
		}else if(parsedLine[0].equals("GET") || parsedLine[0].equals("POST") || parsedLine[0].equals("HEAD")){
			cmd = parsedLine[0];
		}else {
			System.out.println("Returns because command not recognized");
			return -1; // 400 Bad Request
		}
		
		//Resource - Not sure if any format checking is necessary?
		resource = parsedLine[1];
		
		//Version number format checking
		if(parsedLine[2].indexOf("HTTP/") != 0 || !Character.isDigit(parsedLine[2].charAt(5)) ) {
			System.out.println("Returns because malformed Version number");
			return -1;	// 400 Bad Request
		}else {
			boolean decimal = false;
			// System.out.println("parsedLine[2] before substring call = " + parsedLine[2] + "and its length = " + parsedLine[2].length());
			parsedLine[2] = parsedLine[2].substring(5); //-4 to remove \r\n
			// System.out.println("parsedLine[2] = " + parsedLine[2]);
			// System.out.println("parsedLine[2].length() = " + parsedLine[2].length());
			for(int i=1; i < parsedLine[2].length(); i++) {	//we know first index is a digit
				if(!Character.isDigit(parsedLine[2].charAt(i)) && parsedLine[2].charAt(i) != '.') {
					System.out.println("Returns because version number contains an alphabetic letter");
					return -1; // 400 Bad Request - version number contains a alphabetic character instead of numeric
				}else if(parsedLine[2].charAt(i) == '.' && decimal == true) {
					System.out.println("Returns because contains multiple decimal places, malformed");
					return -1; // 400 Bad Request - multiple decimals
				}else if(parsedLine[2].charAt(i) == '.') {
					decimal = true;
				}
			}
			version = Double.parseDouble(parsedLine[2]);
			System.out.println("version number = " + version);
			if(version > 1.0) {
				System.out.println("Returns because version number is not supported");
				return -3; //version not supported
			}
		}
		
		System.out.println("Returns because it is a valid request");
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
		if(!file.exists()) {
			System.out.println("File doesn't exist within webroot");
			head.println("HTTP/1.0 404 Not Found" + "\r\n");
			head.flush();
			return;
		}
		if(!file.canRead()) {
			System.out.println("File is forbidden access");
			head.println("HTTP/1.0 403 Forbidden" + "\r\n");
			head.flush();
			return;
		}
		int contentLength = (int) file.length();
		Date lastModified = getModifiedDate(new Date(file.lastModified()));
		
		System.out.println("lastModified = " + lastModified);
		String type = getMIME();
		// System.out.println("getMime() returns type = "+ type);
		Date expiration = new GregorianCalendar(2021, Calendar.OCTOBER, 2).getTime();
		expiration = getModifiedDate(expiration);
		System.out.println("expiration = " + expiration);
		
		if(cmd.equals("GET") || cmd.equals("POST")) {
			System.out.println("Enters GET/POST execution");
			if(cmd.equals("GET") && ifModified != null) {
				if(lastModified.before(ifModified)) {
					head.println("HTTP/1.0 304 Not Modified" + "\r\n");
					head.flush();
					return;
				}
			}
			byte[] requestedFileData = getRequestedFile(file, contentLength);
			head.print("HTTP/1.0 200 OK\nContent-Type: " + type + "\nContent-Length: " + contentLength + 
						"\nLast-Modified: " + lastModified + "\nContent-Encoding: identity" +
						"\nAllow: GET, POST, HEAD" + "\nExpires: " + expiration + "\r\n");
			head.flush();
			try {
				body.write(requestedFileData, 0, contentLength);
				body.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}else {
			System.out.println("Enters HEAD");
			head.print("HTTP/1.0 200 OK\nContent-Type: " + type + "\nContent-Length: " + contentLength + 
						"\nLast-Modified: " + lastModified + "\nContent-Encoding: identity" + "\r\n");
			head.flush();
		}
		return;
	}
	
	private Date getModifiedDate(Date date) {
		Date lastModified = null;
		SimpleDateFormat form = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss Z");
		form.setTimeZone(TimeZone.getTimeZone("GMT"));
		String dateStr = form.format(date);
		System.out.println("dateStr = " + dateStr);
		try {
			lastModified = form.parse(dateStr);
		}catch(java.text.ParseException e) {
			e.printStackTrace();
		}
		return lastModified;
	}

}

