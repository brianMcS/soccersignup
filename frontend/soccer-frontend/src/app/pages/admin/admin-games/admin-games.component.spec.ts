import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { provideRouter } from '@angular/router';

import { AdminGamesComponent } from './admin-games.component';
import { AdminService } from '../../../services/admin.service';

describe('AdminGamesComponent', () => {
  let component: AdminGamesComponent;
  let fixture: ComponentFixture<AdminGamesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminGamesComponent],
      providers: [
        provideRouter([]),
        {
          provide: AdminService,
          useValue: {
            getAllGames: () => of([])
          }
        }
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
});
