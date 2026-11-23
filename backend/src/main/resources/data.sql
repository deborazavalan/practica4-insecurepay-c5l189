-- ==============================================================================
-- Datos Iniciales de Prueba - InsecurePay Digital Banking Portal
-- ==============================================================================

INSERT INTO USUARIOS (username, password, nombre_completo, email, rol) VALUES
('admin', '$2a$12$jElTA9fnwo/mA0qB8lvuuO1FhkjSlhxwrJRez.V30I3yRwmSa2JTG', 'Administrador General de Sistemas', 'admin@insecurepay.fi.cr', 'ADMIN'),
('auditor_seguridad', '$2a$12$dOvGOwC1/iEuD2Xj9Ngk/egnd.n3I3/nnPXXE2znKS1IEZLY.Imw2', 'Lic. Esteban Solano Chaves', 'esolano@insecurepay.fi.cr', 'AUDITOR'),
('carlos_mora', '$2a$12$5Mv31Uf8nHpCEaXZkuMfwOjmDs9w7CrgSxEClj7bBU.Hud55JWEi.', 'Carlos Mora Morales', 'cmora@correo.cr', 'CLIENTE'),
('maria_rojas', '$2a$12$HYa3Jk9SLrFHTST8PfL0COu.lJdRtIVnb.oLauxzBPjvXxELaJXC2', 'María Rojas Soto', 'mrojas@empresa.cr', 'CLIENTE'),
('juan_perez', '$2a$12$19cs9gqV2JbGHSvp0BryreAICaiORQTvgFqMx158qrqnXFmftGdHS', 'Juan Pérez Delgado', 'jperez@correo.cr', 'CLIENTE');

INSERT INTO CUENTAS_BANCARIAS (usuario_id, numero_cuenta, tipo_cuenta, saldo, moneda, activa) VALUES
(1, 'CR05015202001026458900', 'CORRIENTE', 50000000.00, 'CRC', TRUE),
(3, 'CR05015202001026458901', 'AHORROS', 250000.00, 'CRC', TRUE),
(4, 'CR05015202001026458902', 'CORRIENTE', 4850000.00, 'CRC', TRUE),
(5, 'CR05015202001026458903', 'AHORROS', 75000.00, 'CRC', TRUE);

INSERT INTO TRANSACCIONES (cuenta_origen_id, cuenta_destino_id, monto, concepto_html, fecha, estado) VALUES
(2, 3, 45000.00, 'Pago de servicios profesionales de programacion. Verificado', '2026-03-01 10:15:00', 'COMPLETADA'),
(3, 4, 120000.00, 'Cancelacion cuota de alquiler oficina central. Factura electronica #8849.', '2026-03-05 14:30:00', 'COMPLETADA'),
(3, 2, 15000.00, 'Reembolso almuerzo equipo de trabajo.', '2026-03-12 11:20:00', 'COMPLETADA'),
(4, 2, 35000.00, 'Aporte fondo solidario cooperativo. Transferencia inmediata SINPE Movil', '2026-03-20 09:45:00', 'COMPLETADA');
