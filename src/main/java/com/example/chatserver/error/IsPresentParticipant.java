package com.example.chatserver.error;

public class IsPresentParticipant extends RuntimeException{
    public IsPresentParticipant() {
    }

    public IsPresentParticipant(String message) {
        super(message);
    }

    public IsPresentParticipant(String message, Throwable cause) {
        super(message, cause);
    }

    public IsPresentParticipant(Throwable cause) {
        super(cause);
    }

    public IsPresentParticipant(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
