import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';

import { TeamSheetViewComponent } from './team-sheet-view.component';
import { TeamSheetService } from '../../services/team-sheet.service';

describe('TeamSheetViewComponent', () => {
  let component: TeamSheetViewComponent;
  let fixture: ComponentFixture<TeamSheetViewComponent>;
  let teamSheetService: jasmine.SpyObj<TeamSheetService>;

  beforeEach(async () => {
    teamSheetService = jasmine.createSpyObj<TeamSheetService>(
      'TeamSheetService',
      ['getTeamSheet']
    );
    teamSheetService.getTeamSheet.and.returnValue(of({
      id: 1,
      gameId: 1,
      published: true,
      publishedAt: undefined,
      entries: []
    }));

    await TestBed.configureTestingModule({
      imports: [TeamSheetViewComponent],
      providers: [
        { provide: TeamSheetService, useValue: teamSheetService },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: { get: () => '1' }
            }
          }
        }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TeamSheetViewComponent);
    component = fixture.componentInstance;
  });

  it('treats a missing published team sheet as not announced', () => {
    teamSheetService.getTeamSheet.and.returnValue(throwError(() => ({ status: 404 })));

    fixture.detectChanges();

    expect(component.notPublished).toBeTrue();
    expect(component.error).toContain('not been announced');
  });

  it('shows server failures as errors rather than an unpublished team sheet', () => {
    teamSheetService.getTeamSheet.and.returnValue(throwError(() => ({
      status: 500,
      error: { message: 'Database unavailable' }
    })));

    fixture.detectChanges();

    expect(component.notPublished).toBeFalse();
    expect(component.error).toBe('Database unavailable');
  });
});
