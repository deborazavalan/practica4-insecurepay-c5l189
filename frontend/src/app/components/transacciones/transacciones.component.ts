import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { BancoService } from '../../services/banco.service';
import { AuthService } from '../../services/auth.service';
import { Transaccion } from '../../models/banco.model';

@Component({
  selector: 'app-transacciones',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './transacciones.component.html'
})
export class TransaccionesComponent implements OnInit {
  transacciones: Transaccion[] = [];
  transaccionSeleccionada: Transaccion | null = null;
  terminoBusqueda = '';
  loading = false;
  errorInfo = '';

  constructor(
    private bancoService: BancoService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.cargarTransacciones();
  }

  cargarTransacciones(): void {
    this.loading = true;
    this.errorInfo = '';
    this.bancoService.listarTransacciones().subscribe({
      next: (data) => {
        this.transacciones = data;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.errorInfo = 'Error al cargar historial: ' + (err.error?.message || err.message);
      }
    });
  }

  // [VULNERABILIDAD INTENCIONADA: SQL Injection Trigger]
  ejecutarBusqueda(): void {
    if (!this.terminoBusqueda.trim()) {
      this.cargarTransacciones();
      return;
    }

    this.loading = true;
    this.errorInfo = '';
    this.bancoService.buscarTransacciones(this.terminoBusqueda).subscribe({
      next: (data) => {
        this.transacciones = data;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        // [VULNERABILIDAD INTENCIONADA: A05:2021 - Fuga de Error SQL]
        this.errorInfo = 'Fallo de Base de Datos (Stack Trace): ' + (err.error?.message || err.error?.trace || err.message);
      }
    });
  }

  inyectarPayloadSql(): void {
    this.terminoBusqueda = "' OR '1'='1";
    this.ejecutarBusqueda();
  }

  verComprobante(t: Transaccion): void {
    this.transaccionSeleccionada = t;
  }

  cerrarComprobante(): void {
    this.transaccionSeleccionada = null;
  }
}

