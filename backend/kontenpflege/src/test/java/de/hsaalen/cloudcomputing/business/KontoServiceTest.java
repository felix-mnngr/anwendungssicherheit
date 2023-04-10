package de.hsaalen.cloudcomputing.business;

import de.hsaalen.cloudcomputing.*;
import io.grpc.StatusRuntimeException;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class KontoServiceTest {

    private static final String EMAIL = "test@test.test";
    private static final String BESCHREIBUNG = "TEST";
    private static final double BETRAG = 9.99;
    @GrpcClient
    KontoGprc kontoGrpc;

    @Test
    public void testService() {
        // Create
        KontoReply createReply = kontoGrpc.createKonto(KontoCreateRequest.newBuilder().setEmail(EMAIL).setBeschreibung(BESCHREIBUNG).build()).await().atMost(Duration.ofSeconds(5));
        String uuid = createReply.getUuid();
        assertNotNull(uuid);

        // add Kontobewegung
        kontoGrpc.addKontobewegung(KontoPutRequest.newBuilder().setUuid(uuid).setEmail(EMAIL).setBetrag(BETRAG).setBeschreibung(BESCHREIBUNG).build()).await().atMost(Duration.ofSeconds(5));

        // get Konto By Id
        KontoReply getReply = kontoGrpc.getKontoById(KontoGetRequest.newBuilder().setEmail(EMAIL).setUuid(uuid).build()).await().atMost(Duration.ofSeconds(5));
        assertEquals(EMAIL, getReply.getEmail());
        assertEquals(BETRAG, getReply.getKontostand());
        assertEquals(BESCHREIBUNG, getReply.getBeschreibung());
        assertEquals(uuid, getReply.getUuid());

        // get Kontobewegungen
        List<KontoBewegungReply> kontoBewegungReplies = kontoGrpc.getKontoBewegungenById(KontoGetRequest.newBuilder().setEmail(EMAIL).setUuid(uuid).build()).collect().asList().await().atMost(Duration.ofSeconds(5));
        assertEquals(1, kontoBewegungReplies.size());
        assertEquals(BETRAG, kontoBewegungReplies.get(0).getBetrag());
        assertEquals(BESCHREIBUNG, kontoBewegungReplies.get(0).getBeschreibung());
        assertNotNull(kontoBewegungReplies.get(0).getTimestamp());

        // get Konten by Email
        kontoGrpc.createKonto(KontoCreateRequest.newBuilder().setEmail(EMAIL).setBeschreibung(BESCHREIBUNG + "2").build()).await().atMost(Duration.ofSeconds(5));
        KontoReply kontoReply = kontoGrpc.createKonto(KontoCreateRequest.newBuilder().setEmail("other.user@test.test").setBeschreibung(BESCHREIBUNG).build()).await().atMost(Duration.ofSeconds(5));
        List<KontoReply> kontoReplies = kontoGrpc.getKontenByEmail(KontenGetRequest.newBuilder().setEmail(EMAIL).build()).collect().asList().await().atMost(Duration.ofSeconds(20));
        assertEquals(2, kontoReplies.size());
        assertEquals(2, kontoReplies.stream().filter(r -> r.getEmail().equals(EMAIL)).toList().size());

        // Delete
        kontoReplies.forEach(reply -> kontoGrpc.deleteKonto(KontoDeleteRequest.newBuilder().setEmail(EMAIL).setUuid(reply.getUuid()).build()).await().atMost(Duration.ofSeconds(5)));
        kontoGrpc.deleteKonto(KontoDeleteRequest.newBuilder().setEmail("other.user@test.test").setUuid(kontoReply.getUuid()).build()).await().atMost(Duration.ofSeconds(5));

        // GetById
        assertThrows(StatusRuntimeException.class, () -> kontoGrpc.getKontoById(KontoGetRequest.newBuilder().setEmail(EMAIL).setUuid(uuid).build()).await().atMost(Duration.ofSeconds(5)));
    }

}
