package no.kristiania.http;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;

public class HttpServer {

    private final ServerSocket serverSocket;
    private Path rootDirectory;

    public HttpServer(int serverPort) throws IOException {
        serverSocket = new ServerSocket(serverPort);

        //extende thread klassen. starter ny separert tråd som ting kjører paralellt. programmet fortsetter mens den kjører separert tråd
        //skal håndtere clientene. thread er en variabel
        new Thread(this::handleClient).start();
    }

    private void handleClient() { //skjer det noe feil med clienten. bare skriv ut noe og fortsett. //en blokk som lager en ressurs og sier close når den er ferdig
        try{
            Socket clientSocket = serverSocket.accept();

            String requestLine[] = HttpClient.readLine(clientSocket).split(" ");
            String requestTarget = requestLine[1];

            if (requestTarget.equals("/hello")) {
                String responseText = "<p>Hello world</p>";

                String response = "HTTP/1.1 200 OK\r\n" +
                        "Content-Length:" + responseText.length() + "\r\n" +
                        "Content-Type: text/html\r\n" +
                        "\r\n" +
                        responseText;
                clientSocket.getOutputStream().write(response.getBytes());
            } else {
                String responseText = "File not found: " + requestTarget;

                String response = "HTTP/1.1 404 Not found\r\n" +
                        "Content-Length:" + responseText.length() + "\r\n" +
                        "\r\n" +
                        responseText;
                clientSocket.getOutputStream().write(response.getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        new HttpServer(1962);
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }

    public void setRoot(Path rootDirectory) {
        this.rootDirectory = rootDirectory;
    }
}
