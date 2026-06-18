import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';

import { AdminService } from './admin.service';
import { GameRequest } from '../models/game.model';

describe('AdminService', () => {
  let service: AdminService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(AdminService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('posts recurring games to the batch endpoint', () => {
    const games: GameRequest[] = [{
      gameDate: '2026-07-01',
      kickOffTime: '19:00',
      location: 'Dublin',
      maxPlayers: 14,
      feeAmount: 5,
      revolutLink: ''
    }];

    service.createGames(games).subscribe(response => {
      expect(response).toEqual([]);
    });

    const request = httpTesting.expectOne('/api/games/batch');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ games });
    request.flush([]);
  });
});
