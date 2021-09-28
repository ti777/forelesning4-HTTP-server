package no.kristiania.http;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {

    private final ServerSocket serverSocket;

    public HttpServer(int serverPort) throws IOException {
        serverSocket = new ServerSocket(serverPort);

        //extende thread klassen. starter ny separert tråd som ting kjører paralellt. programmet fortsetter mens den kjører separert tråd
        //skal håndtere clientene
        new Thread(this::handleClient).start();
    }

    private void handleClient() { //skjer det noe feil med clienten. bare skriv ut noe og fortsett
        try{ //en blokk som lager en ressurs og sier close når den er ferdig
            Socket clientSocket = serverSocket.accept();
            String response = "HTTP/1.1 404 Not found\r\nContent-Length: 0\r\n\r\n";
            clientSocket.getOutputStream().write(response.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1962); //åpne server socket, velge port selv

        Socket clientSocket = serverSocket.accept(); //vente på connection fra clienten ved å si accept.

        String html = "Hellå"; //lage eg webside
        String contentType = "text/plain";

        String response = "HTTP/1.1 200 bra\r\n" +
                "Content-Length: " + html.getBytes().length + "\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Connection: close\r\n" +
                "\r\n" +
                html;

        clientSocket.getOutputStream().write(response.getBytes()); //må skrive tilbake en string, bytes til teksten
    }

}