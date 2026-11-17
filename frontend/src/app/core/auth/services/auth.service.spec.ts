import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { AuthService } from './auth.service';
import { User } from '../../models/user.model';

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;

  const seller: User = { id: '1', name: 'Sam', email: 'sam@example.com', role: 'SELLER' };
  const inOneHour = new Date(Date.now() + 60 * 60 * 1000).toISOString();

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
    localStorage.clear();
  });

  it('is not authenticated without a stored token', () => {
    expect(service.isAuthenticated).toBeFalse();
    expect(service.getToken()).toBeNull();
  });

  it('stores the token and loads the user after login', () => {
    service.login({ email: 'sam@example.com', password: 'secret' }).subscribe();

    http.expectOne('/users/auth/login').flush({ token: 'jwt-token', expiresAt: inOneHour });
    http.expectOne('/users/me').flush(seller);

    expect(service.getToken()).toBe('jwt-token');
    expect(service.isAuthenticated).toBeTrue();
    expect(service.isSeller).toBeTrue();
  });

  it('clears the token on logout', () => {
    service.login({ email: 'sam@example.com', password: 'secret' }).subscribe();
    http.expectOne('/users/auth/login').flush({ token: 'jwt-token', expiresAt: inOneHour });
    http.expectOne('/users/me').flush(seller);

    service.logout();

    expect(service.getToken()).toBeNull();
    expect(service.isAuthenticated).toBeFalse();
    expect(service.user).toBeNull();
  });

  it('drops an expired token', () => {
    localStorage.setItem('auth_token', 'old-token');
    localStorage.setItem('auth_expires_at', new Date(Date.now() - 1000).toISOString());

    expect(service.getToken()).toBeNull();
    expect(localStorage.getItem('auth_token')).toBeNull();
  });
});
