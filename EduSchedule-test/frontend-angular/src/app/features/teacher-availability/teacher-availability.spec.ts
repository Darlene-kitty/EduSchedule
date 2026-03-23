import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TeacherAvailability } from './teacher-availability';

describe('TeacherAvailability', () => {
  let component: TeacherAvailability;
  let fixture: ComponentFixture<TeacherAvailability>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TeacherAvailability]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TeacherAvailability);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
