package com.ssafy.enjoyTrip.domain.card.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocationRequest {
    private double latitude;
    private double longitude;
}
