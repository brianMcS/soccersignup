import { Routes } from '@angular/router';
import { SignupComponent } from './components/signup/signup.component';
//import { LoginComponent } from './components/login/login.component';
import { GameSignupListComponent } from './components/game-signup-list/game-signup-list.component';
import { HomeComponent } from './pages/home/home.component';

export const appRoutes: Routes = [
  { path: '', component: HomeComponent},
  { path: 'play', component: GameSignupListComponent },
 //{ path: 'register', component: SignupComponent },
  //{ path: 'login', component: LoginComponent },
    // { path: 'current-list', component: CurrentListComponent },
  // { path: 'history', component: GameHistoryComponent },
];
