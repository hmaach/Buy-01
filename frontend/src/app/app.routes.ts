import { Routes } from '@angular/router';
import { authGuard } from './core/auth/guards/auth.guard';
import { roleGuard } from './core/auth/guards/role.guard';
import { guestGuard } from './core/auth/guards/guest.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./features/products/product-list/product-list').then((m) => m.ProductList),
  },
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/auth/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'register',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/auth/register/register.component').then((m) => m.RegisterComponent),
  },
  {
    path: 'products/new',
    canActivate: [roleGuard(['SELLER'])],
    loadComponent: () =>
      import('./features/products/create-product/create-product').then((m) => m.ProductForm),
  },
  {
    path: 'products/:id/edit',
    canActivate: [roleGuard(['SELLER'])],
    loadComponent: () =>
      import('./features/products/create-product/create-product').then((m) => m.ProductForm),
  },
  {
    path: 'products/list',
    loadComponent: () =>
      import('./features/products/product-list/product-list').then((m) => m.ProductList),
  },
  {
    path: 'products/:id',
    loadComponent: () =>
      import('./features/products/product-detail/product-detail').then((m) => m.ProductDetail),
  },
  {
    path: '**',
    redirectTo: 'products',
  },
];
