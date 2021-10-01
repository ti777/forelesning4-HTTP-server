package no.kristiania.http;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpServer {

    private final ServerSocket serverSocket;
    private Path rootDirectory;
    private List<String> roles = new ArrayList<>();
    private List<Person> people;

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

        String requestLine[] = HttpMessage.readLine(clientSocket).split(" ");
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
            if (query != null) { //hvis query er satt forskjellig fra null
                //parse ut query paremeterne
                Map<String, String> queryMap = parseRequestParameters(query);

                yourName = queryMap.get("lastName") + ", " + queryMap.get("firstName");
            }
            String responseText = "<p>Hello " + yourName + "</p>"; //hente ut variablene

            writeOKResponse(clientSocket, responseText, "text/html");
        } else if (fileTarget.equals("/api/roleOptions")){ //hvis fileTarget ikke er hello, skal vi lage respons og skrive den tilbake
            String responseText = "";

            int value = 1;
            for (String role : roles) {
                responseText += "<option value=" + (value++) + ">" + role + "</option>";
            }


            writeOKResponse(clientSocket, responseText, "text/html");
        } else {
            if (rootDirectory != null && Files.exists(rootDirectory.resolve(fileTarget.substring(1)))){
                String responseText = Files.readString(rootDirectory.resolve(fileTarget.substring(1)));

                String contentType = "text/plain"; //er default
                if (requestTarget.endsWith(".html")){
                    contentType = "text/html";
                }
                writeOKResponse(clientSocket, responseText, contentType);
                return;
            }

            String responseText = "File not found: " + requestTarget;

            String response = "HTTP/1.1 404 Not found\r\n" +
                    "Content-Length:" + responseText.length() + "\r\n" +
                    "Connection: close \r\n" +
                    "\r\n" +
                    responseText;
            clientSocket.getOutputStream().write(response.getBytes());
        }
    }

    private Map<String, String> parseRequestParameters(String query) {
            Map<String, String> queryMap = new HashMap<>();
        for (String queryParameter : query.split("&")) { //skille fornavn og etternavn
            int equalsPos = queryParameter.indexOf('='); //finne hvor "=" er
            String parameterName = queryParameter.substring(0, equalsPos); //fra start til =
            String parameterValue = queryParameter.substring(equalsPos+1);
            queryMap.put(parameterName, parameterValue);
        }
        return queryMap;
    }

    private void writeOKResponse(Socket clientSocket, String responseText, String contentType) throws IOException {
        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Length:" + responseText.length() + "\r\n" +
                "Content-Type:" + contentType + "\r\n" +
                "Connection: close \r\n" +
                "\r\n" +
                responseText;
        clientSocket.getOutputStream().write(response.getBytes());
    }

    public static void main(String[] args) throws IOException {
        HttpServer httpServer = new HttpServer(1962);
        httpServer.setRoles(List.of("Student", "Teaching assistant", "Teacher"));
        httpServer.setRoot(Paths.get("."));
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }

    public void setRoot(Path rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<Person> getPeople() {
        return people;
    }
}
