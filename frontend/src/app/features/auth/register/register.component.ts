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
    MatIconModule
  ],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  registerForm: FormGroup;
  loading = signal(false);
  hidePassword = signal(true);
  avatarPreview = signal<string | null>(null);

  roles: Role[] = ['CLIENT', 'SELLER'];

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.registerForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      role: ['CLIENT', Validators.required],
      avatar: [null]
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      const file = input.files[0];
      
      // Validate file size (max 2MB)
      if (file.size > 2 * 1024 * 1024) {
        this.snackBar.open('File size must be less than 2MB', 'Close', { duration: 3000 });
        return;
      }

      // Create preview
      const reader = new FileReader();
      reader.onload = () => {
        this.avatarPreview.set(reader.result as string);
      };
      reader.readAsDataURL(file);

      this.registerForm.patchValue({ avatar: file });
    }
  }

  async onSubmit() {
    if (this.registerForm.invalid) return;

    this.loading.set(true);
    
    try {
      const { username, email, password, role, avatar } = this.registerForm.value;
      await this.authService.register({ username, email, password, role, avatar });
      
      this.snackBar.open('Registration successful!', 'Close', { duration: 3000 });
      
      // Redirect based on role
      if (role === 'SELLER') {
        this.router.navigate(['/seller/dashboard']);
      } else {
        this.router.navigate(['/products']);
      }
    } catch (error: any) {
      this.snackBar.open(error.message || 'Registration failed', 'Close', { duration: 3000 });
    } finally {
      this.loading.set(false);
    }
  }
}
