import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  {
    path: 'games/:gameId/teams',
    renderMode: RenderMode.Client
  },
  {
    path: 'admin/games/:gameId/teamsheet',
    renderMode: RenderMode.Client
  },
  {
    path: '**',
    renderMode: RenderMode.Prerender
  }
];
