import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { Product, ProductCreateRequest, ProductListDto } from '../models/product.model';

@Injectable({
  providedIn: 'root',
})
export class ProductService {
  readonly http = inject(HttpClient);

  private parentPath = `/products`;


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
        userId: r.userId,
        quantity: r.quantity,
        rating: 4.8,
        reviewsCount: 124,
        thumbnails: actualIds,
        mainImage: actualIds[0],
        oldPrice: r.price + 20.4,
      }
      product.thumbnails = product.thumbnails
      return product;
    }));
  }

  getListOfProducts(beforeTime?: string): Observable<ProductListDto[]> {
    let url = this.parentPath;
    if (beforeTime) {
      url += `?beforeTime=${beforeTime}`;
    }

    return this.http.get<ProductListDto[]>(url).pipe(
      map(products =>
        products.map(p => ({
          ...p,
          badge: this.isNew(p.createdAt) ? 'New' : undefined
        }))
      )
    );
  }

  getMyProductsList(beforeTime?: string): Observable<ProductListDto[]> {
    let url = `${this.parentPath}/user`;
    if (beforeTime) {
      url += `?beforeTime=${beforeTime}`;
    }

    return this.http.get<ProductListDto[]>(url).pipe(
      map(products =>
        products.map(p => ({
          ...p,
          badge: this.isNew(p.createdAt) ? 'New' : undefined
        }))
      )
    );
  }

  updateProduct(id: string, changes: ProductCreateRequest): Observable<ProductCreateRequest> {
    return this.http.put<ProductCreateRequest>(`${this.parentPath}/${id}`, changes);
  }

  deleteProduct(id: string): Observable<void> {
    return this.http.delete<void>(`${this.parentPath}/${id}`);
  }

  private isNew(createdAt?: string): boolean {
    if (!createdAt) return false;

    try {
      const createdDate = new Date(createdAt);
      if (isNaN(createdDate.getTime())) return false;

      const now = Date.now();
      const fiveMinutesAgo = now - 5 * 60 * 1000;

      return createdDate.getTime() >= fiveMinutesAgo;
    } catch {
      return false;
    }
  }
}
