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
import { Eye, EyeOff, LucideAngularModule, ShoppingBag, UserRound } from 'lucide-angular';
import { validateImage } from '../../../shared/utils/media-validation.utils';

@Component({
  selector: 'app-register',
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
    MatIconModule,
    LucideAngularModule,
  ],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css',
})
export class RegisterComponent {
  registerForm: FormGroup;
  loading = signal(false);
  hidePassword = signal(true);

  roles: Role[] = ['CLIENT', 'SELLER'];

  readonly Eye = Eye;
  readonly EyeOff = EyeOff;
  readonly ShoppingBag = ShoppingBag;
  readonly UserRound = UserRound;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar,
  ) {
    this.registerForm = this.fb.group({
      name: [
        '',
        [
          Validators.required,
          Validators.required,
          Validators.minLength(3),
          Validators.maxLength(50),
        ],
      ],
      email: [
        '',
        [
          Validators.required,
          Validators.email,
          Validators.maxLength(100),
          Validators.pattern(/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/),
        ],
      ],
      password: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(30)]],
      role: ['CLIENT', Validators.required],
    });
  }

  async onSubmit() {
    if (this.registerForm.invalid) return;

    this.loading.set(true);

    try {
      const { name, email, password, role } = this.registerForm.value;

      this.authService.register({ name, email, password, role }).subscribe({
        next: () => {
          this.snackBar.open('Registration successful!', 'Close', { duration: 3000 });
          this.router.navigateByUrl('/login', { state: { email } });
        },
        error: (error) => {
          const errorMessage = error.error?.detail || 'Registration failed';
          this.snackBar.open(errorMessage, 'Close', { duration: 3000 });
        },
      });
    } catch (error: any) {
      this.snackBar.open(error.message || 'Registration failed', 'Close', { duration: 3000 });
    } finally {
      this.loading.set(false);
    }
  }
}
