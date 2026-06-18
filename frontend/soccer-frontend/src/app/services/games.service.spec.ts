import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';

import { GamesService } from './games.service';

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
});
