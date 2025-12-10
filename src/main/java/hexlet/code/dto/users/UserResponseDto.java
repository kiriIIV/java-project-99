package hexlet.code.dto.users;

import java.time.LocalDate;
import java.util.Objects;

public record UserResponseDto(
        Long id,
        String email,
        String firstName,
        String lastName,
        LocalDate createdAt
) {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserResponseDto that = (UserResponseDto) o;
        return Objects.equals(id, that.id)
                && Objects.equals(email, that.email)
                && Objects.equals(firstName, that.firstName)
                && Objects.equals(lastName, that.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, firstName, lastName, createdAt);
    }
}
