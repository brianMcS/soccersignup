import { Routes } from '@angular/router';
//import { LoginComponent } from './components/login/login.component';
import { GameSignupListComponent } from './components/game-signup-list/game-signup-list.component';
import { HomeComponent } from './pages/home/home.component';
import { RegisterComponent } from './pages/register/register.component';

export const appRoutes: Routes = [
  { path: '', component: HomeComponent},
  { path: 'play', component: GameSignupListComponent },
  { path: 'register', component: RegisterComponent },
  //{ path: 'login', component: LoginComponent },
    // { path: 'current-list', component: CurrentListComponent },
  // { path: 'history', component: GameHistoryComponent },
];
