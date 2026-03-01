import { inject, Injectable } from '@angular/core';
import { BaseService } from './base.service';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class ProductService {
  readonly http = inject(HttpClient);

  private parentPath = `/media`;

  search(input: string): Observable<MediaResponse[]> {
    return this.http.get<MediaResponse[]>(`${this.parentPath}/search?input=${input}`);
  }

  uploadImages(formData: FormData): Observable<MediaResponse[]> {
    return this.http.post<MediaResponse[]>('/media', formData);
  }

  createProduct(payload: any): Observable<any> {
    // endpoint assumed to be /products, adjust if backend differs
    return this.http.post<any>('/products', payload);
  }
}
