import {CanActivateFn, Router} from '@angular/router';
import {inject} from '@angular/core';
import {UserService} from '../services/user.service';

export const adminGuard: CanActivateFn = () => {
  const user   = inject(UserService);
  const router = inject(Router);

  if (user.isAdmin) {
    return true;
  }

  return router.createUrlTree(['/play']);
};

