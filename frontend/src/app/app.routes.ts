import { Routes } from '@angular/router';
import { authGuard } from './core/auth/guards/auth.guard';
import { roleGuard } from './core/auth/guards/role.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'products',
    pathMatch: 'full'
  },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent)
  },
  {
    path: 'products',
    loadComponent: () => import('./features/products/product-list/product-list.component').then(m => m.ProductListComponent)
  },
  {
    path: 'products/:id',
    loadComponent: () => import('./features/products/product-details/product-details.component').then(m => m.ProductDetailsComponent)
  },
  {
    path: 'seller',
    canActivate: [authGuard, roleGuard(['SELLER'])],
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./features/seller/dashboard/dashboard.component').then(m => m.SellerDashboardComponent)
      },
      {
        path: 'products/new',
        loadComponent: () => import('./features/seller/product-form/product-form.component').then(m => m.ProductFormComponent)
      },
      {
        path: 'products/:id/edit',
        loadComponent: () => import('./features/seller/product-form/product-form.component').then(m => m.ProductFormComponent)
      },
      {
        path: 'media',
        loadComponent: () => import('./features/media/media-manager/media-manager.component').then(m => m.MediaManagerComponent)
      }
    ]
  },
  {
    path: '**',
    redirectTo: 'products'
  }
];
