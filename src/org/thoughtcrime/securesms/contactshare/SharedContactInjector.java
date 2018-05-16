package org.thoughtcrime.securesms.contactshare;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.thoughtcrime.securesms.contacts.ContactAccessor;
import org.thoughtcrime.securesms.glide.KeyedInputStream;
import org.thoughtcrime.securesms.mms.PartAuthority;
import org.thoughtcrime.securesms.mms.SharedContactSlide;
import org.thoughtcrime.securesms.recipients.Recipient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class SharedContactInjector {

  private static final String TAG = SharedContactInjector.class.getSimpleName();

  public static void load(@NonNull Context context, @NonNull SharedContactSlide contactSlide, @NonNull Target target) {
    if (contactSlide.getUri() == null) {
      Log.w(TAG, "Contact slide is missing a Uri. Can't load it.");
      return;
    }

    load(context, contactSlide.getUri(), target);
  }

  @SuppressLint("StaticFieldLeak")
  public static void load(@NonNull Context context, @NonNull Uri contactSlideUri, @NonNull Target target) {
    new AsyncTask<Void, Void, ResolvedContact>() {

      @Override
      protected ResolvedContact doInBackground(Void... voids) {
        try {
          ContactReader   reader     = new ContactReader(PartAuthority.getAttachmentStream(context, contactSlideUri));
          List<Recipient> recipients = ContactUtil.getRecipients(context, reader.getContact());

          for (Recipient recipient : recipients) {
            recipient.resolve();
            // TODO(greyson): Don't do this -- maybe do it indirectly through the DB?
            Recipient.applyCached(recipient.getAddress(), cached -> {
              cached.setIsSystemContact(ContactAccessor.getInstance().isSystemContact(context, recipient.getAddress().serialize()));
            });
          }

          return new ResolvedContact(reader.getContact(), recipients, reader.getAvatar());
        } catch (IOException e) {
          Log.w(TAG, "Encountered an error while reading the contact from disk.", e);
          return null;
        }
      }

      @Override
      protected void onPostExecute(@Nullable ResolvedContact resolvedContact) {
        if (resolvedContact != null) {
          target.setResolvedContact(resolvedContact);
        } else {
          target.setResolvedContact(null);
          Log.w(TAG, "Failed to load contact.");
        }
      }
    }.execute();
  }

  public static class ResolvedContact {

    private final Contact          contact;
    private final List<Recipient>  recipients;
    private final KeyedInputStream avatarStream;

    private ResolvedContact(@Nullable Contact contact, @Nullable List<Recipient> recipients, @Nullable KeyedInputStream avatarStream) {
      this.contact      = contact;
      this.recipients   = recipients != null ? recipients : Collections.emptyList();
      this.avatarStream = avatarStream;
    }

    public @Nullable Contact getContact() {
      return contact;
    }

    public @NonNull List<Recipient> getRecipients() {
      return recipients;
    }

    public @Nullable KeyedInputStream getAvatarStream() {
      return avatarStream;
    }
  }

  public interface Target {
    void setResolvedContact(@Nullable ResolvedContact contact);
  }
}
