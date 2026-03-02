import { Routes } from '@angular/router';
import { CreateProduct } from './features/products/create-product/create-product';
import { ProductDetail } from './features/products/product-detail/product-detail';

export const routes: Routes = [
    { path: "", component: CreateProduct },
    { path: 'product/:id', component: ProductDetail }
];
