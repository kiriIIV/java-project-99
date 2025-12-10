package hexlet.code.dto.auth;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequest {
    @Email
    @Size(max = 255)
    private String email;

    private String username;

    @NotBlank
    @Size(min = 3, max = 255)
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLogin() {
        if (email != null && !email.isBlank()) {
            return email;
        }
        return username;
    }

    @AssertTrue(message = "Either email or username must be provided")
    public boolean isLoginPresent() {
        return (email != null && !email.isBlank())
                || (username != null && !username.isBlank());
    }
}
