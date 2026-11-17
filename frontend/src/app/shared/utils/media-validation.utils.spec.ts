import { validateImage } from './media-validation.utils';

function fakeFile(type: string, sizeInBytes: number): File {
  return new File([new Uint8Array(sizeInBytes)], 'picture', { type });
}

describe('validateImage', () => {
  it('accepts a small PNG', () => {
    expect(validateImage(fakeFile('image/png', 1024))).toBeNull();
  });

  it('rejects a file that is not an image', () => {
    expect(validateImage(fakeFile('application/pdf', 1024))).toContain('Invalid file type');
  });

  it('rejects an image larger than 2 MB', () => {
    const tooBig = 2 * 1024 * 1024 + 1;
    expect(validateImage(fakeFile('image/jpeg', tooBig))).toContain('2 MB');
  });
});
