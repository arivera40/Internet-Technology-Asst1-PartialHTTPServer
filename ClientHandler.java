package test_HTTP;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class ClientHandler extends Thread{
	final InputStream input_stream;
	final OutputStream output_stream;
	final Socket socket;
	
	public ClientHandler(Socket socket, InputStream dis, OutputStream dos) {
		this.socket = socket;
		this.input_stream = dis;
		this.output_stream = dos;
	}
	
	//Entry point when new thread is created and begins execution
	@Override
	public void run() {
		//HTTP client requests are written to the InputStream
		InputStreamReader inputStreamReader = new InputStreamReader(input_stream);
		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		
		String request;
		try {
			//This will continue running until socket connection is closed
			while((request = bufferedReader.readLine()) != null) {	
				HttpParser parser = new HttpParser();
				int status = parser.parseRequest(request);
				if(status == -1) {
					//respond with 404 Bad Request Response (Malformed)
				}else if(status == -2) {
					//respond with 501 Not Implemented
				}else if(status == -3) {
					//respond with 400 Bad Request
				}else if(status == -4) {
					//respond with Http Not Supported
				}else {
					//getHttpResponse(parser) parser.getCommand = command, parser.getResource = resource, parser.getVersion = version
				}
			}
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
}
