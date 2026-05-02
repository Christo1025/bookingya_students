package bdd;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class ReservationSteps {

    private String roomId;
    private String guestId;
    private String reservationId;
    private Response response;

    static {
        String envUrl = System.getenv("BOOKINGYA_API_URL");
        RestAssured.baseURI = System.getProperty(
            "bookingya.api.url",
            envUrl != null && !envUrl.isBlank() ? envUrl : "http://localhost:8081/api"
        );
    }

    @Given("existe una habitacion disponible")
    public void existeUnaHabitacionDisponible() {
        String suffix = uniqueSuffix();

        response = given()
            .contentType(ContentType.JSON)
            .body(Map.of(
                "code", "BDD-" + suffix,
                "name", "BDD room",
                "city", "Bogota",
                "maxGuests", 3,
                "nightlyPrice", 180000,
                "available", true
            ))
            .when()
            .post("/room");

        assertEquals(200, response.statusCode());
        roomId = response.jsonPath().getString("id");
        assertNotNull(roomId);
    }

    @Given("existe un huesped registrado")
    public void existeUnHuespedRegistrado() {
        String suffix = uniqueSuffix();

        response = given()
            .contentType(ContentType.JSON)
            .body(Map.of(
                "identification", "BDD-" + suffix,
                "name", "BDD Guest",
                "email", "bdd-" + suffix + "@example.com"
            ))
            .when()
            .post("/guest");

        assertEquals(200, response.statusCode());
        guestId = response.jsonPath().getString("id");
        assertNotNull(guestId);
    }

    @Given("existe una reserva creada")
    public void existeUnaReservaCreada() {
        existeUnaHabitacionDisponible();
        existeUnHuespedRegistrado();
        elUsuarioCreaUnaReservaValida();
        laReservaDebeCrearseExitosamente();
    }

    @When("el usuario crea una reserva valida")
    public void elUsuarioCreaUnaReservaValida() {
        response = given()
            .contentType(ContentType.JSON)
            .body(Map.of(
                "roomId", roomId,
                "guestId", guestId,
                "checkIn", "2026-08-10T14:00:00",
                "checkOut", "2026-08-12T11:00:00",
                "guestsCount", 2,
                "notes", "BDD reservation"
            ))
            .when()
            .post("/reservation");

        if (response.statusCode() == 200) {
            reservationId = response.jsonPath().getString("id");
        }
    }

    @When("el usuario consulta la reserva por ID")
    public void elUsuarioConsultaLaReservaPorId() {
        response = given()
            .when()
            .get("/reservation/{id}", reservationId);
    }

    @When("el usuario consulta todas las reservas")
    public void elUsuarioConsultaTodasLasReservas() {
        response = given()
            .when()
            .get("/reservation");
    }

    @When("el usuario consulta las reservas del huesped")
    public void elUsuarioConsultaLasReservasDelHuesped() {
        response = given()
            .when()
            .get("/reservation/guest/{guestId}", guestId);
    }

    @When("el usuario actualiza la reserva")
    public void elUsuarioActualizaLaReserva() {
        response = given()
            .contentType(ContentType.JSON)
            .body(Map.of(
                "roomId", roomId,
                "guestId", guestId,
                "checkIn", "2026-08-10T14:00:00",
                "checkOut", "2026-08-13T11:00:00",
                "guestsCount", 2,
                "notes", "BDD reservation updated"
            ))
            .when()
            .put("/reservation/{id}", reservationId);
    }

    @When("el usuario cancela la reserva")
    public void elUsuarioCancelaLaReserva() {
        response = given()
            .when()
            .delete("/reservation/{id}", reservationId);
    }

    @Then("la reserva debe crearse exitosamente")
    public void laReservaDebeCrearseExitosamente() {
        assertEquals(200, response.statusCode());
        assertNotNull(reservationId);
        assertEquals(roomId, response.jsonPath().getString("roomId"));
        assertEquals(guestId, response.jsonPath().getString("guestId"));
    }

    @Then("el sistema debe retornar la informacion de la reserva")
    public void elSistemaDebeRetornarLaInformacionDeLaReserva() {
        assertEquals(200, response.statusCode());
        assertEquals(reservationId, response.jsonPath().getString("id"));
        assertEquals(roomId, response.jsonPath().getString("roomId"));
        assertEquals(guestId, response.jsonPath().getString("guestId"));
    }

    @Then("la reserva debe aparecer en la lista")
    public void laReservaDebeAparecerEnLaLista() {
        assertEquals(200, response.statusCode());
        List<String> reservationIds = response.jsonPath().getList("id");
        assertTrue(reservationIds.contains(reservationId));
    }

    @Then("la lista debe contener la reserva del huesped")
    public void laListaDebeContenerLaReservaDelHuesped() {
        assertEquals(200, response.statusCode());
        List<String> reservationIds = response.jsonPath().getList("id");
        List<String> guestIds = response.jsonPath().getList("guestId");

        assertTrue(reservationIds.contains(reservationId));
        assertTrue(guestIds.contains(guestId));
    }

    @Then("la reserva debe quedar actualizada")
    public void laReservaDebeQuedarActualizada() {
        assertEquals(200, response.statusCode());
        assertEquals("2026-08-13T11:00:00", response.jsonPath().getString("checkOut"));
        assertEquals("BDD reservation updated", response.jsonPath().getString("notes"));
    }

    @Then("la reserva debe quedar cancelada")
    public void laReservaDebeQuedarCancelada() {
        assertEquals(200, response.statusCode());

        Response getDeletedReservation = given()
            .when()
            .get("/reservation/{id}", reservationId);

        assertEquals(404, getDeletedReservation.statusCode());
        assertEquals("Reservation not found", getDeletedReservation.jsonPath().getString("error"));
    }

    private String uniqueSuffix() {
        return System.currentTimeMillis() + "-" + Math.abs(System.nanoTime());
    }
}
