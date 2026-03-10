import { Component, inject, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDividerModule } from '@angular/material/divider';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { LucideAngularModule, Camera, Mail, User, ShieldCheck, Save, Trash2, X } from 'lucide-angular';
import { AuthService } from '../../core/auth/services/auth.service';
import { UserService } from '../../core/services/user.service';
import { MediaService } from '../../core/services/media.service';
import { DeleteAccountDialogComponent } from './delete-account-dialog.component';
import { validateImage } from '../../shared/utils/media-validation.utils';

@Component({
  selector: 'app-user-profile',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSnackBarModule,
    MatDividerModule,
    MatDialogModule,
    LucideAngularModule,
  ],
  templateUrl: './user-profile.html',
  styleUrl: './user-profile.css',
})
export class ProfileComponent {
  private authService = inject(AuthService);
  private fb = inject(FormBuilder);
  private userService = inject(UserService);
  private mediaService = inject(MediaService);
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);
  private router = inject(Router);

  // Icons
  readonly Camera = Camera;
  readonly Mail = Mail;
  readonly User = User;
  readonly ShieldCheck = ShieldCheck;
  readonly Save = Save;
  readonly Trash2 = Trash2;
  readonly X = X;

  currentUser = computed(() => this.authService.user);
  isSeller = computed(() => this.authService.isSeller);

  isSaving = signal(false);
  isDeleting = signal(false);
  
  // Avatar state
  avatarPreview = signal<string | null>(null);
  avatarError = signal<string | null>(null);
  selectedAvatar = signal<File | null>(null);
  newAvatarUploaded = signal(false);

  profileForm = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
    email: [
      '',
      [
        Validators.required,
        Validators.email,
        Validators.maxLength(100),
        Validators.pattern(/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/),
      ],
    ],
  });

  constructor() {
    // Patch form after user is loaded
    this.currentUserEffect();
  }

  private currentUserEffect(): void {
    const user = this.currentUser();
    if (user) {
      this.profileForm.patchValue({
        name: user.name,
        email: user.email,
      });
      // Set avatar preview from existing avatarUrl
      if (user.avatarUrl) {
        this.avatarPreview.set(user.avatarUrl);
      }
    }
  }

  get hasChanges(): boolean {
    const nameChanged = this.profileForm.get('name')?.value !== this.currentUser()?.name;
    const emailChanged = this.profileForm.get('email')?.value !== this.currentUser()?.email;
    return (nameChanged || emailChanged || this.newAvatarUploaded()) && this.profileForm.valid;
  }

  get roleLabel(): string {
    return this.currentUser()?.role ?? 'CLIENT';
  }

  get initials(): string {
    const name = this.currentUser()?.name ?? '';
    return name
      .split(' ')
      .map((n) => n[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);
  }

  private uploadImage(file: File, callback: (id: string) => void): void {
    const formData = new FormData();
    formData.append('files', file);

    this.mediaService.uploadImages(formData).subscribe({
      next: (res) => {
        const id = res[0]?.imagesId;
        if (id) callback(id);
      },
      error: (e) => console.error('Image upload failed', e),
    });
  }

  saveProfile(): void {
    if (!this.hasChanges) return;
    this.isSaving.set(true);

    const updateUser = () => {
      const formData = new FormData();

      const name = this.profileForm.get('name')?.value ?? '';
      const email = this.profileForm.get('email')?.value ?? '';

      formData.append('name', name);
      formData.append('email', email);

      // Append avatar if a new one was selected
      const avatar = this.selectedAvatar();
      if (avatar) {
        formData.append('avatar', avatar);
      }

      this.userService.updateUser(formData).subscribe({
        next: (updatedUser) => {
          if (updatedUser) {
            this.authService.updateCurrentUser(updatedUser);
            this.profileForm.markAsPristine();
            // Reset avatar state after successful update
            this.selectedAvatar.set(null);
            this.newAvatarUploaded.set(false);
            this.avatarPreview.set(updatedUser.avatarUrl || null);
            this.snackBar.open('Profile updated successfully!', '✓', { duration: 3000 });
          }
          this.isSaving.set(false);
        },
        error: (e) => {
          console.error('Profile update failed', e);
          this.snackBar.open('Failed to update profile. Please try again.', 'Dismiss', {
            duration: 3000,
          });
          this.isSaving.set(false);
        },
      });
    };

    updateUser();
  }

  onAvatarSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      const file = input.files[0];
      
      // Validate using validateImage
      const validationError = validateImage(file);
      if (validationError) {
        this.avatarError.set(validationError);
        this.selectedAvatar.set(null);
        return;
      }
      
      this.avatarError.set(null);
      this.selectedAvatar.set(file);
      this.newAvatarUploaded.set(true);
      
      // Create preview
      const reader = new FileReader();
      reader.onload = (e) => {
        this.avatarPreview.set(e.target?.result as string);
      };
      reader.readAsDataURL(file);
    }
  }

  removeAvatar(): void {
    this.selectedAvatar.set(null);
    this.newAvatarUploaded.set(true);
    // Reset to original avatar or null
    this.avatarPreview.set(this.currentUser()?.avatarUrl || null);
  }

  confirmDelete(): void {
    const dialogRef = this.dialog.open(DeleteAccountDialogComponent, {
      width: '400px',
      disableClose: true,
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result === 'delete') {
        this.deleteAccount();
      }
    });
  }

  deleteAccount(): void {
    this.isDeleting.set(true);

    this.userService.deleteUser().subscribe({
      next: () => {
        this.snackBar.open('Account deleted successfully', '✓', { duration: 3000 });
        this.authService.logout();
      },
      error: (e) => {
        console.error('Account deletion failed', e);
        this.snackBar.open('Failed to delete account. Please try again.', 'Dismiss', {
          duration: 3000,
        });
        this.isDeleting.set(false);
      },
    });
  }
}
