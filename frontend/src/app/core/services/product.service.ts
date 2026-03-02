import { Injectable, signal } from '@angular/core';
import { Product, ProductFormData } from '../models/product.model';
import { AuthService } from '../auth/services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private products = signal<Product[]>([
    {
      id: '1',
      name: 'MacBook Pro 14"',
      description: 'Power meets portability with the new MacBook Pro. Featuring the M3 Pro chip, up to 18GB of unified memory, and a stunning Liquid Retina XDR display.',
      price: 1999,
      sellerId: '2',
      sellerName: 'seller1',
      imageUrls: [
        'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=600&h=400&fit=crop',
        'https://images.unsplash.com/photo-1611186871348-b1ce696e52c9?w=600&h=400&fit=crop'
      ],
      createdAt: new Date('2024-01-15')
    },
    {
      id: '2',
      name: 'iPhone 15 Pro',
      description: 'The most powerful iPhone ever. A17 Pro chip, titanium design, and an advanced camera system with 5x optical zoom.',
      price: 999,
      sellerId: '2',
      sellerName: 'seller1',
      imageUrls: [
        'https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=600&h=400&fit=crop',
        'https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5?w=600&h=400&fit=crop'
      ],
      createdAt: new Date('2024-01-20')
    },
    {
      id: '3',
      name: 'iPad Pro 12.9"',
      description: 'The ultimate iPad experience with M2 chip, Liquid Retina XDR display, and Apple Pencil hover.',
      price: 1099,
      sellerId: '2',
      sellerName: 'seller1',
      imageUrls: [
        'https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=600&h=400&fit=crop'
      ],
      createdAt: new Date('2024-02-01')
    },
    {
      id: '4',
      name: 'AirPods Pro',
      description: 'Rebuilt from the sound up. With H2 chip, Active Noise Cancellation, and Adaptive Audio.',
      price: 249,
      sellerId: '2',
      sellerName: 'seller1',
      imageUrls: [
        'https://images.unsplash.com/photo-1606220588913-b3aacb4d2f46?w=600&h=400&fit=crop'
      ],
      createdAt: new Date('2024-02-10')
    },
    {
      id: '5',
      name: 'Apple Watch Ultra 2',
      description: 'The most rugged and capable Apple Watch. With precision GPS, 36-hour battery, and titanium case.',
      price: 799,
      sellerId: '2',
      sellerName: 'seller1',
      imageUrls: [
        'https://images.unsplash.com/photo-1434493789847-2f02dc6ca35d?w=600&h=400&fit=crop'
      ],
      createdAt: new Date('2024-02-15')
    },
    {
      id: '6',
      name: 'Mac Mini',
      description: 'Compact yet powerful. M2 or M2 Pro chip, up to 32GB memory, and four Thunderbolt 4 ports.',
      price: 599,
      sellerId: '2',
      sellerName: 'seller1',
      imageUrls: [
        'https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=600&h=400&fit=crop'
      ],
      createdAt: new Date('2024-02-20')
    },
    {
      id: '7',
      name: 'Studio Display',
      description: '27-inch 5K Retina display with 12MP Ultra Wide camera, three microphones, and six speakers.',
      price: 1599,
      sellerId: '2',
      sellerName: 'seller1',
      imageUrls: [
        'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=600&h=400&fit=crop'
      ],
      createdAt: new Date('2024-03-01')
    },
    {
      id: '8',
      name: 'Magic Keyboard',
      description: 'Keyboard with Touch ID, numeric keypad, and aluminum design. Seamless pairing and charging.',
      price: 199,
      sellerId: '2',
      sellerName: 'seller1',
      imageUrls: [
        'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=600&h=400&fit=crop'
      ],
      createdAt: new Date('2024-03-05')
    }
  ]);

  constructor(private authService: AuthService) {}

  getAllProducts(): Product[] {
    return this.products();
  }

  getProductById(id: string): Product | undefined {
    return this.products().find(p => p.id === id);
  }

  getSellerProducts(sellerId: string): Product[] {
    return this.products().filter(p => p.sellerId === sellerId);
  }

  getCurrentSellerProducts(): Product[] {
    const user = this.authService.user();
    if (!user || user.role !== 'SELLER') return [];
    return this.getSellerProducts(user.id);
  }

  addProduct(formData: ProductFormData): Promise<Product> {
    return new Promise((resolve) => {
      setTimeout(() => {
        const user = this.authService.user();
        if (!user) throw new Error('Not authenticated');

        const newProduct: Product = {
          id: String(Date.now()),
          name: formData.name,
          description: formData.description,
          price: formData.price,
          sellerId: user.id,
          sellerName: user.username,
          imageUrls: formData.images.map(img => URL.createObjectURL(img)),
          createdAt: new Date()
        };

        this.products.update(products => [...products, newProduct]);
        resolve(newProduct);
      }, 500);
    });
  }

  updateProduct(id: string, formData: ProductFormData): Promise<Product> {
    return new Promise((resolve, reject) => {
      setTimeout(() => {
        const user = this.authService.user();
        if (!user) {
          reject(new Error('Not authenticated'));
          return;
        }

        const index = this.products().findIndex(p => p.id === id);
        if (index === -1) {
          reject(new Error('Product not found'));
          return;
        }

        const product = this.products()[index];
        if (product.sellerId !== user.id) {
          reject(new Error('Not authorized'));
          return;
        }

        const updatedProduct: Product = {
          ...product,
          name: formData.name,
          description: formData.description,
          price: formData.price,
          imageUrls: formData.images.length > 0 
            ? formData.images.map(img => URL.createObjectURL(img))
            : product.imageUrls
        };

        this.products.update(products => {
          const newProducts = [...products];
          newProducts[index] = updatedProduct;
          return newProducts;
        });

        resolve(updatedProduct);
      }, 500);
    });
  }

  deleteProduct(id: string): Promise<void> {
    return new Promise((resolve, reject) => {
      setTimeout(() => {
        const user = this.authService.user();
        if (!user) {
          reject(new Error('Not authenticated'));
          return;
        }

        const product = this.products().find(p => p.id === id);
        if (!product) {
          reject(new Error('Product not found'));
          return;
        }

        if (product.sellerId !== user.id) {
          reject(new Error('Not authorized'));
          return;
        }

        this.products.update(products => products.filter(p => p.id !== id));
        resolve();
      }, 500);
    });
  }
}
