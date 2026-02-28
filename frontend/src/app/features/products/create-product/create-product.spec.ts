import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateProduct } from './create-product';

describe('CreateProduct', () => {
  let component: CreateProduct;
  let fixture: ComponentFixture<CreateProduct>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreateProduct]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CreateProduct);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
