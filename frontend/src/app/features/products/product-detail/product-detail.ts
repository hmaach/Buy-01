import { CommonModule } from '@angular/common';
import { Component, inject, input, OnInit, signal } from '@angular/core';
import { Product } from '../../../core/models/api-response.model';
import { ProductService } from '../../../core/services/product.service';

@Component({
  selector: 'app-product-detail',
  imports: [CommonModule],
  templateUrl: './product-detail.html',
  styleUrls: ['./product-detail.scss'],
})

export class ProductDetail implements OnInit {

  readonly productService = inject(ProductService);
  errorMessage = signal<string | null>(null)
  product = signal<Product | null>(null);
  id = input.required<string>();

  selectedImageIndex = 0;
  ngOnInit(): void {
    this.productService.getProduct(this.id()).subscribe({
      next: (p) => this.product.set(p),
      error: (e) => this.errorMessage.set(e.error.title || "unkown error")
    })
  }

  get displayedMain(): string {

    const product = this.product();
    console.log(product?.mainImage, product?.thumbnails);
    if (!product || !product.thumbnails || product.thumbnails.length === 0) {
      return "./empty.png";
    }
    return product.thumbnails[this.selectedImageIndex] || product.mainImage;
  }

  selectThumbnail(index: number): void {
    this.selectedImageIndex = index;
  }

  buyNow(): void {
    console.log('Buy now:', this.product.name);
    // Redirect or handle checkout
  }
}
