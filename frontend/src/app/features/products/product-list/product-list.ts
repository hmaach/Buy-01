import { AfterViewInit, Component, ElementRef, inject, OnDestroy, OnInit, signal, viewChild, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProductService } from '../../../core/services/product.service';
import { env } from '../../../../environments/environment';
import { Router } from '@angular/router';
import { ProductListDto } from '../../../core/models/product.model';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './product-list.html',
  styleUrls: ['./product-list.scss']
})
export class ProductList implements OnInit, OnDestroy, AfterViewInit {
  private productService = inject(ProductService);
  private router = inject(Router);

  products = signal<ProductListDto[]>([]);
  isLoading = signal(false);
  hasMore = signal(true);
  error = signal<string | null>(null);

  private beforeTime = signal<string>('');
  private observer!: IntersectionObserver;

  readonly sentinel = viewChild.required<ElementRef<HTMLDivElement>>('sentinel');

  ngAfterViewInit() {
    const el = this.sentinel();
    if (el?.nativeElement) {
      this.observer.observe(el.nativeElement);
    }
  }

  ngOnInit() {
    this.loadMore();

    this.observer = new IntersectionObserver(
      entries => {
        if (entries[0]?.isIntersecting && this.hasMore() && !this.isLoading()) {
          this.loadMore();
        }
      },
      { threshold: 0.1, rootMargin: '200px' }
    );
  }

  ngOnDestroy() {
    this.observer?.disconnect();
  }

  loadMore() {
    if (this.isLoading()) return;

    this.isLoading.set(true);
    this.error.set(null);

    this.productService.getListOfProducts(this.beforeTime()).subscribe({
      next: (newProducts) => {
        if (newProducts.length === 0) {
          this.hasMore.set(false);
        } else {
          this.products.update(current => [...current, ...newProducts]);

          const lastProduct = newProducts[newProducts.length - 1];
          this.beforeTime.set(lastProduct?.createdAt ?? '');
        }
        this.isLoading.set(false);
      },
      error: (err) => {
        this.error.set('Failed to load products');
        this.isLoading.set(false);
        console.error(err);
      }
    });
  }

  moreDetail(product: ProductListDto) {
    this.router.navigate(['products', product.id]);
  }

  imageUrl(id: string) {
    if (!id) return './empty.png';
    return `${env.mediaUrl}/${id}`;
  }
}

