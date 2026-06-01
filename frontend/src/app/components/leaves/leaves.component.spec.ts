import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { LeavesComponent } from './leaves.component';
import { PRIME_NG_MESSAGE_SERVICE } from '../../testing/prime-ng-test-providers';

describe('LeavesComponent', () => {
  let component: LeavesComponent;
  let fixture: ComponentFixture<LeavesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LeavesComponent],
      providers: [provideHttpClient(), provideRouter([]), ...PRIME_NG_MESSAGE_SERVICE]
    }).compileComponents();

    fixture = TestBed.createComponent(LeavesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
