import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { MediaResponse, Product } from '../models/api-response.model';
import { env } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class ProductService {
  readonly http = inject(HttpClient);

  private parentPath = `/products`;

  uploadImages(formData: FormData): Observable<MediaResponse[]> {
    return this.http.post<MediaResponse[]>('/media', formData);
  }

  createProduct(payload: any): Observable<any> {
    return this.http.post<any>(this.parentPath, payload);
  }

  getProduct(id: String): Observable<Product> {
    return this.http.get<any>(`${this.parentPath}/${id}`).pipe(map(r => {
      const actualIds = JSON.parse(r.imagesIds[0]);

      const product: Product = {
        name: r.name,
        description: r.description,
        price: r.price,
        quantity: r.quantity,
        rating: 4.8,
        reviewsCount: 124,
        thumbnails: actualIds,
        mainImage: `${env.mediaUrl}/${actualIds[0]}`,
        oldPrice: r.price + 20.4,
      }
      product.thumbnails = product.thumbnails.map(v => `${env.mediaUrl}/${v}`)
      return product;
    }));
  }
}
