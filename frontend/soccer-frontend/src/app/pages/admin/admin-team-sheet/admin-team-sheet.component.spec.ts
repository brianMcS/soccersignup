import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { throwError } from 'rxjs';

import { AdminTeamSheetComponent } from './admin-team-sheet.component';
import { TeamSheetService } from '../../../services/team-sheet.service';
import { TeamSheetEntry } from '../../../models/team-sheet.model';

describe('AdminTeamSheetComponent', () => {
  let component: AdminTeamSheetComponent;
  let fixture: ComponentFixture<AdminTeamSheetComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminTeamSheetComponent],
      providers: [
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: { paramMap: { get: () => '1' } }
          }
        },
        {
          provide: TeamSheetService,
          useValue: {
            getTeamSheet: () => throwError(() => ({ status: 404 }))
          }
        }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminTeamSheetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('moves a focused player with arrow keys and keeps them on their team half', () => {
    const entry = createEntry({ positionX: 49, positionY: 50 });
    const event = new KeyboardEvent('keydown', {
      key: 'ArrowRight',
      shiftKey: true
    });
    spyOn(event, 'preventDefault');

    component.onTokenKeydown(event, entry);

    expect(entry.positionX).toBe(50);
    expect(entry.positionY).toBe(50);
    expect(component.selectedEntry).toBe(entry);
    expect(component.isDirty).toBeTrue();
    expect(event.preventDefault).toHaveBeenCalled();
  });

  it('selects a player with Enter for tap placement', () => {
    const entry = createEntry();
    const event = new KeyboardEvent('keydown', { key: 'Enter' });
    spyOn(event, 'preventDefault');

    component.onTokenKeydown(event, entry);

    expect(component.selectedEntry).toBe(entry);
    expect(component.positionAnnouncement).toContain('selected');
  });

  it('places the selected player where the pitch is clicked', () => {
    const entry = createEntry();
    component.selectedEntry = entry;
    component.pitchRef = {
      nativeElement: {
        getBoundingClientRect: () => ({
          left: 100,
          top: 100,
          width: 400,
          height: 200
        })
      }
    } as any;

    component.onPitchClick({
      clientX: 200,
      clientY: 200
    } as MouseEvent);

    expect(entry.positionX).toBe(25);
    expect(entry.positionY).toBe(50);
    expect(component.isDirty).toBeTrue();
  });

  function createEntry(overrides: Partial<TeamSheetEntry> = {}): TeamSheetEntry {
    return {
      playerId: 1,
      playerName: 'Alex Morgan',
      teamSide: 'HOME',
      jerseyNumber: 7,
      positionX: 25,
      positionY: 50,
      ...overrides
    };
  }
});
