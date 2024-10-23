package org.example.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemResponse {
    private String id;
    private String name;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String description;
    private String restaurantId;
    private double price;
}
