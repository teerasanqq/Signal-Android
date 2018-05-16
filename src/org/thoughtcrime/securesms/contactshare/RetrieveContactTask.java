package org.thoughtcrime.securesms.contactshare;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.thoughtcrime.securesms.mms.PartAuthority;
import org.thoughtcrime.securesms.mms.SharedContactSlide;

import java.io.IOException;
import java.io.InputStream;

public class RetrieveContactTask extends AsyncTask<Void, Void, Contact> {

  private static final String TAG = RetrieveContactTask.class.getSimpleName();

  @SuppressLint("StaticFieldLeak")
  private final Context            context;
  private final SharedContactSlide slide;
  private final Callback           callback;

  public RetrieveContactTask(@NonNull Context context, @NonNull SharedContactSlide slide, @NonNull Callback callback) {
    this.context  = context;
    this.slide    = slide;
    this.callback = callback;
  }

  @Override
  protected Contact doInBackground(Void... voids) {
    if (slide.getUri() == null) {
      return null;
    }

    try (InputStream contactStream = PartAuthority.getAttachmentStream(context, slide.getUri())) {
      return new ContactReader(contactStream).getContact();
    } catch (IOException e) {
      Log.w(TAG, "Failed to read contact.", e);
    }
    return null;
  }

  @Override
  protected void onPostExecute(@Nullable Contact contact) {
    callback.onComplete(contact);
  }

  public interface Callback {
    void onComplete(@Nullable Contact contact);
  }
}
