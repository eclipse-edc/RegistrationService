/*
 *  Copyright (c) 2022 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

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
