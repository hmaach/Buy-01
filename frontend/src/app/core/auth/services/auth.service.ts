import { Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { User, Role, LoginRequest, RegisterRequest, AuthResponse } from '../../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly TOKEN_KEY = 'fake_jwt_token';
  private readonly USER_KEY = 'fake_user';

  private currentUser = signal<User | null>(null);

  // Mock users database
  private mockUsers: User[] = [
    {
      id: '1',
      username: 'client1',
      email: 'client@email.com',
      role: 'CLIENT',
      avatarUrl: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=100&h=100&fit=crop'
    },
    {
      id: '2',
      username: 'seller1',
      email: 'seller@email.com',
      role: 'SELLER',
      avatarUrl: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&h=100&fit=crop'
    }
  ];

  constructor(private router: Router) {
    this.loadUserFromStorage();
  }

  private loadUserFromStorage(): void {
    const token = localStorage.getItem(this.TOKEN_KEY);
    const userJson = localStorage.getItem(this.USER_KEY);
    if (token && userJson) {
      try {
        const user = JSON.parse(userJson) as User;
        this.currentUser.set(user);
      } catch {
        this.logout();
      }
    }
  }

  get user() {
    return this.currentUser.asReadonly();
  }

  get isAuthenticated(): boolean {
    return this.currentUser() !== null;
  }

  get isSeller(): boolean {
    return this.currentUser()?.role === 'SELLER';
  }

  get isClient(): boolean {
    return this.currentUser()?.role === 'CLIENT';
  }

  login(request: LoginRequest, role: Role): Promise<AuthResponse> {
    return new Promise((resolve, reject) => {
      setTimeout(() => {
        // Find user by email and role
        const user = this.mockUsers.find(u => 
          u.email === request.email && u.role === role
        );

        if (user && request.password === 'password123') {
          const fakeToken = `fake_jwt_${Date.now()}_${Math.random().toString(36).substring(7)}`;
          
          localStorage.setItem(this.TOKEN_KEY, fakeToken);
          localStorage.setItem(this.USER_KEY, JSON.stringify(user));
          this.currentUser.set(user);

          console.log('Login successful:', { token: fakeToken, user });
          
          resolve({ token: fakeToken, user });
        } else {
          reject(new Error('Invalid email or password'));
        }
      }, 800);
    });
  }

  register(request: RegisterRequest): Promise<AuthResponse> {
    return new Promise((resolve, reject) => {
      setTimeout(() => {
        // Check if email already exists
        const exists = this.mockUsers.some(u => u.email === request.email);
        
        if (exists) {
          reject(new Error('Email already registered'));
          return;
        }

        // Create new user
        const newUser: User = {
          id: String(this.mockUsers.length + 1),
          username: request.username,
          email: request.email,
          role: request.role,
          avatarUrl: request.avatar 
            ? URL.createObjectURL(request.avatar)
            : 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=100&h=100&fit=crop'
        };

        this.mockUsers.push(newUser);

        const fakeToken = `fake_jwt_${Date.now()}_${Math.random().toString(36).substring(7)}`;
        
        localStorage.setItem(this.TOKEN_KEY, fakeToken);
        localStorage.setItem(this.USER_KEY, JSON.stringify(newUser));
        this.currentUser.set(newUser);

        console.log('Registration successful:', { token: fakeToken, user: newUser });

        resolve({ token: fakeToken, user: newUser });
      }, 1000);
    });
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.currentUser.set(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }
}
