package cr.ac.ucr.if0009.insecurepay.service;

import cr.ac.ucr.if0009.insecurepay.dto.TransaccionDto;
import cr.ac.ucr.if0009.insecurepay.dto.TransferenciaDto;
import cr.ac.ucr.if0009.insecurepay.exception.ValidacionException;
import cr.ac.ucr.if0009.insecurepay.model.CuentaBancaria;
import cr.ac.ucr.if0009.insecurepay.model.Transaccion;
import cr.ac.ucr.if0009.insecurepay.repository.CuentaRepository;
import cr.ac.ucr.if0009.insecurepay.repository.TransaccionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransaccionService {

    private final TransaccionRepository transaccionRepository;
    private final CuentaRepository cuentaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public TransaccionService(TransaccionRepository transaccionRepository, CuentaRepository cuentaRepository) {
        this.transaccionRepository = transaccionRepository;
        this.cuentaRepository = cuentaRepository;
    }

    public List<TransaccionDto> listarTodas() {
        return transaccionRepository.findAll().stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    public TransaccionDto obtenerPorId(Long id) {
        Transaccion transaccion = transaccionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaccion no encontrada con ID: " + id));
        return convertirADto(transaccion);
    }

    public List<TransaccionDto> obtenerPorCuenta(Long cuentaId) {
        return transaccionRepository.findByCuentaOrigenIdOrCuentaDestinoIdOrderByFechaDesc(cuentaId, cuentaId).stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    // [VULNERABILIDAD INTENCIONADA: A03:2021 - SQL Injection (SQLi)]
    // Concatenacion insegura directa del parametro en createNativeQuery sin Prepared Statements
    // Payload tipico de exfiltracion: ' OR '1'='1
    @SuppressWarnings("unchecked")
    public List<TransaccionDto> buscarPorConcepto(String termino) {
        String sql = "SELECT * FROM TRANSACCIONES WHERE concepto_html LIKE '%" + termino 
                   + "%' OR estado LIKE '%" + termino + "%'";

        System.out.println("[AUDITORIA FORENSE INSECUREPAY] Ejecutando consulta SQL vulnerable: " + sql);

        List<Transaccion> resultados = entityManager.createNativeQuery(sql, Transaccion.class)
                .getResultList();

        return resultados.stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    private static final int MAX_CONCEPTO = 150;

    @Transactional
    public TransaccionDto transferir(TransferenciaDto dto) {
        String concepto = validarConcepto(dto.getConceptoHtml());

        if (dto.getMonto() == null || dto.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El monto a transferir debe ser mayor a cero.");
        }

        CuentaBancaria cuentaOrigen = cuentaRepository.findById(dto.getCuentaOrigenId())
                .orElseThrow(() -> new RuntimeException("Cuenta de origen no encontrada con ID: " + dto.getCuentaOrigenId()));

        CuentaBancaria cuentaDestino = cuentaRepository.findByNumeroCuenta(dto.getNumeroCuentaDestino())
                .orElseThrow(() -> new RuntimeException("Cuenta de destino no encontrada con numero: " + dto.getNumeroCuentaDestino()));

        if (cuentaOrigen.getSaldo().compareTo(dto.getMonto()) < 0) {
            throw new RuntimeException("Fondos insuficientes en la cuenta de origen seleccionada.");
        }

        cuentaOrigen.setSaldo(cuentaOrigen.getSaldo().subtract(dto.getMonto()));
        cuentaDestino.setSaldo(cuentaDestino.getSaldo().add(dto.getMonto()));
        cuentaRepository.save(cuentaOrigen);
        cuentaRepository.save(cuentaDestino);

        Transaccion transaccion = new Transaccion();
        transaccion.setCuentaOrigen(cuentaOrigen);
        transaccion.setCuentaDestino(cuentaDestino);
        transaccion.setMonto(dto.getMonto());
        transaccion.setConceptoHtml(sanitizarHtml(concepto));
        transaccion.setEstado("COMPLETADA");

        Transaccion guardada = transaccionRepository.save(transaccion);
        return convertirADto(guardada);
    }

    private String validarConcepto(String entrada) {
        if (entrada == null || entrada.isBlank()) {
            throw new ValidacionException("El concepto de la transferencia es obligatorio.");
        }
        String concepto = entrada.trim();
        if (concepto.length() > MAX_CONCEPTO) {
            throw new ValidacionException("El concepto no puede exceder " + MAX_CONCEPTO + " caracteres.");
        }
        return concepto;
    }

    private String sanitizarHtml(String entrada) {
        if (entrada == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(entrada.length());
        for (char c : entrada.toCharArray()) {
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&#x27;");
                case '/' -> sb.append("&#x2F;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    private TransaccionDto convertirADto(Transaccion t) {
        TransaccionDto dto = new TransaccionDto();
        dto.setId(t.getId());
        dto.setCuentaOrigenId(t.getCuentaOrigen().getId());
        dto.setNumeroCuentaOrigen(t.getCuentaOrigen().getNumeroCuenta());
        dto.setTitularOrigen(t.getCuentaOrigen().getUsuario().getNombreCompleto());
        dto.setCuentaDestinoId(t.getCuentaDestino().getId());
        dto.setNumeroCuentaDestino(t.getCuentaDestino().getNumeroCuenta());
        dto.setTitularDestino(t.getCuentaDestino().getUsuario().getNombreCompleto());
        dto.setMonto(t.getMonto());
        dto.setConceptoHtml(t.getConceptoHtml());
        dto.setFecha(t.getFecha());
        dto.setEstado(t.getEstado());
        return dto;
    }
}

