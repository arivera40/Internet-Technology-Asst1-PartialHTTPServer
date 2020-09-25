package test_HTTP;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class ClientHandler extends Thread{
	final DataInputStream input_stream;
	final DataOutputStream output_stream;
	final Socket socket;
	
	public ClientHandler(Socket socket, DataInputStream dis, DataOutputStream dos) {
		this.socket = socket;
		this.input_stream = dis;
		this.output_stream = dos;
	}
	
	//Entry point when new thread is created and begins execution
	@Override
	public void run() {
		//Method to parse client input
		//Method to pass parsed input, in order to receive a response
	}
}
