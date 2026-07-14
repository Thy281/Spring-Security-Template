package br.com.hyugo.demo.config.auth;

public class OAuthLoginException extends RuntimeException {

    public OAuthLoginException(String message) {
        super(message);
    }
}
