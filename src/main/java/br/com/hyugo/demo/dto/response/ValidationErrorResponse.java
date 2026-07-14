package br.com.hyugo.demo.dto.response;

import java.util.Map;

public record ValidationErrorResponse(int status, String error, Map<String, String> errors) {
}
