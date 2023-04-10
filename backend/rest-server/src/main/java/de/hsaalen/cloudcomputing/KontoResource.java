package de.hsaalen.cloudcomputing;

import io.quarkus.grpc.GrpcClient;
import io.quarkus.oidc.IdToken;
import io.quarkus.security.UnauthorizedException;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.jwt.JsonWebToken;

import javax.inject.Inject;
import javax.ws.rs.*;
import java.io.Serializable;

@Path("/api/konto")
public class KontoResource {

    @Inject
    @IdToken
    JsonWebToken idToken;

    @Inject
    @GrpcClient("konto-service")
    KontoGprc kontoService;

    @GET
    @Path("/bewegungen/{uuid}")
    public Multi<KontoBewegungResponse> getKontoBewegungenById(@PathParam("uuid") String uuid) {
        assertEmailIsVerified();
        return kontoService.getKontoBewegungenById(KontoGetRequest.newBuilder().setEmail(getEmailOfCurrentUser()).setUuid(uuid).build()).map(KontoBewegungResponse::new);
    }

    @GET
    @Path("/preview/{uuid}")
    public Uni<KontoResponse> getKontoPreviewById(@PathParam("uuid") String uuid) {
        assertEmailIsVerified();
        return kontoService.getKontoById(KontoGetRequest.newBuilder().setEmail(getEmailOfCurrentUser()).setUuid(uuid).build()).map(KontoResponse::new);
    }

    @GET
    public Multi<KontoResponse> getKontenForUser() {
        assertEmailIsVerified();
        return kontoService.getKontenByEmail(KontenGetRequest.newBuilder().setEmail(getEmailOfCurrentUser()).build()).map(KontoResponse::new);
    }

    @POST
    public Uni<KontoResponse> createKonto(Body body) {
        assertEmailIsVerified();
        if(body.beschreibung == null) {
            throw new BadRequestException("The value 'beschreibung' is mandatory!");
        }
        return kontoService.createKonto(KontoCreateRequest.newBuilder().setEmail(getEmailOfCurrentUser()).setBeschreibung(body.beschreibung).build()).map(KontoResponse::new);
    }

    @POST
    @Path("/bewegungen/{uuid}")
    public Uni<KontoResponse> addKontoBewegung(@PathParam("uuid") String uuid, Body body) {
        assertEmailIsVerified();
        if(body.beschreibung == null) {
            throw new BadRequestException("The value 'beschreibung' is mandatory!");
        }
        if(body.betrag == null) {
            throw new BadRequestException("The value 'betrag' is mandatory!");
        }
        return kontoService.addKontobewegung(KontoPutRequest.newBuilder().setEmail(getEmailOfCurrentUser()).setUuid(uuid).setBetrag(body.betrag).setBeschreibung(body.beschreibung).build()).map(KontoResponse::new);
    }

    @DELETE
    @Path("/{uuid}")
    public Uni<KontoResponse> deleteKonto(@PathParam("uuid") String uuid) {
        assertEmailIsVerified();
        return kontoService.deleteKonto(KontoDeleteRequest.newBuilder().setEmail(getEmailOfCurrentUser()).setUuid(uuid).build()).map(KontoResponse::new);
    }

    private void assertEmailIsVerified() {
        if(idToken.getClaim("email_verified") == null || !((boolean) idToken.getClaim("email_verified"))) {
            throw new UnauthorizedException("The provided email was not verified");
        }
    }

    private String getEmailOfCurrentUser() {
        return idToken.getClaim("email");
    }

    public static class Body {
        private String beschreibung;
        private Double betrag;

        public void setBeschreibung(String beschreibung) {
            this.beschreibung = beschreibung;
        }

        public void setBetrag(Double betrag) {
            this.betrag = betrag;
        }
    }

    public static class KontoResponse implements Serializable {
        final String uuid;
        final String email;
        final String beschreibung;
        final double kontostand;

        public KontoResponse(KontoReply kontoReply) {
            this.uuid = kontoReply.getUuid();
            this.email = kontoReply.getEmail();
            this.beschreibung = kontoReply.getBeschreibung();
            this.kontostand = kontoReply.getKontostand();
        }

        public String getUuid() {
            return uuid;
        }

        public String getEmail() {
            return email;
        }

        public String getBeschreibung() {
            return beschreibung;
        }

        public double getKontostand() {
            return kontostand;
        }
    }

    public static class KontoBewegungResponse {
        final String beschreibung;
        final double betrag;
        final long timestamp;

        public KontoBewegungResponse(KontoBewegungReply kontoBewegungReply) {
            this.beschreibung = kontoBewegungReply.getBeschreibung();
            this.betrag = kontoBewegungReply.getBetrag();
            this.timestamp = kontoBewegungReply.getTimestamp().getSeconds();
        }

        public String getBeschreibung() {
            return beschreibung;
        }

        public double getBetrag() {
            return betrag;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

}