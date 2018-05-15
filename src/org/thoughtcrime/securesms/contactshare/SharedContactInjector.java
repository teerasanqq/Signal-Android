package org.thoughtcrime.securesms.contactshare;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.thoughtcrime.securesms.SignalExecutors;
import org.thoughtcrime.securesms.contactshare.model.Contact;
import org.thoughtcrime.securesms.contactshare.model.ContactStream;
import org.thoughtcrime.securesms.mms.PartAuthority;
import org.thoughtcrime.securesms.mms.SharedContactSlide;
import org.thoughtcrime.securesms.recipients.Recipient;
import org.thoughtcrime.securesms.util.DirectoryHelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Executor;

public class SharedContactInjector {

  private static final String TAG = SharedContactInjector.class.getSimpleName();

  private static volatile SharedContactInjector instance;

  private final Context context;
  // TODO(greyson): Cached thread pool, 3 threads max? Nah, probs asynctask
  private final Executor executor = SignalExecutors.DISK_IO;

  public static SharedContactInjector getInstance(@NonNull Context context) {
    if (instance == null) {
      synchronized (SharedContactInjector.class) {
        if (instance == null) {
          instance = new SharedContactInjector(context);
        }
      }
    }
    return instance;
  }

  private SharedContactInjector(@NonNull Context context) {
    this.context = context;
  }

  public void load(@NonNull SharedContactSlide contactSlide, @NonNull Target target) {
    if (contactSlide.getUri() == null) {
      Log.w(TAG, "Contact slide is missing a Uri. Can't load it.");
      return;
    }

    load(contactSlide.getUri(), target);
  }

  public void load(@NonNull Uri contactSlideUri, @NonNull Target target) {
    executor.execute(() -> {
      final Contact     contact;
      final InputStream avatar;
      try {
        ContactStream.Reader reader = new ContactStream.Reader(PartAuthority.getAttachmentStream(context, contactSlideUri));

        contact = reader.getContact();
        avatar  = reader.getAvatar();

        // TODO(greyson): Do this better -- at least only make the handler once, maybe use asynctask
        new Handler(Looper.getMainLooper()).post(() -> {
          // TODO(greyson): Maybe change this to get a list of (resolved) recipients as well, wrapped with a boolean as to whether this is in the contacts or not -- or we could applyCached setIsContact, probs better
          target.setContact(contact);
          target.setAvatar(avatar);
        });
      } catch (IOException e) {
        Log.e(TAG, "Encountered an error while reading the contact from disk.", e);
      }
    });
  }

  public interface Target {
    void setContact(@Nullable Contact contact);
    void setAvatar(@Nullable InputStream inputStream);
  }
}
