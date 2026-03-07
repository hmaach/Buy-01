export type Role = 'CLIENT' | 'SELLER';

export interface User {
  id: string;
  name: string;
  email: string;
  role: Role;
  avatarUrl: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  role: Role;
  avatar?: string;
}

export interface AuthResponse {
  token: string;
  expiresAt: string;
}
