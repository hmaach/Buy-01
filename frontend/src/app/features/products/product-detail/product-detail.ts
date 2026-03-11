import { CommonModule } from '@angular/common';
import { Component, Inject, inject, input, OnInit, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { ProductService } from '../../../core/services/product.service';
import { Product } from '../../../core/models/product.model';
import { env } from '../../../../environments/environment';
import { MatMenu, MatMenuModule, MatMenuTrigger } from "@angular/material/menu";
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/auth/services/auth.service';

@Component({
  selector: 'app-confirm-delete-dialog',
  standalone: true,
  imports: [MatDialogModule, MatButtonModule],
  template: `
    <h2 mat-dialog-title>Delete product?</h2>
    <mat-dialog-content>Do you really want to delete "<strong>{{ data.name }}</strong>"?</mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Cancel</button>
      <button mat-button color="warn" (click)="onConfirm()">Delete</button>
    </mat-dialog-actions>
  `,
})
export class ConfirmDeleteDialog {
  constructor(
    public dialogRef: MatDialogRef<ConfirmDeleteDialog>,
    @Inject(MAT_DIALOG_DATA) public data: { name: string }
  ) { }

  onConfirm(): void {
    this.dialogRef.close(true);
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}

@Component({
  selector: 'app-product-detail',
  imports: [CommonModule, MatMenu, MatMenuTrigger, MatButtonModule, MatMenuModule, MatDialogModule, MatSnackBarModule],
  templateUrl: './product-detail.html',
  styleUrls: ['./product-detail.scss'],
})

export class ProductDetail implements OnInit {

  readonly productService = inject(ProductService);
  readonly router = inject(Router);
  readonly dialog = inject(MatDialog);
  readonly snackBar = inject(MatSnackBar);
  readonly authService = inject(AuthService);

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
    var id = product.thumbnails[this.selectedImageIndex] || product.mainImage;
    return `${env.mediaUrl}/${id}`;
  }

  selectThumbnail(index: number): void {
    this.selectedImageIndex = index;
  }

  buyNow(): void {
    console.log('Buy now:', this.product.name);
    // Redirect or handle checkout
  }

  async onDeleteClick(): Promise<void> {
    const currentProduct = this.product();
    if (!currentProduct) {
      return;
    }

    const dialogRef = this.dialog.open(ConfirmDeleteDialog, {
      width: '360px',
      data: { name: currentProduct.name },
    });

    const confirmed = await firstValueFrom(dialogRef.afterClosed());
    if (!confirmed) {
      return;
    }

    this.productService.deleteProduct(this.id()).subscribe({
      next: () => {
        this.snackBar.open('Product deleted successfully', 'Close', { duration: 3000 });
        this.router.navigate(['/products']);
      },
      error: (error: any) => {
        this.snackBar.open(error?.message || 'Failed to delete product', 'Close', { duration: 3000 });
      }
    });
  }

  imageUrl(id: string) {
    if (!id) return './empty.png';
    return `${env.mediaUrl}/${id}`;
  }
  goToEdit(): void {
    this.router.navigate([`products/${this.id()}/edit`]);
  }
}
