import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AdminGamesComponent }   from './admin-games/admin-games.component';
import { AdminPlayersComponent } from './admin-players/admin-players.component';

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

  setTab(tab: AdminTab): void {
    this.activeTab = tab;
  }
}
