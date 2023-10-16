package com.authservice.response;

import jakarta.ws.rs.core.Response;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class ResponseWrapper<T> {
    private Response.StatusType statusType;
    private String message;
    private T data;

    public ResponseWrapper(){
        this.statusType = Response.Status.OK;
    };

    public ResponseWrapper(Response.StatusType statusType, String message, T data) {
        this.statusType = statusType;
        this.message = message;
        this.data = data;
    }

    public Response build() {
        return Response.status(statusType)
                .entity(new ResponseEntity(data, message))
                .build();
    }

    private static class ResponseEntity<T> {
        private String message;
        private T data;

        public ResponseEntity(T data, String message) {
            this.message = message;
            this.data = data;
        }

        public T getData() {
            return data;
        }

        public String getMessage() {
            return message;
        }
    }
}
