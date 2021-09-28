package no.kristiania.http;
import org.junit.jupiter.api.Test;

import java.io.IOException;

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
        assertEquals("Hello world", client.getMessageBody());
    }
}
