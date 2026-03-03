export type Role = 'CLIENT' | 'SELLER';

export interface User {
  id: string;
  username: string;
  email: string;
  role: Role;
  avatarUrl: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  role: Role;
  avatar?: File;
}

export interface AuthResponse {
  token: string;
  user: User;
}
