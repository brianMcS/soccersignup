import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { HistoryComponent } from './history.component';
import { GamesService } from '../../services/games.service';

describe('HistoryComponent', () => {
  let component: HistoryComponent;
  let fixture: ComponentFixture<HistoryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HistoryComponent],
      providers: [
        {
          provide: GamesService,
          useValue: { getCompletedGames: () => of([]) }
        }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(HistoryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
