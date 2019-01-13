/**
 * Aalto MCC 2018 course group 9 Firebase Functions.
 *
 * https://firebase.google.com/docs/functions/typescript
 */

import * as admin from "firebase-admin";
import * as functions from "firebase-functions";
import * as _ from "lodash";

import { tmpdir } from "os";
import { join, dirname } from "path";

import * as fs from "fs-extra";
import * as sharp from "sharp";
import * as uuidv4 from "uuid/v4";


admin.initializeApp();

/**
 * Example endpoint for the backend.
 */
export const hello = functions.https.onRequest(async (request, response) => {
  const time: string = new Date().toString();

  response.send(`Hello, the time is ${time}`);
  console.log(`Somebody queried the backend at ${time}`);
});

/**
 * Handler for new chat message triggers.
 *
 * Sends notifications to all chat participants.
 *
 * Users save their device notification tokens to `/users/{userId}/notificationTokens/{notificationToken}`.
 */
export const sendMessageNotifications = functions.database.ref('/messages/{chatId}/{messageId}')
  .onCreate(async (snapshot, context) => {
    const messageId: string = context.params.messageId;
    const chatId: string = context.params.chatId;
    const message = snapshot.val();

    console.log(`Message ${JSON.stringify(message)} created with message ID ${JSON.stringify(messageId)} in chat ID ${JSON.stringify(chatId)}`)

    const chatReference = await admin.database().ref(`/chats/${chatId}`).once("value");
    const chat = chatReference.val();

    console.log(`Chat data for chat ID ${JSON.stringify(chatId)} is ${JSON.stringify(chat)} with members ${JSON.stringify(chat.members)}`);

    const members = _.pickBy(chat.members, (val, key) =>  key !== message.sender );

    const tokenReferences = await Promise.all(_.keys(members).map(key => {
      return admin.database().ref(`/users/public/${key}/notificationTokens`).once("value");
    }));

    const tokens: string[] = _.flatMap(tokenReferences, reference => {
      return _.isNull(reference) ? [] : _.values(reference.val());
    });

    console.log(`Notification tokens are ${JSON.stringify(tokens)}`);

    let chatTitle; 
    if (members.size > 2) {
      chatTitle = chat.title;
    } else {
      const senderDisplayNameRef = await admin.database().ref(`/users/public/${message.sender}/displayName`).once("value");
      chatTitle = senderDisplayNameRef.val();
    }

    const payload = {
      notification: {
        title: chatTitle,
        body: message.text,
      }
    };

    console.log(`Sending payload ${JSON.stringify(payload)} to device tokens ${JSON.stringify(tokens)}`);
    const response = await admin.messaging().sendToDevice(tokens, payload);
    console.log(`Notification sent with response ${JSON.stringify(response)}`)
  });

/**
 * Handler for resizing chat message images that are uploaded into a GCS bucket.
 *
 * The bucket path resembles one such as:
 *
 *  /users/{userId}/messages/{chatId}/{filename}
 *
 * Resizes images into predefined thumbnail sizes after upload.
 *
 * Note that this handler is triggered by the images it itself uploads,
 * leading to potential uncontrolled handler recursion which can exhaust
 * storage quotas and the project account's wallet.
 *
 * Inspired by AngularFirebase tutorial that also uses Sharp for image processing:
 *
 *   https://angularfirebase.com/lessons/image-thumbnail-resizer-cloud-function/
 */

function formatFilename(name: string, width: number, height: number, uuid: string, suffix: string): string {
  return `message@${name}@${width}x${height}@${uuid}.${suffix}`;
}

