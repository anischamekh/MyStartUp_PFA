Feature: Leave management

  Scenario: Employee submits leave and HR is notified
    Given an employee is authenticated
    When he submits leave request
    Then HR receives notification
