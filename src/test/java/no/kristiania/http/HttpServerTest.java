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
    void shouldEchoQueryParameter() throws IOException {
        HttpServer server = new HttpServer(0);
        HttpClient client = new HttpClient("localhost", server.getPort(), "/hello?yourName=Tiff");
        assertEquals("<p>Hello Tiff</p>", client.getMessageBody());

    }

    @Test
    void shouldServeFiles() throws IOException {
        HttpServer server= new HttpServer(0); //har en server, sier til server: se etter filer et sted på disk
        server.setRoot(Paths.get("target/test-classes"));

        String fileContent = "A file created at " + LocalTime.now(); //skriver innhold til e fil i den katalogen
        String exampleFile = "examples-file.txt";
        Files.write(Paths.get("target/test-classes/" + exampleFile), fileContent.getBytes()); //legger en fil ned på disk. skriver ut fileContent

        HttpClient client = new HttpClient("localhost", server.getPort(), "/" + exampleFile); //finne samme fil
        assertEquals(fileContent, client.getMessageBody()); //henter filen og forventer å tilbake innholdet
        assertEquals("text/plain", client.getHeader("Content-Type"));
    }

    @Test
    void shouldUseFileExtensionForContentType() throws IOException {
        HttpServer server= new HttpServer(0);
        server.setRoot(Paths.get("target/test-classes"));

        String fileContent = "<p>Hello</p>";
        String exampleFile = "examples-file.html";
        Files.write(Paths.get("target/test-classes/example-file.html"), fileContent.getBytes());

        HttpClient client = new HttpClient("localhost", server.getPort(), "/example-file.html");
        assertEquals("text/html", client.getHeader("Content-Type"));
    }
}
