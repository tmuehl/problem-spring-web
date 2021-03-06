package org.zalando.problem.spring.web.advice;

/*
 * #%L
 * problem-spring-web
 * %%
 * Copyright (C) 2015 Zalando SE
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.Problem;
import org.zalando.problem.spring.web.advice.custom.CustomAdviceTrait;
import org.zalando.problem.spring.web.advice.general.GeneralAdviceTrait;
import org.zalando.problem.spring.web.advice.http.HttpAdviceTrait;
import org.zalando.problem.spring.web.advice.io.IOAdviceTrait;
import org.zalando.problem.spring.web.advice.routing.RoutingAdviceTrait;
import org.zalando.problem.spring.web.advice.validation.ValidationAdviceTrait;

import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.function.Function;

import static java.util.function.Function.identity;
import static org.zalando.problem.spring.web.advice.MediaTypes.determineContentType;

/**
 * <p>
 * Advice traits are simple interfaces that provide a single method with a default
 * implementation. They are used to provide {@link ExceptionHandler} implementations to be used in
 * {@link Controller Controllers} and/or in a {@link ControllerAdvice}. Clients can choose which traits they what to
 * use à la carte.
 * </p>
 * <p/>
 * <p>
 * Advice traits are grouped in packages, based on they use cases. Every package has a composite advice trait that
 * bundles all traits of that package. Additionally there is one {@link ProblemHandling major composite advice trait}
 * that in turn bundles all other composites.
 * </p>
 *
 * @see ControllerAdvice
 * @see ExceptionHandler
 * @see Throwable
 * @see Exception
 * @see Problem
 * @see ProblemHandling
 * @see CustomAdviceTrait
 * @see GeneralAdviceTrait
 * @see HttpAdviceTrait
 * @see IOAdviceTrait
 * @see RoutingAdviceTrait
 * @see ValidationAdviceTrait
 */
public interface AdviceTrait {

    default ResponseEntity<Problem> create(final Response.StatusType status, final Throwable throwable,
            final NativeWebRequest request,
            final Function<ResponseEntity.BodyBuilder, ResponseEntity.BodyBuilder> buildable) {
        return create(status, throwable.getMessage(), request, buildable);
    }

    default ResponseEntity<Problem> create(final Response.StatusType status, final Throwable throwable,
            final NativeWebRequest request) {
        return create(status, throwable, request, identity());
    }

    default ResponseEntity<Problem> create(final Response.StatusType status, final String message,
            final NativeWebRequest request,
            final Function<ResponseEntity.BodyBuilder, ResponseEntity.BodyBuilder> buildable) {
        return create(Problem.valueOf(status, message), request, buildable);
    }

    default ResponseEntity<Problem> create(final Response.StatusType status, final String message,
            final NativeWebRequest request) {
        return create(status, message, request, identity());
    }

    default ResponseEntity<Problem> create(final Problem problem, final NativeWebRequest request) {
        return create(problem, request, identity());
    }

    default ResponseEntity<Problem> create(final Problem problem, final NativeWebRequest request,
            final Function<ResponseEntity.BodyBuilder, ResponseEntity.BodyBuilder> buildable) {
        final HttpStatus status = HttpStatus.valueOf(problem.getStatus().getStatusCode());
        final ResponseEntity.BodyBuilder builder = buildable.apply(ResponseEntity.status(status));

        final Optional<MediaType> contentType = determineContentType(request);
        if (contentType.isPresent()) {
            return builder.contentType(contentType.get()).body(problem);
        }
        return builder.body(null);
    }

}
