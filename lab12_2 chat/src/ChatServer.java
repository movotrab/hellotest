import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

public class ChatServer {

	private static final int PORT = 54321;
	private static HashSet<String> names = new HashSet<>();
	private static HashSet<PrintWriter> outWriters = new HashSet<>();
	
	public static void main(String[] args) {
		
		System.out.println("Server started!");
		ServerSocket dataSocket = null;
		Socket socket;
		Thread st;
		
		try {
			dataSocket = new ServerSocket(PORT);
			
			while(true) {
				socket = dataSocket.accept();
				st = new ClientHandlerThread(socket);
				st.start();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			try {
				dataSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private static class ClientHandlerThread extends Thread{
		private Socket socket;
		private String name;
		
		private BufferedReader inFromClient;
		private PrintWriter outToClient;
		
		
		public ClientHandlerThread(Socket socket) {
			this.socket = socket;
		}
		
		public void run() {
			
			try {
				inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				outToClient = new PrintWriter(socket.getOutputStream(),true);
				
				
				while(true) {
					outToClient.println("SETNAME");
					name = inFromClient.readLine();
					if(name == null)
						return;
					
					synchronized (names) {
						if(!names.contains(name)) {
							names.add(name);
							break;
						}
					}
				}
				
				outToClient.println("ACCEPTED");
				
				synchronized (outWriters) {
					outWriters.add(outToClient);
				}
				
				while(true) {
					String input = inFromClient.readLine();
					if(input == null)
						return;
					
					for(PrintWriter writer : outWriters)
						writer.println("MESSAGE "+name+": "+input);
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally {
				if(name != null)
					names.remove(name);
				if(outToClient != null)
					outWriters.remove(outToClient);
				try {
					socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
