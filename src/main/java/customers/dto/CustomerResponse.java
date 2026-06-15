package customers.dto;

import java.time.LocalDateTime;

public record CustomerResponse(
        Long id,
        String fullname,
        String email,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

}
