package org.thoughtcrime.securesms.contactshare;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import org.thoughtcrime.securesms.contactshare.model.Contact;
import org.thoughtcrime.securesms.recipients.Recipient;
import org.thoughtcrime.securesms.util.DirectoryHelper;

import java.io.IOException;

public class RefreshContactTask extends AsyncTask<Void, Void, Void> {

  private static final String TAG = RefreshContactTask.class.getSimpleName();

  @SuppressLint("StaticFieldLeak")
  private final Context context;
  private final Contact contact;

  public RefreshContactTask(@NonNull Context context, @NonNull Contact contact) {
    this.context = context.getApplicationContext();
    this.contact = contact;
  }

  @Override
  protected Void doInBackground(Void... voids) {
    for (Recipient recipient : ContactUtil.getRecipients(context, contact)) {
      try {
        DirectoryHelper.refreshDirectoryFor(context, recipient);
      } catch (IOException e) {
        Log.w(TAG, "Failed to refresh a recipient.", e);
      }
    }
    return null;
  }
}
