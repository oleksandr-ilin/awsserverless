package com.task05.model;

import java.util.Map;

public record EventRequest(int principalId, Map<String, String> content) {
}
