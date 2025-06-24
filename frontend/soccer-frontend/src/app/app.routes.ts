import { Routes } from '@angular/router';

export const appRoutes: Routes = [
  { path: '', loadComponent: () => import('./pages/home/home.component').then(m => m.HomeComponent) },
  { path: 'play', loadComponent: () => import('./components/game-signup-list/game-signup-list.component').then(m => m.GameSignupListComponent) },
  { path: 'register', loadComponent: () => import('./pages/register/register.component').then(m => m.RegisterComponent) },
  { path: 'players', loadComponent: () => import('./player-list/player-list.component').then(m => m.PlayerListComponent) }
];
