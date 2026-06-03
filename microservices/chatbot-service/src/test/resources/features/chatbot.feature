Feature: Chatbot
  The HR assistant answers questions for authenticated users.

  Scenario: Ask chatbot a question
    Given a user is authenticated
    When the user sends a question
    Then the chatbot returns a response
