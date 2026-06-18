import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';

import { GamesService } from './games.service';
import { GameRequest } from '../models/game.model';

describe('GamesService', () => {
  let service: GamesService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(GamesService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('sends the signup version when reporting payment', () => {
    service.reportPayment(12, 34, 5).subscribe();

    const request = httpTesting.expectOne('/api/gameslots/12/players/34/pay');
    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual({ version: 5 });
    request.flush({});
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

  it('sends the signup version when confirming payment', () => {
    service.confirmPayment(12, 34, 5).subscribe();

    const request = httpTesting.expectOne('/api/gameslots/12/players/34/confirm');
    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual({ version: 5 });
    request.flush({});
  });

  it('sends the signup version when resetting payment', () => {
    service.rejectPayment(12, 34, 5).subscribe();

    const request = httpTesting.expectOne('/api/gameslots/12/players/34/reject');
    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual({ version: 5 });
    request.flush({});
  });
});
