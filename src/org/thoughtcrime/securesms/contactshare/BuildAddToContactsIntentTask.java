package org.thoughtcrime.securesms.contactshare;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.thoughtcrime.securesms.contactshare.model.Contact;
import org.thoughtcrime.securesms.contactshare.model.ContactReader;
import org.thoughtcrime.securesms.contactshare.model.ContactStream;
import org.thoughtcrime.securesms.mms.PartAuthority;
import org.thoughtcrime.securesms.mms.SharedContactSlide;
import org.thoughtcrime.securesms.util.Util;

import java.io.IOException;
import java.io.InputStream;

public class BuildAddToContactsIntentTask extends AsyncTask<Void, Void, Intent> {

  private static final String TAG = BuildAddToContactsIntentTask.class.getSimpleName();

  @SuppressLint("StaticFieldLeak")
  private final Context  context;
  private final Uri      sharedContactUri;
  private final Callback callback;

  public BuildAddToContactsIntentTask(@NonNull Context context, @NonNull SharedContactSlide contactSlide, @NonNull Callback callback) {
    if (contactSlide.getUri() == null) {
      throw new IllegalStateException("The slide must have a Uri.");
    }

    this.context          = context;
    this.sharedContactUri = contactSlide.getUri();
    this.callback         = callback;
  }

  public BuildAddToContactsIntentTask(@NonNull Context context, @NonNull Uri sharedContactUri, @NonNull Callback callback) {
    this.context          = context;
    this.sharedContactUri = sharedContactUri;
    this.callback         = callback;
  }

  @Override
  protected Intent doInBackground(Void... voids) {

    Contact contact;
    byte[] avatarBytes;

    try (InputStream contactStream = PartAuthority.getAttachmentStream(context, sharedContactUri)) {
      ContactReader contactReader = new ContactReader(contactStream);
      contact = contactReader.getContact();

      InputStream avatarStream = contactReader.getAvatar();
      if (avatarStream != null) {
        avatarBytes = Util.readFully(avatarStream);
      } else {
        avatarBytes = null;
      }
    } catch (IOException e) {
      Log.w(TAG, "Failed to read contact info.", e);
      return null;
    }

    return ContactUtil.buildAddToContactsIntent(contact, avatarBytes);
  }

  @Override
  protected void onPostExecute(Intent intent) {
    callback.onComplete(intent);
  }

  public interface Callback {
    void onComplete(@Nullable Intent intent);
  }
}
