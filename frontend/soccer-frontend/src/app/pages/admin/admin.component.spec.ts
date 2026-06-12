import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { provideRouter } from '@angular/router';

import { AdminComponent } from './admin.component';
import { AdminService } from '../../services/admin.service';
import { UserService } from '../../services/user.service';

describe('AdminComponent', () => {
  let component: AdminComponent;
  let fixture: ComponentFixture<AdminComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminComponent],
      providers: [
        provideRouter([]),
        {
          provide: UserService,
          useValue: { isAdmin: true }
        },
        {
          provide: AdminService,
          useValue: {
            getAllGames: () => of([]),
            getAllPlayers: () => of([])
          }
        }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
