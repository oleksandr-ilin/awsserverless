package com.task05.model;

import java.util.Map;

public record CreatedEventResponse(int statusCode, Map<String, Object> event) {
}
