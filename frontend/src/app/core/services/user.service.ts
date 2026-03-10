import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { User } from '../models/user.model';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  readonly http = inject(HttpClient);

  private parentPath = `/users`;

  updateUser(formData: FormData): Observable<User> {
    return this.http.put<User>(`${this.parentPath}/me`, formData);
  }

  deleteUser(): Observable<void> {
    return this.http.delete<void>(`${this.parentPath}/me`);
  }
}
