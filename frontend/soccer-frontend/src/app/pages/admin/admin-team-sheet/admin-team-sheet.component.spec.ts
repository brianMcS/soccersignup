import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminTeamSheetComponent } from './admin-team-sheet.component';

describe('AdminTeamSheetComponent', () => {
  let component: AdminTeamSheetComponent;
  let fixture: ComponentFixture<AdminTeamSheetComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminTeamSheetComponent]
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
