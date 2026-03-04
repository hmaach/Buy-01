import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProductService } from '../../../core/services/product.service';
import { ProductCreateRequest } from '../../../core/models/api-response.model';
import { env } from '../../../../environments/environment';
import { Router } from '@angular/router';

@Component({
  selector: 'app-create-product',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './create-product.html',
  styleUrls: ['./create-product.scss'],
})
export class CreateProduct {
  readonly productService = inject(ProductService);
  readonly router = inject(Router);

  form: FormGroup;

  mainImagePreview: string | null = null;
  thumbnailPreviews: string[] = [];

  initialMain = './empty.png';
  initialThumbs = [];

  errorMessage = signal<string | null>(null);
  okMessage = signal<string | null>(null);

  constructor(private fb: FormBuilder) {
    this.form = this.fb.group({
      name: ['Wireless Audio Max 2', [Validators.required, Validators.minLength(3)]],
      description: [
        'Next-generation spatial audio headphones...',
        [Validators.required, Validators.minLength(20)]
      ],
      price: [299.00, [Validators.required, Validators.min(0.01)]],
      quantity: [48, [Validators.required, Validators.min(0)]],
    });
  }

  ngOnInit() {
    // Optional: if editing existing product, load initial previews here
  }

  // ───────────────────────────────────────────────
  //  Main Image Upload / Replace
  // ───────────────────────────────────────────────
  onMainImageSelected(event: Event): void {
    this.errorMessage.set(null);
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;

    const file = input.files[0];
    if (!this.isValidImage(file)) return;

    const formData = new FormData();
    if (file) {
      formData.append('files', file);
    }
    this.productService.uploadImages(formData).subscribe({
      next: (v) => this.mainImagePreview = v[0].imagesId,
      error: (e) => console.error(e),
    })
  }

  // ───────────────────────────────────────────────
  //  Multiple Thumbnails (Media Library) Add
  // ───────────────────────────────────────────────
  onThumbnailsSelected(event: Event): void {
    this.errorMessage.set(null);
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;


    const formData = new FormData();
    Array.from(input.files).forEach(file => {
      if (this.isValidImage(file)) {
        formData.append('files', file);
      }
    });
    this.productService.uploadImages(formData).subscribe({
      next: (v) => this.thumbnailPreviews.push(...v.map(e => e.imagesId)),
      error: (e) => console.error(e),
    })
    // Reset input so same file can be selected again if needed
    input.value = '';
  }

  // Drag & Drop support (for both zones)
  onDrop(event: DragEvent, target: 'main' | 'thumbnails'): void {
    // event.preventDefault();
    // event.stopPropagation();

    // if (!event.dataTransfer?.files?.length) return;

    // const files = Array.from(event.dataTransfer.files);

    // if (target === 'main') {
    //   // For main → take first valid image only
    //   const file = files.find(f => this.isValidImage(f));
    //   if (file) {
    //     this.mainImagePreview = { file, url: URL.createObjectURL(file) };
    //   }
    // } else {
    //   // For thumbnails → add multiple
    //   files.forEach(file => {
    //     if (this.isValidImage(file)) {
    //       this.thumbnailPreviews.push({ file, url: URL.createObjectURL(file) });
    //     }
    //   });
    // }
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
  }

  private isValidImage(file: File): boolean {
    const validTypes = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];
    const maxSize = 2 * 1024 * 1024; // 2MB

    if (!validTypes.includes(file.type)) {
      this.errorMessage.set('Only images allowed (jpg, png, webp, gif)');
      return false;
    }

    if (file.size > maxSize) {
      this.errorMessage.set('File too large — max 2MB');
      return false;
    }

    return true;
  }

  // Optional: remove a thumbnail preview
  removeThumbnail(index: number): void {
    const removed = this.thumbnailPreviews.splice(index, 1)[0];
    URL.revokeObjectURL(removed); // free memory
  }

  onSubmit(): void {
    this.errorMessage.set(null);
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const payload: ProductCreateRequest = {
      ...this.form.value,
      imagesIds: [this.mainImagePreview, ...this.thumbnailPreviews]
    };
    console.table(payload.imagesIds);

    this.productService.createProduct(payload).subscribe({
      next: (productResp) => {
        this.okMessage.set('Product created successfully')
        this.router.navigate(['/product', productResp.id]);
      },
      error: (err) => {        
        this.errorMessage.set(err.error.title  || "unknown error") 
        console.error('Error during upload/create chain:', err);
      }
    });
  }

  // Cleanup memory when component destroys
  ngOnDestroy(): void {
    if (this.mainImagePreview) URL.revokeObjectURL(this.mainImagePreview);
    this.thumbnailPreviews.forEach(p => URL.revokeObjectURL(p));
  }

  get name() { return this.form.get('name'); }
  get description() { return this.form.get('description'); }
  get price() { return this.form.get('price'); }
  get quantity() { return this.form.get('quantity'); }
  get displayedMain(): string {
    if (this.mainImagePreview) {
      return `${env.mediaUrl}/${this.mainImagePreview}`
    }
    return this.initialMain;
  }
  get displayedThumbs(): string[] { return this.thumbnailPreviews.map(v => `${env.mediaUrl}/${v}`) }

}
