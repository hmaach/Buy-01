import { Component, inject, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDividerModule } from '@angular/material/divider';
import { LucideAngularModule, Camera, Mail, User, ShieldCheck, Save, Trash2 } from 'lucide-angular';
import { AuthService } from '../../core/auth/services/auth.service';
import { UserService } from '../../core/services/user.service';
import { validateImage } from '../../shared/utils/media-validation.utils';
import { MediaService } from '../../core/services/media.service';

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

  // Icons
  readonly Camera = Camera;
  readonly Mail = Mail;
  readonly User = User;
  readonly ShieldCheck = ShieldCheck;
  readonly Save = Save;
  readonly Trash2 = Trash2;

  currentUser = computed(() => this.authService.user);
  isSeller = computed(() => this.authService.isSeller);

  avatarPreview = signal<string | null>(null);
  avatarFile = signal<File | null>(null);
  avatarRemoved = signal(false);
  isSaving = signal(false);

  profileForm = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
    email: [{ value: '', disabled: true }],
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
      this.avatarPreview.set(user.avatarId ?? null);
    }
  }

  get hasChanges(): boolean {
    const nameChanged = this.profileForm.get('name')?.value !== this.currentUser()?.name;
    const avatarChanged = this.avatarFile() !== null || this.avatarRemoved();
    return (nameChanged || avatarChanged) && this.profileForm.valid;
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

  get displayAvatar(): string | null {
    if (this.avatarRemoved()) return null;
    return this.avatarPreview() ?? this.currentUser()?.avatarId ?? null;
  }

  onAvatarSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    const errorMessage = validateImage(file);
    if (errorMessage) {
      this.snackBar.open(errorMessage, 'Dismiss', { duration: 3000 });
      return;
    }

    this.avatarFile.set(file);
    this.avatarRemoved.set(false);

    const reader = new FileReader();
    reader.onload = () => this.avatarPreview.set(reader.result as string);
    reader.readAsDataURL(file);
  }

  removeAvatar(): void {
    this.avatarPreview.set(null);
    this.avatarFile.set(null);
    this.avatarRemoved.set(!!this.currentUser()?.avatarId);
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

    const updateUser = (avatarId?: string) => {
      const formData = new FormData();

      const name = this.profileForm.get('name')?.value ?? '';

      formData.append('name', name);
      if (avatarId) formData.append('avatarId', avatarId);
      else if (!this.avatarRemoved())
        formData.append('avatarId', this.currentUser()?.avatarId ?? '');

      this.userService.updateUser(formData).subscribe({
        next: (updatedUser) => {
          if (updatedUser) {
            this.authService.updateCurrentUser(updatedUser);
            this.profileForm.markAsPristine();
            this.avatarRemoved.set(false);
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

    if (this.avatarFile()) {
      this.uploadImage(this.avatarFile()!, (id) => updateUser(id));
    } else {
      updateUser();
    }
  }
}
