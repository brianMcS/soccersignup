import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GameSignupListComponent } from './game-signup-list.component';

describe('GameSignupListComponent', () => {
  let component: GameSignupListComponent;
  let fixture: ComponentFixture<GameSignupListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GameSignupListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GameSignupListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
