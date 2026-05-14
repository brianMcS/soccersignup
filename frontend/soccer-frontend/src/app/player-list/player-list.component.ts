import { Component, OnInit } from '@angular/core';
import { PlayerService } from '../services/player.service';
import { Player } from '../models/player.model';
import { CommonModule, NgFor } from '@angular/common';
import { FormsModule } from '@angular/forms';


@Component({
  selector: 'app-player-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './player-list.component.html',
  styleUrl: './player-list.component.css'
})
export class PlayerListComponent implements OnInit {
  players: Player[] = [];
  filteredPlayers: Player[] = [];
  filterText = '';
  page = 0;

  constructor(private playerService: PlayerService) {}

  ngOnInit(): void {
      this.playerService.getPlayers().subscribe(data => {
        this.players = data;
      });
  }


  // nextPage(): void{
  //   this.page++;
  //   this.loadPlayers();
  // }

  // prevPage(): void {
  //   if(this.page > 0) this.page--;
  //   this.loadPlayers();
  // }

}
