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

import org.eclipse.edc.spi.result.AbstractResult;

import java.util.List;
import java.util.function.Function;


public class ApiResult<T> extends AbstractResult<T, ApiFailure> {

    protected ApiResult(T content, ApiFailure failure) {
        super(content, failure);
    }

    public static <T> ApiResult<T> success(T content) {
        return new ApiResult<>(content, null);
    }

    public static <T> ApiResult<T> conflict(String message) {
        return new ApiResult<>(null, new ApiFailure(List.of(message), 409));
    }

    public static <T> ApiResult<T> notFound(String message) {
        return new ApiResult<>(null, new ApiFailure(List.of(message), 404));
    }

    public static <T> ApiResult<T> badRequest(String... message) {
        return badRequest(List.of(message));
    }

    public static <T> ApiResult<T> badRequest(List<String> messages) {
        return new ApiResult<>(null, new ApiFailure(messages, 400));
    }

    public static <T> ApiResult<T> success() {
        return ApiResult.success(null);
    }

    public static <T> ApiResult<T> failure(int code, String... message) {
        return new ApiResult<>(null, new ApiFailure(List.of(message), code));
    }

    public <R> ApiResult<R> map(Function<T, R> mapFunction) {
        if (succeeded()) {
            return ApiResult.success(mapFunction.apply(getContent()));
        } else {
            return ApiResult.failure(reason(), getFailureDetail());
        }
    }

    public int reason() {
        return getFailure().code();
    }
}
