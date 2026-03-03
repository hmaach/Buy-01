import { Injectable, signal } from '@angular/core';
import { Media } from '../models/media.model';
import { AuthService } from '../auth/services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class MediaService {
  private media = signal<Media[]>([
    {
      id: '1',
      url: 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=600&h=400&fit=crop',
      filename: 'macbook-1.jpg',
      uploadedAt: new Date('2024-01-15')
    },
    {
      id: '2',
      url: 'https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=600&h=400&fit=crop',
      filename: 'iphone-1.jpg',
      uploadedAt: new Date('2024-01-20')
    },
    {
      id: '3',
      url: 'https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=600&h=400&fit=crop',
      filename: 'ipad-1.jpg',
      uploadedAt: new Date('2024-02-01')
    }
  ]);

  constructor(private authService: AuthService) {}

  getAllMedia(): Media[] {
    return this.media();
  }

  uploadMedia(file: File): Promise<Media> {
    return new Promise((resolve, reject) => {
      setTimeout(() => {
        // Validate file size (max 2MB)
        if (file.size > 2 * 1024 * 1024) {
          reject(new Error('File size must be less than 2MB'));
          return;
        }

        const newMedia: Media = {
          id: String(Date.now()),
          url: URL.createObjectURL(file),
          filename: file.name,
          uploadedAt: new Date()
        };

        this.media.update(media => [...media, newMedia]);
        resolve(newMedia);
      }, 1000);
    });
  }

  deleteMedia(id: string): Promise<void> {
    return new Promise((resolve, reject) => {
      setTimeout(() => {
        const mediaItem = this.media().find(m => m.id === id);
        if (!mediaItem) {
          reject(new Error('Media not found'));
          return;
        }

        this.media.update(media => media.filter(m => m.id !== id));
        resolve();
      }, 500);
    });
  }
}
