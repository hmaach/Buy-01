import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ProductService } from '../../../core/services/product.service';
import { Product } from '../../../core/models/product.model';

@Component({
  selector: 'app-product-details',
  standalone: true,
  imports: [CommonModule, RouterLink, MatCardModule, MatButtonModule, MatIconModule, MatSnackBarModule],
  templateUrl: './product-details.component.html',
  styleUrl: './product-details.component.css'
})
export class ProductDetailsComponent implements OnInit {
  product = signal<Product | null>(null);
  selectedImageIndex = signal(0);

  constructor(
    private route: ActivatedRoute,
    private productService: ProductService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      const product = this.productService.getProductById(id);
      this.product.set(product || null);
    }
  }

  selectImage(index: number) {
    this.selectedImageIndex.set(index);
  }

  contactSeller() {
    this.snackBar.open('Contact feature coming soon!', 'Close', { duration: 3000 });
  }
}
