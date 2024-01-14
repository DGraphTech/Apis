package com.dgraphtech.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Notification {
    private Long id;

    private String eventType;
    private String eventData;
}
