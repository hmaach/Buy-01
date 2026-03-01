interface MediaItem {
  localUrl: string;
  file?: File;
  isUploading: boolean;
  uploadError?: string;
  serverResponse?: MediaResponse;
}

interface MediaResponse {
  imagesId: string;
  status: 'PENDING' | 'LINKED'
}

interface ProductCreateRequest {
  name: string,
  description: string,
  price: number,
  int: number,
  imagesIds: string[]
}

