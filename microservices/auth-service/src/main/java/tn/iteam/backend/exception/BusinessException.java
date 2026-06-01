package tn.iteam.backend.exception;

/** User-facing business rule violation; mapped to HTTP 400 with a clear JSON message. */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
