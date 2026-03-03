import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatIconModule } from '@angular/material/icon';
import { ProductService } from '../../../core/services/product.service';

@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatIconModule
  ],
  templateUrl: './product-form.component.html',
  styleUrl: './product-form.component.css'
})
export class ProductFormComponent implements OnInit {
  form: FormGroup;
  loading = signal(false);
  isEdit = false;
  productId: string | null = null;
  imagePreviews = signal<string[]>([]);

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private productService: ProductService,
    private snackBar: MatSnackBar
  ) {
    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      description: ['', [Validators.required, Validators.minLength(10)]],
      price: [null, [Validators.required, Validators.min(1)]]
    });
  }

  ngOnInit() {
    this.productId = this.route.snapshot.paramMap.get('id');
    if (this.productId && this.productId !== 'new') {
      this.isEdit = true;
      const product = this.productService.getProductById(this.productId);
      if (product) {
        this.form.patchValue({
          name: product.name,
          description: product.description,
          price: product.price
        });
        this.imagePreviews.set(product.imageUrls);
      }
    }
  }

  onFilesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      const files = Array.from(input.files);
      
      // Validate file size
      const oversized = files.some(f => f.size > 2 * 1024 * 1024);
      if (oversized) {
        this.snackBar.open('File size must be less than 2MB', 'Close', { duration: 3000 });
        return;
      }

      // Create previews
      const previews: string[] = [];
      files.forEach(file => {
        const reader = new FileReader();
        reader.onload = () => {
          previews.push(reader.result as string);
          if (previews.length === files.length) {
            this.imagePreviews.update(current => [...current, ...previews]);
          }
        };
        reader.readAsDataURL(file);
      });
    }
  }

  removeImage(index: number): void {
    this.imagePreviews.update(previews => previews.filter((_, i) => i !== index));
  }

  async onSubmit() {
    if (this.form.invalid) return;

    this.loading.set(true);

    try {
      const { name, description, price } = this.form.value;
      const images = this.imagePreviews().map(url => {
        // Convert data URL to File for mock
        const arr = url.split(',');
        const mime = arr[0].match(/:(.*?);/)?.[1] || 'image/jpeg';
        const bstr = atob(arr[1]);
        let n = bstr.length;
        const u8arr = new Uint8Array(n);
        while (n--) {
          u8arr[n] = bstr.charCodeAt(n);
        }
        return new File([u8arr], 'image.jpg', { type: mime });
      });

      if (this.isEdit && this.productId) {
        await this.productService.updateProduct(this.productId, { name, description, price, images });
        this.snackBar.open('Product updated', 'Close', { duration: 3000 });
      } else {
        await this.productService.addProduct({ name, description, price, images });
        this.snackBar.open('Product created', 'Close', { duration: 3000 });
      }

      this.router.navigate(['/seller/dashboard']);
    } catch (error: any) {
      this.snackBar.open(error.message || 'An error occurred', 'Close', { duration: 3000 });
    } finally {
      this.loading.set(false);
    }
  }
}
