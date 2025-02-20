# Admin API Documentation

## Overview

This API provides administrative functionality for managing users and chat histories within the application. It is designed for use by administrators to perform CRUD (Create, Read, Update, Delete) operations on users and chat histories.

### Authentication

All endpoints in this module require the user to be authenticated and have the role of `admin`. If a non-admin user attempts to access these endpoints, a `403 Forbidden` error will be returned.

## Endpoints

### 1. List All Users

- **Endpoint**: `GET /admin/users`
- **Description**: Retrieve a list of all registered users.
- **Response**: A list of user objects.
- **Permissions**: Admin only.

### 2. Get User by ID

- **Endpoint**: `GET /admin/users/{user_id}`
- **Description**: Retrieve details of a specific user by their ID.
- **Parameters**: 
  - `user_id` (path): The ID of the user to retrieve.
- **Response**: A user object.
- **Permissions**: Admin only.

### 3. Update User

- **Endpoint**: `PUT /admin/users/{user_id}`
- **Description**: Update the details of an existing user.
- **Parameters**: 
  - `user_id` (path): The ID of the user to update.
  - `body` (JSON): A JSON object containing the user's updated details.
- **Response**: The updated user object.
- **Permissions**: Admin only.

### 4. Delete User

- **Endpoint**: `DELETE /admin/users/{user_id}`
- **Description**: Delete a user by their ID.
- **Parameters**: 
  - `user_id` (path): The ID of the user to delete.
- **Response**: The deleted user object.
- **Permissions**: Admin only.

### 5. List All Chat Histories

- **Endpoint**: `GET /admin/chats`
- **Description**: Retrieve a list of all chat histories.
- **Response**: A list of chat history objects.
- **Permissions**: Admin only.

### 6. Delete Chat History

- **Endpoint**: `DELETE /admin/chats/{chat_id}`
- **Description**: Delete a specific chat history by its ID.
- **Parameters**: 
  - `chat_id` (path): The ID of the chat history to delete.
- **Response**: The deleted chat history object.
- **Permissions**: Admin only.

## Error Handling

- **403 Forbidden**: Returned if the user is not an admin.
- **404 Not Found**: Returned if the specified user or chat history does not exist.

## Example Requests

### 1. List All Users

```bash
curl -X 'GET' \
  'http://localhost:8000/admin/users' \
  -H 'accept: application/json' \
  -H 'Authorization: Bearer <token>'
```

### 2. Get User by ID

```bash
curl -X 'GET' \
  'http://localhost:8000/admin/users/1' \
  -H 'accept: application/json' \
  -H 'Authorization: Bearer <token>'
```

### 3. Update User

```bash
curl -X 'PUT' \
  'http://localhost:8000/admin/users/1' \
  -H 'accept: application/json' \
  -H 'Authorization: Bearer <token>' \
  -H 'Content-Type: application/json' \
  -d '{
  "fullname": "Updated Name",
  "email": "updated@example.com",
  "phone": "1234567890",
  "role": "admin"
}'
```

### 4. Delete User

```bash
curl -X 'DELETE' \
  'http://localhost:8000/admin/users/1' \
  -H 'accept: application/json' \
  -H 'Authorization: Bearer <token>'
```

### 5. List All Chat Histories

```bash
curl -X 'GET' \
  'http://localhost:8000/admin/chats' \
  -H 'accept: application/json' \
  -H 'Authorization: Bearer <token>'
```

### 6. Delete Chat History

```bash
curl -X 'DELETE' \
  'http://localhost:8000/admin/chats/1' \
  -H 'accept: application/json' \
  -H 'Authorization: Bearer <token>'
```