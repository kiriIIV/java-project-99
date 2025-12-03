package hexlet.code.controllers;

import hexlet.code.dto.auth.LoginRequest;
import hexlet.code.dto.auth.LoginResponse;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import hexlet.code.security.JwtService;
import jakarta.validation.Valid;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        String login = request.getEmail();
        if (login == null || login.isBlank()) {
            login = request.getUsername();
        }

        if (login == null || login.isBlank()) {
            throw new AuthenticationException("Login (email/username) is required") {

            };
        }

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(login, request.getPassword());

        authenticationManager.authenticate(authToken);

        Optional<User> userOpt = userRepository.findByEmail(login);
        User user = userOpt.orElseThrow();

        String jwt = jwtService.issue(user.getId(), user.getEmail());
        return ResponseEntity.ok(new LoginResponse(jwt));
    }
}
