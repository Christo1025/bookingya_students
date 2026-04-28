Feature: Booking Management

  Scenario: Create a booking successfully
    Given a room exists with ID 1
    And a guest exists with ID 1
    And valid check-in and check-out dates are selected
    And the number of guests is 2
    When the user creates the booking
    Then the booking should be created successfully

  Scenario: Consult a booking successfully
    Given a booking already exists in the system
    When the user searches for the booking
    Then the booking information should be displayed correctly

  Scenario: Update an existing booking successfully
    Given a booking already exists in the system
    When the user updates the check-out date and notes
    Then the booking should be updated successfully

  Scenario: Delete a booking successfully
    Given a booking already exists in the system
    When the user deletes the booking
    Then the booking should be removed successfully

  Scenario: Get a booking by ID successfully
    Given a booking exists with ID 1
    When the user searches for the booking by ID
    Then the system should return the correct booking information