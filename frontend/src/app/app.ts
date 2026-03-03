import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from './shared/components/navbar/navbar.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, NavbarComponent],
  template: `
    <app-navbar></app-navbar>
    <main class="content">
      <router-outlet></router-outlet>
    </main>
  `,
  styles: [`
    :host {
      display: flex;
      flex-direction: column;
      min-height: 100vh;
    }
    
    .content {
      flex: 1;
      padding: 24px;
      max-width: 1200px;
      margin: 0 auto;
      width: 100%;
      box-sizing: border-box;
    }
  `]
})
export class AppComponent {}
