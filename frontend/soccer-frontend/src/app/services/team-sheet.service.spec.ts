import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';

import { TeamSheetService } from './team-sheet.service';

describe('TeamSheetService', () => {
  let service: TeamSheetService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(TeamSheetService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('sends the team sheet version when auto-splitting an existing sheet', () => {
    service.autoSplit(12, 5).subscribe();

    const request = httpTesting.expectOne('/api/games/12/teamsheet/auto-split');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ version: 5 });
    request.flush({});
  });

  it('sends the team sheet version when publishing', () => {
    service.publishTeamSheet(12, 5).subscribe();

    const request = httpTesting.expectOne('/api/games/12/teamsheet/publish');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ version: 5 });
    request.flush({});
  });
});
