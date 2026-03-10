import { Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, catchError, BehaviorSubject, switchMap } from 'rxjs';
import { LoginRequest, RegisterRequest, AuthResponse, User } from '../../models/user.model';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly TOKEN_KEY = 'auth_token';
  private readonly EXPIRES_AT_KEY = 'auth_expires_at';

  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  private currentUserSignal = signal<User | null>(null);

  constructor(
    private http: HttpClient,
    private router: Router,
  ) {
    this.initializeAuth();
  }

  private initializeAuth(): void {
    const token = this.getToken();
    const expiresAt = this.getExpiresAt();

    if (token && expiresAt) {
      const expiresAtDate = new Date(expiresAt);
      const now = new Date();

      if (now < expiresAtDate) {
        try {
          this.getCurrentUser()
            .pipe(
              tap((user) => {
                if (user) {
                  this.currentUserSubject.next(user);
                  this.currentUserSignal.set(user);
                }
              }),
            )
            .subscribe();
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
    return this.getToken() !== null;
  }

  get isSeller(): boolean {
    return this.currentUserSignal()?.role === 'SELLER';
  }

  get isClient(): boolean {
    return this.currentUserSignal()?.role === 'CLIENT';
  }

  login(credentials: LoginRequest): Observable<User | null> {
    return this.http.post<AuthResponse>(`/users/auth/login`, credentials).pipe(
      tap((response) => {
        this.handleAuthSuccess(response);
      }),
      switchMap(() => this.getCurrentUser()),
      tap((user) => {
        if (user) {
          this.currentUserSubject.next(user);
          this.currentUserSignal.set(user);
        }
      }),
      catchError((error) => {
        console.error('Login error:', error);
        throw error;
      }),
    );
  }

  getCurrentUser(): Observable<User | null> {
    return this.http.get<User | null>(`/users/me`).pipe(
      catchError((error) => {
        console.error('Error fetching current user:', error);
        throw error;
      }),
    );
  }

  public updateCurrentUser(user: User): void {
    this.currentUserSubject.next(user);
    this.currentUserSignal.set(user);
  }

  register(request: RegisterRequest): Observable<AuthResponse> {
    const payload = {
      name: request.name,
      email: request.email,
      password: request.password,
      role: request.role,
    };

    return this.http.post<AuthResponse>(`/users/auth/register`, payload).pipe(
      tap((response) => {
        this.router.navigateByUrl('/login', { state: { email: request.email } });
        this.handleAuthSuccess(response);
      }),
      catchError((error) => {
        console.error('Registration error:', error);
        throw error;
      }),
    );
  }

  private handleAuthSuccess(response: AuthResponse): void {
    localStorage.setItem(this.TOKEN_KEY, response.token);
    localStorage.setItem(this.EXPIRES_AT_KEY, response.expiresAt);
  }

  logout(): void {
    this.clearAuth();
    this.router.navigate(['/login']);
  }

  private clearAuth(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.EXPIRES_AT_KEY);
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
        this.logout();
        return null;
      }
    }

    return token;
  }

  getExpiresAt(): string | null {
    return localStorage.getItem(this.EXPIRES_AT_KEY);
  }

  getUserRole(): string | null {
    return this.currentUserSignal()?.role || null;
  }
}
