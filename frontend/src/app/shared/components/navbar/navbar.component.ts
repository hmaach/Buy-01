import { Component, inject, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { AuthService } from '../../../core/auth/services/auth.service';
import {
  LucideAngularModule,
  CircleUserRound,
  CircleX,
  Menu,
  ShoppingCart,
  LogIn,
  LogOut,
  LayoutDashboard,
  UserPlus,
} from 'lucide-angular';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    RouterLinkActive,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatDividerModule,
    LucideAngularModule,
  ],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css'],
})
export class NavbarComponent {
  private authService = inject(AuthService);

  readonly CircleUserRound = CircleUserRound;
  readonly CircleX = CircleX;
  readonly Menu = Menu;
  readonly ShoppingCart = ShoppingCart;
  readonly LogIn = LogIn;
  readonly LogOut = LogOut;
  readonly LayoutDashboard = LayoutDashboard;
  readonly UserPlus = UserPlus;

  currentUser = computed(() => this.authService.user);
  isLoggedIn = computed(() => this.authService.isAuthenticated);
  isSeller = computed(() => this.authService.isSeller);

  // Mobile menu state
  mobileMenuOpen = false;

  toggleMobileMenu(): void {
    this.mobileMenuOpen = !this.mobileMenuOpen;
  }

  closeMobileMenu(): void {
    this.mobileMenuOpen = false;
  }

  logout(): void {
    this.authService.logout();
    this.closeMobileMenu();
  }
}
