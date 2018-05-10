package org.thoughtcrime.securesms.contactshare;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.recipients.Recipient;

public class RetrieveThreadIdTask extends AsyncTask<Void, Void, Long> {

  @SuppressLint("StaticFieldLeak")
  private final Context   context;
  private final Recipient recipient;
  private final Callback  callback;

  public RetrieveThreadIdTask(@NonNull Context context, @NonNull Recipient recipient, @NonNull Callback callback) {
    this.context = context.getApplicationContext();
    this.recipient = recipient;
    this.callback = callback;
  }

  @Override
  protected Long doInBackground(Void... voids) {
    return DatabaseFactory.getThreadDatabase(context).getThreadIdFor(recipient);
  }

  @Override
  protected void onPostExecute(Long threadId) {
    callback.onComplete(threadId);
  }

  public interface Callback {
    void onComplete(long threadId);
  }
}
