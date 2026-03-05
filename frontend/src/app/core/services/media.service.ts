import { inject, Injectable, signal } from '@angular/core';
import { Media } from '../models/media.model';
import { AuthService } from '../auth/services/auth.service';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MediaResponse } from '../models/api-response.model';

@Injectable({
  providedIn: 'root'
})
export class MediaService {
  readonly http = inject(HttpClient);

  private parentPath = `/media`;

  uploadImages(formData: FormData): Observable<MediaResponse[]> {
    return this.http.post<MediaResponse[]>(this.parentPath, formData);
  }
  deleteImage(id: string): Observable<any> {
    return this.http.delete(`${this.parentPath}/${id}`);
  }
}
