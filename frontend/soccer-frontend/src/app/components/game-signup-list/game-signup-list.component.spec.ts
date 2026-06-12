import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BehaviorSubject, of, throwError } from 'rxjs';

import { GameSignupListComponent } from './game-signup-list.component';
import { GamesService } from '../../services/games.service';
import { UserService } from '../../services/user.service';
import { TeamSheetService } from '../../services/team-sheet.service';
import { NotificationService } from '../../services/notification.service';

describe('GameSignupListComponent', () => {
  let component: GameSignupListComponent;
  let fixture: ComponentFixture<GameSignupListComponent>;
  let gamesService: jasmine.SpyObj<GamesService>;

  beforeEach(async () => {
    gamesService = jasmine.createSpyObj<GamesService>(
      'GamesService',
      ['getAllGames', 'getSignups', 'joinGame', 'leaveGame']
    );
    gamesService.getAllGames.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [GameSignupListComponent],
      providers: [
        { provide: GamesService, useValue: gamesService },
        {
          provide: UserService,
          useValue: { currentUser$: new BehaviorSubject(null) }
        },
        {
          provide: TeamSheetService,
          useValue: jasmine.createSpyObj<TeamSheetService>('TeamSheetService', ['getTeamSheet'])
        },
        {
          provide: NotificationService,
          useValue: jasmine.createSpyObj<NotificationService>('NotificationService', ['fetchUnreadCount'])
        }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GameSignupListComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();

    expect(component).toBeTruthy();
  });

  it('loads the earliest open game regardless of API order', () => {
    gamesService.getAllGames.and.returnValue(of([
      {
        id: 2,
        gameDate: '2026-07-09',
        location: 'Pitch',
        maxPlayers: 18,
        feeAmount: 5,
        status: 'OPEN'
      },
      {
        id: 1,
        gameDate: '2026-06-11',
        location: 'Pitch',
        maxPlayers: 18,
        feeAmount: 5,
        status: 'OPEN'
      }
    ]));
    spyOn(component, 'loadSignups');

    component.loadLatestGame();

    expect(component.game?.gameDate).toBe('2026-06-11');
    expect(component.loadSignups).toHaveBeenCalledOnceWith(1);
  });

  it('shows an error state when games cannot be loaded', () => {
    gamesService.getAllGames.and.returnValue(throwError(() => ({
      error: { message: 'Service unavailable' }
    })));

    component.loadLatestGame();

    expect(component.pageState).toBe('error');
    expect(component.actionError).toBe('Service unavailable');
  });
});
