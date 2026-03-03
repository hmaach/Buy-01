export interface Product {
  id: string;
  name: string;
  description: string;
  price: number;
  sellerId: string;
  sellerName: string;
  imageUrls: string[];
  createdAt: Date;
}

export interface ProductFormData {
  name: string;
  description: string;
  price: number;
  images: File[];
}
