import { Routes } from '@angular/router';
import {authGuard} from './guards/auth.guard';
import {adminGuard} from './guards/admin.guard';

export const appRoutes: Routes = [
  // ─── Public ──────────────────────────────────────────────────────────────
  {
    path: '',
    loadComponent: () => import('./pages/home/home.component')
      .then(m => m.HomeComponent)
  },
  {
    path: 'register',
    loadComponent: () => import('./pages/register/register.component')
      .then(m => m.RegisterComponent)
  },

  // ─── Auth-gated (players) ─────────────────────────────────────────────────
  {
    path: 'play',
    canActivate: [authGuard],
    loadComponent: () => import('./components/game-signup-list/game-signup-list.component')
      .then(m => m.GameSignupListComponent)
  },
  {
    path: 'history',
    canActivate: [authGuard],
    loadComponent: () => import('./components/history/history.component')
      .then(m => m.HistoryComponent)
  },
  {
    path: 'games/:gameId/teams',
    canActivate: [authGuard],
    loadComponent: () => import('./components/team-sheet-view/team-sheet-view.component')
      .then(m => m.TeamSheetViewComponent)
  },
  {
    path: 'admin/games/:gameId/teamsheet',
    canActivate: [adminGuard],
    loadComponent: () =>
      import('./pages/admin/admin-team-sheet/admin-team-sheet.component')
        .then(m => m.AdminTeamSheetComponent)
  },

  // ─── Admin-gated ──────────────────────────────────────────────────────────
  {
    path: 'admin',
    canActivate: [adminGuard],
    loadComponent: () => import('./pages/admin/admin.component')
      .then(m => m.AdminComponent)
  },

  // ─── Fallback ─────────────────────────────────────────────────────────────
  {
    path: '**',
    loadComponent: () => import('./pages/not-found/not-found.component')
      .then(m => m.NotFoundComponent)
  }
];
