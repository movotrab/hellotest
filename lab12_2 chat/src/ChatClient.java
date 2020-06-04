import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ChatClient extends JFrame implements Runnable {

	private BufferedReader inFromServer;
	private PrintWriter outToServer;

	private JTextField textField = new JTextField(40);
	private JTextArea messageArea = new JTextArea(8, 40);

	public ChatClient() {
		super("Chat App");
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		textField.setEditable(false);
		messageArea.setEnabled(false);
		messageArea.setLineWrap(true);

		getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);
		getContentPane().add(textField, BorderLayout.SOUTH);

		textField.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				outToServer.println(textField.getText());
				textField.setText("");
			}
		});
		
		pack();
		setVisible(true);
	}

	private String getServerAddress() {
		return JOptionPane.showInputDialog(this, "Enter Server IP", "Welcome to the Chat",
				JOptionPane.QUESTION_MESSAGE);
	}

	private String getUserName() {
		return JOptionPane.showInputDialog(this, "Enter User Name", "Select name", JOptionPane.QUESTION_MESSAGE);
	}

	@Override
	public void run() {
		String serverAddress = getServerAddress();

		Socket socket = null;

		try {
			socket = new Socket(serverAddress, 54321);

			inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			outToServer = new PrintWriter(socket.getOutputStream(), true);

			while (true) {
				String line = inFromServer.readLine();
				if (line.startsWith("SETNAME")) {
					outToServer.println(getUserName());
					textField.requestFocus();
				} else if (line.startsWith("ACCEPTED")) {
					textField.setEditable(true);
				} else if(line.startsWith("MESSAGE")){
					messageArea.append(line.substring(8)+"\r\n");
					messageArea.setCaretPosition(messageArea.getDocument().getLength()-1);
				}else
					System.exit(1);
			}

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			if(inFromServer != null)
				try {
					inFromServer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			if(outToServer != null)
				outToServer.close();
			if(socket != null)
				try {
					socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}

	}

	public static void main(String[] args) {
		ChatClient client = new ChatClient();
		new Thread(client).start();

	}

}
