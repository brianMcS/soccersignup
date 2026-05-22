import { Component, OnInit } from '@angular/core';
import { CommonModule, NgFor } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {Player} from '../../models/player.model';
import {PlayerService} from '../../services/player.service';


@Component({
  selector: 'app-player-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './players.component.html',
  styleUrl: './players.component.css'
})
export class PlayersComponent implements OnInit {
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
