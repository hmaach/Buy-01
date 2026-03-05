export interface ProductCreateRequest {
  name: string,
  description: string,
  price: number,
  int: number,
  imagesIds: string[]
}


export interface Product {
  name: string,
  price: number,
  oldPrice: number,
  rating: 4.8,
  reviewsCount: 124,
  quantity: number,
  description: string,
  mainImage: string,
  thumbnails: string[],
}

export interface ProductListDto {
  id: string,
  name: string,
  price: number,
  image: string,
  createdAt: string;
  badge?: 'New'
}