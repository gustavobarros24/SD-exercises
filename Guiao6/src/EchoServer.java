import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class EchoServer {

    public static void main(String[] args) {
        try {
            ServerSocket ss = new ServerSocket(12345);
            int soma = 0;
            int count = 0;

            while (true) {
                Socket socket = ss.accept();

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream());

                String line;
                while ((line = in.readLine()) != null) {
                    try{
                        soma = soma + Integer.parseInt(line);
                        count++;
                    }
                    catch(NumberFormatException e){
                        e.printStackTrace();
                    }
                    out.println(soma);
                    out.flush();
                }

                float media = (float) soma/count;

                out.println(Float.toString(media));
                out.flush();

                soma = 0;
                count = 0;

                socket.shutdownOutput();
                socket.shutdownInput();
                socket.close();
            }
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
