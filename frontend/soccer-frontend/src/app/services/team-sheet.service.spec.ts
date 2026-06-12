import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

import { TeamSheetService } from './team-sheet.service';

describe('TeamSheetService', () => {
  let service: TeamSheetService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(TeamSheetService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
