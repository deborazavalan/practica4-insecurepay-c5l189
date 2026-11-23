package cr.ac.ucr.if0009.insecurepay.service;

import cr.ac.ucr.if0009.insecurepay.dto.CuentaDto;
import cr.ac.ucr.if0009.insecurepay.exception.AccesoDenegadoException;
import cr.ac.ucr.if0009.insecurepay.exception.RecursoNoEncontradoException;
import cr.ac.ucr.if0009.insecurepay.exception.TokenInvalidoException;
import cr.ac.ucr.if0009.insecurepay.model.CuentaBancaria;
import cr.ac.ucr.if0009.insecurepay.model.Rol;
import cr.ac.ucr.if0009.insecurepay.repository.CuentaRepository;
import cr.ac.ucr.if0009.insecurepay.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CuentaService {

    private static final String PREFIJO_BEARER = "Bearer ";

    private final CuentaRepository cuentaRepository;
    private final JwtUtil jwtUtil;

    public CuentaService(CuentaRepository cuentaRepository, JwtUtil jwtUtil) {
        this.cuentaRepository = cuentaRepository;
        this.jwtUtil = jwtUtil;
    }

    public List<CuentaDto> listarTodas() {
        return cuentaRepository.findAll().stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CuentaDto obtenerCuentaSegura(Long cuentaId, String authHeader) {
        Claims claims = validarToken(extraerBearerToken(authHeader));
        String usernameAutenticado = claims.getSubject();
        String rol = claims.get("rol", String.class);

        CuentaBancaria cuenta = cuentaRepository.findById(cuentaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La cuenta solicitada no existe."));

        boolean esPropietario = cuenta.getUsuario().getUsername().equals(usernameAutenticado);
        boolean esAdmin = Rol.ADMIN.name().equals(rol);

        if (!esPropietario && !esAdmin) {
            throw new AccesoDenegadoException("No tiene permisos para consultar la cuenta solicitada.");
        }

        return convertirADto(cuenta);
    }

    public List<CuentaDto> obtenerPorUsuario(Long usuarioId) {
        return cuentaRepository.findByUsuarioId(usuarioId).stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    private String extraerBearerToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(PREFIJO_BEARER)) {
            throw new TokenInvalidoException("Token de autorizacion ausente o con formato invalido.");
        }
        String token = authHeader.substring(PREFIJO_BEARER.length()).trim();
        if (token.isEmpty()) {
            throw new TokenInvalidoException("Token de autorizacion ausente o con formato invalido.");
        }
        return token;
    }

    private Claims validarToken(String token) {
        try {
            Claims claims = jwtUtil.getClaims(token);
            if (claims.getSubject() == null || claims.getSubject().isBlank()) {
                throw new TokenInvalidoException("Token de autorizacion invalido o expirado.");
            }
            return claims;
        } catch (JwtException | IllegalArgumentException e) {
            throw new TokenInvalidoException("Token de autorizacion invalido o expirado.");
        }
    }

    private CuentaDto convertirADto(CuentaBancaria cuenta) {
        CuentaDto dto = new CuentaDto();
        dto.setId(cuenta.getId());
        dto.setUsuarioId(cuenta.getUsuario().getId());
        dto.setTitular(cuenta.getUsuario().getNombreCompleto());
        dto.setNumeroCuenta(cuenta.getNumeroCuenta());
        dto.setTipoCuenta(cuenta.getTipoCuenta().name());
        dto.setSaldo(cuenta.getSaldo());
        dto.setMoneda(cuenta.getMoneda());
        dto.setActiva(cuenta.getActiva());
        return dto;
    }
}
