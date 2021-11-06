package it.decimo.auth_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.springframework.data.geo.Point;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    private Double x;
    private Double y;

    @JsonIgnore
    public Point toPoint() {
        return new Point(x, y);
    }
}