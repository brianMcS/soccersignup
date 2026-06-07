import { TestBed } from '@angular/core/testing';

import { TeamSheetService } from './team-sheet.service';

describe('TeamSheetService', () => {
  let service: TeamSheetService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TeamSheetService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
