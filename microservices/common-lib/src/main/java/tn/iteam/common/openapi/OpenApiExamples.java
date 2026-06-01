package tn.iteam.common.openapi;

public final class OpenApiExamples {

    private OpenApiExamples() {}

    public static final String LOGIN_REQUEST = """
            {"username":"john","password":"Password1!"}
            """;

    public static final String LOGIN_RESPONSE = """
            {"token":"eyJ...","refreshToken":"eyJ...","userId":1,"username":"john","fullName":"John Doe","role":"EMPLOYEE"}
            """;

    public static final String LEAVE_REQUEST = """
            {"startDate":"2026-06-01","endDate":"2026-06-05","reason":"Vacation","leaveType":"ANNUAL","managerId":2}
            """;

    public static final String ERROR_RESPONSE = """
            {"message":"Business rule violation","timestamp":"2026-05-24T10:00:00Z"}
            """;
}
