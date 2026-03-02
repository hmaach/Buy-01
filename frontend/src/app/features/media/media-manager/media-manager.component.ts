import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MediaService } from '../../../core/services/media.service';
import { Media } from '../../../core/models/media.model';

@Component({
  selector: 'app-media-manager',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, MatIconModule, MatSnackBarModule, MatProgressSpinnerModule],
  templateUrl: './media-manager.component.html',
  styleUrl: './media-manager.component.css'
})
export class MediaManagerComponent implements OnInit {
  media = signal<Media[]>([]);
  loading = signal(false);

  constructor(
    private mediaService: MediaService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.media.set(this.mediaService.getAllMedia());
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      this.loading.set(true);
      this.mediaService.uploadMedia(input.files[0])
        .then(() => {
          this.snackBar.open('Image uploaded', 'Close', { duration: 3000 });
          this.ngOnInit();
        })
        .catch((error: any) => {
          this.snackBar.open(error.message, 'Close', { duration: 3000 });
        })
        .finally(() => {
          this.loading.set(false);
          input.value = '';
        });
    }
  }

  deleteMedia(id: string): void {
    if (confirm('Are you sure you want to delete this image?')) {
      this.mediaService.deleteMedia(id)
        .then(() => {
          this.snackBar.open('Image deleted', 'Close', { duration: 3000 });
          this.ngOnInit();
        })
        .catch((error: any) => {
          this.snackBar.open(error.message, 'Close', { duration: 3000 });
        });
    }
  }
}
