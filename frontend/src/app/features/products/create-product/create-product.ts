import { Component, computed, effect, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ProductService } from '../../../core/services/product.service';
import { env } from '../../../../environments/environment';
import { MediaService } from '../../../core/services/media.service';

@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './product-form.html',
  styleUrls: ['./product-form.scss']
})
export class ProductForm {
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  readonly productService = inject(ProductService);
  readonly mediaService = inject(MediaService);

  isEditMode = signal(false);
  isLoading = signal(false);
  errorMessage = signal<string | null>(null);
  okMessage = signal<string | null>(null);
  productId = signal<string | null>(null);

  mainImagePreview = signal<string | null>(null);
  thumbnailPreviews = signal<string[]>([]);

  readonly initialMain = './empty.png';

  form: FormGroup = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
    description: ['', [Validators.required, Validators.minLength(20), Validators.maxLength(500)]],
    price: [0, [Validators.required, Validators.min(0.01)]],
    quantity: [0, [Validators.required, Validators.min(0)]],
  });

  pageTitle = computed(() => this.isEditMode() ? 'Edit Product Details' : 'Create Product Details');
  submitText = computed(() => this.isEditMode() ? 'Update Product' : 'Create Product');

  constructor() {
    effect(() => {
      const id = this.route.snapshot.paramMap.get('id');
      if (id) {
        this.productId.set(id);
        this.loadProduct(id);
      } else {
        this.resetForm();
      }
    });
  }

  private loadProduct(id: string) {
    this.isLoading.set(true);
    this.productService.getProduct(id).subscribe({
      next: (product) => {
        this.form.patchValue({
          name: product.name,
          description: product.description,
          price: product.price,
          quantity: product.quantity,
        });

        this.mainImagePreview.set(product.mainImage || null);
        this.thumbnailPreviews.set(product.thumbnails.splice(1) || []);

        this.isEditMode.set(true);
        this.isLoading.set(false);
      },
      error: () => {
        this.errorMessage.set('Failed to load product');
        this.isLoading.set(false);
      }
    });
  }

  private resetForm() {
    this.form.reset({ name: '', description: '', price: 0, quantity: 0 });
    this.mainImagePreview.set(null);
    this.thumbnailPreviews.set([]);
    this.isEditMode.set(false);
    this.errorMessage.set(null);
    this.okMessage.set(null);
  }

  onMainImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const mainImageId = this.mainImagePreview();
    if (!input.files?.length) return;

    const file = input.files[0];
    if (!mainImageId) {
      this.uploadImage(file, true);
      return;
    }
    this.mediaService.deleteImage(mainImageId).subscribe({
      next: () => this.uploadImage(file, true),
      error: () => this.errorMessage.set('Delete Image failed'),
    })
  }

  onMainImageDrop(event: DragEvent): void {
    event.preventDefault();
    const file = event.dataTransfer?.files[0];
    if (file) this.uploadImage(file, true);
  }

  onThumbnailsSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;
    Array.from(input.files).forEach(file => this.uploadImage(file, false));
    input.value = '';
  }

  private uploadImage(file: File, isMain: boolean): void {
    if (!this.isValidImage(file)) return;

    const formData = new FormData();
    formData.append('files', file);

    this.mediaService.uploadImages(formData).subscribe({
      next: (res) => {
        const id = res[0]?.imagesId;
        if (id) {
          if (isMain) {
            this.mainImagePreview.set(id);
          } else {
            this.thumbnailPreviews.update(prev => [...prev, id]);
          }
        }
      },
      error: () => this.errorMessage.set('Image upload failed')
    });
  }

  removeThumbnail(index: number): void {
    var id = this.thumbnailPreviews()[index];
    this.mediaService.deleteImage(id).subscribe({
      next: () => {
        this.thumbnailPreviews.update(prev => {
          const newList = [...prev];
          newList.splice(index, 1);
          return newList;
        });
      },
      error: () => this.errorMessage.set('Delete Image failed'),
    })
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
  }

  private isValidImage(file: File): boolean {
    const validTypes = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];
    const maxSize = 2 * 1024 * 1024;
    if (!validTypes.includes(file.type)) {
      this.errorMessage.set('Only JPG, PNG, WebP, GIF allowed');
      return false;
    }
    if (file.size > maxSize) {
      this.errorMessage.set('File too large (max 2MB)');
      return false;
    }
    return true;
  }

  // ── Form getters ─────────────────────────────────────────
  get name() { return this.form.get('name'); }
  get description() { return this.form.get('description'); }
  get price() { return this.form.get('price'); }
  get quantity() { return this.form.get('quantity'); }

  get displayedMain(): string {
    return this.mainImagePreview()
      ? `${env.mediaUrl}/${this.mainImagePreview()}`
      : this.initialMain;
  }

  get displayedThumbs(): string[] {
    return this.thumbnailPreviews().map(id => `${env.mediaUrl}/${id}`);
  }

  // ── Submit ───────────────────────────────────────────────
  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.okMessage.set(null);

    const imagesIds = [
      this.mainImagePreview(),
      ...this.thumbnailPreviews()
    ].filter((id): id is string => !!id);

    const payload = {
      ...this.form.value,
      imagesIds
    };

    const observable = this.isEditMode()
      ? this.productService.updateProduct(this.productId()!, payload)
      : this.productService.createProduct(payload);

    observable.subscribe({
      next: (resp) => {
        this.okMessage.set(this.isEditMode() ? 'Product updated successfully' : 'Product created successfully');
        this.isLoading.set(false);
        this.router.navigate(['/products', resp.id || this.productId()]);
      },
      error: (err) => {
        this.errorMessage.set(err.error?.message || 'Operation failed');
        this.isLoading.set(false);
      }
    });
  }
}