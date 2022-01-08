package it.decimo.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MenuItem {
    private Integer menuItemId;

    private Integer merchantId;

    private Integer categoryId;

    private String name;

    private Float price;
}
