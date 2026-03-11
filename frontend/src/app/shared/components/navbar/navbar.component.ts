import { Component, inject, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavigationEnd, Router, RouterLink, RouterLinkActive } from '@angular/router';
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
  House,
  PackagePlus,
} from 'lucide-angular';
import { filter } from 'rxjs';
import { env } from '../../../../environments/environment';

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
  private router = inject(Router);

  readonly CircleUserRound = CircleUserRound;
  readonly CircleX = CircleX;
  readonly Menu = Menu;
  readonly ShoppingCart = ShoppingCart;
  readonly LogIn = LogIn;
  readonly LogOut = LogOut;
  readonly LayoutDashboard = LayoutDashboard;
  readonly UserPlus = UserPlus;
  readonly Home = House;
  readonly PackagePlus = PackagePlus;

  env = env;

  currentUser = computed(() => {
    const user = this.authService.user;
    console.log('Navbar currentUser:', user);
    return user;
  });
  isLoggedIn = computed(() => {
    const loggedIn = this.authService.isAuthenticated;
    console.log('Navbar isLoggedIn:', loggedIn);
    return loggedIn;
  });
  isSeller = computed(() => this.authService.isSeller);

  currentUrl: string = '';
  mobileMenuOpen = false;

  ngOnInit(): void {
    this.router.events
      .pipe(filter((event) => event instanceof NavigationEnd))
      .subscribe((event: NavigationEnd) => {
        this.currentUrl = event.urlAfterRedirects;
      });
  }

  // Mobile menu state

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
