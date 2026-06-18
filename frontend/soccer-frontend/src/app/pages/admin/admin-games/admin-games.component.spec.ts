import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { provideRouter } from '@angular/router';

import { AdminGamesComponent } from './admin-games.component';
import { GamesService } from '../../../services/games.service';

describe('AdminGamesComponent', () => {
  let component: AdminGamesComponent;
  let fixture: ComponentFixture<AdminGamesComponent>;
  let gamesService: jasmine.SpyObj<GamesService>;

  beforeEach(async () => {
    gamesService = jasmine.createSpyObj<GamesService>(
      'GamesService',
      [
        'getAllGames',
        'createGame',
        'createGames',
        'updateGame',
        'closeGame',
        'getSignups',
        'confirmPayment',
        'rejectPayment'
      ]);
    gamesService.getAllGames.and.returnValue(of([]));
    gamesService.createGames.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [AdminGamesComponent],
      providers: [
        provideRouter([]),
        { provide: GamesService, useValue: gamesService }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminGamesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('uses the batch API when creating recurring games', () => {
    component.viewMode = 'create';
    component.recurring = true;
    component.recurringCount = 3;
    component.form = {
      gameDate: '2026-07-01',
      kickOffTime: '19:00',
      location: 'Dublin',
      maxPlayers: 14,
      feeAmount: 5,
      revolutLink: ''
    };

    component.submitForm();

    expect(gamesService.createGames).toHaveBeenCalledOnceWith([
      jasmine.objectContaining({ gameDate: '2026-07-01' }),
      jasmine.objectContaining({ gameDate: '2026-07-08' }),
      jasmine.objectContaining({ gameDate: '2026-07-15' })
    ]);
    expect(gamesService.createGame).not.toHaveBeenCalled();
  });
});
