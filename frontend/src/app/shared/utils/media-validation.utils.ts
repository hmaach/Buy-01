export function validateImage(file: File): string | null {
  const validTypes = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];
  const maxSize = 2 * 1024 * 1024;
  if (!validTypes.includes(file.type)) {
    return 'Invalid file type. Please upload a JPEG, PNG, WebP, or GIF file.';
  }
  if (file.size > maxSize) {
    return 'File size exceeds the limit of 2 MB.';
  }
  return null;
}
