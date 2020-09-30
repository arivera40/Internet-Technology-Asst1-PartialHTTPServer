package test_HTTP;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;


public class PartialHTTP1Server {

	public static void main(String[] args) {
//		int portNum = Integer.parseInt(args[0]);
		try {
			//Server is listening to port
			ServerSocket socket = new ServerSocket(8080);
			System.out.println("Server started.\nListening for connections on port : " + 8080 + " ...\n");
			
			//Capacity for concurrent connections will start at 5
			ArrayList<Thread> connections = new ArrayList<Thread>(5);
			//running infinite loop to accept multiple client connections
			while(true) {
				int index = contractList(connections);
				if(connections.size() <= 50 || index != -1) {
					Socket connection = socket.accept();
					
					//Obtaining input and output streams to pass to thread
					InputStream input_stream = connection.getInputStream();
					OutputStream output_stream = connection.getOutputStream();
					
					//Create thread used to take care of communication between client and server
					Thread t = new ClientHandler(connection, input_stream, output_stream);
					connections.add(index, t);
					t.start();
				}else {
					Socket connection = socket.accept();
					
					OutputStream output_stream = connection.getOutputStream();
					PrintWriter response = new PrintWriter(output_stream);
					
					response.println("HTTP/1.0 503 Service Unavailable" + "\n\r");
					response.flush();
					response.close();
					output_stream.close();
					connection.close();
				}
			}
		} catch (IOException e) {
			System.err.println("Server Connection error : " + e.getMessage());
		}

	}
	
	public static int contractList(ArrayList<Thread> connections) {
		int index = -1;
		for(int i=0; i < connections.size(); i++) {
			if(!connections.get(i).isAlive()) {
				index = (index == -1) ? i : index;
				connections.set(i, null);
			}
		}
		return index;
	}

}
