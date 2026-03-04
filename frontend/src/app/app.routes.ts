import { Routes } from '@angular/router';
import { authGuard } from './core/auth/guards/auth.guard';
import { roleGuard } from './core/auth/guards/role.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent)
  },
  {
    path: "products/new",
    loadComponent: () => import('./features/products/create-product/create-product').then(m => m.CreateProduct)
  },
  {
    path: 'products/list',
    loadComponent: () => import('./features/products/product-list/product-list').then(m => m.ProductList)
  },
  {
    path: 'products/:id',
    loadComponent: () => import('./features/products/product-detail/product-detail').then(m => m.ProductDetail)
  },
  // {
  //   path: 'seller',
  //   canActivate: [authGuard, roleGuard(['SELLER'])],
  //   children: [
  //     {
  //       path: 'dashboard',
  //       loadComponent: () => import('./features/seller/dashboard/dashboard.component').then(m => m.SellerDashboardComponent)
  //     },
  //     {
  //       path: 'products/new',
  //       loadComponent: () => import('./features/seller/product-form/product-form.component').then(m => m.ProductFormComponent)
  //     },
  //     {
  //       path: 'products/:id/edit',
  //       loadComponent: () => import('./features/seller/product-form/product-form.component').then(m => m.ProductFormComponent)
  //     },
  //     {
  //       path: 'media',
  //       loadComponent: () => import('./features/media/media-manager/media-manager.component').then(m => m.MediaManagerComponent)
  //     }
  //   ]
  // },
  {
    path: '**',
    redirectTo: 'products'
  }
];
