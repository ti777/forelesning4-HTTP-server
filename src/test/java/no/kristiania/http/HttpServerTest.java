package no.kristiania.http;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpServerTest {

    private final HttpServer server = new HttpServer(0);

    public HttpServerTest() throws IOException {//konstruktør som kaster exception videre
    }

    @Test
    void shouldReturn404ForUnknownRequestTarget() throws IOException {
        HttpClient client  = new HttpClient("localhost", server.getPort(), "/non-existing");
        assertEquals(404, client.getStatusCode());
    }

    @Test //serveren bør fortelle oss hvilken fil den bør gi oss, og den ikke fantes i tekstmelding
    void shouldRespondWithRequestTargetIn404() throws IOException {
        HttpClient client  = new HttpClient("localhost", server.getPort(), "/non-existing");
        assertEquals("File not found: /non-existing", client.getMessageBody());
    }

    @Test
    void shouldRespondWith200forKnownRequestTarget() throws IOException {
        HttpClient client = new HttpClient("localhost", server.getPort(), "/hello");
        assertAll(
                () -> assertEquals(200, client.getStatusCode()),
                () -> assertEquals("text/html", client.getHeader("Content-Type")),
                () -> assertEquals("<p>Hello world</p>", client.getMessageBody())
        );
    }

    @Test
    void shouldHandleMoreThanOneRequest() throws IOException {
        //HttpServer server = new HttpServer(0); // starter en server på port 0 betyr velg en vilkårlig port, java velger
        assertEquals(200, new HttpClient("localhost", server.getPort(), "/hello").getStatusCode());
        assertEquals(200, new HttpClient("localhost", server.getPort(), "/hello").getStatusCode()); //gjør 2 httpRequester skal begge gi 200 statuskode
        //spørr hvilken port serverSocketen startet på
    }

    @Test
    void shouldEchoQueryParameter() throws IOException {
        HttpClient client = new HttpClient("localhost", server.getPort(), "/hello?firstName=Test&lastName=Persson");
        assertEquals("<p>Hello Persson, Test</p>", client.getMessageBody());

    }

    @Test
    void shouldServeFiles() throws IOException { //har en server, sier til server: se etter filer et sted på disk
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
        server.setRoot(Paths.get("target/test-classes"));

        String fileContent = "<p>Hello</p>";
        String exampleFile = "examples-file.html";
        Files.write(Paths.get("target/test-classes/example-file.html"), fileContent.getBytes());

        HttpClient client = new HttpClient("localhost", server.getPort(), "/example-file.html");
        assertEquals("text/html", client.getHeader("Content-Type"));
    }

    @Test
    void shouldReturnRolesFromServer() throws IOException {
        server.setRoles(List.of("Teacher", "Student")); //gitt at servenen vår er satt opp med et sett med roller vi skal returnere, så er det disse

        HttpClient client = new HttpClient("localhost", server.getPort(), "/api/roleOptions");
        assertEquals(
                "<option value=1>Teacher</option><option value=2>Student</option>",
                client.getMessageBody()
        );
    }
}
