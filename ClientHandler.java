package test_HTTP;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler extends Thread {
	final InputStream input_stream;
	final OutputStream output_stream;
	final Socket socket;

	public ClientHandler(Socket socket, InputStream dis, OutputStream dos) {
		this.socket = socket;
		this.input_stream = dis;
		this.output_stream = dos;
	}

	// Entry point when new thread is created and begins execution
	@Override
	public void run() {
		try {
			// HTTP client requests are written to the InputStream
			String request = getClientRequest();

			// HTTP server responses are written into the OutputStream - we use PrintWriter to output response header
			// and we use BufferedOutputStream to output response body/data when necessary
			PrintWriter responseHead = new PrintWriter(output_stream);
			BufferedOutputStream outStreamWriter = new BufferedOutputStream(output_stream);

			// This will continue running until socket connection is closed
			//Header may need to change to account for option If-Modified request (this would be in the following line)

			HttpParser parser = new HttpParser();
			int status = parser.parseRequest(request);
			if (status == -1) {
				// respond with 404 Bad Request
				responseHead.println("HTTP/1.0 400 Bad Request" + "\n\r");
				responseHead.flush();

			} else if (status == -2) {
				// respond with 501 Not Implemented
				responseHead.println("HTTP/1.0 501 Not Implemented" + "\n\r");
				responseHead.flush();

			} else if (status == -3) {
				// respond with HTTP Version Not Supported
				responseHead.println("HTTP/1.0 505 HTTP Version Not Supported" + "\n\r");
				responseHead.flush();

			} else {
				parser.getHttpResponse(responseHead, outStreamWriter);
			}
			//wait quarter second
			wait(250);
			//PrintWriter and BufferedOutputStream already flush()'ed after use
			//Close down communication object
			responseHead.close();
			outStreamWriter.close();
			input_stream.close();
			output_stream.close();
			socket.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private String getClientRequest() throws IOException {
		StringBuilder request = new StringBuilder();
		do {
			request.append((char) input_stream.read());
		}while(input_stream.available() > 0);
		return request.toString();
	}
}
