package br.com.hyugo.demo.dto.response;

public record ErrorResponse(int status, String error, String message) {
}
