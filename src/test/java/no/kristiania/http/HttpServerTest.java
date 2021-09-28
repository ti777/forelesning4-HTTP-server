package no.kristiania.http;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

public class HttpServerTest {

    @Test
    void shouldReturn404ForUnknownRequestTarget() throws IOException {
        HttpServer server = new HttpServer(10001);
        HttpClient client  = new HttpClient("localhost", server.getPort(), "/non-existing");
        assertEquals(404, client.getStatusCode());
    }

    @Test //serveren bør fortelle oss hvilken fil den bør gi oss, og den ikke fantes i tekstmelding
    void shouldRespondWithRequestTargetIn404() throws IOException {
        HttpServer server = new HttpServer(10002);
        HttpClient client  = new HttpClient("localhost", server.getPort(), "/non-existing");
        assertEquals("File not found: /non-existing", client.getMessageBody());
    }

    @Test
    void shouldRespondWith200forKnownRequestTarget() throws IOException {
        HttpServer server = new HttpServer(1003);
        HttpClient client = new HttpClient("localhost", server.getPort(), "/hello");
        assertAll(
                () -> assertEquals(200, client.getStatusCode()),
                () -> assertEquals("text/html", client.getHeader("Content-Type")),
                () -> assertEquals("<p>Hello world</p>", client.getMessageBody())
        );
    }

    @Test
    void shouldServeFiles() throws IOException {
        HttpServer server= new HttpServer(0); //har en server, sier til server: se etter filer et sted på disk
        server.setRoot(Paths.get("target/test-classes"));

        String fileContent = "A file created at " + LocalTime.now();
        Files.write(Paths.get("target/test-claess/examples-file.txt"), fileContent.getBytes()); //legger en fil ned på disk. skriver ut fileContent

        HttpClient client = new HttpClient("localhost", server.getPort(), "/example.file.txt"); //finne samme fil
        assertEquals(fileContent, client.getMessageBody()); //henter filen og forventer å tilbake innholdet
    }
}
