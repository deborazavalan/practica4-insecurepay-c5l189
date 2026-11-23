import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  username = '';
  password = '';
  errorMsg = '';
  loading = false;

  constructor(private authService: AuthService, private router: Router) {}

  onLogin(): void {
    if (!this.username || !this.password) {
      this.errorMsg = 'Por favor complete las credenciales.';
      return;
    }
    this.loading = true;
    this.errorMsg = '';

    this.authService.login(this.username, this.password).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/dashboard']);
      },
      error: (err: HttpErrorResponse) => {
        this.loading = false;
        this.errorMsg = this.mensajeError(err);
      }
    });
  }

  private mensajeError(err: HttpErrorResponse): string {
    if (err.status === 0) {
      return 'No se pudo conectar con el servidor. Verifique su conexion e intente de nuevo.';
    }
    if (err.status === 401) {
      return err.error?.detail || 'Usuario o clave incorrectos.';
    }
    if (err.status === 429) {
      return 'Demasiados intentos. Espere un momento antes de volver a intentarlo.';
    }
    if (err.status >= 500) {
      return 'El servicio no esta disponible en este momento. Intente mas tarde.';
    }
    return 'No fue posible iniciar sesion. Intente de nuevo.';
  }

  fillUser(user: string, pass: string): void {
    this.username = user;
    this.password = pass;
  }
}

