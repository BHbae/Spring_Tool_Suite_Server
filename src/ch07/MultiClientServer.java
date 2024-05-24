package ch07;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class MultiClientServer {

	private static final int PORT = 5000;
	private static Vector<PrintWriter> clientWriters = new Vector<>();

	public static void main(String[] args) {
		System.out.println("Server started.....");

		try (ServerSocket serverSocket = new ServerSocket(PORT)) {

			while (true) {
				Socket socket = serverSocket.accept();

				new clientHandler(socket).start();
			}

		} catch (Exception e) {
			// TODO: handle exception
		}

	} // end of main

	// 정적 내부 클래스 설계
	private static class clientHandler extends Thread {
		private Socket socket;
		private PrintWriter out;
		private BufferedReader in;

		public clientHandler(Socket socket) {
			this.socket = socket;
		}

		// 스레드 start() 호출시 동작되는 메서드 - 약속
		@Override
		public void run() {

			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);
				
				// 코드추가
				// 클라이언트로부터 이름 받기(약속되어 있음)
				String nameMessage = in.readLine();
				if(nameMessage != null && nameMessage.startsWith("NAME:")) {
					String clientName = nameMessage.substring(5);
					broadcastMessage("해당 서버에 입장 :" + clientName + "님");
				} else {
					// 약속과 다르게 접근했다면 종료 처리
					socket.close();
					return;
				}
				
				clientWriters.add(out);

				String meassage;
				while ((meassage = in.readLine()) != null) {
					System.out.println("Receiverd : " + meassage);

					// 약속 -> 클라이언트,서버
					// : 기준으로 처리, / 기준, <--
					// MSG: 안녕\n
					String[] parts = meassage.split(":", 2);
					System.out.println("parts 인덱스 갯수 : " + parts.length);
					// 명련 부분을 분리
					String command = parts[0];
					String data = parts.length > 1 ? parts[1] : "";

					if (command.equals("MSG")) {
						System.out.println("연결된 전체 사용자에게 MSG 방송");
						broadcastMessage(meassage);
					} else if (command.equals("BYE")) {
						System.out.println("Client disconnnected...");
						break; // while 구문 종효
					}

				} // end of while

				// ... finally 구문으로 빠진다

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					socket.close();
					// 도전 과제
					// 서버측에서 관리하고 있는 P.W제거 해야 한다.
					// 인덱스 번호가 필효하다.
					// clientWriters.add() 할떄 지정된 나의 인덱스 번호가 필요
					// clientWriters.remove();
					System.out.println(" Client 연결 해제 ");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		} // end of ClientHandlre

		// 모든 클라이언트에게 메세지 보내기 - 브로드 캐스트
		private static void broadcastMessage(String message) {

			for (PrintWriter writer : clientWriters) {
				writer.println();
			}

		}

	}

} // end of class
