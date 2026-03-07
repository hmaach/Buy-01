import { Injectable, signal, computed } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, catchError, of, BehaviorSubject } from 'rxjs';
import { LoginRequest, RegisterRequest, AuthResponse, Role, User } from '../../models/user.model';
import { env } from '../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly TOKEN_KEY = 'auth_token';
  private readonly EXPIRES_AT_KEY = 'auth_expires_at';
  private readonly USER_KEY = 'auth_user';

  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  private currentUserSignal = signal<User | null>(null);

  constructor(
    private http: HttpClient,
    private router: Router
  ) {
    this.initializeAuth();
  }

  private initializeAuth(): void {
    const token = this.getToken();
    const expiresAt = this.getExpiresAt();
    const userJson = localStorage.getItem(this.USER_KEY);

    if (token && expiresAt && userJson) {
      const expiresAtDate = new Date(expiresAt);
      const now = new Date();

      if (now < expiresAtDate) {
        try {
          const user = JSON.parse(userJson) as User;
          this.currentUserSubject.next(user);
          this.currentUserSignal.set(user);
        } catch {
          this.clearAuth();
        }
      } else {
        // Token expired
        this.logout();
      }
    }
  }

  get user() {
    return this.currentUserSignal();
  }

  get isAuthenticated(): boolean {
    return this.currentUserSignal() !== null;
  }

  get isSeller(): boolean {
    return this.currentUserSignal()?.role === 'SELLER';
  }

  get isClient(): boolean {
    return this.currentUserSignal()?.role === 'CLIENT';
  }

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`/users/auth/login`, credentials).pipe(
      tap(response => {
        this.handleAuthSuccess(response);
      }),
      catchError(error => {
        console.error('Login error:', error);
        throw error;
      })
    );
  }

  register(request: RegisterRequest): Observable<AuthResponse> {
    // Use placeholder UUID for avatar
    const payload = {
      ...request,
      avatar: request.avatar || '00000000-0000-0000-0000-000000000000'
    };

    return this.http.post<AuthResponse>(`/users/auth/register`, payload).pipe(
      tap(response => {
        this.handleAuthSuccess(response);
      }),
      catchError(error => {
        console.error('Registration error:', error);
        throw error;
      })
    );
  }

  private handleAuthSuccess(response: AuthResponse): void {
    localStorage.setItem(this.TOKEN_KEY, response.token);
    localStorage.setItem(this.EXPIRES_AT_KEY, response.expiresAt);

    // Extract user info from token (decode JWT)
    const user = this.extractUserFromToken(response.token);
    if (user) {
      localStorage.setItem(this.USER_KEY, JSON.stringify(user));
      this.currentUserSubject.next(user);
      this.currentUserSignal.set(user);
    }
  }

  private extractUserFromToken(token: string): User | null {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return {
        id: payload.sub || payload.userId || 'unknown',
        name: payload.name || payload.name || 'User',
        email: payload.email || 'user@example.com',
        role: payload.role || 'CLIENT',
        avatarUrl: payload.avatar || ''
      };
    } catch {
      // Return minimal user info since we don't get user details from login response
      return {
        id: 'unknown',
        name: 'User',
        email: 'user@example.com',
        role: 'CLIENT',
        avatarUrl: ''
      };
    }
  }

  logout(): void {
    this.clearAuth();
    this.router.navigate(['/login']);
  }

  private clearAuth(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.EXPIRES_AT_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.currentUserSubject.next(null);
    this.currentUserSignal.set(null);
  }

  getToken(): string | null {
    const token = localStorage.getItem(this.TOKEN_KEY);
    const expiresAt = this.getExpiresAt();

    if (token && expiresAt) {
      const expiresAtDate = new Date(expiresAt);
      const now = new Date();

      if (now >= expiresAtDate) {
        // Token expired, logout
        this.logout();
        return null;
      }
    }

    return token;
  }

  getExpiresAt(): string | null {
    return localStorage.getItem(this.EXPIRES_AT_KEY);
  }
}