export const resizeUploadedImage = functions.storage
  .object()
  .onFinalize(async (object, context) => {
      const bucketFilePath: string = object.name;

      const bucketFilePathMatcher = 'users/(.+?)/messages/(.+?)/(.*)';
      const matches = bucketFilePath.match(bucketFilePathMatcher);
      if (!matches) {
        console.log(`Skipping resize for ${bucketFilePath} which does not match path validator ${bucketFilePathMatcher}`);
        return false;
      }

      if (!object.name.includes("messages")) {
        console.log(`Skipping resize for ${bucketFilePath} which was not uploaded as a chat message`);
        return false
      }

      if (object.name.includes("message@")) {
        console.log(`Skipping resize for ${bucketFilePath} which is a product of a resize`);
        return false;
      }

      if (!object.contentType.includes("image")) {
        console.log(`Skipping resize for ${bucketFilePath} which is not an image`);
        return false;
      }

      const userId: string = matches[1];
      const chatId: string = matches[2];
      const originalFilename: string = matches[3].split("/").pop();

      const uuid: string = uuidv4();  // The local file is always assigned an unique identifier
      const suffix: string = "jpeg";  // JPEG encoding is used on the client

      // Allow uploads via admin Firebase UI for easier development and debugging
      const uploader = _.get(context, 'auth.uid');
      if (uploader && uploader !== userId) {
        console.error(`User uploaded file ${bucketFilePath} into strange path with uid ${uploader}`);
        return false;
      }

      console.log(`Resizing image ${bucketFilePath} with metadata ${JSON.stringify(object.metadata)} and context ${JSON.stringify(context)}`);

      const chatReference = await admin.database().ref(`/chats/${chatId}`).once("value");
      const chat = chatReference.val();
      const members = _.keys(chat.members);

      if (_.indexOf(members, userId) === -1) {
        console.error(`User ${userId} uploaded file ${bucketFilePath} but is not part of the chat ${JSON.stringify(chat)}`);
        return false;
      }

      const temporaryDirectory: string = join(tmpdir(), "messages");
      const temporaryFilePath: string = join(temporaryDirectory, `${uuid}.${suffix}`);

      await fs.ensureDir(temporaryDirectory);

      const bucket = admin.storage().bucket();
      await bucket.file(bucketFilePath).download({
          destination: temporaryFilePath
      });

      const image = sharp(temporaryFilePath);
      const metadata = await image.metadata();

      const sizes: any = {
        low: {width: 640, height: 480},
        high: {width: 1280, height: 960},
        original: {width: metadata.width, height: metadata.height}
      };

      // Get and validate the image sizing and feature attributes that are sent by the client
      // if they are not in the legal values, override them with sane defaults

      let imageSizing: string = _.get(object, "metadata.imageSizing", "original");
      if (_.indexOf(["low", "high", "original"], imageSizing) === -1) {
        imageSizing = "original";
      }

      let imageFeature: string = _.get(object, "metadata.imageFeature", "Other");
      if (_.indexOf(["Technology", "Food", "Screenshot", "Other"], imageFeature) === -1) {
        imageFeature = "Other";
      }

      const message: any = {
        sender: userId,
        createdAt: Date.now(),
        text: "New chat image",
        image: {
          filename: originalFilename,
          imageSizing: imageSizing,
          imageFeature: imageFeature,
        },
      };

      const originalTemporaryFilename: string = formatFilename(imageSizing, metadata.width, metadata.height, uuid, suffix);
      const originalTemporaryFilePath: string = join(temporaryDirectory, originalTemporaryFilename);

      const targetBucketDirectory: string = `messages/${chatId}`;
      const originalBucketFilePath: string = join(targetBucketDirectory, originalTemporaryFilename);

      await image.toFile(originalTemporaryFilePath);
      await bucket.upload(originalTemporaryFilePath, {destination: originalBucketFilePath});

      const uploads = _.map(sizes, async (size, name) => {
          const resizedBucketFileName: string = formatFilename(name, size.width, size.height, uuid, suffix);
          const resizedBucketFilePath: string = join(temporaryDirectory, resizedBucketFileName);

          let destination: string;

          if (metadata.width <= size.width && metadata.height <= size.height) {
            destination = originalBucketFilePath;
          } else {
            destination = join(targetBucketDirectory, resizedBucketFileName);

            await image.resize(size.width, size.height).toFile(resizedBucketFilePath);
            await bucket.upload(resizedBucketFilePath, {destination: destination});
          }

          message.image[name] = destination;
      });

      await Promise.all(uploads);
      console.log(`All resized variants for ${object.name} have been generated and uploaded`);

      const messageRef = await admin.database().ref(`/messages/${chatId}`).push(message);
      console.log(`New image message successfully added with unique key ${messageRef.key}: ${JSON.stringify(message)}`);

      await fs.remove(temporaryDirectory);
      console.log(`Temporary directories for ${object.name} resizing have been cleaned up and removed`);

      await bucket.file(bucketFilePath).delete();
      console.log(`Original file ${bucketFilePath} deleted from the user uploads folder`);

      return true;
  });
