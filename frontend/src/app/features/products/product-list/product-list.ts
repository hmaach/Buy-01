import { Component, OnInit, signal } from '@angular/core';
import { Product } from '../../../core/models/product.model';
import { CommonModule } from '@angular/common';
interface ProductListDto {
  id: string,
  name: string,
  price: number,
  image: string,
  badge?: 'New'
}
@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './product-list.html',
  styleUrls: ['./product-list.scss']
})
export class ProductList implements OnInit {

  products: ProductListDto[] = [
    {
      id: "1",
      name: 'Wireless Audio Max 2',
      price: 299.00,
      image: 'https://images.unsplash.com/photo-1605640840607-14ac428ac0a8?w=800',
      badge: 'New',
    },
    {
      id: "2",
      name: 'Pro Earbuds v3',
      price: 149.00,
      image: 'https://images.unsplash.com/photo-1605640840607-14ac428ac0a8?w=800',
    },
    {
      id: "3",
      name: 'Studio Monitor Pro',
      price: 399.00,
      image: 'https://images.unsplash.com/photo-1583394838336-acd977736f90?w=800',
    },
    {
      id: "4",
      name: 'Active Sport Wireless',
      price: 129.00,
      image: 'https://images.unsplash.com/photo-1613040809024-b4ef374e73c2?w=800',
    },
  ];

  addToCart(product: ProductListDto) {
    console.log(`Added to cart: ${product.name} - $${product.price}`);
    // Here you would call cart service
  }

  ngOnInit() {
    // this.products.set(this.productService.getAllProducts());
  }
}
