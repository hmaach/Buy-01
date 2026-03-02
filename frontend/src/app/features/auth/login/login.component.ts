import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../../core/auth/services/auth.service';
import { Role } from '../../../core/models/user.model';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatIconModule
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  loginForm: FormGroup;
  loading = signal(false);
  hidePassword = signal(true);

  roles: Role[] = ['CLIENT', 'SELLER'];

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      role: ['CLIENT', Validators.required]
    });
  }

  async onSubmit() {
    if (this.loginForm.invalid) return;

    this.loading.set(true);
    
    try {
      const { email, password, role } = this.loginForm.value;
      const response = await this.authService.login({ email, password }, role);
      
      this.snackBar.open('Login successful!', 'Close', { duration: 3000 });
      
      // Redirect based on role
      if (response.user.role === 'SELLER') {
        this.router.navigate(['/seller/dashboard']);
      } else {
        this.router.navigate(['/products']);
      }
    } catch (error: any) {
      this.snackBar.open(error.message || 'Login failed', 'Close', { duration: 3000 });
    } finally {
      this.loading.set(false);
    }
  }
}
