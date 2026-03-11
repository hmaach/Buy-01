# Frontend - Buy 01


### Authentication (Mock JWT)
- **Login**: `POST /login` - Email/password authentication with role selection (CLIENT | SELLER)
- **Register**: `POST /register` - User registration with optional avatar upload for sellers
- **JWT**: Mock JWT tokens stored in localStorage with auth interceptor

### Profile Management
- **GET /me**: Profile viewing via navbar dropdown
- **PUT /me**: Profile update (sellers can upload avatars)

### Product Features
- **Product List**: Grid display with 8 Apple products (iPhone, MacBook, iPad, etc.)
- **Product Details**: Full product information with images
- **Seller Dashboard**: Manage own products (add/edit/delete)
- **Product Form**: Create and edit products with image selection

### Media Management
- **Media Manager**: Upload/delete images (sellers only)
- Delegates to Media Service architecture

### Architecture
- **Angular 20** with standalone components
- **Angular Material** with Apple Store-inspired theme (#0071E3)
- **Lazy-loaded routes** for optimal performance
- **Role-based guards** (CLIENT | SELLER)

### Routes
- `/login` - Login page
- `/register` - Registration page
- `/products` - Product listing
- `/products/:id` - Product details
- `/seller/dashboard` - Seller dashboard
- `/seller/products/new` - Add product
- `/seller/products/:id/edit` - Edit product
- `/seller/media` - Media manager

### Test Accounts
- **Client**: client@email.com / password123
- **Seller**: seller@email.com / password123

The frontend is running at **http://localhost:4200/**