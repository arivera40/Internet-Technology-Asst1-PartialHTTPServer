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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;


public class PartialHTTP1Server {

	public static void main(String[] args) {
		int portNum = Integer.parseInt(args[0]);
		try {
			//Server is listening to port
			ServerSocket socket = new ServerSocket(portNum);
			System.out.println("Server started.\nListening for connections on port : " + portNum + " ...\n");
			
			//running infinite loop to accept multiple client connections
			while(true) {
				Socket connection = socket.accept();
				
				//Obtaining input and output streams to pass to thread
				DataInputStream input_stream = new DataInputStream(connection.getInputStream());
				DataOutputStream output_stream = new DataOutputStream(connection.getOutputStream());
				
				//Create thread used to take care of communication between client and server
				Thread t = new ClientHandler(connection, input_stream, output_stream);
				t.start();
			}
			
			
		} catch (IOException e) {
			System.err.println("Server Connection error : " + e.getMessage());
		}

	}

}
