import { TestBed, ComponentFixture } from '@angular/core/testing';
import { ActivatedRoute, Router, convertToParamMap } from '@angular/router';
import { of, throwError } from 'rxjs';
import { MatSnackBar } from '@angular/material/snack-bar';

import { ProductForm } from './create-product';
import { ProductService } from '../../../core/services/product.service';
import { MediaService } from '../../../core/services/media.service';

describe('ProductForm', () => {
  let fixture: ComponentFixture<ProductForm>;
  let component: ProductForm;
  let productService: jasmine.SpyObj<ProductService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    productService = jasmine.createSpyObj<ProductService>('ProductService', [
      'createProduct',
      'getProduct',
      'updateProduct',
    ]);
    router = jasmine.createSpyObj<Router>('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [ProductForm],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({}),
            },
          },
        },
        { provide: ProductService, useValue: productService },
        {
          provide: MediaService,
          useValue: jasmine.createSpyObj<MediaService>('MediaService', ['uploadImages', 'deleteImage']),
        },
        { provide: Router, useValue: router },
        {
          provide: MatSnackBar,
          useValue: jasmine.createSpyObj<MatSnackBar>('MatSnackBar', ['open']),
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ProductForm);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('creates in create mode', () => {
    expect(component).toBeTruthy();
    expect(component.isEditMode()).toBeFalse();
    expect(component.pageTitle()).toBe('Create Product Details');
    expect(component.submitText()).toBe('Create Product');
  });

  it('does not call createProduct when the form is invalid', () => {
    component.onSubmit();

    expect(component.form.invalid).toBeTrue();
    expect(productService.createProduct).not.toHaveBeenCalled();
    expect(component.name?.touched).toBeTrue();
    expect(component.description?.touched).toBeTrue();
  });

  it('submits a valid create payload and navigates to the product details page', () => {
    productService.createProduct.and.returnValue(of({ id: 'product-123' }));
    router.navigate.and.returnValue(Promise.resolve(true));

    component.form.setValue({
      name: 'Gaming Mouse',
      description: 'High precision wireless mouse for gaming and office work.',
      price: 49.99,
      quantity: 12,
    });
    component.mainImagePreview.set('main-image-id');
    component.thumbnailPreviews.set(['thumb-1', 'thumb-2']);

    component.onSubmit();

    expect(productService.createProduct).toHaveBeenCalledWith({
      name: 'Gaming Mouse',
      description: 'High precision wireless mouse for gaming and office work.',
      price: 49.99,
      quantity: 12,
      imagesIds: ['main-image-id', 'thumb-1', 'thumb-2'],
    });
    expect(component.okMessage()).toBe('Product created successfully');
    expect(component.isLoading()).toBeFalse();
    expect(router.navigate).toHaveBeenCalledWith(['/products', 'product-123']);
  });

  it('stores the backend error message when create fails', () => {
    productService.createProduct.and.returnValue(
      throwError(() => ({
        error: {
          detail: 'Name already exists',
        },
      })),
    );

    component.form.setValue({
      name: 'Gaming Mouse',
      description: 'High precision wireless mouse for gaming and office work.',
      price: 49.99,
      quantity: 12,
    });

    component.onSubmit();

    expect(productService.createProduct).toHaveBeenCalled();
    expect(component.errorMessage()).toBe('Name already exists');
    expect(component.isLoading()).toBeFalse();
    expect(router.navigate).not.toHaveBeenCalled();
  });
});
