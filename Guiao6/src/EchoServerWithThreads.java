import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class EchoServerWithThreads {
    public static void main(String[] args){
        try{
            ServerSocket ss = new ServerSocket(12345);
            EchoServerWithThreads server = new EchoServerWithThreads();
            while(true){
                Socket socket = ss.accept();
                ClientHandler clientHandler = server.new ClientHandler(socket);
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public class ClientHandler implements Runnable{
        private Socket socket;

        public ClientHandler(Socket socket){
            this.socket = socket;
        }

        @Override
        public void run(){
            int soma = 0;
            int count = 0;

            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream());

                String line;
                while((line = in.readLine()) != null){
                    try {
                        soma = soma + Integer.parseInt(line);
                        count++;
                    } 
                    catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    out.println(soma);
                }

                float media = (float) soma/count;

                out.println(Float.toString(media));

            } 
            catch (Exception e) {
                e.printStackTrace();
            }
            finally{
                try {
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
