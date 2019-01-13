# Aalto Mobile Cloud Computing 2018 group work

The backend for this project *was* set up at:

- [GCP](https://console.cloud.google.com/home/dashboard?authuser=1&organizationId=93098847132&orgonly=true&project=mcc-fall-2018-g09&supportedpurview=organizationId)
- [Firebase](https://console.firebase.google.com/u/1/project/mcc-fall-2018-g09)

# Deployment

The project is deployed with a simple GNU Make command that is defined in the project root `Makefile`.

Before deployment, configure the `frontend` folder Android build by setting the `sdk.dir` into the `frontend/local.properties` file:

    # Example frontend/local.properties file for demonstration purposes
    # Local SDK location might be under /opt/ or a Windows specific path
    sdk.dir=/home/username/Android/Sdk

Also make sure you have an active Firebase login (with `firebase login`) for the correct permissions for deployment:

After configuration is done, run the `make deploy` command which builds the Android application into `frontend/app/build/outputs/apk` path (`debug` and `release` subfolders per build type) and deploys the Firebase application onto the Firebase platform.

If you have `ANDROID_HOME` variable set the prerequisite setup can be automated with the following script:

    cd frontend && echo "sdk.dir=$ANDROID_HOME" > local.properties && cd ..
    cd backend && npm install && npm run firebase login && cd ..
    make deploy

# Implemented features

The frontend implements many features in itself while the backend offers support for IAM rules, image uploads and push notifications. The application also supports link previewing as an extra feature.

### Frontend

- [x] User authentication
- [x] Profile settings
- [x] Search for users
- [x] Initiating a chat
- [x] Messaging
- [x] Notifications
- [x] Image resolution settings
- [x] Gallery of chat images
- [x] Leaving a chat group
- [x] Responsive user interface

### Backend

- [x] Access rules for user authentication, authorization, and access rules for profile information, chats, and messages
- [x] Database for user profile information, chats, and messages
- [x] Serverless paradigm for push notifications, image resizing, and image message generation from uploads in the backend via Cloud Functions

### Extra

- [x] Link preview

# Frontend

Implemented as a native Android application in Java 8.

### Tool and language versions

- Java 8
- Android Studio 3.2
- Android API Level 25 (Nougat) compatible with Nexus S AVD

### Components

- Firebase (https://firebase.google.com/)
- Firebase Authentication (https://firebase.google.com/docs/auth/)
- Firebase ML Kit (https://firebase.google.com/docs/ml-kit/)

# Backend

Implemented as a serverless Firebase application in Node.js 8.

Tool and language versions:

- Node.js 8
- npm 6

### Setup

Go to the `backend` folder and run `npm install`. To authorize your computer with Firebase run `npm run firebase login`.

After authorizing with Firebase you should also install the backend functions NPM packages by going to `backend/functions` and running `npm install`.

After setting up the Firebase side you can use `npm run firebase deploy` to upload new versions of the backend application.

You can update the functions in the `backend/functions` folder and deploy them with `npm run deploy` command.

### Links
- [Sample chat from firebase](https://github.com/firebase/friendlychat-web)
- [Backend requirements specification](https://mycourses.aalto.fi/pluginfile.php/744150/mod_resource/content/12/cs-e4100-fall-2018-project.pdf)
- [GCP account setup instructions](https://mycourses.aalto.fi/mod/page/view.php?id=353353)
- [GCP IAM and user management](https://console.cloud.google.com/iam-admin/iam?authuser=1&folder=&organizationId=93098847132&project=mcc-fall-2018-g09)
- [GCP project home](https://console.cloud.google.com/home/dashboard?authuser=1&organizationId=93098847132&project=mcc-fall-2018-g09)
- [Firebase project home](https://console.firebase.google.com/u/1/project/mcc-fall-2018-g09/overview)

### Components

- Firebase (https://firebase.google.com/)
- Firebase Authentication (https://firebase.google.com/docs/auth/)
- Firebase Realtime Database (https://firebase.google.com/docs/database/)
- Cloud Storage (https://firebase.google.com/docs/storage/)
- Cloud Functions (https://firebase.google.com/docs/functions/)

### Backend feature documentation

Text messages are sent by inserting them into `/messages/${chatId}` in Firebase Realtime Database.

Image messages are sent by uploading them into `/users/${userId}/messages/${chatId}` in Firebase Storage. The backend processes and resizes the images. The resized images are inserted into `/messages/${chatId}` after resizing. The backend inserts the image information with addresses and references into the database.

Push notifications for new messages are sent automatically to chat members after a user sends message to a chat by inserting it into Firebase Realtime Database or uploads an image into Firebase Storage.

Authorization is primarily handled by Firebase and defined by `backend/database.rules.json` and `backend/storage.rules`. The backend Firebase Functions implement some slim authorization code, but the amount of AAA code in the Firebase Functions is relatively small.
