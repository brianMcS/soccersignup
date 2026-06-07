import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TeamSheetViewComponent } from './team-sheet-view.component';

describe('TeamSheetViewComponent', () => {
  let component: TeamSheetViewComponent;
  let fixture: ComponentFixture<TeamSheetViewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TeamSheetViewComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TeamSheetViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
