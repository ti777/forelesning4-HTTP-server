package no.kristiania.http;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HttpServer {

    private final ServerSocket serverSocket;
    private Path rootDirectory;

    public HttpServer(int serverPort) throws IOException {
        serverSocket = new ServerSocket(serverPort);

        //extende thread klassen. starter ny separert tråd som ting kjører paralellt. programmet fortsetter mens den kjører separert tråd
        //skal håndtere clientene. thread er en variabel
        new Thread(this::handleClients).start();
    }

    private void handleClients() {
        try {
            while (true) { //tar og gjør alt i en løkke
                handleClient();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient() throws IOException { //skjer det noe feil med clienten. bare skriv ut noe og fortsett. //en blokk som lager en ressurs og sier close når den er ferdig
        Socket clientSocket = serverSocket.accept();

        String requestLine[] = HttpClient.readLine(clientSocket).split(" ");
        String requestTarget = requestLine[1];

        int questionsPos = requestTarget.indexOf('?');
        String fileTarget;
        String query = null;
        if (questionsPos != -1){
            fileTarget = requestTarget.substring(0, questionsPos); //tar biten foran
            query = requestTarget.substring(questionsPos + 1); //ta colon  : i header. den første etter.
        }else {
            fileTarget = requestTarget; //fileTarget skal være lik hele requestTarget
        }

        if (fileTarget.equals("/hello")) {
            String yourName = "world";
            if (query != null){ //hvis query er satt forskjellig fra null
                yourName = query.split("=")[1];//splitte den å "=" og det etter = skal være yourName
            }
            String responseText = "<p>Hello " + yourName +"</p>";

            String response = "HTTP/1.1 200 OK\r\n" +
                    "Content-Length:" + responseText.length() + "\r\n" +
                    "Content-Type: text/html\r\n" +
                    "\r\n" +
                    responseText;
            clientSocket.getOutputStream().write(response.getBytes());
        } else {
            if (rootDirectory != null && Files.exists(rootDirectory.resolve(fileTarget.substring(1)))){
                String responseText = Files.readString(rootDirectory.resolve(fileTarget.substring(1)));

                String contentType = "text/plain"; //er default
                if (requestTarget.endsWith(".html")){
                    contentType = "text/html";
                }

                String response = "HTTP/1.1 200 OK\r\n" +
                        "Content-Length:" + responseText.length() + "\r\n" +
                        "Content-Type:" + contentType + "\r\n" +
                        "\r\n" +
                        responseText;
                clientSocket.getOutputStream().write(response.getBytes());
            }

            String responseText = "File not found: " + requestTarget;

            String response = "HTTP/1.1 404 Not found\r\n" +
                    "Content-Length:" + responseText.length() + "\r\n" +
                    "\r\n" +
                    responseText;
            clientSocket.getOutputStream().write(response.getBytes());
        }
    }

    public static void main(String[] args) throws IOException {
        HttpServer httpServer = new HttpServer(1962);
        httpServer.setRoot(Paths.get("."));
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }

    public void setRoot(Path rootDirectory) {
        this.rootDirectory = rootDirectory;
    }
}
