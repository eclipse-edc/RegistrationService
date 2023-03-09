package org.eclipse.edc.registration.client.response;

import org.eclipse.edc.spi.result.Failure;

import java.util.List;

import static java.util.List.of;


public class ApiFailure extends Failure {
    public static final ApiFailure CONFLICT = new ApiFailure(of(), 409);
    public static final ApiFailure NOT_FOUND = new ApiFailure(of(), 404);
    public static final ApiFailure BAD_REQUEST = new ApiFailure(of(), 400);
    private final int code;

    public ApiFailure(List<String> messages, int code) {
        super(messages);
        this.code = code;
    }

    public static ApiFailure from(int code) {
        return new ApiFailure(of(), code);
    }

    public int code() {
        return code;
    }

}
