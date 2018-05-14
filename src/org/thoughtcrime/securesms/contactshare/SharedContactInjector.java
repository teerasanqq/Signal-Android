package org.thoughtcrime.securesms.contactshare;

import android.content.Context;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;

public class SharedContactInjector {

  private static final String TAG = SharedContactInjector.class.getSimpleName();

  private static volatile SharedContactInjector instance;

  private final Context context;
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
    executor.execute(() -> {
      final Contact     contact;
      final InputStream avatar;
      try {
        ContactStream.Reader reader = new ContactStream.Reader(PartAuthority.getAttachmentStream(context, contactSlide.getUri()));

        contact = reader.getContact();
        avatar  = reader.getAvatar();

        // TODO(greyson): Do this better
        new Handler(Looper.getMainLooper()).post(() -> {
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
