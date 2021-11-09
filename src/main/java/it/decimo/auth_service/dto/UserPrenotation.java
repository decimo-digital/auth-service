package it.decimo.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPrenotation {

    private int userId;

    private int prenotationId;

    private boolean active;

    private Integer dateOfDeletion;

}
