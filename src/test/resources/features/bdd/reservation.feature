Feature: Gestion de reservas

  Scenario: Crear una reserva exitosamente
    Given existe una habitacion disponible
    And existe un huesped registrado
    When el usuario crea una reserva valida
    Then la reserva debe crearse exitosamente

  Scenario: Obtener una reserva por ID
    Given existe una reserva creada
    When el usuario consulta la reserva por ID
    Then el sistema debe retornar la informacion de la reserva

  Scenario: Consultar todas las reservas
    Given existe una reserva creada
    When el usuario consulta todas las reservas
    Then la reserva debe aparecer en la lista

  Scenario: Consultar reservas por huesped
    Given existe una reserva creada
    When el usuario consulta las reservas del huesped
    Then la lista debe contener la reserva del huesped

  Scenario: Actualizar una reserva existente
    Given existe una reserva creada
    When el usuario actualiza la reserva
    Then la reserva debe quedar actualizada

  Scenario: Cancelar una reserva existente
    Given existe una reserva creada
    When el usuario cancela la reserva
    Then la reserva debe quedar cancelada
