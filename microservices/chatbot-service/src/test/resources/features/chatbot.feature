Feature: Chatbot

  Scenario: Manager asks project status
    Given a manager is authenticated
    When he asks project status
    Then chatbot returns real project statistics
