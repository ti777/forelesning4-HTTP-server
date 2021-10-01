package no.kristiania.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.HashMap;

public class HttpMessage {
    public String startLine;
    public final HashMap<String, String> headerFields = new HashMap<>();
    public String messageBody;

    public HttpMessage(Socket socket) throws IOException {
        startLine = HttpMessage.readLine(socket);
        readHeaders(socket);
        messageBody = HttpMessage.readCharacters(socket, getContentLength());
    }

    public int getContentLength() {
        return Integer.parseInt((getHeader("Content-Length"))); //tolker en tekst som en integer, teksten vi skal tolke er headerfielden
    }

    public String getHeader(String headerName) {
        return headerFields.get(headerName);
    }

    static String readCharacters(Socket socket, int contentLength) throws IOException {
        StringBuilder result = new StringBuilder();
        InputStream in = socket.getInputStream();

        for (int i = 0; i < contentLength; i++) {
            result.append((char) in.read());
        }

        return result.toString();
    }

    private void readHeaders(Socket socket) throws IOException {
        String headerLine;
        while (!(headerLine = HttpMessage.readLine(socket)).isBlank()){ // les headlines, inntil du kommer til en blank headerline
            int colonPos = headerLine.indexOf(':'); //hva er posisjonen til ":"
            String key = headerLine.substring(0, colonPos); // key er da headerLine, alt som er opptil ":"
            String value = headerLine.substring(colonPos+1).trim(); //value er alt som starter et hakk etter ":". skal ikke ha med ":". og fjerne (trim) widespace
            headerFields.put(key, value);
        }
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
}
