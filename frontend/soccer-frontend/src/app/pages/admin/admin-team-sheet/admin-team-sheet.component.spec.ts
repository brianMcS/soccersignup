import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { throwError } from 'rxjs';

import { AdminTeamSheetComponent } from './admin-team-sheet.component';
import { TeamSheetService } from '../../../services/team-sheet.service';

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
});
