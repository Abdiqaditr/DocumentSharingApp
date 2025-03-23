# DocumentSharingApp
 The app integrates Firebase services to provide essential features such as user profile management, push notifications, file upload and retrieval, and dynamic profile picture updates.
# Features

## User Profile Management
Users can create and update their profiles with the following details:

- Full Name
- Email Address
- Profile Picture (uploaded to Firebase Storage with URL stored in Realtime Database)
- Bio
When a user logs in, their profile details are retrieved and displayed.

# File Upload & Storage

- Users can upload various file types, including PDFs, images, and documents.
- Uploaded files are stored in Firebase Storage with metadata (file name, download URL, and timestamp) saved in Firebase Realtime Database.

# File Retrieval & Display
- Retrieved file details are displayed in a RecyclerView for easy navigation.
- Users can download files by clicking on them.
# User Authentication
- Implemented secure authentication methods such as:
- Email/Password Login

# Dependencies
- Firebase Cloud Messaging
- Firebase Realtime Database
- Firebase Storage
- Firebase Authentication

# Usage

- Sign Up / Log In: Create an account or log in using email/password or Google sign-in.
- Profile Management: Update profile details, including uploading a profile picture.
- File Upload: Upload various file types and view uploaded files in the RecyclerView.
- Notifications: Receive push notifications for key events.
- Search: Use the search bar to filter files by name

# screenshoots 
![screenshoot 2](https://github.com/user-attachments/assets/401c8d0f-2f37-41e2-bc7a-76565a749a0d)
![screenshoot 1](https://github.com/user-attachments/assets/717b608a-fbbd-4925-a99d-9e25f2d7e27b)
![screenshot 3](https://github.com/user-attachments/assets/bc4157c4-f168-4640-b220-d14c18493e55)
