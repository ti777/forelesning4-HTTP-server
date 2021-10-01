package no.kristiania.http;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class HttpClient {
    private final int statusCode;
    private final HashMap<String, String> headerFields = new HashMap<>();
    private final String messageBody;

    private HttpMessage httpMessage;

    public HttpClient(String host, int port, String requestTarget) throws IOException {
        Socket socket = new Socket(host, port);

        String request = "GET " + requestTarget + " HTTP/1.1\r\n" +
                        "Host: " + host + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n";
        socket.getOutputStream().write(request.getBytes());



        String statusLine = readLine(socket);
        this.statusCode = Integer.parseInt(statusLine.split(" ")[1]);

        String headerLine;
        while (!(headerLine = readLine(socket)).isBlank()){ // les headlines, inntil du kommer til en blank headerline
            int colonPos = headerLine.indexOf(':'); //hva er posisjonen til ":"
            String key = headerLine.substring(0, colonPos); // key er da headerLine, alt som er opptil ":"
            String value = headerLine.substring(colonPos+1).trim(); //value er alt som starter et hakk etter ":". skal ikke ha med ":". og fjerne (trim) widespace
            headerFields.put(key, value);
        }

        this.messageBody = readCharacters(socket, getContentLength());

    }

    private String readCharacters(Socket socket, int contentLength) throws IOException {
        StringBuilder result = new StringBuilder();
        InputStream in = socket.getInputStream();

        for (int i = 0; i < contentLength; i++) {
            result.append((char) in.read());
        }

        return result.toString();
    }

    //public static String
    static String readLine(Socket socket) throws IOException {
        StringBuilder result = new StringBuilder();
        InputStream in = socket.getInputStream();

        int c;
        while ((c = in.read()) != -1 && c != '\r') {
            result.append((char)c);
        }
        in.read(); //lese og kaste e ny karakter
        return result.toString();
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getHeader(String headerName) {
        return headerFields.get(headerName);
    }

    public int getContentLength() {
        return Integer.parseInt((getHeader("Content-Length"))); //tolker en tekst som en integer, teksten vi skal tolke er headerfielden
    }

    public String getMessageBody() {
        return messageBody;
    }
}
