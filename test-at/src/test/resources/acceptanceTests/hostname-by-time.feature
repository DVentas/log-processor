Feature: Feature for check hostname by time aggregation

  Scenario: Test hostname by time aggregation
    Given a consumer listenning hostname-connections-by-time-result topic
    When send message to hostname-connections-by-time with request
    Then should receive message in 10 seconds
    And response should have correct data
