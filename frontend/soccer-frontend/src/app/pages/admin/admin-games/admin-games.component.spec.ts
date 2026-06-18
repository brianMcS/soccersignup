import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { provideRouter } from '@angular/router';

import { AdminGamesComponent } from './admin-games.component';
import { AdminService } from '../../../services/admin.service';

describe('AdminGamesComponent', () => {
  let component: AdminGamesComponent;
  let fixture: ComponentFixture<AdminGamesComponent>;
  let adminService: jasmine.SpyObj<AdminService>;

  beforeEach(async () => {
    adminService = jasmine.createSpyObj<AdminService>(
      'AdminService',
      [
        'getAllGames',
        'createGame',
        'createGames',
        'updateGame',
        'closeGame',
        'getSignupsForGame',
        'confirmPayment',
        'rejectPayment'
      ]);
    adminService.getAllGames.and.returnValue(of([]));
    adminService.createGames.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [AdminGamesComponent],
      providers: [
        provideRouter([]),
        { provide: AdminService, useValue: adminService }
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

    expect(adminService.createGames).toHaveBeenCalledOnceWith([
      jasmine.objectContaining({ gameDate: '2026-07-01' }),
      jasmine.objectContaining({ gameDate: '2026-07-08' }),
      jasmine.objectContaining({ gameDate: '2026-07-15' })
    ]);
    expect(adminService.createGame).not.toHaveBeenCalled();
  });
});
