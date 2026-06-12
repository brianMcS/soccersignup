import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AdminGamesComponent }   from './admin-games/admin-games.component';
import { AdminPlayersComponent } from './admin-players/admin-players.component';
import { UserService } from '../../services/user.service';

type AdminTab = 'games' | 'players';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, RouterModule, AdminGamesComponent, AdminPlayersComponent],
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.css'
})
export class AdminComponent {
  activeTab: AdminTab = 'games';

  constructor(private userService: UserService) {}

  get isAdmin(): boolean {
    return this.userService.isAdmin;
  }

  setTab(tab: AdminTab): void {
    if (tab === 'players' && !this.isAdmin) {
      return;
    }
    this.activeTab = tab;
  }
}
