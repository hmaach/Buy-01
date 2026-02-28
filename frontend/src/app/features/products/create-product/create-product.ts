import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';

@Component({
  selector: 'app-create-product',
  imports: [CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatIconModule,
    MatSlideToggleModule,
    MatChipsModule],
  templateUrl: './create-product.html',
  styleUrl: './create-product.scss',
})
export class CreateProduct {
  productForm: FormGroup;
  isDarkMode = false;

  selectedImage = 'https://picsum.photos/id/1/600/400';
  thumbnails = [
    'https://picsum.photos/id/1/600/400',
    'https://picsum.photos/id/2/600/400',
    'https://picsum.photos/id/3/600/400'
  ];

  constructor(private fb: FormBuilder) {
    this.productForm = this.fb.group({
      name: ['Wireless Audio Max 2'],
      description: ['Next-generation spatial audio headphones...'],
      price: [299.00],
      category: ['audio']
    });
  }

  toggleTheme() {
    this.isDarkMode = !this.isDarkMode;
    // You can apply a class to the body or a container for CSS variables
    document.body.classList.toggle('dark-theme');
  }

  removeTag(tag: string) {
    console.log('Remove tag:', tag);
  }
}
