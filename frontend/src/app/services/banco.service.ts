import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Cuenta, SaldoCuenta, Transaccion, TransferenciaRequest } from '../models/banco.model';

@Injectable({
  providedIn: 'root'
})
export class BancoService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  obtenerCuentasUsuario(usuarioId: number): Observable<Cuenta[]> {
    return this.http.get<Cuenta[]>(`${this.apiUrl}/cuentas/usuario/${usuarioId}`);
  }

  obtenerCuentaPorId(id: number): Observable<Cuenta> {
    return this.http.get<Cuenta>(`${this.apiUrl}/cuentas/${id}`);
  }

  obtenerSaldoCuenta(id: number): Observable<SaldoCuenta> {
    return this.http.get<SaldoCuenta>(`${this.apiUrl}/cuentas/${id}/saldo`);
  }

  listarTodasLasCuentas(): Observable<Cuenta[]> {
    return this.http.get<Cuenta[]>(`${this.apiUrl}/cuentas`);
  }

  listarTransacciones(): Observable<Transaccion[]> {
    return this.http.get<Transaccion[]>(`${this.apiUrl}/transacciones`);
  }

  obtenerTransaccionPorId(id: number): Observable<Transaccion> {
    return this.http.get<Transaccion>(`${this.apiUrl}/transacciones/${id}`);
  }

  obtenerTransaccionesPorCuenta(cuentaId: number): Observable<Transaccion[]> {
    return this.http.get<Transaccion[]>(`${this.apiUrl}/transacciones/cuenta/${cuentaId}`);
  }

  buscarTransacciones(termino: string): Observable<Transaccion[]> {
    return this.http.get<Transaccion[]>(`${this.apiUrl}/transacciones/buscar?termino=${encodeURIComponent(termino)}`);
  }

  transferir(req: TransferenciaRequest): Observable<Transaccion> {
    return this.http.post<Transaccion>(`${this.apiUrl}/transacciones/transferir`, req);
  }
}

