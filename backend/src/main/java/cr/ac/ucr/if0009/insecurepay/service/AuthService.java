package cr.ac.ucr.if0009.insecurepay.service;

import cr.ac.ucr.if0009.insecurepay.dto.LoginRequest;
import cr.ac.ucr.if0009.insecurepay.dto.LoginResponse;
import cr.ac.ucr.if0009.insecurepay.model.Usuario;
import cr.ac.ucr.if0009.insecurepay.repository.UsuarioRepository;
import cr.ac.ucr.if0009.insecurepay.util.JwtUtil;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final String CREDENCIALES_INVALIDAS = "Credenciales invalidas";

    private final UsuarioRepository usuarioRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarioRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse autenticar(LoginRequest request) {
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            throw new BadCredentialsException(CREDENCIALES_INVALIDAS);
        }

        Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException(CREDENCIALES_INVALIDAS));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new BadCredentialsException(CREDENCIALES_INVALIDAS);
        }

        String token = jwtUtil.generateToken(usuario.getUsername(), usuario.getRol().name(), usuario.getId());

        return new LoginResponse(
                token,
                usuario.getUsername(),
                usuario.getNombreCompleto(),
                usuario.getRol().name(),
                usuario.getId()
        );
    }
}
